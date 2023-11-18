package com.example.giftmoa.Data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class GetUsedTeamGifticonResponseData(
    @SerializedName("data") val data: UsedTeamData?,
    @SerializedName("message") val message: String?,
    @SerializedName("code") val code: String?
)

data class UsedTeamData(
    @SerializedName("data") val dataList: List<UsedGifticon>?,
    @SerializedName("totalCount") val totalCount: Long?,
    @SerializedName("nextPage") val nextPage: Long?
)

@Parcelize
data class UsedGifticon(
    @SerializedName("id") val id: Long?,
    @SerializedName("name") val name: String?,
    @SerializedName("gifticonImagePath") val gifticonImagePath: String?,
    @SerializedName("exchangePlace") val exchangePlace: String?,
    @SerializedName("dueDate") val dueDate: String?,
    @SerializedName("gifticonType") val gifticonType: String?,
    @SerializedName("status") var status: String?,
    @SerializedName("usedDate") val usedDate: String?,
    @SerializedName("author") val author: Author?,
    @SerializedName("category") val category: Category?,
    @SerializedName("gifticonHistories") val gifticonHistories : List<GifticonHistories>?
): Parcelable

@Parcelize
data class GifticonHistories(
    @SerializedName("id") val id: Long?,
    @SerializedName("usedPrice") val usedPrice: String?,
    @SerializedName("leftPrice") val leftPrice: String?,
    @SerializedName("usedDate") val usedDate: String?,
): Parcelable
