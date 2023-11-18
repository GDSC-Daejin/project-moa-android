package com.example.giftmoa

import com.example.giftmoa.Data.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    //@Headers("Authorization: Bearer $access_token")

    // gifticon-controller-------------------------------------------------------------------------
    // 기프티콘 수정
    @PUT("/api/v1/gifticon")
    fun updateGifticon(@Body requestBody: UpdateGifticonRequest): Call<UpdateGifticonResponse>

    // 기프티콘 생성
    @POST("/api/v1/gifticon")
    fun addGifticon(@Body requestBody: AddGifticonRequest): Call<UpdateGifticonResponse>

    // 금액권 사용금액 입력
    @POST("/api/v1/gifticon/money")
    fun updateGifticonHistory(@Body requestBody: UpdateGifticonHistoryRequest): Call<GifticonHistoryResponse>

    // 기프티콘 조회(상세페이지)
    @GET("/api/v1/gifticon/{gifticonId}")
    fun getGifticonDetail(@Path("gifticonId") gifticonId: Long): Call<GetGifticonDetailResponse>

    // 기프티콘 삭제
    @DELETE("/api/v1/gifticon/{gifticonId}")
    fun deleteGifticon(@Path("gifticonId") gifticonId: Long): Call<LogoutUserResponse>

    // 기프티콘 사용 완료(다시 누르면 취소)
    @PUT("/api/v1/gifticon/use/{gifticonId}")
    fun useGifticon(@Path("gifticonId") gifticonId: Long): Call<GetGifticonDetailResponse>

    // 사용가능한 기프티콘 리스트
    @GET("/api/v1/gifticon/usable_list")
    fun getUsableGifticonList(
        @Query("size") size: Int?,
        @Query("page") page: Int?
    ): Call<GetGifticonListResponse>

    // 사용불가한 기프티콘 리스트
    @GET("/api/v1/gifticon/disable_list")
    fun getUsedGifticonList(
        @Query("size") size: Int?,
        @Query("page") page: Int?
    ): Call<GetGifticonListResponse>

    // 전체 기프티콘 리스트
    @GET("/api/v1/gifticon/all_list")
    fun getAllGifticonList(
        @Query("size") size: Int?,
        @Query("page") page: Int?
    ): Call<GetGifticonListResponse>

    // 최근 기프티콘 리스트
    @GET("/api/v1/gifticon/recent_list")
    fun getRecentGifticonList(
        @Query("size") size: Int?,
        @Query("page") page: Int?
    ): Call<GetGifticonListResponse>

    // 카테고리별 기프티콘 리스트
    @GET("/api/v1/gifticon/category_list")
    fun getCategoryGifticonList(
        @Query("categoryId") categoryId: Long,
        @Query("size") size: Int?, @Query("page") page: Int?
    ): Call<GetGifticonListResponse>

    // 금액권 사용기록 가져오기
    @GET("/api/v1/gifticon/money/{gifticonId}")
    fun getGifticonHistoryList(@Path("gifticonId") gifticonId: Long): Call<GetGifticonHistoryListResponse>

    // auth-controller-----------------------------------------------------------------------------
    @GET("/api/v1/kakaologin")
    fun kakaoLogin(@Query("accessToken") accessToken: String,
                   @Query("tokenId") tokenId : String): Call<GetKakaoLoginResponse>

    // 토큰 재발급
    @POST("/api/v1/token")
    fun refreshToken(@Body requestBody: RefreshTokenRequest): Call<GetKakaoLoginResponse>

    // 회원 탈퇴
    @POST("/api/v1/auth/user")
    fun deleteUser(): Call<LogoutUserResponse>

    // 로그아웃
    @POST("/auth/user/logout")
    fun logoutUser(@Body requestBody: RefreshTokenRequest): Call<LogoutUserResponse>

    // team-controller-----------------------------------------------------------------------------

    //팀 생성
    @POST("/api/v1/team")
    fun createShareRoom(@Body shareRoomData: TeamCreateData) : Call<ShareRoomResponseData>

    //팀 참가
    @POST("/api/v1/team/join")
    fun joinShareRoom(@Body joinData : TeamJoinData) : Call<ShareRoomResponseData>

    //팀에 기프티콘 공유
    @POST("/api/v1/team/gifticon")
    fun teamShareGificon(@Body shareGifticonData : TeamShareGiftIcon) : Call<ShareRoomGifticonResponseData>

    // 팀 목록 가져오기
    @GET("/api/v1/team/my_team")
    fun getMyTeamList(): Call<GetMyTeamListResponse>

    // 팀에 속한 기프티콘 가져오기
    @GET("/api/v1/team/gifticon/{teamId}")
    fun getTeamGifticonList(@Path("teamId") teamId : Long,
                            @Query("page") page : Int,
                            @Query("size") size : Int): Call<GetTeamGifticonListResponse>

    // 팀에 공유하기 위한 자기 기프티콘 가져오기
    @GET("/api/v1/gifticon/recent_list")
    fun getShareGifticonList(
        @Query("size") size: Int?,
        @Query("page") page: Int?
    ): Call<GetGifticonListResponse>

    //팀 탈퇴하기
    @DELETE("/api/v1/team/{teamId}")
    fun deleteShareRoom(@Path("teamId") teamId : Int) : Call<ShareRoomResponseData>

    //팀 수정하기
    @PUT("/api/v1/team/{teamId}")
    fun editShareRoom(@Path("teamId") teamId : Int,
                      @Body editRequestData : EditRequestShareRoomData) : Call<ShareRoomResponseData>

    //팀 기프티콘 각종 목록 조회(필터)
    @GET("/api/v1/team/gifticon/{teamId}/{request}")
    fun getShareRoomGifticonFilterData(@Path("request") request : String,
                                       @Path("teamId") teamId : Int,
                                       @Query("page") page : Int,
                                       @Query("size") size : Int,) : Call<GetGifticonListResponse>


    // user-controller-----------------------------------------------------------------------------
    // 유저 정보 수정
    @PUT("/api/v1/user")
    fun updateUser(@Body requestBody: UpdateUserRequest): Call<UpdateUserResponse>

    // 내 정보 가져오기
    @GET("/api/v1/user/me")
    fun getMyProfile(): Call<GetMyProfileResponse>

    // 내 기프티콘 전체 개수 가져오기
    @GET("/api/v1/gifticon/count")
    fun getMyGifticonCount(): Call<LogoutUserResponse>

    // 사용가능한 기프티콘 개수 가져오기
    @GET("/api/v1/gifticon/usable_count")
    fun getUsableGifticonCount(): Call<LogoutUserResponse>

    // 사용한 기프티콘 개수 가져오기
    @GET("/api/v1/gifticon/used_count")
    fun getUsedGifticonCount(): Call<LogoutUserResponse>

    // category-controller-------------------------------------------------------------------------
    // 카테고리 생성
    @POST("/api/v1/category")
    fun addCategory(@Body requestBody: AddCategoryRequest): Call<AddCategoryResponse>

    // 카테고리 가져오기
    @GET("/api/v1/category")
    fun getCategoryList(): Call<GetCategoryListResponse>


    // fcm-controller-------------------------------------------------------------------------
    // fcm에 알람 전송용?
    @POST("/api/v1/fcm")
    fun postFCM(@Query("request") request : PostFcmRequestData) : Call<PostFcmResponseData>
}