package com.example.giftmoa.Data

import com.google.gson.annotations.SerializedName

data class GifticonHistoryResponse(
    @SerializedName("data") val data: GifticonHistoryData?,
    @SerializedName("message") val message: String?,
    @SerializedName("code") val code: String?
)

data class GifticonHistoryData(
    @SerializedName("gifticonHistoryId") val gifticonHistoryId: Long?,
    @SerializedName("usedPrice") val usedPrice: Long?,
    @SerializedName("leftPrice") val leftPrice: Long?,
    @SerializedName("usedDate") val usedDate: String?,
    @SerializedName("gifticonId") val gifticonId: Long?,
    @SerializedName("usedUser") val usedUser: UsedUser?
)

data class UsedUser(
    @SerializedName("id") val id: Long?,
    @SerializedName("nickname") val nickname: String?,
    @SerializedName("profileImageUrl") val profileImageUrl: String?
)
