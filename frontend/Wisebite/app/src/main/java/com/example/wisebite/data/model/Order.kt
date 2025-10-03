package com.example.wisebite.data.model

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*

data class Order(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("customer_id")
    val customerId: String,
    
    @SerializedName("status")
    private val _status: String, // backend sends string
    
    @SerializedName("total_amount")
    val totalAmount: Double,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("delivery_address")
    val deliveryAddress: String?,
    
    @SerializedName("notes")
    val notes: String?,
    
    @SerializedName("preferred_pickup_time")
    val preferredPickupTime: String?,
    
    @SerializedName("customer")
    val customer: User?,
    
    @SerializedName("items")
    val items: List<OrderItem>?,
    
    @SerializedName("store")
    val store: Store? = null
) {
    // Convert string status to enum for UI
    val status: OrderStatus
        get() = OrderStatus.fromString(_status)
        
    // Helper for displaying pickup time
    val pickupTimeDisplay: String
        get() {
            return try {
                if (preferredPickupTime != null) {
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale.getDefault())
                    val pickupDate = inputFormat.parse(preferredPickupTime)
                    "Nhận lúc: ${outputFormat.format(pickupDate ?: Date())}"
                } else {
                    "Thời gian nhận: Chưa chọn"
                }
            } catch (e: Exception) {
                "Thời gian nhận: Không xác định"
            }
        }
}

data class OrderItem(
    @SerializedName("quantity")
    val quantity: Int,
    
    @SerializedName("price_per_item")
    val unitPrice: Double, // renamed for clarity
    
    @SerializedName("surprise_bag")
    val surpriseBag: SurpriseBag?,
    
    @SerializedName("food_item")
    val foodItem: FoodItem?
)

// Request models for creating orders
data class CreateOrderRequest(
    @SerializedName("items")
    val items: List<CreateOrderItemRequest>,
    
    @SerializedName("delivery_address")
    val deliveryAddress: String?,
    
    @SerializedName("notes")
    val notes: String?,
    
    @SerializedName("preferred_pickup_time")
    val preferredPickupTime: String? // ISO format: yyyy-MM-dd'T'HH:mm:ss
)

data class CreateOrderItemRequest(
    @SerializedName("surprise_bag_id")
    val surpriseBagId: String?,
    
    @SerializedName("food_item_id")
    val foodItemId: String?,
    
    @SerializedName("quantity")
    val quantity: Int
)

data class OrderStatusUpdateRequest(
    @SerializedName("status")
    val status: String
)

// Response wrapper for pagination
data class OrdersResponse(
    @SerializedName("data")
    val data: List<Order>,
    
    @SerializedName("count")
    val count: Int,
    
    @SerializedName("skip")
    val skip: Int = 0,
    
    @SerializedName("limit")
    val limit: Int = 100
)

// Order status enum for convenience
enum class OrderStatus(val value: String, val displayName: String, val color: Long) {
    PENDING("pending", "Chờ xác nhận", 0xFFFF9800),
    PENDING_PAYMENT("pending_payment", "Chờ thanh toán", 0xFFFF5722),
    CONFIRMED("confirmed", "Đã xác nhận", 0xFF2196F3),
    PREPARING("preparing", "Đang chuẩn bị", 0xFF9C27B0),
    READY_FOR_PICKUP("ready_for_pickup", "Sẵn sàng nhận", 0xFF4CAF50),
    AWAITING_PICKUP("awaiting_pickup", "Chờ nhận hàng", 0xFF9C27B0),
    COMPLETED("completed", "Đã hoàn thành", 0xFF4CAF50),
    CANCELLED("cancelled", "Đã hủy", 0xFF757575);
    
    companion object {
        fun fromString(status: String): OrderStatus {
            return values().find { it.value == status } ?: PENDING
        }
    }
}