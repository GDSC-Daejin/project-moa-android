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
    fun deleteGifticon(@Path("gifticonId") gifticonId: Long): Call<UpdateGifticonResponse>

    // 사용가능한 기프티콘 리스트
    @GET("/api/v1/gifticon/usable_list")
    fun getUsableGifticonList(@Query("size") size: Int?): Call<GetGifticonListResponse>

    // 사용불가한 기프티콘 리스트
    @GET("/api/v1/gifticon/disabled_list")
    fun getUsedGifticonList(@Query("size") size: Int?): Call<GetGifticonListResponse>

    // 전체 기프티콘 리스트
    @GET("/api/v1/gifticon/all_list")
    fun getAllGifticonList(@Query("size") size: Int?): Call<GetGifticonListResponse>

    // 최근 기프티콘 리스트
    @GET("/api/v1/gifticon/recent_list")
    fun getRecentGifticonList(@Query("size") size: Int?): Call<GetGifticonListResponse>

    // 카테고리별 기프티콘 리스트
    @GET("/api/v1/gifticon/category_list")
    fun getCategoryGifticonList(
        @Query("categoryId") categoryId: Long,
        @Query("size") size: Int?
    ): Call<GetGifticonListResponse>

    // 금액권 사용기록 가져오기
    @GET("/api/v1/gifticon/money/{gifticonId}")
    fun getGifticonHistoryList(@Path("gifticonId") gifticonId: Long): Call<GetGifticonHistoryListResponse>

    // auth-controller-----------------------------------------------------------------------------
    @GET("/kakaologin")
    fun kakaoLogin(@Query("accessToken") accessToken: String): Call<GetKakaoLoginResponse>

    // 토큰 재발급
    @POST("/token")
    fun refreshToken(@Body requestBody: RefreshTokenRequest): Call<GetKakaoLoginResponse>

    // 회원 탈퇴
    @POST("/auth/user")
    fun deleteUser(): Call<LogoutUserResponse>

    // 로그아웃
    @POST("/auth/user/logout")
    fun logoutUser(): Call<LogoutUserResponse>

    // team-controller-----------------------------------------------------------------------------
    // 팀 목록 가져오기
    @GET("/api/v1/team/my_team")
    suspend fun getMyTeamList(): Response<GetMyTeamListResponse>

    // 팀에 속한 기프티콘 가져오기
    @GET("/api/v1/team/gifitcon/{teamId}")
    suspend fun getTeamGifticonList(@Path("teamId") teamId: Long): Response<GetTeamGifticonListResponse>


    // user-controller-----------------------------------------------------------------------------
    // 유저 정보 수정
    @PUT("/user")
    fun updateUser(@Body requestBody: UpdateUserRequest): Call<UpdateUserResponse>

    // category-controller-------------------------------------------------------------------------
    // 카테고리 생성
    @POST("/api/v1/category")
    fun addCategory(@Body requestBody: AddCategoryRequest): Call<AddCategoryResponse>

    // 카테고리 가져오기
    @GET("/api/v1/category")
    fun getCategoryList(): Call<GetCategoryListResponse>
}