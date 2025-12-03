package com.example.wisebitemerchant.data.api

import com.example.wisebitemerchant.data.model.Order
import com.example.wisebitemerchant.data.model.OrderStatusUpdateRequest
import com.example.wisebitemerchant.data.model.OrdersResponse
import retrofit2.Response
import retrofit2.http.*

// Data classes for API requests/responses
data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val access_token: String,
    val token_type: String = "bearer"
)

data class GoogleSignInRequest(
    val id_token: String
)

data class CreateFoodItemRequest(
    val name: String,
    val description: String? = null,
    val sku: String? = null,
    val standard_price: Double,
    val cost_price: Double? = null,
    val total_quantity: Int,
    val is_fresh: Boolean = true,
    val category_id: String? = null,
    val ingredients: String? = null,
    val allergens: String? = null,
    val weight: Double? = null,
    val unit: String = "piece"
)

data class FoodItemResponse(
    val id: String,
    val name: String,
    val description: String? = null,
    val sku: String? = null,
    val image_url: String? = null,
    val standard_price: Double,
    val cost_price: Double? = null,
    val is_fresh: Boolean,
    val expires_at: String? = null,
    val total_quantity: Int,
    val surplus_quantity: Int,
    val reserved_quantity: Int,
    val available_quantity: Int,
    val is_marked_for_surplus: Boolean,
    val surplus_discount_percentage: Double? = null,
    val surplus_price: Double? = null,
    val marked_surplus_at: String? = null,
    val ingredients: String? = null,
    val allergens: String? = null,
    val weight: Double? = null,
    val unit: String,
    val is_available: Boolean,
    val is_active: Boolean,
    val created_at: String,
    val updated_at: String,
    val last_inventory_update: String? = null,
    val store_id: String,
    val category_id: String? = null,
    val category: CategoryResponse? = null
)

data class CategoryResponse(
    val id: String,
    val name: String,
    val parent_category_id: String? = null,
    val description: String? = null,
    val is_active: Boolean,
    val created_at: String,
    val subcategories: List<CategoryResponse>? = null
)

data class SurplusMarkingRequest(
    val surplus_quantity: Int,
    val discount_percentage: Double,
    val surplus_price: Double? = null
)

data class InventoryUpdateRequest(
    val new_total_quantity: Int,
    val change_type: String,
    val reason: String? = null
)

// Surprise Bag related data classes
data class CreateSurpriseBagRequest(
    val name: String,
    val description: String? = null,
    val bag_type: String = "combo", // "combo", "single_category"
    val original_value: Double,
    val discounted_price: Double,
    val discount_percentage: Double,
    val quantity_available: Int,
    val max_per_customer: Int = 1,
    val available_from: String, // ISO format: "2025-10-01T10:00:00"
    val available_until: String, // ISO format: "2025-10-01T16:00:00"
    val pickup_start_time: String, // ISO format: "2025-10-01T17:00:00"
    val pickup_end_time: String, // ISO format: "2025-10-01T21:00:00"
    val is_active: Boolean = true,
    val is_auto_generated: Boolean = false
)

data class SurpriseBagResponse(
    val id: String,
    val name: String,
    val description: String? = null,
    val bag_type: String,
    val original_value: Double,
    val discounted_price: Double,
    val discount_percentage: Double,
    val quantity_available: Int,
    val max_per_customer: Int,
    val available_from: String,
    val available_until: String,
    val pickup_start_time: String,
    val pickup_end_time: String,
    val is_active: Boolean,
    val is_auto_generated: Boolean,
    val created_at: String,
    val updated_at: String,
    val store_id: String
)

// Wrapper for the paginated API response
data class SurpriseBagListResponse(
    val data: List<SurpriseBagResponse>,
    val count: Int
)

data class UpdateSurpriseBagRequest(
    val name: String? = null,
    val description: String? = null,
    val bag_type: String? = null,
    val original_value: Double? = null,
    val discounted_price: Double? = null,
    val discount_percentage: Double? = null,
    val quantity_available: Int? = null,
    val max_per_customer: Int? = null,
    val available_from: String? = null,
    val available_until: String? = null,
    val pickup_start_time: String? = null,
    val pickup_end_time: String? = null,
    val is_active: Boolean? = null,
    val is_auto_generated: Boolean? = null
)

