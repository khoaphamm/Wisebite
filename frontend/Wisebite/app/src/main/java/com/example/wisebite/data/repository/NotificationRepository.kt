package com.example.wisebite.data.repository

import com.example.wisebite.data.model.Notification
import com.example.wisebite.data.model.ApiResult
import com.example.wisebite.data.api.NotificationApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NotificationRepository private constructor() {
    
    companion object {
        private const val BASE_URL = "https://nondiabolic-twanna-unsensitive.ngrok-free.dev/"
        
        @Volatile
        private var INSTANCE: NotificationRepository? = null
        
        fun getInstance(): NotificationRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NotificationRepository().also { INSTANCE = it }
            }
        }
    }
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    private val notificationApiService = retrofit.create(NotificationApiService::class.java)
    
    suspend fun loadNotifications(
        authToken: String,
        skip: Int = 0,
        limit: Int = 20,
        unreadOnly: Boolean = false
    ): ApiResult<List<Notification>> = withContext(Dispatchers.IO) {
        try {
            val response = notificationApiService.getUserNotifications(
                authToken = "Bearer $authToken",
                skip = skip,
                limit = limit,
                unreadOnly = unreadOnly
            )
            
            if (response.isSuccessful) {
                val notificationResponse = response.body()
                if (notificationResponse != null) {
                    ApiResult.Success(notificationResponse.notifications)
                } else {
                    ApiResult.Error("No data received from server")
                }
            } else {
                ApiResult.Error("Failed to load notifications: ${response.message()}")
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error: ${e.message}")
        }
    }
    
    suspend fun markNotificationAsRead(
        authToken: String,
        notificationId: String
    ): ApiResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = notificationApiService.markNotificationAsRead(
                authToken = "Bearer $authToken",
                notificationId = notificationId
            )
            
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error("Failed to mark notification as read: ${response.message()}")
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error: ${e.message}")
        }
    }
}