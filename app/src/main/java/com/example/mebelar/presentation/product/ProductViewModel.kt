package com.example.mebelar.presentation.product

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mebelar.domain.model.Data
import com.example.mebelar.domain.model.ProductDetailData
import com.example.mebelar.ui.theme.ScreenState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ProductViewModel(val productId: String): ViewModel() {
    private val _product = MutableLiveData<ProductDetailData>()
    val product: LiveData<ProductDetailData> get() = _product

    private val _screenState = MutableLiveData<ScreenState>(ScreenState.Loading)
    val screenState: LiveData<ScreenState> get() = _screenState

    fun fetchProductDetail() {
        _product.value = Data().findProductById(productId)
        _screenState.value = ScreenState.Success
    }
}