interface WisebiteApiService {
    
    @FormUrlEncoded
    @POST("api/v1/auth/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<LoginResponse>
    
    @POST("api/v1/auth/google")
    suspend fun googleSignIn(
        @Body request: GoogleSignInRequest
    ): Response<LoginResponse>
    
    @GET("api/v1/merchant/food-items/categories/hierarchy")
    suspend fun getCategoryHierarchy(): Response<List<CategoryResponse>>
    
    @GET("api/v1/merchant/food-items/")
    suspend fun getFoodItems(
        @Header("Authorization") authorization: String,
        @Query("category_id") categoryId: String? = null,
        @Query("is_surplus_available") isSurplusAvailable: Boolean? = null,
        @Query("is_active") isActive: Boolean? = null
    ): Response<List<FoodItemResponse>>
    
    @POST("api/v1/merchant/food-items/")
    suspend fun createFoodItem(
        @Header("Authorization") authorization: String,
        @Body request: CreateFoodItemRequest
    ): Response<FoodItemResponse>
    
    @PUT("api/v1/merchant/food-items/{item_id}")
    suspend fun updateFoodItem(
        @Header("Authorization") authorization: String,
        @Path("item_id") itemId: String,
        @Body request: CreateFoodItemRequest
    ): Response<FoodItemResponse>
    
    @POST("api/v1/merchant/food-items/{item_id}/mark-surplus")
    suspend fun markSurplus(
        @Header("Authorization") authorization: String,
        @Path("item_id") itemId: String,
        @Body request: SurplusMarkingRequest
    ): Response<FoodItemResponse>
    
    @POST("api/v1/merchant/food-items/{item_id}/update-inventory")
    suspend fun updateInventory(
        @Header("Authorization") authorization: String,
        @Path("item_id") itemId: String,
        @Body request: InventoryUpdateRequest
    ): Response<FoodItemResponse>
    
    @DELETE("api/v1/merchant/food-items/{item_id}")
    suspend fun deleteFoodItem(
        @Header("Authorization") authorization: String,
        @Path("item_id") itemId: String
    ): Response<Unit>
    
    // Surprise Bag endpoints
    @GET("api/v1/surprise-bag/")
    suspend fun getSurpriseBags(
        @Header("Authorization") authorization: String,
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 100
    ): Response<SurpriseBagListResponse>
    
    @GET("api/v1/surprise-bag/my-store")
    suspend fun getMyStoreSurpriseBags(
        @Header("Authorization") authorization: String,
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 100
    ): Response<SurpriseBagListResponse>
    
    @POST("api/v1/surprise-bag/")
    suspend fun createSurpriseBag(
        @Header("Authorization") authorization: String,
        @Body request: CreateSurpriseBagRequest
    ): Response<SurpriseBagResponse>
    
    @PUT("api/v1/surprise-bag/{bag_id}")
    suspend fun updateSurpriseBag(
        @Header("Authorization") authorization: String,
        @Path("bag_id") bagId: String,
        @Body request: UpdateSurpriseBagRequest
    ): Response<SurpriseBagResponse>
    
    @DELETE("api/v1/surprise-bag/{bag_id}")
    suspend fun deleteSurpriseBag(
        @Header("Authorization") authorization: String,
        @Path("bag_id") bagId: String
    ): Response<Unit>
    
    // Order Management endpoints
    @GET("api/v1/orders/vendor/me")
    suspend fun getMyOrders(
        @Header("Authorization") authorization: String
    ): Response<OrdersResponse>
    
    @GET("api/v1/orders/{order_id}")
    suspend fun getOrderById(
        @Header("Authorization") authorization: String,
        @Path("order_id") orderId: String
    ): Response<Order>
    
    @PATCH("api/v1/orders/{order_id}/status")
    suspend fun updateOrderStatus(
        @Header("Authorization") authorization: String,
        @Path("order_id") orderId: String,
        @Body request: OrderStatusUpdateRequest
    ): Response<Order>
}