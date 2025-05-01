package com.example.mebelar.domain.repository

import android.icu.text.StringSearch
import com.example.mebelar.domain.model.ProductCardData
import com.example.mebelar.domain.model.ProductDetailData
import retrofit2.http.Query

interface ProductRepository {
    suspend fun getProductDetail(productId: Int): ProductDetailData

    suspend fun getProducts(categoryId: Int? = null): List<ProductCardData>

    suspend fun getViews(): List<ProductCardData>

    suspend fun searchProduct(searchQuery: String): List<ProductCardData>

    suspend fun getFavorites(): List<ProductCardData>

    suspend fun addFavorite(productId: Int): Boolean

    suspend fun removeFavorite(productId: Int): Boolean
}