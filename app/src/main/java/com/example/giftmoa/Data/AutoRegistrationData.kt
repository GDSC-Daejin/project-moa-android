package com.example.giftmoa.Data

import com.google.gson.annotations.SerializedName

data class AutoRegistrationData (
    val gifticons: List<ParsedGifticon>,
    val categories: List<CategoryItem>,
    @SerializedName("share_rooms") val shareRooms: List<ShareRoomItem>,
)
