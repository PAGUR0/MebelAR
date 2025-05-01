package com.example.mebelar.domain.repository

import com.example.mebelar.domain.model.CategoryCardData

interface CategoryRepository {
    suspend fun getCategories(categoryId: Int? = null): List<CategoryCardData>
}
