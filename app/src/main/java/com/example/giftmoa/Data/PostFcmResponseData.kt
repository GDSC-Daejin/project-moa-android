package com.example.giftmoa.Data

import com.google.gson.annotations.SerializedName

data class PostFcmResponseData (
    @SerializedName("data")
    val data : List<Any>?,
    @SerializedName("message")
    val message : String?,
    @SerializedName("code")
    val code : String?
)