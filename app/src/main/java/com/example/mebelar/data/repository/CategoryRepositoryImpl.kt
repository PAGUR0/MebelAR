package com.example.mebelar.data.repository

import android.util.Log
import com.example.mebelar.data.network.ApiService
import com.example.mebelar.domain.model.CategoryCardData
import com.example.mebelar.domain.repository.CategoryRepository

class CategoryRepositoryImpl(
    private val apiService: ApiService
) : CategoryRepository {
    override suspend fun getCategories(categoryId: Int?): List<CategoryCardData> {
        val response = apiService.getCategories(categoryId)

        Log.d("API_DEBUG", "getCategories called with categoryId = $categoryId")
        Log.d("API_DEBUG", "isSuccessful = ${response.isSuccessful}")

        // Логирование содержания ответа
        response.body()?.let {
            Log.d("API_DEBUG", "Response body: $it")
        } ?: Log.d("API_DEBUG", "Response body is null")

        Log.d("API_DEBUG", "errorBody = ${response.errorBody()?.toString()}")

        if (response.isSuccessful) {
            val body = response.body()
            if (body == null || body.isEmpty()) {
                Log.e("API_DEBUG", "Response body is null or empty despite successful status")
                return emptyList()
            }
            Log.d("API_DEBUG", "Categories successfully fetched: ${body.size} categories")
            return body.map { dto ->
                Log.d("API_DEBUG", "Parsed DTO: $dto")
                CategoryCardData(
                    dto.id,
                    dto.image,
                    dto.name,
                )
            }
        } else {
            Log.e("API_DEBUG", "Failed to fetch categories. Error code: ${response.code()}")
            throw Exception("Ошибка загрузки категорий: ${response.code()} - ${response.message()}")
        }
    }
}


