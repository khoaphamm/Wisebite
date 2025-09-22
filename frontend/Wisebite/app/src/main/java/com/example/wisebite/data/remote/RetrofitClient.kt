package com.example.wisebite.data.remote

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    
    private const val TAG = "RetrofitClient"
    
    private fun getLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor { message ->
            Log.d(TAG, "HTTP: $message")
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }
    
    private fun getOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(getLoggingInterceptor())
            .addInterceptor { chain ->
                val request = chain.request()
                Log.d(TAG, "Making request to: ${request.url}")
                val response = chain.proceed(request)
                Log.d(TAG, "Response code: ${response.code}")
                response
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    private val retrofit: Retrofit by lazy {
        val baseUrl = WisebiteApiService.BASE_URL
        Log.d(TAG, "Initializing Retrofit with base URL: $baseUrl")
        
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(getOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    val apiService: WisebiteApiService by lazy {
        retrofit.create(WisebiteApiService::class.java)
    }
}