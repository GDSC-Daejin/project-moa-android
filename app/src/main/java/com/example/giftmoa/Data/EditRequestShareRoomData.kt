package com.example.giftmoa.Data

import com.google.gson.annotations.SerializedName

data class EditRequestShareRoomData(
    @SerializedName("teamName")
    val teamName : String?,
    @SerializedName("teamImage")
    val teamImage : String?
)
