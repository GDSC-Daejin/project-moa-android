package com.example.giftmoa.Data

import com.google.gson.annotations.SerializedName

data class ShareRoomGetTeamData(
    @SerializedName("data")
    var data: List<GetTeamData>,
    @SerializedName("message")
    var message : String,
    @SerializedName("code")
    var code : String
)

data class GetTeamData(
    @SerializedName("id")
    var id : Int,
    @SerializedName("teamCode")
    var teamCode : String,
    @SerializedName("teamName")
    var teamName : String,
    @SerializedName("teamImage")
    var teamImage : String?,
    @SerializedName("teamLeaderNickName")
    var teamLeaderNickName : String?,
    @SerializedName("teamMembers")
    var teamMembers : List<GetTeamMembers>,
) : java.io.Serializable {

}

data class GetTeamMembers(
    @SerializedName("id")
    var id : Int,
    @SerializedName("nickname")
    var nickname : String,
    @SerializedName("profileImageUrl")
    var profileImageUrl : String?,
): java.io.Serializable {

}