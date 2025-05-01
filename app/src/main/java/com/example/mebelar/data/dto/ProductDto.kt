package com.example.mebelar.data.dto

data class ProductDetailDto(
    val id: Int,
    val name: String,
    val images: List<String>,
    val price: Int,
    val discountPrice: Int,
    val description: String,
    val modelUrl: String,
    val specifications: Map<String, String>,
    val store: String,
    val storeUrl: String,
    val category: List<String>,
    val favorite: Boolean?
)

data class ProductCardDto(
    val id: Int,
    val images: List<String>,
    val name: String,
    val price: Int,
    val discountPrice: Int,
    val favorite: Boolean?
)

