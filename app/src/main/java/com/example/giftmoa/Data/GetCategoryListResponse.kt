package com.example.giftmoa.Data

data class GetCategoryListResponse(
    val data: List<CategoryItem>?,
    val message: String?,
    val code: String?
)
