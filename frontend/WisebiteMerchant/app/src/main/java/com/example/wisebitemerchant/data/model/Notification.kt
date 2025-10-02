package com.example.wisebitemerchant.data.model

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
 * Sealed class for API result handling
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}

/**
 * Extension function to convert backend Notification to MerchantNotification
 */
fun Notification.toMerchantNotification(): com.example.wisebitemerchant.service.MerchantNotification {
    return com.example.wisebitemerchant.service.MerchantNotification(
        id = this.id,
        title = this.title,
        message = this.content,
        type = when (this.type.lowercase()) {
            "new_order" -> com.example.wisebitemerchant.service.MerchantNotificationType.NEW_ORDER
            "order_cancelled" -> com.example.wisebitemerchant.service.MerchantNotificationType.ORDER_CANCELLED
            "pickup_ready" -> com.example.wisebitemerchant.service.MerchantNotificationType.PICKUP_READY
            "payment_received" -> com.example.wisebitemerchant.service.MerchantNotificationType.PAYMENT_RECEIVED
            else -> com.example.wisebitemerchant.service.MerchantNotificationType.SYSTEM_UPDATE
        },
        isImportant = this.type.lowercase() in listOf("new_order", "order_cancelled"),
        orderId = this.relatedEntityId,
        timestamp = try {
            // Convert ISO timestamp to milliseconds
            LocalDateTime.parse(this.createdAt.replace("Z", "")).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    )
}