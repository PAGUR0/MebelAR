package com.example.mebelar.data.network

import android.content.Context
import android.util.Log
import com.example.mebelar.data.dto.TokenResponse
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TokenAuthenticator(private val context: Context) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        Log.d("TokenAuthenticator", "Attempting to refresh token due to ${response.code} error")

        if (responseCount(response) >= 3) {
            Log.w("TokenAuthenticator", "Max retry attempts reached (3), aborting")
            return null
        }

        val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        val refreshToken = sharedPreferences.getString("refresh_token", null)
        Log.d("TokenAuthenticator", "Refresh token: ${refreshToken?.take(10) ?: "null"}")

        if (refreshToken == null) {
            Log.w("TokenAuthenticator", "No refresh token available, cannot refresh")
            return null // Не очищаем токены, чтобы не потерять их преждевременно
        }

        try {
            // Создаем временный Retrofit клиент без authInterceptor
            val retrofit = Retrofit.Builder()
                .baseUrl("http://91.132.57.82:5000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val apiService = retrofit.create(ApiService::class.java)

            val refreshResponse = apiService.refreshToken(mapOf("refresh_token" to refreshToken)).execute()
            Log.d("TokenAuthenticator", "Refresh response: code=${refreshResponse.code()}, message=${refreshResponse.message()}")

            if (refreshResponse.isSuccessful) {
                val tokenResponse = refreshResponse.body()
                if (tokenResponse != null) {
                    Log.d("TokenAuthenticator", "Token refreshed: new access_token=${tokenResponse.accessToken.take(10)}..., refresh_token=${tokenResponse.refreshToken.take(10)}...")
                    // Сохраняем новые токены
                    val saved = AuthUtils.saveTokens(context, tokenResponse.accessToken, tokenResponse.refreshToken)
                    if (saved) {
                        Log.d("TokenAuthenticator", "New tokens saved successfully")
                        // Создаем новый запрос с обновленным токеном
                        return response.request.newBuilder()
                            .header("Authorization", "Bearer ${tokenResponse.accessToken}")
                            .build()
                    } else {
                        Log.e("TokenAuthenticator", "Failed to save new tokens")
                        return null
                    }
                } else {
                    Log.e("TokenAuthenticator", "Refresh failed: response body is null")
                    val rawBody = refreshResponse.raw().body?.string()
                    Log.e("TokenAuthenticator", "Raw response body: $rawBody")
                    return null
                }
            } else {
                Log.e("TokenAuthenticator", "Refresh failed: code=${refreshResponse.code()}, message=${refreshResponse.message()}")
                val errorBody = refreshResponse.errorBody()?.string()
                Log.e("TokenAuthenticator", "Error body: $errorBody")
                return null
            }
        } catch (e: Exception) {
            Log.e("TokenAuthenticator", "Error refreshing token: ${e.message}")
            return null
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}