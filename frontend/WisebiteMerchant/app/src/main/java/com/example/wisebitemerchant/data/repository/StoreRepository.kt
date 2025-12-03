package com.example.wisebitemerchant.data.repository

import android.util.Log
import com.example.wisebitemerchant.data.manager.TokenManager
import com.example.wisebitemerchant.data.model.Store
import com.example.wisebitemerchant.data.model.StoreCreateRequest
import com.example.wisebitemerchant.data.remote.MerchantApiService

class StoreRepository private constructor(
    private val apiService: MerchantApiService,
    private val tokenManager: TokenManager
) {
    
    companion object {
        @Volatile
        private var INSTANCE: StoreRepository? = null
        
        fun getInstance(apiService: MerchantApiService, tokenManager: TokenManager): StoreRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = StoreRepository(apiService, tokenManager)
                INSTANCE = instance
                instance
            }
        }
    }
    
    suspend fun getMyStore(): Result<Store> {
        return try {
            val authToken = tokenManager.getAuthToken()
            if (authToken == null) {
                return Result.failure(Exception("Vui lòng đăng nhập lại"))
            }
            
            val response = apiService.getMyStore(authToken)
            
            if (response.isSuccessful) {
                response.body()?.let { store ->
                    Result.success(store)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Phiên đăng nhập đã hết hạn"
                    403 -> "Không có quyền truy cập"
                    404 -> "Cửa hàng không tồn tại"
                    else -> "Không thể lấy thông tin cửa hàng"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e("StoreRepository", "Get store error", e)
            Result.failure(Exception("Lỗi mạng. Vui lòng kiểm tra kết nối."))
        }
    }
    
    suspend fun createStore(storeRequest: StoreCreateRequest): Result<Store> {
        return try {
            Log.d("StoreRepository", "Creating store...")
            Log.d("StoreRepository", "Store request: $storeRequest")
            Log.d("StoreRepository", "  name: ${storeRequest.name}")
            Log.d("StoreRepository", "  description: ${storeRequest.description}")
            Log.d("StoreRepository", "  address: ${storeRequest.address}")
            Log.d("StoreRepository", "  logoUrl: ${storeRequest.logoUrl}")
            
            val authToken = tokenManager.getAuthToken()
            if (authToken == null) {
                Log.e("StoreRepository", "No auth token available")
                return Result.failure(Exception("Vui lòng đăng nhập lại"))
            }
            
            Log.d("StoreRepository", "Auth token available: ${authToken.take(20)}...")
            Log.d("StoreRepository", "Making API call to create store...")
            
            val response = apiService.createStore(authToken, storeRequest)
            
            Log.d("StoreRepository", "Create store response code: ${response.code()}")
            Log.d("StoreRepository", "Response message: ${response.message()}")
            
            if (response.isSuccessful) {
                response.body()?.let { createdStore ->
                    Log.d("StoreRepository", "Store created successfully: ${createdStore.id}")
                    Result.success(createdStore)
                } ?: run {
                    Log.e("StoreRepository", "Empty response body")
                    Result.failure(Exception("Empty response"))
                }
            } else {
                // Read error body for detailed error message
                val errorBody = try {
                    response.errorBody()?.string()
                } catch (e: Exception) {
                    "Could not read error body: ${e.message}"
                }
                
                Log.e("StoreRepository", "Failed to create store:")
                Log.e("StoreRepository", "  Response code: ${response.code()}")
                Log.e("StoreRepository", "  Response message: ${response.message()}")
                Log.e("StoreRepository", "  Error body: $errorBody")
                
                val errorMessage = when (response.code()) {
                    401 -> "Phiên đăng nhập đã hết hạn"
                    403 -> "Không có quyền truy cập"
                    400 -> "Dữ liệu không hợp lệ: $errorBody"
                    409 -> "Cửa hàng đã tồn tại"
                    else -> "Không thể tạo cửa hàng: $errorBody"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e("StoreRepository", "Create store network error", e)
            Result.failure(Exception("Lỗi mạng. Vui lòng kiểm tra kết nối."))
        }
    }
    
    suspend fun updateStore(store: Store): Result<Store> {
        return try {
            val authToken = tokenManager.getAuthToken()
            if (authToken == null) {
                return Result.failure(Exception("Vui lòng đăng nhập lại"))
            }
            
            val response = apiService.updateStore(authToken, store)
            
            if (response.isSuccessful) {
                response.body()?.let { updatedStore ->
                    Result.success(updatedStore)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Phiên đăng nhập đã hết hạn"
                    403 -> "Không có quyền truy cập"
                    400 -> "Dữ liệu không hợp lệ"
                    404 -> "Cửa hàng không tồn tại"
                    else -> "Không thể cập nhật thông tin cửa hàng"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e("StoreRepository", "Update store error", e)
            Result.failure(Exception("Lỗi mạng. Vui lòng kiểm tra kết nối."))
        }
    }
}