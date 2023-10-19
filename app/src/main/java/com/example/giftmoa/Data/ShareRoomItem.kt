package com.example.giftmoa.Data

import com.google.gson.annotations.SerializedName

data class ShareRoomItem (
    val teamId: Long? = null,
    @SerializedName("team_name") val teamName: String? = null,
    @SerializedName("team_thumbnail_image_url") val teamThumbnailImage: String? = null,
    @SerializedName("team_members") val teamMembers: List<TeamMemberItem>? = null,
)