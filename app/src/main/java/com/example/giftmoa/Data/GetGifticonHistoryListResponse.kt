package com.example.giftmoa.Data

import com.google.gson.annotations.SerializedName

data class GetGifticonHistoryListResponse(
    @SerializedName("data") val historyList: List<GifticonHistoryData>?,
    @SerializedName("message") val message: String?,
    @SerializedName("code") val code: String?
)

data class GifticonHistoryItem(
    @SerializedName("gifticonHistoryId") val gifticonHistoryId: Long?,
    @SerializedName("usedPrice") val usedPrice: Int?,
    @SerializedName("leftPrice") val leftPrice: Int?,
    @SerializedName("usedDate") val usedDate: String?,
    @SerializedName("gifticonId") val gifticonId: Long?,
    @SerializedName("usedUser") val usedUser: UsedUser?
)

