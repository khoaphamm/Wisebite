package com.example.wisebitemerchant.data.model

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class SurpriseBag(
    val id: String,
    val name: String,
    val description: String? = null,
    val bagType: String, // "combo", "single_category"
    val category: String, // "Fresh Grocery", "Thịt/Cá", "Rau/Củ", "Trái cây", "Bánh mì"
    val originalValue: Double,
    val discountedPrice: Double,
    val quantityAvailable: Int,
    val pickupStartTime: LocalDateTime,
    val pickupEndTime: LocalDateTime,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val storeId: String
) {
    val discountPercentage: Double
        get() = ((originalValue - discountedPrice) / originalValue) * 100
    
    val isAvailableForPickup: Boolean
        get() {
            val now = LocalDateTime.now()
            return now.isAfter(pickupStartTime) && now.isBefore(pickupEndTime) && quantityAvailable > 0
        }
    
    val timeDisplayText: String
        get() {
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            return "${pickupStartTime.format(formatter)} - ${pickupEndTime.format(formatter)}"
        }
}

// Predefined categories for surprise bags
object SurpriseBagCategories {
    const val COMBO = "Combo"
    const val MEAT_FISH = "Thịt/Cá"
    const val VEGETABLES = "Rau/Củ"
    const val FRUITS = "Trái cây"
    const val BREAD = "Bánh mì"
    
    val ALL_CATEGORIES = listOf(
        COMBO,
        MEAT_FISH,
        VEGETABLES,
        FRUITS,
        BREAD
    )
    
    fun getCategoryDisplayName(category: String): String {
        return when (category) {
            COMBO -> "Combo (Tổng hợp)"
            MEAT_FISH -> "Thịt/Cá"
            VEGETABLES -> "Rau/Củ"
            FRUITS -> "Trái cây"
            BREAD -> "Bánh mì"
            else -> category
        }
    }
}

// Time window constraints
object SurpriseBagTimeWindows {
    const val ORDER_START_HOUR = 14 // 2 PM
    const val ORDER_END_HOUR = 18   // 6 PM
    const val PICKUP_START_HOUR = 14 // 2 PM  
    const val PICKUP_END_HOUR = 20   // 8 PM
    
    const val MIN_DISCOUNT_PERCENTAGE = 45.0 // Minimum 45% discount
    const val COMMISSION_PERCENTAGE = 5.0    // 5% commission included
}