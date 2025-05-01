package com.example.mebelar.presentation.catalog

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mebelar.domain.model.CategoryCardData
import com.example.mebelar.domain.model.ProductCardData
import com.example.mebelar.domain.usecase.AddFavoriteUseCase
import com.example.mebelar.domain.usecase.GetCategoriesUseCase
import com.example.mebelar.domain.usecase.GetProductUseCase
import com.example.mebelar.domain.usecase.RemoveFavoriteUseCase
import com.example.mebelar.ui.state.ScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.compose.material3.SnackbarHostState

class CatalogViewModel(
    private val getProductUseCase: GetProductUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val addFavoriteUseCase: AddFavoriteUseCase,
    private val removeFavoriteUseCase: RemoveFavoriteUseCase,
    private val categoryId: Int?
) : ViewModel() {
    private val _categories = MutableStateFlow<List<CategoryCardData>>(emptyList())
    val categories: StateFlow<List<CategoryCardData>> get() = _categories

    private val _products = MutableStateFlow<List<ProductCardData>>(emptyList())
    val products: StateFlow<List<ProductCardData>> get() = _products

    private val _screenState = MutableStateFlow<ScreenState>(ScreenState.Idle)
    val screenState: StateFlow<ScreenState> get() = _screenState

    fun fetchCatalog() {
        _screenState.value = ScreenState.Loading
        viewModelScope.launch {
            try {
                val products = getProductUseCase.invoke(categoryId)
                Log.d("CatalogViewModel", "Products loaded: ${products.size} items")
                _products.value = products
            } catch (e: Exception) {
                Log.e("CatalogViewModel", "Error loading products: ${e.message}")
                _products.value = emptyList()
                _screenState.value = ScreenState.Error
                return@launch
            }

            try {
                val categories = getCategoriesUseCase.invoke(categoryId)
                _categories.value = categories
            } catch (e: Exception) {
                Log.e("CatalogViewModel", "Error loading categories: ${e.message}")
                _categories.value = emptyList()
                _screenState.value = ScreenState.Error
                return@launch
            }

            _screenState.value = ScreenState.Idle
        }
    }

    fun setFavorite(productId: Int, snackbarHostState: SnackbarHostState) {
        viewModelScope.launch {
            try {
                val currentProducts = _products.value
                val product = currentProducts.find { it.id == productId } ?: return@launch
                if (product.favorite) {
                    val success = removeFavoriteUseCase.invoke(productId)
                    if (success) {
                        _products.value = currentProducts.map {
                            if (it.id == productId) it.copy(favorite = false) else it
                        }
                        snackbarHostState.showSnackbar("Удалено из избранного")
                    }
                } else {
                    val success = addFavoriteUseCase.invoke(productId)
                    if (success) {
                        _products.value = currentProducts.map {
                            if (it.id == productId) it.copy(favorite = true) else it
                        }
                        snackbarHostState.showSnackbar("Добавлено в избранное")
                    }
                }
            } catch (e: Exception) {
                if (e.message?.contains("Для добавления в избранное необходимо зарегистрироваться") == true ||
                    e.message?.contains("Для удаления из избранного необходимо зарегистрироваться") == true) {
                    snackbarHostState.showSnackbar("Для добавления в избранное необходимо зарегистрироваться")
                } else {
                    snackbarHostState.showSnackbar("Ошибка: ${e.message}")
                }
            }
        }
    }
}

class CatalogViewModelFactory(
    private val getProductUseCase: GetProductUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val addFavoriteUseCase: AddFavoriteUseCase,
    private val removeFavoriteUseCase: RemoveFavoriteUseCase,
    private val categoryId: Int?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CatalogViewModel(
            getProductUseCase,
            getCategoriesUseCase,
            addFavoriteUseCase,
            removeFavoriteUseCase,
            categoryId
        ) as T
    }
}