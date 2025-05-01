package com.example.mebelar.presentation.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mebelar.data.repository.ProductRepositoryImpl
import com.example.mebelar.domain.model.ProductDetailData
import com.example.mebelar.domain.usecase.AddFavoriteUseCase
import com.example.mebelar.domain.usecase.GetProductDetailUseCase
import com.example.mebelar.domain.usecase.RemoveFavoriteUseCase
import com.example.mebelar.ui.state.ScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.compose.material3.SnackbarHostState

class ProductViewModel(
    private val getProductDetailUseCase: GetProductDetailUseCase,
    private val addFavoriteUseCase: AddFavoriteUseCase,
    private val removeFavoriteUseCase: RemoveFavoriteUseCase,
    private val productId: Int
) : ViewModel() {
    private val _product = MutableStateFlow<ProductDetailData?>(null)
    val product: StateFlow<ProductDetailData?> get() = _product

    private val _screenState = MutableStateFlow<ScreenState>(ScreenState.Loading)
    val screenState: StateFlow<ScreenState> get() = _screenState

    fun fetchProductDetail() {
        _screenState.value = ScreenState.Loading
        viewModelScope.launch {
            try {
                val product = getProductDetailUseCase.invoke(productId)
                _product.value = product
                _screenState.value = ScreenState.Idle
            } catch (e: Exception) {
                _product.value = null
                _screenState.value = ScreenState.Error
            }
        }
    }

    fun setFavorite(snackbarHostState: SnackbarHostState) {
        viewModelScope.launch {
            try {
                val currentProduct = _product.value ?: return@launch
                if (currentProduct.favorite) {
                    val success = removeFavoriteUseCase.invoke(productId)
                    if (success) {
                        _product.value = currentProduct.copy(favorite = false)
                        snackbarHostState.showSnackbar("Удалено из избранного")
                    }
                } else {
                    val success = addFavoriteUseCase.invoke(productId)
                    if (success) {
                        _product.value = currentProduct.copy(favorite = true)
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

class ProductViewModelFactory(
    private val getProductDetailUseCase: GetProductDetailUseCase,
    private val addFavoriteUseCase: AddFavoriteUseCase,
    private val removeFavoriteUseCase: RemoveFavoriteUseCase,
    private val productId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProductViewModel(
            getProductDetailUseCase,
            addFavoriteUseCase,
            removeFavoriteUseCase,
            productId
        ) as T
    }
}