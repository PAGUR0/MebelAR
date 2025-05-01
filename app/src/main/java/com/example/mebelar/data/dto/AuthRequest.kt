package com.example.mebelar.data.dto

data class RegisterRequest(
    val login: String,
    val password: String,
    val first_name: String?,
    val last_name: String?
)

data class LoginRequest(
    val login: String,
    val password: String
)