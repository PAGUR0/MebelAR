package com.example.mebelar.presentation.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mebelar.data.dto.AuthResult
import com.example.mebelar.domain.model.ProductCardData
import com.example.mebelar.domain.usecase.CheckAuthUseCase
import com.example.mebelar.domain.usecase.GetViewsUseCase
import com.example.mebelar.domain.usecase.LoginUseCase
import com.example.mebelar.domain.usecase.RegisterUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase,
): ViewModel() {
    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> get() = _registerState

    fun register(login: String, password: String, firstName: String?, lastName: String?) {
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading
            when (val result = registerUseCase(login, password, firstName, lastName)) {
                is AuthResult.Success -> _registerState.value = RegisterState.Success
                is AuthResult.Error -> _registerState.value = RegisterState.Error(result.message)
            }
        }
    }
    fun resetState() {
        _registerState.value = RegisterState.Idle
    }

}

sealed class RegisterState {
    object Idle : RegisterState()
    object Success : RegisterState()
    object Loading : RegisterState()
    data class Error(val message: String) : RegisterState()
}


class RegisterViewModelFactory(
    val registerUseCase: RegisterUseCase,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RegisterViewModel(registerUseCase) as T
    }
}