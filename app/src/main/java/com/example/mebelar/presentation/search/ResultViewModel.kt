package com.example.mebelar.presentation.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mebelar.domain.model.ProductCardData
import com.example.mebelar.domain.usecase.SearchProductUseCase
import com.example.mebelar.ui.state.ScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SearchProductViewModel(
    private val searchProductUseCase: SearchProductUseCase,
    private val searchQuery: String
) : ViewModel() {

    private val _products = MutableStateFlow<List<ProductCardData>>(emptyList())
    val products: StateFlow<List<ProductCardData>> get() = _products

    private val _screenState = MutableStateFlow<ScreenState>(ScreenState.Idle)
    val screenState: StateFlow<ScreenState> get() = _screenState

    private val _favoriteState = MutableStateFlow<Boolean?>(null)
    val favoriteState: StateFlow<Boolean?> get() = _favoriteState

    fun fetchSearchProduct() {
        _screenState.value = ScreenState.Loading
        viewModelScope.launch {
            try {
                val products = searchProductUseCase.invoke(searchQuery)
                Log.d("SearchViewModel", "Products fetched: ${products.size}")
                _products.value = products
                _screenState.value = ScreenState.Idle
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Error fetching products: ${e.message}")
                _products.value = emptyList()
                _screenState.value = ScreenState.Error
            }
        }
    }

    fun setFavorite(productId: Int) {
        viewModelScope.launch {
            // Здесь должен быть вызов API для изменения статуса избранного
            // Для примера предполагаем, что статус меняется
            _products.value = _products.value.map {
                if (it.id == productId) {
                    _favoriteState.value = !it.favorite
                    it.copy(favorite = !it.favorite)
                } else {
                    it
                }
            }
        }
    }

    fun resetFavoriteState() {
        _favoriteState.value = null
    }
}

class SearchProductViewModelFactory(
    private val searchProductUseCase: SearchProductUseCase,
    private val searchQuery: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SearchProductViewModel(searchProductUseCase, searchQuery) as T
    }
}