package com.example.giftmoa

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import timber.log.Timber

class GlobalApplication : Application() {
   private val nativeKey = BuildConfig.kakao_app_key
    override fun onCreate() {
        super.onCreate()

        // KaKao SDK  초기화
        KakaoSdk.init(this, nativeKey)

        Timber.plant(Timber.DebugTree())
    }
}