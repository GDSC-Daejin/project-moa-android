package com.example.giftmoa.Data

import android.net.Uri
import com.google.gson.annotations.SerializedName

//body만
data class ShareRoomData(
    var title : String,
    var numberOfPeople : Int,
    var shareCouponCount : Int,
    var master : String,
    var roomCode : String,
    var roomBackground : String?
) : java.io.Serializable {

}

//팀생성body
data class TeamCreateData(
    @SerializedName("teamName")
    var teamName:String,
    @SerializedName("teamImage")
    var teamImage:String
)

//팀 들어가기body
data class TeamJoinData(
    @SerializedName("teamCode")
    var teamCode : String
)

//팀 기프티콘 공유 body
data class TeamShareGiftIcon(
    @SerializedName("teamId")
    var teamId : Int,
    @SerializedName("gifticonId")
    var gifticonId : Int
)
