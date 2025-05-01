package com.example.mebelar.presentation.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mebelar.domain.model.CategoryCardData
import com.example.mebelar.domain.usecase.GetCategoriesUseCase
import com.example.mebelar.ui.state.ScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CategoriesViewModel(
    private val getCategoriesUseCase: GetCategoriesUseCase
) : ViewModel() {
    private val _categories = MutableStateFlow<List<CategoryCardData>>(emptyList())
    val categories: StateFlow<List<CategoryCardData>> get() = _categories

    private val _screenState = MutableStateFlow<ScreenState>(ScreenState.Idle)
    val screenState: StateFlow<ScreenState> get() = _screenState

    fun fetchCategories(){
        _screenState.value = ScreenState.Loading
        viewModelScope.launch {
            try {
                var categories = getCategoriesUseCase.invoke(null)
                _categories.value = categories
                _screenState.value = ScreenState.Idle
            } catch (_: Exception) {
                _categories.value = emptyList()
                _screenState.value = ScreenState.Error
            }
        }
    }
}

class CategoriesViewModelFactory(
    private val getCategoriesUseCase: GetCategoriesUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CategoriesViewModel(getCategoriesUseCase) as T
    }
}