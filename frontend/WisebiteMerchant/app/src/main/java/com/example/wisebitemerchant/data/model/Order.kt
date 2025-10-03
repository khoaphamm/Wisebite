package com.example.wisebitemerchant.data.model

import com.google.gson.annotations.SerializedName
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

data class Order(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("customer_id")
    val customerId: String,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("total_amount")
    val totalAmount: Double,
    
    @SerializedName("delivery_address")
    val deliveryAddress: String?,
    
    @SerializedName("notes")
    val notes: String?,
    
    @SerializedName("preferred_pickup_time")
    val preferredPickupTime: String?,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("customer")
    val customer: Customer?,
    
    @SerializedName("items")
    val items: List<OrderItem>?
) {
    // Helper properties for UI display
    val formattedTotalAmount: String
        get() = NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(totalAmount)
    
    val formattedCreatedAt: String
        get() = try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val date = inputFormat.parse(createdAt)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            createdAt
        }
    
    val formattedPickupTime: String?
        get() = preferredPickupTime?.let { time ->
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val date = inputFormat.parse(time)
                outputFormat.format(date ?: Date())
            } catch (e: Exception) {
                time
            }
        }
    
    val itemsDisplay: String
        get() = items?.joinToString(", ") { item ->
            val name = item.surpriseBag?.name ?: item.foodItem?.name ?: "Unknown Item"
            if (item.quantity > 1) "$name x${item.quantity}" else name
        } ?: "No items"
    
    val customerName: String
        get() = customer?.fullName ?: "Unknown Customer"
    
    val merchantOrderStatus: MerchantOrderStatus
        get() = when (status.lowercase()) {
            "pending" -> MerchantOrderStatus.NEW
            "confirmed" -> MerchantOrderStatus.CONFIRMED
            "awaiting_pickup" -> MerchantOrderStatus.READY
            "completed" -> MerchantOrderStatus.COMPLETED
            "cancelled" -> MerchantOrderStatus.CANCELLED
            else -> MerchantOrderStatus.NEW
        }
}

data class Customer(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("full_name")
    val fullName: String,
    
    @SerializedName("phone_number")
    val phoneNumber: String,
    
    @SerializedName("email")
    val email: String
)

data class OrderItem(
    @SerializedName("quantity")
    val quantity: Int,
    
    @SerializedName("price_per_item")
    val pricePerItem: Double,
    
    @SerializedName("surprise_bag")
    val surpriseBag: SurpriseBagSummary?,
    
    @SerializedName("food_item")
    val foodItem: FoodItemSummary?
)

data class SurpriseBagSummary(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String?
)

data class FoodItemSummary(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String?
)

data class OrderStatusUpdateRequest(
    @SerializedName("status")
    val status: String
)

data class OrdersResponse(
    @SerializedName("data")
    val data: List<Order>,
    
    @SerializedName("count")
    val count: Int
)

// Enum for merchant order statuses
enum class MerchantOrderStatus {
    NEW, CONFIRMED, PREPARING, READY, COMPLETED, CANCELLED
}