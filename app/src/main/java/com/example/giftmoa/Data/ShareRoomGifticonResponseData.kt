package com.example.giftmoa.Data

import com.google.gson.annotations.SerializedName

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
