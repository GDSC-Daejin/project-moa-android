package com.example.giftmoa

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient

class SplashActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //KakaoSdk.init(this, "ddf76b216d0d2c4c13a9f777aac121a5")

        var keyHash = Utility.getKeyHash(this)
        Log.e("HashKey", keyHash)

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
}