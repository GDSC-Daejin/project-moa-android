package com.example.giftmoa.Data

data class GetKakaoLoginResponse(
    val data: Data,
    val message: String,
    val code: String
)

data class Data(
    val grantType: String,
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpiresIn: Long
)