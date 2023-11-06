package com.example.giftmoa.Data

import com.google.gson.annotations.SerializedName

data class UpdateGifticonHistoryRequest(
    @SerializedName("gifticonId") val gifticonId: Long? = null,
    @SerializedName("money") val money: Long? = null,
)
