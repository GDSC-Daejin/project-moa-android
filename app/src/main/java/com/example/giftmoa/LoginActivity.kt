package com.example.giftmoa

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.giftmoa.Data.KakaoLoginUserData
import com.example.giftmoa.Data.SaveSharedPreference
import com.example.giftmoa.databinding.ActivityLoginBinding
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {
    private lateinit var mBinding : ActivityLoginBinding
    private val kakaoAppKey = BuildConfig.kakao_app_key
    private val sharedPreference = SaveSharedPreference()

    private val SERVER_URL = BuildConfig.server_URL
    private val retrofit = Retrofit.Builder().baseUrl(SERVER_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val service: MoaInterface = retrofit.create(MoaInterface::class.java)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityLoginBinding.inflate(layoutInflater)

        setContentView(mBinding.root)
        mBinding.loginBtn.setOnClickListener {
            kakaoLogin() //로그인
        }

        /* KakaoSdk.init(this, kakaoAppKey)

        mBinding.kakaoLogoutBtn.setOnClickListener {
            kakaoLogout() //로그아웃
        }
        mBinding.kakaoUnlinkBtn.setOnClickListener {
            kakaoUnlink() //연결해제
        }*/


    }

    private fun kakaoLogin() {
        // 카카오계정으로 로그인 공통 callback 구성
        // 카카오톡으로 로그인 할 수 없어 카카오계정으로 로그인할 경우 사용됨
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                //TextMsg(this, "카카오계정으로 로그인 실패 : ${error}")
                Log.e("ERROR" , "${error}")
            } else if (token != null) {

                Log.d("[카카오로그인]","로그인에 성공하였습니다.\n${token.accessToken}")
                UserApiClient.instance.accessTokenInfo { tokenInfo, error ->
                    UserApiClient.instance.me { user, error ->
                        /*TextMsg(this, "카카오계정으로 로그인 성공 \n\n " +
                                "token: ${token.accessToken} \n\n " +
                                "me: ${user!!.kakaoAccount!!.name}")*/

                        val sharedPreference = SaveSharedPreference()
                        sharedPreference.setToken(this@LoginActivity, token.accessToken)
                        sharedPreference.setName(this@LoginActivity, user!!.kakaoAccount!!.name)

                        CoroutineScope(Dispatchers.IO).launch {
                            service.kakaoLogin(token.accessToken).enqueue(object : retrofit2.Callback<KakaoLoginUserData> {
                                override fun onResponse(
                                    call: retrofit2.Call<KakaoLoginUserData>,
                                    response: retrofit2.Response<KakaoLoginUserData?>
                                ) {
                                    if (response.isSuccessful) {
                                        println("success")
                                        val builder =  OkHttpClient.Builder()
                                            .connectTimeout(1, TimeUnit.SECONDS)
                                            .readTimeout(30, TimeUnit.SECONDS)
                                            .writeTimeout(15, TimeUnit.SECONDS)
                                            .addInterceptor(HeaderInterceptor(response.body()!!.data.accessToken))
                                        builder.build()


                                    } else {
                                        println("faafa")
                                        Log.d("test", response.errorBody()?.string()!!)
                                        Log.d("message", call.request().toString())
                                        println(response.code())
                                    }
                                }
                                override fun onFailure(call: retrofit2.Call<KakaoLoginUserData>, t: Throwable) {
                                    println("실패" + t.message.toString())
                                }
                            })
                        }

                        /*//TODO: 최종적으로 카카오로그인 및 유저정보 가져온 결과
                        UserApiClient.instance.me { user, error ->
                            TextMsg(this, "카카오계정으로 로그인 성공 \n\n " +
                                    "token: ${token.accessToken} \n\n " +
                                    "me: ${user}")

                            Log.e("TEST", "${token.idToken}")
                        }*/
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        this.finish()
                        //Log.e("TEST", "${token.idToken}")
                    }
                }
            }
        }

        // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                if (error != null) {
                    //TextMsg(this, "카카오톡으로 로그인 실패 : ${error}")

                    // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                    // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }

                    // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                    UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
                } else if (token != null) {
                    //TextMsg(this, "카카오톡으로 로그인 성공 ${token.accessToken}")
                    Log.i("LOGIN", "카카오톡으로 로그인 성공 ${token.accessToken}")
                    val intent = Intent(this, MainActivity::class.java)
                    //startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                    startActivity(intent)
                    finish()
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(this@LoginActivity, callback = callback)
        }
    }

    private fun TextMsg(act: Activity, msg : String){
        mBinding.tv.text = msg
    }

    class HeaderInterceptor constructor(private val token: String) : Interceptor {

        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val token = "Bearer $token"
            val newRequest = chain.request().newBuilder()
                .addHeader("Authorization", token)
                .build()
            return chain.proceed(newRequest)
        }
    }

    /*private fun kakaoLogout(){
        // 로그아웃
        UserApiClient.instance.logout { error ->
            if (error != null) {
                Log.e("Hello", "로그아웃 실패. SDK에서 토큰 삭제됨", error)
                TextMsg(this, "로그아웃 실패. SDK에서 토큰 삭제됨: ${error}")
            }
            else {
                Log.i("Hello", "로그아웃 성공. SDK에서 토큰 삭제됨")
                TextMsg(this, "로그아웃 성공. SDK에서 토큰 삭제됨")
            }
        }
    }

    private fun kakaoUnlink(){
        // 연결 끊기
        UserApiClient.instance.unlink { error ->
            if (error != null) {
                TextMsg(this, "연결 끊기 실패: ${error}")
            }
            else {
                TextMsg(this, "연결 끊기 성공. SDK에서 토큰 삭제 됨")
            }
        }
    }

    private fun TextMsg(act: Activity, msg : String){
        mBinding.tv.text = msg
    }*/
}