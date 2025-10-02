package com.example.wisebite.data.model

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

/**
 * Data model representing a notification from the backend API
 */
data class Notification(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("type") val type: String,
    @SerializedName("is_read") val isRead: Boolean,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("related_entity_id") val relatedEntityId: String? = null,
    @SerializedName("related_entity_type") val relatedEntityType: String? = null
)

/**
 * Response wrapper for notification list
 */
data class NotificationListResponse(
    @SerializedName("notifications") val notifications: List<Notification>,
    @SerializedName("total") val total: Int,
    @SerializedName("unread_count") val unreadCount: Int
)

/**
 * Request body for marking notification as read
 */
data class MarkAsReadRequest(
    @SerializedName("notification_id") val notificationId: String
)

/**
 * Generic API response wrapper
 */
data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: T? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("error") val error: String? = null
)

/**
 * Sealed class for API result handling
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}

data class NotificationCreate(
    val title: String,
    val message: String,
    @SerializedName("is_important")
    val isImportant: Boolean = false
)

data class MarkAsReadResponse(
    val msg: String
)

/**
 * Extension function to convert backend Notification to WisebiteNotification
 */
fun Notification.toWisebiteNotification(): com.example.wisebite.service.WisebiteNotification {
    return com.example.wisebite.service.WisebiteNotification(
        id = this.id,
        title = this.title,
        message = this.content,
        type = when (this.type.lowercase()) {
            "order_update" -> com.example.wisebite.service.NotificationType.ORDER_UPDATE
            "pickup_reminder" -> com.example.wisebite.service.NotificationType.PICKUP_REMINDER
            "promotion" -> com.example.wisebite.service.NotificationType.PROMOTION
            else -> com.example.wisebite.service.NotificationType.ORDER_UPDATE
        },
        isImportant = this.type.lowercase() in listOf("order_update", "pickup_reminder"),
        timestamp = try {
            // Convert ISO timestamp to milliseconds
            LocalDateTime.parse(this.createdAt.replace("Z", "")).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (e: Exception) {
            System.currentTimeMillis()
        },
        isRead = this.isRead
    )
}