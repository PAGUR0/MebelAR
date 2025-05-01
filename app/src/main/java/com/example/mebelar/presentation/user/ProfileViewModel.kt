package com.example.mebelar.presentation.user

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mebelar.domain.model.ProductCardData
import com.example.mebelar.domain.usecase.CheckAuthUseCase
import com.example.mebelar.domain.usecase.GetFavoritesUseCase
import com.example.mebelar.domain.usecase.GetViewsUseCase
import com.example.mebelar.domain.usecase.AddFavoriteUseCase
import com.example.mebelar.domain.usecase.RemoveFavoriteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ProfileState {
    object Loading : ProfileState()
    object Authenticated : ProfileState()
    object Unauthenticated : ProfileState()
    data class Error(val message: String) : ProfileState()
}

class ProfileViewModel(
    private val checkAuthUseCase: CheckAuthUseCase,
    private val getViewsUseCase: GetViewsUseCase,
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val addFavoriteUseCase: AddFavoriteUseCase,
    private val removeFavoriteUseCase: RemoveFavoriteUseCase,
    private val context: Context // Добавляем Context для доступа к SharedPreferences
) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState: StateFlow<ProfileState> get() = _profileState

    private val _viewsList = MutableStateFlow<List<ProductCardData>>(emptyList())
    val viewsList: StateFlow<List<ProductCardData>> get() = _viewsList

    private val _favoritesList = MutableStateFlow<List<ProductCardData>>(emptyList())
    val favoritesList: StateFlow<List<ProductCardData>> get() = _favoritesList

    private val _userName = MutableStateFlow(Pair("", ""))
    val userName: StateFlow<Pair<String, String>> get() = _userName

    fun checkAuth() {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                val isAuthenticated = checkAuthUseCase.invoke()
                if (isAuthenticated) {
                    // Загружаем имя и фамилию из SharedPreferences
                    val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                    val firstName = sharedPreferences.getString("first_name", "") ?: ""
                    val lastName = sharedPreferences.getString("last_name", "") ?: ""
                    Log.d("ProfileViewModel", "Loaded user name from SharedPreferences: firstName=$firstName, lastName=$lastName")
                    _userName.value = Pair(firstName, lastName)
                    _profileState.value = ProfileState.Authenticated
                    loadViews()
                    loadFavorites()
                } else {
                    Log.d("ProfileViewModel", "User is not authenticated")
                    _profileState.value = ProfileState.Unauthenticated
                    _userName.value = Pair("", "")
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error loading profile: ${e.message}")
                _profileState.value = ProfileState.Error("Ошибка загрузки профиля: ${e.message}")
                _userName.value = Pair("", "")
            }
        }
    }

    private suspend fun loadViews() {
        try {
            val views = getViewsUseCase.invoke()
            Log.d("ProfileViewModel", "Views loaded: ${views.size} items")
            _viewsList.value = views
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "Error loading views: ${e.message}")
            if (e.message?.contains("Для просмотра истории необходимо зарегистрироваться") == true) {
                _profileState.value = ProfileState.Unauthenticated
            }
        }
    }

    private suspend fun loadFavorites() {
        try {
            val favorites = getFavoritesUseCase.invoke()
            Log.d("ProfileViewModel", "Favorites loaded: ${favorites.size} items")
            _favoritesList.value = favorites
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "Error loading favorites: ${e.message}")
            if (e.message?.contains("Для просмотра избранного необходимо зарегистрироваться") == true) {
                _profileState.value = ProfileState.Unauthenticated
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            Log.d("ProfileViewModel", "Logging out")
            _profileState.value = ProfileState.Unauthenticated
            _viewsList.value = emptyList()
            _favoritesList.value = emptyList()
            _userName.value = Pair("", "")
        }
    }

    fun updateUserName(firstName: String, lastName: String) {
        Log.d("ProfileViewModel", "Updating user name to: $firstName $lastName")
        _userName.value = Pair(firstName, lastName)
        // Сохраняем обновленные данные в SharedPreferences
        val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("first_name", firstName)
            putString("last_name", lastName)
            apply()
        }
        Log.d("ProfileViewModel", "User name saved to SharedPreferences: firstName=$firstName, lastName=$lastName")
    }

    fun addFavorite(productId: Int) {
        viewModelScope.launch {
            try {
                val success = addFavoriteUseCase.invoke(productId)
                if (success) {
                    Log.d("ProfileViewModel", "Product $productId added to favorites")
                    loadFavorites()
                    _viewsList.value = _viewsList.value.map {
                        if (it.id == productId) it.copy(favorite = true) else it
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error adding favorite: ${e.message}")
                throw e
            }
        }
    }

    fun removeFavorite(productId: Int) {
        viewModelScope.launch {
            try {
                val success = removeFavoriteUseCase.invoke(productId)
                if (success) {
                    Log.d("ProfileViewModel", "Product $productId removed from favorites")
                    loadFavorites()
                    _viewsList.value = _viewsList.value.map {
                        if (it.id == productId) it.copy(favorite = false) else it
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error removing favorite: ${e.message}")
                throw e
            }
        }
    }
}

class ProfileViewModelFactory(
    private val checkAuthUseCase: CheckAuthUseCase,
    private val getViewsUseCase: GetViewsUseCase,
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val addFavoriteUseCase: AddFavoriteUseCase,
    private val removeFavoriteUseCase: RemoveFavoriteUseCase,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProfileViewModel(
            checkAuthUseCase,
            getViewsUseCase,
            getFavoritesUseCase,
            addFavoriteUseCase,
            removeFavoriteUseCase,
            context
        ) as T
    }
}