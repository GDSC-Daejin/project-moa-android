package com.example.giftmoa.Data

import com.google.gson.annotations.SerializedName

data class ShareRoomGetTeamGifticonData (
    @SerializedName("data")
    var data : GetTeamGifticonData,
    @SerializedName("message")
    var message : String,
    @SerializedName("code")
    var code : String
)

data class GetTeamGifticonData(
    @SerializedName("data")
    var data: List<ShareRoomGifticon>,
    @SerializedName("totalCount")
    var totalCount : Int,
    @SerializedName("nextPage")
    var nextPage : Int,
)