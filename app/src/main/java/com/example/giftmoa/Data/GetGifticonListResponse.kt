package com.example.giftmoa.Data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class GetGifticonListResponse(
    @SerializedName("data") val data: GenericData?,
    @SerializedName("message") val message: String?,
    @SerializedName("code") val code: String?
)

data class GenericData(
    @SerializedName("data") val dataList: List<Gifticon>?,
    @SerializedName("totalCount") val totalCount: Long?,
    @SerializedName("nextPage") val nextPage: Long?
)

@Parcelize
data class Gifticon(
    @SerializedName("id") val id: Long?,
    @SerializedName("name") val name: String?,
    @SerializedName("gifticonImagePath") val gifticonImagePath: String?,
    @SerializedName("exchangePlace") val exchangePlace: String?,
    @SerializedName("dueDate") val dueDate: String?,
    @SerializedName("gifticonType") val gifticonType: String?,
    @SerializedName("status") var status: String?,
    @SerializedName("usedDate") val usedDate: String?,
    @SerializedName("author") val author: Author?,
    @SerializedName("category") val category: Category?
): Parcelable
