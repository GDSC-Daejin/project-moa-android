package com.example.giftmoa.Data

import com.google.gson.annotations.SerializedName

data class UpdateGifticonResponse(
    @SerializedName("data") val data: GifticonData?,
    @SerializedName("message") val message: String?,
    @SerializedName("code") val code: String?
)

// "data" 객체를 나타내는 클래스
data class GifticonData(
    @SerializedName("gifticonId") val gifticonId: Long?,
    @SerializedName("name") val name: String?,
    @SerializedName("barcodeNumber") val barcodeNumber: String?,
    @SerializedName("gifticonImagePath") val gifticonImagePath: String?,
    @SerializedName("exchangePlace") val exchangePlace: String?,
    @SerializedName("dueDate") val dueDate: String?,
    @SerializedName("gifticonType") val gifticonType: String?,
    @SerializedName("orderNumber") val orderNumber: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("usedDate") val usedDate: String?,
    @SerializedName("author") val author: Author?,
    @SerializedName("category") val category: Category?,
    @SerializedName("gifticonMoney") val gifticonMoney: String?
)

// "author" 객체를 나타내는 클래스
/*data class Author(
    @SerializedName("id") val id: Long,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("profileImageUrl") val profileImageUrl: String?
)

// "category" 객체를 나타내는 클래스
data class Category(
    @SerializedName("id") val id: Long,
    @SerializedName("categoryName") val categoryName: String
)*/
