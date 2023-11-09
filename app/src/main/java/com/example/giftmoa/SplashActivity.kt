package com.example.giftmoa

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.giftmoa.Data.GetKakaoLoginResponse
import com.example.giftmoa.Data.RefreshTokenRequest
import com.example.giftmoa.databinding.ActivityMyProfileBinding
import com.example.giftmoa.databinding.ActivitySplashBinding
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SplashActivity: AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var keyHash = Utility.getKeyHash(this)
        Log.e("HashKey", keyHash)

        val sharedPref = getSharedPreferences("TokenData", Context.MODE_PRIVATE)
        val grantType = sharedPref.getString("grantType", null) // 기본값은 null
        val accessToken = sharedPref.getString("accessToken", null) // 기본값은 null
        val refreshToken = sharedPref.getString("refreshToken", null) // 기본값은 null
        val accessTokenExpiresIn = sharedPref.getLong("accessTokenExpiresIn", -1) // 기본값은 -1

        /*// 만약 토큰이 만료되었다면
        if (accessTokenExpiresIn < System.currentTimeMillis()) {
            // 토큰 갱신
            refreshToken()
        }*/

        // 카카오 로그인 상태 확인
        if (AuthApiClient.instance.hasToken()) {
            // 이미 로그인되어 있는 경우 메인 액티비티로 이동
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            // 로그인되어 있지 않은 경우 로그인 액티비티로 이동
            startActivity(Intent(this, Login2Activity::class.java))
        }
        finish() // 현재 액티비티 종료
    }

    /*private fun refreshToken(refreshTokenRequest: RefreshTokenRequest) {
        Retrofit2Generator.create(this).refreshToken(refreshTokenRequest).enqueue(object :
            Callback<GetKakaoLoginResponse> {
            override fun onResponse(call: Call<GetKakaoLoginResponse>, response: Response<GetKakaoLoginResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        val sharedPref = getSharedPreferences("TokenData", Context.MODE_PRIVATE)
                        with (sharedPref.edit()) {
                            putString("accessToken", body.accessToken)
                            putString("refreshToken", body.refreshToken)
                            putLong("accessTokenExpiresIn", body.accessTokenExpiresIn)
                            apply()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<GetKakaoLoginResponse>, t: Throwable) {
                Log.e("MainActivity", "토큰 갱신 실패", t)
            }
        })
    }*/
}