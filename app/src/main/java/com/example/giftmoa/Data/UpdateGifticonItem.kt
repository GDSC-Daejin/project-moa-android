package com.example.giftmoa.Data

data class UpdateGifticonItem(
    val id: Long? = null,
    val name: String? = null,
    val barcodeNumber: String? = null,
    val image: String? = null,
    val exchangePlace: String? = null,
    val dueDate: String? = null,
    val orderNumber: String? = null,
    val gifticonType: String? = null,
    val categoryId: Long? = null,
    val amount: Long? = null,
)
