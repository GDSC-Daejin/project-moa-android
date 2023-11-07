package com.example.giftmoa.Data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class GetMyProfileResponse(
    @SerializedName("data") val data: MyProfileData?,
    @SerializedName("message") val message: String?,
    @SerializedName("code") val code: String?
)

@Parcelize
data class MyProfileData(
    @SerializedName("id") val id: Long?,
    @SerializedName("nickname") val nickname: String?,
    @SerializedName("profileImageUrl") val profileImageUrl: String?
): Parcelable
