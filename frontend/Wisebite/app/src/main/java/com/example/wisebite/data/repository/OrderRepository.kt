package com.example.wisebite.data.repository

import android.content.Context
import com.example.wisebite.data.model.CreateOrderRequest
import com.example.wisebite.data.model.Order
import com.example.wisebite.data.model.OrderStatusUpdateRequest
import com.example.wisebite.data.remote.RetrofitClient
import com.example.wisebite.data.repository.TokenManager
import kotlinx.coroutines.flow.first

class OrderRepository private constructor(
    private val tokenManager: TokenManager
) {
    
    companion object {
        @Volatile
        private var INSTANCE: OrderRepository? = null
        
        fun getInstance(context: Context): OrderRepository {
            return INSTANCE ?: synchronized(this) {
                val tokenManager = TokenManager.getInstance(context)
                INSTANCE ?: OrderRepository(tokenManager).also { INSTANCE = it }
            }
        }
    }
    
    private val apiService = RetrofitClient.apiService
    
    private suspend fun getAuthToken(): String? {
        // Check if token is expired before using it
        if (tokenManager.isTokenExpired()) {
            android.util.Log.w("OrderRepository", "Token is expired")
            return null
        }
        
        val token = tokenManager.getToken().first()
        return if (token != null) "Bearer $token" else null
    }
    
    /**
     * Execute an API call with automatic token validation
     */
    private suspend fun <T> executeWithAuth(
        operation: String,
        apiCall: suspend (authToken: String) -> retrofit2.Response<T>
    ): Result<T> {
        return try {
            val authToken = getAuthToken()
            if (authToken == null) {
                android.util.Log.e("OrderRepository", "$operation - no valid auth token")
                return Result.failure(Exception("Authentication required - please login again"))
            }
            
            val response = apiCall(authToken)
            
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    Result.success(body)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "Invalid order data or insufficient stock"
                    401, 403 -> "Authentication expired - please login again"
                    404 -> "Items not found"
                    422 -> "Invalid data format"
                    else -> "Request failed (${response.code()})"
                }
                android.util.Log.e("OrderRepository", "$operation failed: ${response.code()}")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            android.util.Log.e("OrderRepository", "Error in $operation", e)
            Result.failure(Exception("Network error. Please check your connection."))
        }
    }
    
    suspend fun createOrder(orderRequest: CreateOrderRequest): Result<Order> {
        return executeWithAuth("createOrder") { authToken ->
            apiService.createOrder(authToken, orderRequest)
        }.onSuccess { order ->
            android.util.Log.d("OrderRepository", "Order created successfully: ${order.id}")
        }
    }
    
    suspend fun getUserOrders(): Result<List<Order>> {
        return executeWithAuth("getUserOrders") { authToken ->
            apiService.getUserOrders(authToken)
        }.map { ordersResponse ->
            android.util.Log.d("OrderRepository", "Fetched ${ordersResponse.data.size} orders")
            ordersResponse.data
        }
    }
    
    suspend fun getOrder(orderId: String): Result<Order> {
        return executeWithAuth("getOrder") { authToken ->
            apiService.getOrder(authToken, orderId)
        }.onSuccess { order ->
            android.util.Log.d("OrderRepository", "Fetched order: ${order.id}")
        }
    }
    
    suspend fun cancelOrder(orderId: String): Result<Order> {
        return executeWithAuth("cancelOrder") { authToken ->
            apiService.cancelOrder(authToken, orderId)
        }.onSuccess { order ->
            android.util.Log.d("OrderRepository", "Order cancelled: ${order.id}")
        }
    }
}