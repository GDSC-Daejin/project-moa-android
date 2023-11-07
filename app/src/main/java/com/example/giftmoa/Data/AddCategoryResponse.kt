package com.example.giftmoa.Data

data class AddCategoryResponse(
    val data: CategoryItem?,
    val message: String?,
    val code: String?
)

data class AddCategoryData(
    val id: Long?,
    val categoryName: String?
)
