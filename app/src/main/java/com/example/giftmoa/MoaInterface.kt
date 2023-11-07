package com.example.giftmoa

import com.example.giftmoa.Data.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MoaInterface {
    @GET("/kakaologin")
    fun kakaoLogin(@Query("accessToken") accessToken : String) : Call<KakaoLoginUserData>


    ////
    //공유방 관련
    @POST("/api/v1/team")
    fun createShareRoom(@Body shareRoomData: TeamCreateData) : Call<ShareRoomResponseData>

    @POST("/api/v1/team/join")
    fun joinShareRoom(@Body joinData : TeamJoinData) : Call<ShareRoomResponseData>

    @POST("/api/v1/team/gifticon")
    fun teamShareGificon(@Body shareGifticonData : TeamShareGiftIcon) : Call<ShareRoomGifticonResponseData>

    @GET("/api/v1/team/my_team")
    fun getMyShareRoom() : Call<ShareRoomGetTeamData>

    @GET("/api/v1/team/gifticon/{teamId}")
    fun getMyShareRoomGifticon(@Path("teamId") teamId : Int,
                               @Query("page") page : Int,
                               @Query("size") size : Int
                               ) : Call<ShareRoomGetTeamGifticonData>
}