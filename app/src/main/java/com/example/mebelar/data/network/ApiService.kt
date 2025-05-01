package com.example.mebelar.data.network

import com.example.mebelar.data.dto.CategoryCardDto
import com.example.mebelar.data.dto.LoginRequest
import com.example.mebelar.data.dto.ProductCardDto
import com.example.mebelar.data.dto.ProductDetailDto
import com.example.mebelar.data.dto.RegisterRequest
import com.example.mebelar.data.dto.TokenResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    // Категории
    @GET("api/categories")
    suspend fun getCategories(
        @Query("category_id") categoryId: Int? = null
    ): Response<List<CategoryCardDto>>

    // Продукты
    @GET("api/products")
    suspend fun getProducts(
        @Query("category_id") categoryId: Int? = null
    ): Response<List<ProductCardDto>>

    @GET("api/product/{id}")
    suspend fun getProductDetail(
        @Path("id") id: Int
    ): Response<ProductDetailDto>

    @GET("api/views")
    suspend fun getViews(): Response<List<ProductCardDto>>

    @GET("/api/products/search")
    suspend fun searchProducts(
        @Query("q") query: String
    ): Response<List<ProductCardDto>>

    @POST("api/login")
    suspend fun login(@Body body: LoginRequest): Response<TokenResponse>

    @POST("api/register")
    suspend fun register(@Body body: RegisterRequest): Response<Map<String, Any>>

    @POST("refresh")
    fun refreshToken(
        @Body body: Map<String, String>
    ): Call<TokenResponse>

    @GET("api/favorites")
    suspend fun getFavorites(
        @Header("Authorization") token: String
    ): Response<List<ProductCardDto>>

    @POST("api/favorites")
    suspend fun addFavorite(
        @Header("Authorization") token: String,
        @Body body: Map<String, Int>
    ): Response<Map<String, String>>

    @DELETE("api/favorites/{product_id}")
    suspend fun removeFavorite(
        @Header("Authorization") token: String,
        @Path("product_id") productId: Int
    ): Response<Map<String, String>>
}