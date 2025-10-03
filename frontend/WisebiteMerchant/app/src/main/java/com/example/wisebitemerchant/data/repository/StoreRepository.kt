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
            val authToken = tokenManager.getAuthToken()
            if (authToken == null) {
                return Result.failure(Exception("Vui lòng đăng nhập lại"))
            }
            
            val response = apiService.createStore(authToken, storeRequest)
            
            if (response.isSuccessful) {
                response.body()?.let { createdStore ->
                    Result.success(createdStore)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Phiên đăng nhập đã hết hạn"
                    403 -> "Không có quyền truy cập"
                    400 -> "Dữ liệu không hợp lệ"
                    409 -> "Cửa hàng đã tồn tại"
                    else -> "Không thể tạo cửa hàng"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e("StoreRepository", "Create store error", e)
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