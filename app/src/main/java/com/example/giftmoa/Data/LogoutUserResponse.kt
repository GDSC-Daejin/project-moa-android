package com.example.giftmoa.Data

import com.google.gson.annotations.SerializedName

data class LogoutUserResponse(
    @SerializedName("data") val data: Data,
    @SerializedName("data") val message: String,
    @SerializedName("data") val code: String
)
