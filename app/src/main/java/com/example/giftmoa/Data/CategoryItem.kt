package com.example.giftmoa.Data

import com.google.gson.annotations.SerializedName

data class CategoryItem (
    @SerializedName("id") val id: Long? = null,
    @SerializedName("categoryName") val categoryName: String? = null,
)
