package com.example.wisebitemerchant.data.model

import java.text.SimpleDateFormat
import java.util.*

data class SurpriseBag(
    val id: String,
    val name: String,
    val description: String? = null,
    val bagType: String, // "combo", "single_category"
    val category: String, // "Fresh Grocery", "Thịt/Cá", "Rau/Củ", "Trái cây", "Bánh mì"
    val originalValue: Double,
    val discountedPrice: Double,
    val quantityAvailable: Int,
    val pickupStartTime: Date,
    val pickupEndTime: Date,
    val isActive: Boolean,
    val createdAt: Date,
    val updatedAt: Date,
    val storeId: String
) {
    val discountPercentage: Double
        get() = ((originalValue - discountedPrice) / originalValue) * 100
    
    val isAvailableForPickup: Boolean
        get() {
            val now = Date()
            // DEMO MODE: More lenient availability checking for demo purposes
            return quantityAvailable > 0 && (now.before(pickupEndTime) || pickupEndTime.time > now.time - 24 * 60 * 60 * 1000) // Allow 24 hours after end time for demo
        }
    
    val timeDisplayText: String
        get() {
            val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            return "${formatter.format(pickupStartTime)} - ${formatter.format(pickupEndTime)}"
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

// Time window constraints - DEMO MODE: More permissive hours
object SurpriseBagTimeWindows {
    const val ORDER_START_HOUR = 0  // 12 AM - Allow all day for demo
    const val ORDER_END_HOUR = 23   // 11 PM - Allow all day for demo
    const val PICKUP_START_HOUR = 0 // 12 AM - Allow all day for demo
    const val PICKUP_END_HOUR = 23  // 11 PM - Allow all day for demo
    
    const val MIN_DISCOUNT_PERCENTAGE = 45.0 // Minimum 45% discount
    const val COMMISSION_PERCENTAGE = 5.0    // 5% commission included
}