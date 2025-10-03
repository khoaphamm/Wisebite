package com.example.wisebitemerchant.data.repository

import android.content.Context
import android.util.Log
import com.example.wisebitemerchant.data.api.WisebiteApiService
import com.example.wisebitemerchant.data.model.Order
import com.example.wisebitemerchant.data.model.OrderStatusUpdateRequest
import com.example.wisebitemerchant.data.model.OrdersResponse
import com.example.wisebitemerchant.data.manager.TokenManager
import com.example.wisebitemerchant.data.remote.RetrofitClient
import kotlinx.coroutines.flow.first

class OrderRepository(
    private val context: Context,
    private val apiService: WisebiteApiService = RetrofitClient.apiService
) {
    private val tokenManager = TokenManager.getInstance(context)
    
    companion object {
        @Volatile
        private var INSTANCE: OrderRepository? = null
        
        fun getInstance(context: Context): OrderRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: OrderRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private suspend fun getAuthHeader(): String? {
        return try {
            val token = tokenManager.getToken().first()
            if (!token.isNullOrEmpty()) "Bearer $token" else null
        } catch (e: Exception) {
            Log.e("OrderRepository", "Error getting auth token", e)
            null
        }
    }
    
    suspend fun getMyOrders(): ApiResult<List<Order>> {
        return try {
            val authHeader = getAuthHeader()
                ?: return ApiResult.Error("Authentication required")
            
            Log.d("OrderRepository", "Fetching merchant orders...")
            val response = apiService.getMyOrders(authHeader)
            
            Log.d("OrderRepository", "Response code: ${response.code()}")
            Log.d("OrderRepository", "Response body: ${response.body()}")
            
            if (response.isSuccessful && response.body() != null) {
                val ordersResponse = response.body()!!
                Log.d("OrderRepository", "Successfully fetched ${ordersResponse.data.size} orders")
                
                // Log detailed order information
                ordersResponse.data.forEachIndexed { index, order ->
                    Log.d("OrderRepository", "Order ${index + 1}:")
                    Log.d("OrderRepository", "  ID: ${order.id}")
                    Log.d("OrderRepository", "  Customer ID: ${order.customerId}")
                    Log.d("OrderRepository", "  Customer object: ${order.customer}")
                    Log.d("OrderRepository", "  Customer name: ${order.customerName}")
                    Log.d("OrderRepository", "  Items: ${order.items}")
                    Log.d("OrderRepository", "  Items display: ${order.itemsDisplay}")
                    Log.d("OrderRepository", "  Status: ${order.status}")
                    
                    if (order.customer != null) {
                        Log.d("OrderRepository", "    Customer full name: ${order.customer.fullName}")
                        Log.d("OrderRepository", "    Customer email: ${order.customer.email}")
                    } else {
                        Log.w("OrderRepository", "    Customer object is NULL!")
                    }
                    
                    if (order.items != null && order.items.isNotEmpty()) {
                        Log.d("OrderRepository", "    Items count: ${order.items.size}")
                        order.items.forEachIndexed { itemIndex, item ->
                            Log.d("OrderRepository", "      Item ${itemIndex + 1}:")
                            Log.d("OrderRepository", "        Quantity: ${item.quantity}")
                            Log.d("OrderRepository", "        Price: ${item.pricePerItem}")
                            Log.d("OrderRepository", "        Surprise bag: ${item.surpriseBag}")
                            Log.d("OrderRepository", "        Food item: ${item.foodItem}")
                            
                            if (item.surpriseBag != null) {
                                Log.d("OrderRepository", "          Bag name: ${item.surpriseBag.name}")
                                Log.d("OrderRepository", "          Bag description: ${item.surpriseBag.description}")
                            } else {
                                Log.d("OrderRepository", "          Surprise bag is NULL")
                            }
                            
                            if (item.foodItem != null) {
                                Log.d("OrderRepository", "          Food name: ${item.foodItem.name}")
                                Log.d("OrderRepository", "          Food description: ${item.foodItem.description}")
                            } else {
                                Log.d("OrderRepository", "          Food item is NULL")
                            }
                        }
                    } else {
                        Log.w("OrderRepository", "    Items is NULL or empty!")
                    }
                }
                
                ApiResult.Success(ordersResponse.data)
            } else {
                val errorMsg = "Failed to fetch orders: ${response.code()} - ${response.message()}"
                Log.e("OrderRepository", errorMsg)
                ApiResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Network error: ${e.message}"
            Log.e("OrderRepository", errorMsg, e)
            ApiResult.Error(errorMsg)
        }
    }
    
    suspend fun getOrderById(orderId: String): ApiResult<Order> {
        return try {
            val authHeader = getAuthHeader()
                ?: return ApiResult.Error("Authentication required")
            
            Log.d("OrderRepository", "Fetching order details for ID: $orderId")
            val response = apiService.getOrderById(authHeader, orderId)
            
            if (response.isSuccessful && response.body() != null) {
                val order = response.body()!!
                Log.d("OrderRepository", "Successfully fetched order details")
                ApiResult.Success(order)
            } else {
                val errorMsg = "Failed to fetch order details: ${response.code()} - ${response.message()}"
                Log.e("OrderRepository", errorMsg)
                ApiResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Network error: ${e.message}"
            Log.e("OrderRepository", errorMsg, e)
            ApiResult.Error(errorMsg)
        }
    }
    
    suspend fun updateOrderStatus(orderId: String, newStatus: String): ApiResult<Order> {
        return try {
            val authHeader = getAuthHeader()
                ?: return ApiResult.Error("Authentication required")
            
            Log.d("OrderRepository", "Updating order $orderId status to: $newStatus")
            val request = OrderStatusUpdateRequest(status = newStatus)
            val response = apiService.updateOrderStatus(authHeader, orderId, request)
            
            if (response.isSuccessful && response.body() != null) {
                val order = response.body()!!
                Log.d("OrderRepository", "Successfully updated order status")
                ApiResult.Success(order)
            } else {
                val errorMsg = "Failed to update order status: ${response.code()} - ${response.message()}"
                Log.e("OrderRepository", errorMsg)
                ApiResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Network error: ${e.message}"
            Log.e("OrderRepository", errorMsg, e)
            ApiResult.Error(errorMsg)
        }
    }
    
    suspend fun acceptOrder(orderId: String): ApiResult<Order> {
        return updateOrderStatus(orderId, "confirmed")
    }
    
    suspend fun rejectOrder(orderId: String): ApiResult<Order> {
        return updateOrderStatus(orderId, "cancelled")
    }
    
    suspend fun markOrderReady(orderId: String): ApiResult<Order> {
        return updateOrderStatus(orderId, "awaiting_pickup")
    }
    
    suspend fun markOrderCompleted(orderId: String): ApiResult<Order> {
        return updateOrderStatus(orderId, "completed")
    }
}