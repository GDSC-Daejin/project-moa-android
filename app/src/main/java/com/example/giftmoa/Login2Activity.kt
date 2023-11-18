package com.example.giftmoa

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.giftmoa.Data.Data1
import com.example.giftmoa.Data.GetKakaoLoginResponse
import com.example.giftmoa.Data.SaveSharedPreference
import com.example.giftmoa.FCM.MyFirebaseMessagingService
import com.example.giftmoa.databinding.ActivityLoginBinding
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.AuthError
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Login2Activity: AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val TAG = "Login2Activity"

    private val sharedPreference = SaveSharedPreference()

    private var isReady = false

    // 카카오계정으로 로그인 공통 callback 구성
    // 카카오톡으로 로그인 할 수 없어 카카오계정으로 로그인할 경우 사용됨
    private val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            if (error is AuthError && error.response.error == "misconfigured") {
                Log.e("LoginActivity", "Invalid android_key_hash or ios_bundle_id or web_site_url. Check your configuration", error)
            }
            Toast.makeText(this, "카카오계정으로 로그인 실패", Toast.LENGTH_SHORT).show()
            Log.e("LoginActivity", "카카오계정으로 로그인 실패", error)
        } else if (token != null) {
            Toast.makeText(this, "카카오계정으로 로그인 성공", Toast.LENGTH_SHORT).show()
            Log.i("LoginActivity", "카카오계정으로 로그인 성공: ${token.accessToken}")

            sendAccessTokenToServer(token.accessToken)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        initSplashScreen()
        super.onCreate(savedInstanceState)

        binding  = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //KakaoSdk.init(this, "ddf76b216d0d2c4c13a9f777aac121a5")

        binding.loginBtn.setOnClickListener {
            // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
                UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                    if (error != null) {
                        Log.e(TAG, "카카오톡으로 로그인 실패", error)

                        // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                        // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
                        if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                            return@loginWithKakaoTalk
                        }

                        // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                        UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
                    } else if (token != null) {
                        UserApiClient.instance.me { user, error ->
                            sharedPreference.setName(this@Login2Activity, user!!.kakaoAccount!!.name).toString()
                        }

                        Toast.makeText(this, "카카오톡으로 로그인 성공", Toast.LENGTH_SHORT).show()

                        Log.i(TAG, "accessToken: ${token}")


                        sendAccessTokenToServer(token.accessToken)
                    }
                }
            } else {
                UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
            }
        }

    }

    private fun sendAccessTokenToServer(accessToken: String) {
        //FCM 설정 및 token값 가져오기
        val fcmToken = MyFirebaseMessagingService().getFirebaseToken()
        //또는 저장한 값 val sharedPref = getSharedPreferences("firebaseToken", Context.MODE_PRIVATE)
        //val fcm = sharedPref.getString("firebaseToken", null)
        Retrofit2Generator.create(this).kakaoLogin(accessToken, fcmToken).enqueue(object : Callback<GetKakaoLoginResponse> {
            override fun onResponse(call: Call<GetKakaoLoginResponse>, response: Response<GetKakaoLoginResponse>) {
                if (response.isSuccessful) {
                    // 로그인 데이터 저장
                    response.body()?.data?.let { data ->
                        saveLoginData(data)
                    }
                    // 메인 액티비티로 이동
                    startActivity(Intent(this@Login2Activity, MainActivity::class.java))
                    finish()
                } else {
                    Log.e("LoginActivity", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<GetKakaoLoginResponse>, t: Throwable) {
                Log.e("LoginActivity", "Retrofit onFailure: ", t)
            }
        })
    }

    private fun saveLoginData(data: Data1) {
        val sharedPref = getSharedPreferences("TokenData", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("grantType", data.grantType)
            putString("accessToken", data.accessToken)
            putString("refreshToken", data.refreshToken)
            putLong("accessTokenExpiresIn", data.accessTokenExpiresIn)
            apply() // 비동기적으로 데이터를 저장
        }
    }

    private fun initData() {
        // 별도의 데이터 처리가 없기 때문에 3초의 딜레이를 줌.
        // 선행되어야 하는 작업이 있는 경우, 이곳에서 처리 후 isReady를 변경.
        CoroutineScope(Dispatchers.IO).launch {
            delay(1000)
        }
        isReady = true
    }
    private fun initSplashScreen() {
        initData()
        val splashScreen = installSplashScreen()
        val content: View = findViewById(android.R.id.content)
        // SplashScreen이 생성되고 그려질 때 계속해서 호출된다.
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    return if (isReady) {
                        // 3초 후 Splash Screen 제거
                        content.viewTreeObserver.removeOnPreDrawListener(this)
                        true
                    } else {
                        // The content is not ready
                        false
                    }
                }
            }
        )
    }
}