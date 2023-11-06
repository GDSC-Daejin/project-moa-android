package com.example.giftmoa.Data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class GifticonDetailItem(
    val id: Long? = null,
    val name: String? = null,
    val barcodeNumber: String? = null,
    val gifticonImagePath: String? = null,
    val exchangePlace: String? = null,
    val dueDate: String? = null,
    val gifticonType: String? = null,
    val orderNumber: String? = null,
    val status: String? = null,
    val usedDate: String? = null,
    val author: Author? = null,
    val category: Category?,
    val amount: Long? = null,
    val usageHistories: List<UsageHistoryItem>? = null,
): Parcelable

@Parcelize
data class Author(
    @SerializedName("id") val id: Long?,
    @SerializedName("nickname") val nickname: String?,
    @SerializedName("profileImageUrl") val profileImageUrl: String?
): Parcelable

@Parcelize
data class Category(
    @SerializedName("id") val id: Long?,
    @SerializedName("categoryName") val categoryName: String?
): Parcelable
