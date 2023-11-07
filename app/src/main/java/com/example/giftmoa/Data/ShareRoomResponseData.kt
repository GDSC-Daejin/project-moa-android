package com.example.giftmoa.Data

import com.google.gson.annotations.SerializedName

class ShareRoomResponseData(
    @SerializedName("data")
    var data : TeamData,
    @SerializedName("message")
    var message : String,
    @SerializedName("code")
    var code : String
)

data class TeamData (
    @SerializedName("teamId")
    var teamId : Int,
    @SerializedName("teamCode")
    var teamCode : String,
    @SerializedName("teamName")
    var teamName : String,
    @SerializedName("status")
    var status : String,
    @SerializedName("teamLeaderNickName")
    var teamLeaderNickName : String
)
