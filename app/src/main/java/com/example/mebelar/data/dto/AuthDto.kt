package com.example.mebelar.data.dto

sealed class AuthResult {
    data class Success(val accessToken: String, val refreshToken: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}