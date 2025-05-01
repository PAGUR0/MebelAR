package com.example.mebelar.domain.model


data class ProductDetailData(
    val id: Int,
    val name: String,
    val description: String,
    val price: Int,
    val discountPrice: Int?,
    val images: List<String>,
    val modelUrl: String,
    val characteristics: Map<String, String>,
    val marketUrl: String,
    val market: String,
    val category: List<String>,
    val favorite: Boolean
)

data class ProductCardData(
    val id: Int,
    val images: List<String>,
    val name: String,
    val newPrice: Int,
    val oldPrice: Int?,
    val favorite: Boolean
)