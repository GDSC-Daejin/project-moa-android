package com.example.giftmoa.Data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class UpdateGifticonRequest(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("barcodeNumber") val barcodeNumber: String? = null,
    @SerializedName("gifticonImagePath") val gifticonImagePath: String? = null,
    @SerializedName("exchangePlace") val exchangePlace: String? = null,
    @SerializedName("dueDate") val dueDate: String? = null,
    @SerializedName("orderNumber") val orderNumber: String? = null,
    @SerializedName("gifticonType") val gifticonType: String? = null,
    @SerializedName("gifticonMoney") val gifticonMoney: String? = null,
    @SerializedName("categoryId") val categoryId: Long? = null,
): Parcelable
