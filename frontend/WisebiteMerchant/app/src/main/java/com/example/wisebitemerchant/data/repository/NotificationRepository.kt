package com.example.wisebitemerchant.data.repository

import com.example.wisebitemerchant.data.api.NotificationApiService
import com.example.wisebitemerchant.data.model.*
import com.example.wisebitemerchant.service.MerchantNotification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Repository for handling notification data operations
 * Provides abstraction layer between API and UI components
 */
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
    
    /**
     * Load notifications from the backend API
     * @param authToken Bearer token for authentication
     * @param skip Number of notifications to skip (for pagination)
     * @param limit Maximum number of notifications to fetch
     * @param unreadOnly Whether to fetch only unread notifications
     * @return ApiResult containing list of MerchantNotifications
     */
    suspend fun loadNotifications(
        authToken: String,
        skip: Int = 0,
        limit: Int = 20,
        unreadOnly: Boolean = false
    ): ApiResult<List<MerchantNotification>> = withContext(Dispatchers.IO) {
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
                    val merchantNotifications = notificationResponse.notifications.map { notification ->
                        notification.toMerchantNotification()
                    }
                    ApiResult.Success(merchantNotifications)
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
    
    /**
     * Mark a notification as read
     * @param authToken Bearer token for authentication
     * @param notificationId ID of the notification to mark as read
     * @return ApiResult indicating success or failure
     */
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
    
    /**
     * Mark all notifications as read
     * @param authToken Bearer token for authentication
     * @return ApiResult indicating success or failure
     */
    suspend fun markAllNotificationsAsRead(
        authToken: String
    ): ApiResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = notificationApiService.markAllNotificationsAsRead(
                authToken = "Bearer $authToken"
            )
            
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error("Failed to mark all notifications as read: ${response.message()}")
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error: ${e.message}")
        }
    }
    
    /**
     * Get unread notification count
     * @param authToken Bearer token for authentication
     * @return ApiResult containing the unread count
     */
    suspend fun getUnreadCount(authToken: String): ApiResult<Int> = withContext(Dispatchers.IO) {
        try {
            val response = notificationApiService.getUnreadCount(
                authToken = "Bearer $authToken"
            )
            
            if (response.isSuccessful) {
                val notificationResponse = response.body()
                if (notificationResponse != null) {
                    ApiResult.Success(notificationResponse.unreadCount)
                } else {
                    ApiResult.Error("No data received from server")
                }
            } else {
                ApiResult.Error("Failed to get unread count: ${response.message()}")
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error: ${e.message}")
        }
    }
    
    /**
     * Delete a notification
     * @param authToken Bearer token for authentication
     * @param notificationId ID of the notification to delete
     * @return ApiResult indicating success or failure
     */
    suspend fun deleteNotification(
        authToken: String,
        notificationId: String
    ): ApiResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = notificationApiService.deleteNotification(
                authToken = "Bearer $authToken",
                notificationId = notificationId
            )
            
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error("Failed to delete notification: ${response.message()}")
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error: ${e.message}")
        }
    }
}