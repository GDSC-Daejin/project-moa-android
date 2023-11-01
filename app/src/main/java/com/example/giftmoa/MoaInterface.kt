package com.example.giftmoa

import retrofit2.Call
import com.example.giftmoa.Data.KakaoLoginUserData
import retrofit2.http.GET
import retrofit2.http.Query

interface MoaInterface {
    @GET("/kakaologin")
    fun kakaoLogin(@Query("accessToken") accessToken : String) : Call<KakaoLoginUserData>

}