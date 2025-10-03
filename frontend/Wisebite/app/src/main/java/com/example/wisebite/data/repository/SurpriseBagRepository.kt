package com.example.wisebite.data.repository

import android.content.Context
import android.util.Log
import com.example.wisebite.data.repository.TokenManager
import com.example.wisebite.data.model.Store
import com.example.wisebite.data.model.SurpriseBag
import com.example.wisebite.data.repository.ApiResult
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
            if (!token.isNullOrEmpty()) "Bearer $token" else null
        } catch (e: Exception) {
            Log.e("SurpriseBagRepository", "Error getting auth token", e)
            null
        }
    }
    
    suspend fun getAvailableStores(): ApiResult<List<Store>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("SurpriseBagRepository", "Fetching all stores")
                val response = apiService.getAvailableStores()
                
                Log.d("SurpriseBagRepository", "Response code: ${response.code()}")
                Log.d("SurpriseBagRepository", "Response body: ${response.body()}")
                
                if (response.isSuccessful && response.body() != null) {
                    val stores = response.body()!!
                    Log.d("SurpriseBagRepository", "Successfully fetched ${stores.size} stores")
                    ApiResult.Success(stores)
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
        maxPrice: Double? = null
    ): ApiResult<List<SurpriseBag>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("SurpriseBagRepository", "Fetching surprise bags - category: $category, maxPrice: $maxPrice")
                val response = apiService.getAllSurpriseBags(category, null, null, null, maxPrice)
                
                Log.d("SurpriseBagRepository", "Response code: ${response.code()}")
                Log.d("SurpriseBagRepository", "Response body: ${response.body()}")
                
                if (response.isSuccessful && response.body() != null) {
                    val bags = response.body()!!
                    Log.d("SurpriseBagRepository", "Successfully fetched ${bags.size} surprise bags")
                    ApiResult.Success(bags)
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