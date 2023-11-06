package com.example.giftmoa

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.giftmoa.Data.Data
import com.example.giftmoa.Data.GetKakaoLoginResponse
import com.example.giftmoa.databinding.ActivityLoginBinding
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.AuthError
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Login2Activity: AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val TAG = "LoginActivity"

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
        Retrofit2Generator.create(this).kakaoLogin(accessToken).enqueue(object : Callback<GetKakaoLoginResponse> {
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

    private fun saveLoginData(data: Data) {
        val sharedPref = getSharedPreferences("TokenData", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("grantType", data.grantType)
            putString("accessToken", data.accessToken)
            putString("refreshToken", data.refreshToken)
            putLong("accessTokenExpiresIn", data.accessTokenExpiresIn)
            apply() // 비동기적으로 데이터를 저장
        }
    }
}