package com.example.wisebitemerchant.data.api

import com.example.wisebitemerchant.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * API service for notification endpoints
 * Integrates with backend notification system at /api/v1/notifications/
 */
interface NotificationApiService {
    
    /**
     * Get notifications for the current merchant user
     * Uses user-specific endpoint: GET /user/me/notifications
     */
    @GET("user/me/notifications")
    suspend fun getUserNotifications(
        @Header("Authorization") authToken: String,
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 20,
        @Query("unread_only") unreadOnly: Boolean = false
    ): Response<NotificationListResponse>
    
    /**
     * Mark a notification as read
     * Uses user endpoint: PUT /user/read/{notification_id}
     */
    @PUT("user/read/{notification_id}")
    suspend fun markNotificationAsRead(
        @Header("Authorization") authToken: String,
        @Path("notification_id") notificationId: String
    ): Response<ApiResponse<Unit>>
    
    /**
     * Mark all notifications as read for the current user
     * Uses user endpoint: PUT /user/read_all
     */
    @PUT("user/read_all")
    suspend fun markAllNotificationsAsRead(
        @Header("Authorization") authToken: String
    ): Response<ApiResponse<Unit>>
    
    /**
     * Get unread notification count
     * Uses user endpoint: GET /user/me/notifications with unread_only=true
     */
    @GET("user/me/notifications")
    suspend fun getUnreadCount(
        @Header("Authorization") authToken: String,
        @Query("unread_only") unreadOnly: Boolean = true,
        @Query("limit") limit: Int = 1  // We only need the count
    ): Response<NotificationListResponse>
    
    /**
     * Delete a notification (if supported by backend)
     * Uses general endpoint: DELETE /api/v1/notifications/{notification_id}
     */
    @DELETE("api/v1/notifications/{notification_id}")
    suspend fun deleteNotification(
        @Header("Authorization") authToken: String,
        @Path("notification_id") notificationId: String
    ): Response<ApiResponse<Unit>>
}