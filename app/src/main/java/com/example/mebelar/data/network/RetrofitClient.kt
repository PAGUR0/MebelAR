package com.example.mebelar.data.network

import android.content.Context
import android.util.Log
import com.example.mebelar.data.dto.TokenResponse
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://91.132.57.82:5000/"

    fun getApiService(context: Context): ApiService {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = Interceptor { chain ->
            val request = chain.request()
            val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
            val accessToken = sharedPreferences.getString("access_token", null)
            Log.d("RetrofitClient", "Access token: ${accessToken?.take(10) ?: "null"}")

            val newRequest = if (accessToken != null) {
                Log.d("RetrofitClient", "Adding Authorization header: Bearer $accessToken")
                request.newBuilder()
                    .addHeader("Authorization", "Bearer $accessToken")
                    .build()
            } else {
                Log.w("RetrofitClient", "No auth token available, proceeding without Authorization header")
                request
            }
            chain.proceed(newRequest)
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(authInterceptor)
            .authenticator(TokenAuthenticator(context))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    fun downloadModelFromUrl(url: String, onSuccess: (InputStream) -> Unit, onFailure: (Throwable) -> Unit) {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                onFailure(e)
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.byteStream()?.let {
                        onSuccess(it)
                    } ?: onFailure(Throwable("Empty response body"))
                } else {
                    onFailure(Throwable("Failed to download model"))
                }
            }
        })
    }
}
// gunicorn -w 4 -b 0.0.0.0:5000 app:app --keep-alive 30 --worker-class gevent --log-level debug