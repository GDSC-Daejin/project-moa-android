package com.example.giftmoa.Data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class ParsedGifticon (
    val name: String? = null,
    val image: String? = null,
    @SerializedName("barcode_number") val barcodeNumber: String? = null,
    @SerializedName("exchange_place") val exchangePlace: String? = null,
    @SerializedName("due_date") val dueDate: String? = null,
    @SerializedName("order_number") val orderNumber: String? = null,
    val amount: Long? = null,
): Parcelable