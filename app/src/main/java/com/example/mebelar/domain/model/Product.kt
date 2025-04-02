package com.example.mebelar.domain.model

data class ProductCardData(
    val id: String,
    val name: String,
    val images: List<String>, // Список URL изображений
    val price: Double,
    val discountPrice: Double? = null,
    val category: List<String>
)

data class ProductDetailData(
    val id: String,
    val article: String,
    val market: String,
    val name: String,
    val images: List<String>,
    val description: String,
    val modelUrl: String,
    val price: Double,
    val discountPrice: Double?,
    val characteristics: Map<String, String>,
    val marketUrl: String,
    val category: List<String>
)