package com.example.giftmoa

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Retrofit2Generator {

    private const val BASE_URL = "https://moa-backend.kro.kr"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create()) // JSON 컨버터 추가
        // 여기에 필요한 경우 로깅 인터셉터나 기타 인터셉터 등을 추가할 수 있습니다.
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}