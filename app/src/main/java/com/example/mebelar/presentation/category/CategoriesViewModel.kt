package com.example.mebelar.presentation.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mebelar.domain.model.CategoriesCardData
import com.example.mebelar.domain.model.Data
import com.example.mebelar.ui.theme.ScreenState
import kotlinx.coroutines.launch

class CategoriesViewModel() : ViewModel() {
    private val _categories = MutableLiveData<List<CategoriesCardData>>(emptyList())
    val categories: LiveData<List<CategoriesCardData>> get() = _categories

    private val _screenState = MutableLiveData<ScreenState>(ScreenState.Idle)
    val screenState: LiveData<ScreenState> get() = _screenState

    fun fetchCategories(){
        viewModelScope.launch(){
            _categories.value = Data().categorys
            _screenState.value = ScreenState.Success
        }
    }
}