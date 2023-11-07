package com.example.giftmoa.Data

data class GetKakaoLoginResponse(
    val data: Data1,
    val message: String,
    val code: String
)

data class Data1(
    val grantType: String,
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpiresIn: Long
)