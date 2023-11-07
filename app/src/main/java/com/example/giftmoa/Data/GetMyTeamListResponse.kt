package com.example.giftmoa.Data

import com.google.gson.annotations.SerializedName

data class GetMyTeamListResponse(
    @SerializedName("data") val data: List<Team>?,
    @SerializedName("message") val message: String?,
    @SerializedName("code") val code: String?
)
