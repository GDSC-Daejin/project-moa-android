package com.example.giftmoa.Data

import com.google.gson.annotations.SerializedName

data class UpdateUserRequest(
    @SerializedName("nickname") val nickname: String? = null,
    @SerializedName("profileImageUrl") val profileImageUrl: String? = null
)
