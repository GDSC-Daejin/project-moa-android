package com.example.giftmoa.Data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

class ShareRoomResponseData(
    @SerializedName("data")
    var data : TeamData,
    @SerializedName("message")
    var message : String,
    @SerializedName("code")
    var code : String
)

@Parcelize
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
    var teamLeaderNickName : String,
    @SerializedName("teamImage")
    var teamImage : String?
): Parcelable
