package com.example.mebelar.presentation.catalog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mebelar.domain.model.CategoriesCardData
import com.example.mebelar.domain.model.Data
import com.example.mebelar.domain.model.ProductCardData
import com.example.mebelar.ui.theme.ScreenState
import kotlinx.coroutines.launch

class CatalogViewModel(val catalogId: String) : ViewModel() {
    private val _categories = MutableLiveData<List<CategoriesCardData>>(emptyList())
    val categories: LiveData<List<CategoriesCardData>> get() = _categories

    private val _products = MutableLiveData<List<ProductCardData>>(emptyList())
    val products: LiveData<List<ProductCardData>> get() = _products

    private val _screenState = MutableLiveData<ScreenState>(ScreenState.Idle)
    val screenState: LiveData<ScreenState> get() = _screenState

    fun fetchCategories(){
        viewModelScope.launch(){
            if (catalogId != "all"){
                _categories.value = Data().findCategoryById(catalogId)
            } else{
                _categories.value = Data().categorys
            }

            if (catalogId != "all"){
                _products.value = Data().filterProductsByCategory(catalogId)

            } else{
                _products.value = Data().product
            }

            _screenState.value = ScreenState.Success
        }
    }
}