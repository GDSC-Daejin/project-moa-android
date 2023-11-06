package com.example.giftmoa.Data

import com.google.gson.annotations.SerializedName

data class AddGifticonRequest(
    @SerializedName("name") val name: String? = null,
    @SerializedName("barcodeNumber") val barcodeNumber: String? = null,
    @SerializedName("gifticonImagePath") val giftticonImagePath: String? = null,
    @SerializedName("exchangePlace") val exchangePlace: String? = null,
    @SerializedName("dueDate") val dueDate: String? = null,
    @SerializedName("orderNumber") val orderNumber: String? = null,
    @SerializedName("gifticonType") val gifticonType: String? = null,
    @SerializedName("gifticonMoney") val gifticonMoney: String? = null,
    @SerializedName("categoryId") val categoryId: Long? = null,
)