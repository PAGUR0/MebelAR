package com.example.mebelar.domain.repository

import com.example.mebelar.data.dto.AuthResult

interface AuthRepository {
    suspend fun register(login: String, password: String, firstName: String?, lastName: String?): AuthResult
    suspend fun login(login: String, password: String): AuthResult
    suspend fun isAuthenticated(): Boolean
    suspend fun logout(): Boolean
    suspend fun saveTokens(accessToken: String, refreshToken: String): Boolean
}