package com.example.giftmoa.Data

import com.google.gson.annotations.SerializedName

data class PostFcmRequestData(
    @SerializedName("targetUserId")
    val targetUserId : Int?,
    @SerializedName("title")
    val title : String?,
    @SerializedName("body")
    val body : String?
)
