package com.example.giftmoa.Data

import com.google.gson.annotations.SerializedName

data class TeamMemberItem (
    @SerializedName("user_id") val userId: String? = null,
    @SerializedName("user_name") val userName: String? = null,
    @SerializedName("profile_image_url") val profileImageUrl: String? = null,
)
