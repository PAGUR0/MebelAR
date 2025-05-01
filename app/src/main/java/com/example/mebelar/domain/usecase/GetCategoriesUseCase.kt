package com.example.mebelar.domain.usecase

import com.example.mebelar.domain.model.CategoryCardData
import com.example.mebelar.domain.repository.CategoryRepository

class GetCategoriesUseCase(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(categoryId: Int?): List<CategoryCardData> {
        return categoryRepository.getCategories(categoryId)
    }
}