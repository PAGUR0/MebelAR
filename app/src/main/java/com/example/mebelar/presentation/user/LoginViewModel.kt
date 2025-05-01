package com.example.mebelar.presentation.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mebelar.data.dto.AuthResult
import com.example.mebelar.domain.usecase.LoginUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    val loginUseCase: LoginUseCase,
) : ViewModel() {
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> get() = _loginState

    fun login(login: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            when (val result = loginUseCase(login, password)) {
                is AuthResult.Success -> _loginState.value = LoginState.Success
                is AuthResult.Error -> _loginState.value = LoginState.Error(result.message ?: "Неверный логин или пароль")
            }
        }
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Success : LoginState()
    object Loading : LoginState()
    data class Error(val message: String) : LoginState()
}

class LoginViewModelFactory(
    val loginUseCase: LoginUseCase,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LoginViewModel(loginUseCase) as T
    }
}