package com.example.giftmoa.Data

import com.google.gson.annotations.SerializedName

data class CategoryItem (
    val id: Long? = null,
    @SerializedName("category_name") val categoryName: String? = null,
)
