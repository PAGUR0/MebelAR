package com.example.mebelar.domain.usecase

import com.example.mebelar.domain.model.ProductCardData
import com.example.mebelar.domain.model.ProductDetailData
import com.example.mebelar.domain.repository.ProductRepository

class GetProductDetailUseCase(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(productId: Int): ProductDetailData {
        return productRepository.getProductDetail(productId)
    }
}

class GetProductUseCase(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(categoryId: Int?): List<ProductCardData> {
        return productRepository.getProducts(categoryId)
    }
}

class GetViewsUseCase(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(): List<ProductCardData> {
        return productRepository.getViews()
    }
}

class SearchProductUseCase(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(searchQuery: String): List<ProductCardData> {
        return productRepository.searchProduct(searchQuery)
    }
}