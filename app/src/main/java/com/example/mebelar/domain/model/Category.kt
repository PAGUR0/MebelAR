package com.example.mebelar.domain.model

data class CategoriesCardData(
    val id: String,
    val name: String,
    val image: String
)

data class CategoryData(
    val id: String,
    val name: String,
    val subcategory: List<CategoriesCardData>? = null
)