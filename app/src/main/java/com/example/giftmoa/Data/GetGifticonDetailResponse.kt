package com.example.giftmoa.Data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class GetGifticonDetailResponse(
    @SerializedName("data") val data: GifticonDetailData?,
    @SerializedName("message") val message: String?,
    @SerializedName("code") val code: String?
)

data class GifticonDetailData(
    @SerializedName("gifticon") val gifticon: GifticonDetail?,
    @SerializedName("teamList") val teamList: List<Team>?
)

data class GifticonDetail(
    @SerializedName("gifticonId") val gifticonId: Long?,
    @SerializedName("name") val name: String?,
    @SerializedName("barcodeNumber") val barcodeNumber: String?,
    @SerializedName("gifticonImagePath") val gifticonImagePath: String?,
    @SerializedName("exchangePlace") val exchangePlace: String?,
    @SerializedName("dueDate") val dueDate: String?,
    @SerializedName("gifticonType") val gifticonType: String?,
    @SerializedName("orderNumber") val orderNumber: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("usedDate") val usedDate: String?,
    @SerializedName("author") val author: Author?,
    @SerializedName("category") val category: Category?,
    @SerializedName("gifticonMoney") val gifticonMoney: String?
)

@Parcelize
data class Team(
    @SerializedName("id") val id: Long?,
    @SerializedName("teamCode") val teamCode: String?,
    @SerializedName("teamName") val teamName: String?,
    @SerializedName("teamImage") val teamImage: String?,
    @SerializedName("teamLeaderNickname") val teamLeaderNickname: String?,
    @SerializedName("teamMembers") val teamMembers: List<Member>?
) : Parcelable

/*data class Author(
    @SerializedName("id") val id: Long,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("profileImageUrl") val profileImageUrl: String?
)

data class Category(
    @SerializedName("id") val id: Long,
    @SerializedName("categoryName") val categoryName: String
)*/

@Parcelize
data class Member(
    @SerializedName("id") val id: Long?,
    @SerializedName("nickname") val nickname: String?,
    @SerializedName("profileImageUrl") val profileImageUrl: String?
) : Parcelable

