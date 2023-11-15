package com.example.giftmoa.Data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class ShareRoomGifticonResponseData (
    @SerializedName("data")
    var data : ShareRoomGifticonData,
    @SerializedName("message")
    var message : String,
    @SerializedName("code")
    var code : String
)

data class ShareRoomGifticonData (
    @SerializedName("id")
    var id : Int,
    @SerializedName("teamName")
    var teamName : String,
    @SerializedName("teamLeaderNickName")
    var teamLeaderNickName : String,
    @SerializedName("gifticon")
    var gifticon : ShareRoomGifticon,

)

data class ShareRoomGifticon(
    @SerializedName("gifticonId")
    var gifticonId : Int,
    @SerializedName("name")
    var name : String,
    @SerializedName("barcodeNumber")
    var barcodeNumber : String,
    @SerializedName("gifticonImagePath")
    var gifticonImagePath : String?,
    @SerializedName("exchangePlace")
    var exchangePlace : String,
    @SerializedName("dueDate")
    var dueDate : String, //마감일
    @SerializedName("gifticonType")
    var gifticonType : String,
    @SerializedName("orderNumber")
    var orderNumber : String,
    @SerializedName("status")
    var status : String,
    @SerializedName("usedDate")
    var usedDate : String?,
    @SerializedName("author")
    var author : ShareRoomAuthor,
    @SerializedName("category")
    var category : ShareRoomCategory,
    @SerializedName("gifticonMoney")
    var gifticonMoney : String,

    var isSelected : Boolean?
)

@Parcelize
data class shareGifticon(
    @SerializedName("id") val id: Long?,
    @SerializedName("name") val name: String?,
    @SerializedName("gifticonImagePath") val gifticonImagePath: String?,
    @SerializedName("exchangePlace") val exchangePlace: String?,
    @SerializedName("dueDate") val dueDate: String?,
    @SerializedName("gifticonType") val gifticonType: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("usedDate") val usedDate: String?,
    @SerializedName("author") val author: Author?,
    @SerializedName("category") val category: Category?,

    var isSelected : Boolean?
): Parcelable


data class ShareRoomAuthor(
    @SerializedName("id")
    var id : Int,
    @SerializedName("nickname")
    var nickname : String,
    @SerializedName("profileImageUrl")
    var profileImageUrl : String

)

data class ShareRoomCategory(
    @SerializedName("id")
    var id : Int,
    @SerializedName("categoryName")
    var categoryName : String,
)
