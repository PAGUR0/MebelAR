package com.example.mebelar.data.repository

import android.content.Context
import android.util.Log
import com.example.mebelar.data.network.ApiService
import com.example.mebelar.domain.model.ProductCardData
import com.example.mebelar.domain.model.ProductDetailData
import com.example.mebelar.domain.repository.ProductRepository

class ProductRepositoryImpl(
    private val apiService: ApiService,
    private val context: Context
) : ProductRepository {
    override suspend fun getProducts(categoryId: Int?): List<ProductCardData> {
        Log.d("API_DEBUG", "getProducts called with categoryId = $categoryId")

        val response = apiService.getProducts(categoryId)

        Log.d("API_DEBUG", "isSuccessful = ${response.isSuccessful}")
        Log.d("API_DEBUG", "raw body = ${response.body()}")
        Log.d("API_DEBUG", "errorBody = ${response.errorBody()?.toString()}")

        if (response.isSuccessful) {
            val dtoList = response.body()
            if (dtoList == null) {
                Log.e("API_DEBUG", "Body is null despite 200 OK")
                return emptyList()
            }
            Log.d("API_DEBUG", "Products successfully fetched: ${dtoList.size} products")

            return dtoList.map { dto ->
                Log.d("API_DEBUG", "Parsed DTO: id=${dto.id}, name=${dto.name}, favorite=${dto.favorite}")
                ProductCardData(
                    dto.id,
                    dto.images,
                    dto.name,
                    dto.price,
                    dto.discountPrice,
                    favorite = dto.favorite ?: false
                )
            }
        } else {
            Log.e("API_DEBUG", "Failed to fetch products. Error code: ${response.code()}")
            throw Exception("Ошибка загрузки товаров: ${response.code()}")
        }
    }

    override suspend fun searchProduct(searchQuery: String): List<ProductCardData> {
        Log.d("API_DEBUG", "getSearch called with search = $searchQuery")

        val response = apiService.searchProducts(searchQuery)

        Log.d("API_DEBUG", "isSuccessful = ${response.isSuccessful}")
        Log.d("API_DEBUG", "raw body = ${response.body()}")
        Log.d("API_DEBUG", "errorBody = ${response.errorBody()?.toString()}")

        if (response.isSuccessful) {
            val dtoList = response.body()
            if (dtoList == null) {
                Log.e("API_DEBUG", "Body is null despite 200 OK")
                return emptyList()
            }
            Log.d("API_DEBUG", "Products successfully fetched: ${dtoList.size} products")

            return dtoList.map { dto ->
                Log.d("API_DEBUG", "Parsed DTO: id=${dto.id}, name=${dto.name}, favorite=${dto.favorite}")
                ProductCardData(
                    dto.id,
                    dto.images,
                    dto.name,
                    dto.price,
                    dto.discountPrice,
                    favorite = dto.favorite ?: false
                )
            }
        } else {
            Log.e("API_DEBUG", "Failed to fetch products. Error code: ${response.code()}")
            throw Exception("Ошибка загрузки товаров: ${response.code()}")
        }
    }

    override suspend fun getProductDetail(productId: Int): ProductDetailData {
        Log.d("API_DEBUG", "getProductDetail called with productId = $productId")

        val response = apiService.getProductDetail(productId)

        Log.d("API_DEBUG", "isSuccessful = ${response.isSuccessful}")
        Log.d("API_DEBUG", "raw body = ${response.body()}")
        Log.d("API_DEBUG", "errorBody = ${response.errorBody()?.string()}")

        if (response.isSuccessful) {
            val product = response.body()
            if (product == null) {
                Log.e("API_DEBUG", "Response body is null despite 200 OK")
                throw Exception("Пустой ответ от сервера")
            }
            Log.d("API_DEBUG", "Product successfully fetched: id=${product.id}, name=${product.name}, favorite=${product.favorite}")
            return ProductDetailData(
                product.id,
                product.name,
                product.description,
                product.price,
                product.discountPrice,
                product.images,
                product.modelUrl,
                product.specifications,
                product.storeUrl,
                product.store,
                product.category,
                favorite = product.favorite ?: false
            )
        } else {
            Log.e("API_DEBUG", "Failed to fetch product. Error code: ${response.code()}")
            throw Exception("Ошибка загрузки товара: ${response.code()}")
        }
    }

    override suspend fun getViews(): List<ProductCardData> {
        Log.d("API_DEBUG", "getViews called")

        val token = getAuthToken()
        if (token == null) {
            Log.e("API_DEBUG", "No auth token available, user not authenticated")
            throw Exception("Для просмотра истории необходимо зарегистрироваться")
        }

        try {
            val response = apiService.getViews()

            Log.d("API_DEBUG", "isSuccessful = ${response.isSuccessful}")
            Log.d("API_DEBUG", "raw body = ${response.body()}")
            Log.d("API_DEBUG", "errorBody = ${response.errorBody()?.string()}")

            if (response.isSuccessful) {
                val views = response.body()
                if (views == null || views.isEmpty()) {
                    Log.d("API_DEBUG", "No views found")
                    return emptyList()
                }

                Log.d("API_DEBUG", "Views successfully fetched: ${views.size} items")
                return views.map {
                    Log.d("API_DEBUG", "Parsed view DTO: id=${it.id}, name=${it.name}, favorite=${it.favorite}")
                    ProductCardData(
                        id = it.id,
                        name = it.name,
                        images = it.images,
                        oldPrice = it.price,
                        newPrice = it.discountPrice,
                        favorite = it.favorite ?: false
                    )
                }
            } else {
                Log.e("API_DEBUG", "Failed to fetch views. Error code: ${response.code()}")
                throw Exception("Ошибка загрузки данных просмотров: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("API_DEBUG", "General error: ${e.message}")
            throw Exception(e.message ?: "Неизвестная ошибка")
        }
    }

    override suspend fun getFavorites(): List<ProductCardData> {
        Log.d("API_DEBUG", "getFavorites called")

        val token = getAuthToken() ?: throw Exception("Для просмотра избранного необходимо зарегистрироваться")
        try {
            val response = apiService.getFavorites(token)

            Log.d("API_DEBUG", "isSuccessful = ${response.isSuccessful}")
            Log.d("API_DEBUG", "raw body = ${response.body()}")
            Log.d("API_DEBUG", "errorBody = ${response.errorBody()?.string()}")

            if (response.isSuccessful) {
                val favorites = response.body()
                if (favorites == null || favorites.isEmpty()) {
                    Log.d("API_DEBUG", "No favorites found")
                    return emptyList()
                }

                Log.d("API_DEBUG", "Favorites successfully fetched: ${favorites.size} items")
                return favorites.map {
                    Log.d("API_DEBUG", "Parsed favorite DTO: id=${it.id}, name=${it.name}, favorite=${it.favorite}")
                    ProductCardData(
                        id = it.id,
                        name = it.name,
                        images = it.images,
                        oldPrice = it.price,
                        newPrice = it.discountPrice,
                        favorite = it.favorite ?: true
                    )
                }
            } else {
                Log.e("API_DEBUG", "Failed to fetch favorites. Error code: ${response.code()}")
                throw Exception("Ошибка загрузки избранного: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("API_DEBUG", "General error: ${e.message}")
            throw Exception(e.message ?: "Неизвестная ошибка")
        }
    }

    override suspend fun addFavorite(productId: Int): Boolean {
        Log.d("API_DEBUG", "addFavorite called with productId = $productId")

        val token = getAuthToken() ?: throw Exception("Для добавления в избранное необходимо зарегистрироваться")
        try {
            val response = apiService.addFavorite(token, mapOf("product_id" to productId))

            Log.d("API_DEBUG", "isSuccessful = ${response.isSuccessful}")
            Log.d("API_DEBUG", "raw body = ${response.body()}")
            Log.d("API_DEBUG", "errorBody = ${response.errorBody()?.string()}")

            return if (response.isSuccessful) {
                Log.d("API_DEBUG", "Product added to favorites")
                true
            } else {
                Log.e("API_DEBUG", "Failed to add favorite. Error code: ${response.code()}")
                throw Exception("Ошибка добавления в избранное: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("API_DEBUG", "General error: ${e.message}")
            throw Exception(e.message ?: "Неизвестная ошибка")
        }
    }

    override suspend fun removeFavorite(productId: Int): Boolean {
        Log.d("API_DEBUG", "removeFavorite called with productId = $productId")

        val token = getAuthToken() ?: throw Exception("Для удаления из избранного необходимо зарегистрироваться")
        try {
            val response = apiService.removeFavorite(token, productId)

            Log.d("API_DEBUG", "isSuccessful = ${response.isSuccessful}")
            Log.d("API_DEBUG", "raw body = ${response.body()}")
            Log.d("API_DEBUG", "errorBody = ${response.errorBody()?.string()}")

            return if (response.isSuccessful) {
                Log.d("API_DEBUG", "Product removed from favorites")
                true
            } else {
                Log.e("API_DEBUG", "Failed to remove favorite. Error code: ${response.code()}")
                throw Exception("Ошибка удаления из избранного: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("API_DEBUG", "General error: ${e.message}")
            throw Exception(e.message ?: "Неизвестная ошибка")
        }
    }

    private fun getAuthToken(): String? {
        val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        val accessToken = sharedPreferences.getString("access_token", null)
        Log.d("ProductRepository", "getAuthToken: access_token=${accessToken?.take(10) ?: "null"}")
        return accessToken?.let { "Bearer $it" }
    }
}