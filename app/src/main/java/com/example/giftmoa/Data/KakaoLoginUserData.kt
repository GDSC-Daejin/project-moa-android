package com.example.giftmoa.Data

import com.google.gson.annotations.SerializedName

class KakaoLoginUserData(
    @SerializedName("data")
    var data : Data,
    @SerializedName("message")
    var message : String,
    @SerializedName("code")
    var code : String
) : java.io.Serializable {

}

data class Data(
    @SerializedName("grantType")
    var grantType : String,
    @SerializedName("accessToken")
    var accessToken : String,
    @SerializedName("refreshToken")
    var refreshToken : String,
    @SerializedName("accessTokenExpiresIn")
    var accessTokenExpiresIn : Long
)
