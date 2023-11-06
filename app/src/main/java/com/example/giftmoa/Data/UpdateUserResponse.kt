package com.example.giftmoa.Data

data class UpdateUserResponse(
    val data: UpdateUserData,
    val message: String,
    val code: String
)

data class UpdateUserData(
    val id: Long,
    val nickname: String,
    val profileImageUrl: String?
)
