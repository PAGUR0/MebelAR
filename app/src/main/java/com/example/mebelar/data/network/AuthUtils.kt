package com.example.mebelar.data.network

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log

object AuthUtils {
    private const val PREFS_NAME = "auth"

    @SuppressLint("ApplySharedPref")
    fun saveTokens(context: Context, accessToken: String, refreshToken: String): Boolean {
        if (accessToken.isBlank() || refreshToken.isBlank()) {
            Log.e("AuthUtils", "Failed to save tokens: accessToken or refreshToken is empty")
            return false
        }

        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("access_token", accessToken)
            putString("refresh_token", refreshToken)
            commit() // Используем commit для синхронной записи
        }

        // Проверяем, что токены сохранены
        val savedAccessToken = sharedPreferences.getString("access_token", null)
        val savedRefreshToken = sharedPreferences.getString("refresh_token", null)
        val isSaved = savedAccessToken == accessToken && savedRefreshToken == refreshToken
        Log.d("AuthUtils", "Tokens saved: access_token=${accessToken.take(10)}..., refresh_token=${refreshToken.take(10)}..., success=$isSaved")
        if (!isSaved) {
            Log.e("AuthUtils", "Failed to verify saved tokens: access_token=$savedAccessToken, refresh_token=$savedRefreshToken")
        }
        return isSaved
    }

    fun getRefreshToken(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val refreshToken = sharedPreferences.getString("refresh_token", null)
        Log.d("AuthUtils", "getRefreshToken: refresh_token=${refreshToken?.take(10) ?: "null"}")
        return refreshToken
    }

    fun getToken(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val accessToken = sharedPreferences.getString("access_token", null)
        Log.d("AuthUtils", "getToken: access_token=${accessToken?.take(10) ?: "null"}")
        return accessToken
    }

    fun isAuthenticated(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("access_token", null)
        Log.d("AuthUtils", "isAuthenticated: token=${token != null}")
        return token != null && token.isNotEmpty()
    }

    fun clearTokens(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            remove("access_token")
            remove("refresh_token")
            commit()
        }
        Log.d("AuthUtils", "Tokens cleared")
    }

    fun clearAuthData(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            clear()
            commit()
        }
        Log.d("AuthUtils", "Auth data cleared")
    }
}