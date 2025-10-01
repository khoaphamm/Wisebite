package com.example.wisebite.data.repository

import android.content.Context
import android.util.Log
import com.example.wisebite.data.model.Store
import com.example.wisebite.data.model.SurpriseBag
import com.example.wisebite.data.remote.RetrofitClient
import com.example.wisebite.data.remote.WisebiteApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class SurpriseBagRepository(
    private val context: Context,
    private val apiService: WisebiteApiService = RetrofitClient.apiService
) {
    private val tokenManager = TokenManager.getInstance(context)
    
    companion object {
        @Volatile
        private var INSTANCE: SurpriseBagRepository? = null
        
        fun getInstance(context: Context): SurpriseBagRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SurpriseBagRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private suspend fun getAuthHeader(): String? {
        return try {
            val token = tokenManager.getToken().first()
            if (token.isNotEmpty()) "Bearer $token" else null
        } catch (e: Exception) {
            Log.e("SurpriseBagRepository", "Error getting auth token", e)
            null
        }
    }
    
    suspend fun getAvailableStores(city: String? = null): ApiResult<List<Store>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = if (city != null) {
                    apiService.getAvailableStores(city)
                } else {
                    apiService.getAvailableStores()
                }
                
                if (response.isSuccessful && response.body() != null) {
                    ApiResult.Success(response.body()!!)
                } else {
                    val errorMsg = "Failed to fetch stores: ${response.code()} - ${response.message()}"
                    Log.e("SurpriseBagRepository", errorMsg)
                    ApiResult.Error(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Network error: ${e.message}"
                Log.e("SurpriseBagRepository", errorMsg, e)
                ApiResult.Error(errorMsg)
            }
        }
    }
    
    suspend fun getStoreSurpriseBags(storeId: String, category: String? = null): ApiResult<List<SurpriseBag>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = if (category != null) {
                    apiService.getStoreSurpriseBags(storeId, category)
                } else {
                    apiService.getStoreSurpriseBags(storeId)
                }
                
                if (response.isSuccessful && response.body() != null) {
                    ApiResult.Success(response.body()!!)
                } else {
                    val errorMsg = "Failed to fetch surprise bags: ${response.code()} - ${response.message()}"
                    Log.e("SurpriseBagRepository", errorMsg)
                    ApiResult.Error(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Network error: ${e.message}"
                Log.e("SurpriseBagRepository", errorMsg, e)
                ApiResult.Error(errorMsg)
            }
        }
    }
    
    suspend fun getAllSurpriseBags(
        category: String? = null,
        city: String? = null,
        maxPrice: Double? = null
    ): ApiResult<List<SurpriseBag>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAllSurpriseBags(category, city, null, null, maxPrice)
                
                if (response.isSuccessful && response.body() != null) {
                    ApiResult.Success(response.body()!!)
                } else {
                    val errorMsg = "Failed to fetch surprise bags: ${response.code()} - ${response.message()}"
                    Log.e("SurpriseBagRepository", errorMsg)
                    ApiResult.Error(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Network error: ${e.message}"
                Log.e("SurpriseBagRepository", errorMsg, e)
                ApiResult.Error(errorMsg)
            }
        }
    }
    
    suspend fun getSurpriseBagDetails(bagId: String): ApiResult<SurpriseBag> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getSurpriseBagDetails(bagId)
                
                if (response.isSuccessful && response.body() != null) {
                    ApiResult.Success(response.body()!!)
                } else {
                    val errorMsg = "Failed to fetch surprise bag details: ${response.code()} - ${response.message()}"
                    Log.e("SurpriseBagRepository", errorMsg)
                    ApiResult.Error(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Network error: ${e.message}"
                Log.e("SurpriseBagRepository", errorMsg, e)
                ApiResult.Error(errorMsg)
            }
        }
    }
    
    suspend fun getAvailableCategories(): ApiResult<List<String>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAvailableCategories()
                
                if (response.isSuccessful && response.body() != null) {
                    ApiResult.Success(response.body()!!)
                } else {
                    val errorMsg = "Failed to fetch categories: ${response.code()} - ${response.message()}"
                    Log.e("SurpriseBagRepository", errorMsg)
                    ApiResult.Error(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Network error: ${e.message}"
                Log.e("SurpriseBagRepository", errorMsg, e)
                ApiResult.Error(errorMsg)
            }
        }
    }
}

sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}