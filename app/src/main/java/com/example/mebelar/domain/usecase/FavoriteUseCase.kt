package com.example.mebelar.domain.usecase

import com.example.mebelar.domain.model.ProductCardData
import com.example.mebelar.domain.repository.ProductRepository

class GetFavoritesUseCase(private val repository: ProductRepository) {
    suspend operator fun invoke(): List<ProductCardData> {
        return repository.getFavorites()
    }
}

class AddFavoriteUseCase(private val repository: ProductRepository) {
    suspend operator fun invoke(productId: Int): Boolean {
        return repository.addFavorite(productId)
    }
}

class RemoveFavoriteUseCase(private val repository: ProductRepository) {
    suspend operator fun invoke(productId: Int): Boolean {
        return repository.removeFavorite(productId)
    }
}