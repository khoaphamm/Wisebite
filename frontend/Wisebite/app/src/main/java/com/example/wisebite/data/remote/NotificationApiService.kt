package com.example.wisebite.data.remote

import com.example.wisebite.data.model.Notification
import com.example.wisebite.data.model.MarkAsReadResponse
import retrofit2.Response
import retrofit2.http.*

interface NotificationApiService {
    
    @GET("user/me/notifications")
    suspend fun getUserNotifications(): Response<List<Notification>>
    
    @POST("user/read/{notification_id}")
    suspend fun markNotificationAsRead(
        @Path("notification_id") notificationId: String
    ): Response<MarkAsReadResponse>
}