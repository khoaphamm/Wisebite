package com.example.wisebitemerchant.data.model

data class FoodItem(
    val id: String,
    val name: String,
    val description: String? = null,
    val sku: String? = null,
    val imageUrl: String? = null,
    val standardPrice: Double,
    val costPrice: Double? = null,
    val isFresh: Boolean = true,
    val totalQuantity: Int,
    val surplusQuantity: Int = 0,
    val reservedQuantity: Int = 0,
    val availableQuantity: Int,
    val isMarkedForSurplus: Boolean = false,
    val surplusDiscountPercentage: Double? = null,
    val surplusPrice: Double? = null,
    val ingredients: String? = null,
    val allergens: String? = null,
    val weight: Double? = null,
    val unit: String = "piece",
    val isAvailable: Boolean = true,
    val isActive: Boolean = true,
    val categoryId: String? = null,
    val categoryName: String? = null
)

data class Category(
    val id: String,
    val name: String,
    val parentCategoryId: String? = null,
    val description: String? = null,
    val isActive: Boolean = true
)