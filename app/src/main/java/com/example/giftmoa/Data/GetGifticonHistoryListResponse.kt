package com.example.giftmoa.Data

import com.google.gson.annotations.SerializedName

data class GetGifticonHistoryListResponse(
    @SerializedName("data") val data: List<GifticonHistoryData>?,
    @SerializedName("message") val message: String?,
    @SerializedName("code") val code: String?
)

data class GifticonHistoryItem(
    @SerializedName("gifticonHistoryId") val gifticonHistoryId: Long?,
    @SerializedName("usedPrice") val usedPrice: Long?,
    @SerializedName("leftPrice") val leftPrice: Long?,
    @SerializedName("usedDate") val usedDate: String?,
    @SerializedName("gifticonId") val gifticonId: Long?,
    @SerializedName("usedUser") val usedUser: UsedUser?
)

