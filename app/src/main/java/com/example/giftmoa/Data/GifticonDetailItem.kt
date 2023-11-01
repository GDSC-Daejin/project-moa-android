package com.example.giftmoa.Data

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
)

data class Author(
    val id: Long? = null,
    val nickname: String? = null,
    val profileImageUrl: String? = null,
)

data class Category(
    val id: Long? = null,
    val categoryName: String? = null,
)
