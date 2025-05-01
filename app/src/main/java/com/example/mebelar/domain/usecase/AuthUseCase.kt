package com.example.mebelar.domain.usecase

import android.util.Log
import com.example.mebelar.data.dto.AuthResult
import com.example.mebelar.domain.repository.AuthRepository

class CheckAuthUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(): Boolean {
        Log.d("CheckAuthUseCase", "Checking authentication status")
        val isAuthenticated = authRepository.isAuthenticated()
        Log.d("CheckAuthUseCase", "Authentication status: $isAuthenticated")
        return isAuthenticated
    }
}

class RegisterUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(login: String, password: String, firstName: String?, lastName: String?): AuthResult {
        return authRepository.register(login, password, firstName, lastName)
    }
}

class LoginUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(login: String, password: String): AuthResult {
        return authRepository.login(login, password)
    }
}