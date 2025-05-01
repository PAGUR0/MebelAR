package com.example.mebelar.data.repository

import android.content.Context
import android.util.Log
import com.example.mebelar.data.dto.AuthResult
import com.example.mebelar.data.dto.LoginRequest
import com.example.mebelar.data.dto.RegisterRequest
import com.example.mebelar.data.network.ApiService
import com.example.mebelar.data.network.AuthUtils
import com.example.mebelar.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class AuthRepositoryImpl(
    private val apiService: ApiService,
    private val context: Context
) : AuthRepository {

    override suspend fun login(login: String, password: String): AuthResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("AuthRepository", "Attempting login with login=$login")
                val request = LoginRequest(login = login, password = password)
                val response = apiService.login(request)
                Log.d("AuthRepository", "Login response: code=${response.code()}, message=${response.message()}")

                if (response.isSuccessful) {
                    val tokenResponse = response.body()
                    if (tokenResponse != null) {
                        Log.d("AuthRepository", "Token response received: access_token=${tokenResponse.accessToken.take(10)}..., refresh_token=${tokenResponse.refreshToken.take(10)}..., first_name=${tokenResponse.firstName}, last_name=${tokenResponse.lastName}")
                        // Логируем полный токен для диагностики
                        Log.d("AuthRepository", "Full access_token=${tokenResponse.accessToken}")
                        val saved = saveTokens(tokenResponse.accessToken, tokenResponse.refreshToken)
                        if (saved) {
                            // Сохраняем firstName и lastName в SharedPreferences
                            val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                            with(sharedPreferences.edit()) {
                                putString("first_name", tokenResponse.firstName)
                                putString("last_name", tokenResponse.lastName)
                                apply()
                            }
                            Log.d("AuthRepository", "Login successful, tokens and user info saved")
                            // Проверяем содержимое SharedPreferences
                            Log.d("AuthRepository", "SharedPreferences after save: access_token=${sharedPreferences.getString("access_token", "null")}, refresh_token=${sharedPreferences.getString("refresh_token", "null")}, first_name=${sharedPreferences.getString("first_name", "null")}, last_name=${sharedPreferences.getString("last_name", "null")}")
                            AuthResult.Success(tokenResponse.accessToken, tokenResponse.refreshToken)
                        } else {
                            Log.e("AuthRepository", "Login failed: could not save tokens")
                            AuthResult.Error("Ошибка сохранения токенов")
                        }
                    } else {
                        Log.e("AuthRepository", "Login failed: response body is null")
                        val rawBody = response.raw().body?.string()
                        Log.e("AuthRepository", "Raw response body: $rawBody")
                        AuthResult.Error("Пустой ответ от сервера")
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "No error body"
                    Log.e("AuthRepository", "Login failed: code=${response.code()}, message=${response.message()}, errorBody=$errorBody")
                    val errorMessage = when (response.code()) {
                        400 -> "Логин или пароль не указаны"
                        401 -> "Неверный логин или пароль"
                        else -> "Ошибка сервера: ${response.code()}"
                    }
                    AuthResult.Error(errorMessage)
                }
            } catch (e: HttpException) {
                Log.e("AuthRepository", "Login HTTP error: code=${e.code()}, message=${e.message()}")
                AuthResult.Error("Ошибка HTTP: ${e.message()}")
            } catch (e: IOException) {
                Log.e("AuthRepository", "Login network error: ${e.message}")
                AuthResult.Error("Ошибка сети: проверьте подключение")
            } catch (e: Exception) {
                Log.e("AuthRepository", "Login unexpected error: ${e.message}")
                AuthResult.Error("Неизвестная ошибка: ${e.message}")
            }
        }
    }

    override suspend fun register(login: String, password: String, firstName: String?, lastName: String?): AuthResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("AuthRepository", "Attempting registration with login=$login, firstName=$firstName, lastName=$lastName")
                val request = RegisterRequest(
                    login = login,
                    password = password,
                    first_name = firstName,
                    last_name = lastName
                )
                val response = apiService.register(request)
                Log.d("AuthRepository", "Register response: code=${response.code()}, message=${response.message()}")

                if (response.isSuccessful) {
                    Log.d("AuthRepository", "Registration successful for login=$login")
                    AuthResult.Success("", "") // Сервер не возвращает токены при регистрации
                } else {
                    val errorBody = response.errorBody()?.string() ?: "No error body"
                    Log.e("AuthRepository", "Registration failed: code=${response.code()}, message=${response.message()}, errorBody=$errorBody")
                    val errorMessage = when (response.code()) {
                        400 -> "Пользователь уже существует или неверные данные"
                        else -> "Ошибка сервера: ${response.code()}"
                    }
                    AuthResult.Error(errorMessage)
                }
            } catch (e: HttpException) {
                Log.e("AuthRepository", "Registration HTTP error: code=${e.code()}, message=${e.message()}")
                AuthResult.Error("Ошибка HTTP: ${e.message()}")
            } catch (e: IOException) {
                Log.e("AuthRepository", "Registration network error: ${e.message}")
                AuthResult.Error("Ошибка сети: проверьте подключение")
            } catch (e: Exception) {
                Log.e("AuthRepository", "Registration unexpected error: ${e.message}")
                AuthResult.Error("Неизвестная ошибка: ${e.message}")
            }
        }
    }

    override suspend fun isAuthenticated(): Boolean {
        val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        val accessToken = sharedPreferences.getString("access_token", null)
        Log.d("AuthRepository", "Checking authentication: access_token=${accessToken?.take(10) ?: "null"}")
        return accessToken != null && accessToken.isNotEmpty()
    }

    override suspend fun logout(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                AuthUtils.clearTokens(context)
                // Очищаем first_name и last_name при выходе
                val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                with(sharedPreferences.edit()) {
                    remove("first_name")
                    remove("last_name")
                    apply()
                }
                Log.d("AuthRepository", "Logged out, tokens and user info cleared")
                true
            } catch (e: Exception) {
                Log.e("AuthRepository", "Logout error: ${e.message}")
                false
            }
        }
    }

    override suspend fun saveTokens(accessToken: String, refreshToken: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val saved = AuthUtils.saveTokens(context, accessToken, refreshToken)
                if (saved) {
                    Log.d("AuthRepository", "Tokens saved successfully: access_token=${accessToken.take(10)}..., refresh_token=${refreshToken.take(10)}...")
                } else {
                    Log.e("AuthRepository", "Failed to save tokens")
                }
                saved
            } catch (e: Exception) {
                Log.e("AuthRepository", "Error saving tokens: ${e.message}")
                false
            }
        }
    }
}