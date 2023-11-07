package com.example.giftmoa.Data

import com.google.gson.annotations.SerializedName

data class LogoutUserResponse(
    @SerializedName("data") val data: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("code") val code: String?
)
