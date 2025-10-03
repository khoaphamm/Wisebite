package com.example.wisebitemerchant.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.wisebitemerchant.data.api.*
import com.example.wisebitemerchant.data.manager.TokenManager
import com.example.wisebitemerchant.data.model.FoodItem
import com.example.wisebitemerchant.data.model.Category
import com.example.wisebitemerchant.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FoodItemRepository private constructor(
    private val context: Context
) {
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
    private val tokenManager: TokenManager = TokenManager.getInstance(context)
    
    private val apiService: WisebiteApiService by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WisebiteApiService::class.java)
    }
    
    companion object {
        @Volatile
        private var INSTANCE: FoodItemRepository? = null
        
        fun getInstance(context: Context): FoodItemRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FoodItemRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    // Updated to use TokenManager instead of SharedPreferences
    private suspend fun getAuthToken(): String? {
        return try {
            val token = tokenManager.getToken().first()
            Log.d("FoodItemRepository", "Retrieved token from TokenManager: ${if (token != null) "${token.take(20)}..." else "null"}")
            token
        } catch (e: Exception) {
            Log.e("FoodItemRepository", "Error getting auth token from TokenManager", e)
            null
        }
    }
    
    private suspend fun getAuthHeader(): String? {
        val token = getAuthToken()
        val header = if (token != null) "Bearer $token" else null
        Log.d("FoodItemRepository", "Auth header: ${if (header != null) "${header.take(30)}..." else "null"}")
        return header
    }
    
    suspend fun clearAuthToken() {
        Log.d("FoodItemRepository", "Clearing auth token via TokenManager")
        tokenManager.clearTokens()
    }
    
    suspend fun isAuthenticated(): Boolean {
        return getAuthToken() != null
    }
    
    suspend fun login(email: String, password: String): ApiResult<LoginResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("FoodItemRepository", "Attempting login for: $email")
                Log.d("FoodItemRepository", "API Base URL: ${Constants.BASE_URL}")
                
                val response = apiService.login(email, password)
                Log.d("FoodItemRepository", "Login response code: ${response.code()}")
                
                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!
                    Log.d("FoodItemRepository", "Login successful, token received")
                    
                    // Save token to SharedPreferences
                    sharedPreferences.edit()
                        .putString(Constants.PREF_ACCESS_TOKEN, loginResponse.access_token)
                        .apply()
                    ApiResult.Success(loginResponse)
                } else {
                    val errorMsg = "Login failed: ${response.code()} - ${response.message()}"
                    Log.e("FoodItemRepository", errorMsg)
                    ApiResult.Error(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Network error during login: ${e.message}"
                Log.e("FoodItemRepository", errorMsg, e)
                ApiResult.Error(errorMsg)
            }
        }
    }
    
    suspend fun googleSignIn(idToken: String): ApiResult<LoginResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("FoodItemRepository", "Attempting Google sign in")
                Log.d("FoodItemRepository", "API Base URL: ${Constants.BASE_URL}")
                
                val request = GoogleSignInRequest(id_token = idToken)
                val response = apiService.googleSignIn(request)
                Log.d("FoodItemRepository", "Google sign in response code: ${response.code()}")
                
                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!
                    Log.d("FoodItemRepository", "Google sign in successful, token received: ${loginResponse.access_token.take(20)}...")
                    
                    // Save token to SharedPreferences
                    sharedPreferences.edit()
                        .putString(Constants.PREF_ACCESS_TOKEN, loginResponse.access_token)
                        .apply()
                    
                    Log.d("FoodItemRepository", "Token saved to SharedPreferences")
                    ApiResult.Success(loginResponse)
                } else {
                    val errorMsg = "Google sign in failed: ${response.code()} - ${response.message()}"
                    Log.e("FoodItemRepository", errorMsg)
                    ApiResult.Error(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Network error during Google sign in: ${e.message}"
                Log.e("FoodItemRepository", errorMsg, e)
                ApiResult.Error(errorMsg)
            }
        }
    }
    
    suspend fun getCategoryHierarchy(): ApiResult<List<Category>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("FoodItemRepository", "Fetching category hierarchy...")
                val response = apiService.getCategoryHierarchy()
                if (response.isSuccessful && response.body() != null) {
                    Log.d("FoodItemRepository", "Raw response: ${response.body()}")
                    val categories = response.body()!!.flatMap { parentCategory ->
                        // If has subcategories, return them; otherwise return the parent
                        if (!parentCategory.subcategories.isNullOrEmpty()) {
                            parentCategory.subcategories!!.map { it.toCategory() }
                        } else {
                            listOf(parentCategory.toCategory())
                        }
                    }
                    Log.d("FoodItemRepository", "Flattened categories: ${categories.size} items")
                    categories.forEach { cat ->
                        Log.d("FoodItemRepository", "Category: ${cat.name} (${cat.id})")
                    }
                    ApiResult.Success(categories)
                } else {
                    Log.e("FoodItemRepository", "API error: ${response.code()} - ${response.message()}")
                    ApiResult.Error("Failed to fetch categories: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("FoodItemRepository", "Network error fetching categories", e)
                ApiResult.Error("Network error: ${e.message}")
            }
        }
    }
    
    suspend fun getFoodItems(
        categoryId: String? = null,
        isSurplusAvailable: Boolean? = null,
        isActive: Boolean? = null
    ): ApiResult<List<FoodItem>> {
        return withContext(Dispatchers.IO) {
            try {
                val authHeader = getAuthHeader()
                if (authHeader == null) {
                    Log.e("FoodItemRepository", "No auth token available")
                    return@withContext ApiResult.Error("Not authenticated")
                }
                
                Log.d("FoodItemRepository", "Fetching food items with auth header: ${authHeader.take(20)}...")
                
                val response = apiService.getFoodItems(
                    authorization = authHeader,
                    categoryId = categoryId,
                    isSurplusAvailable = isSurplusAvailable,
                    isActive = isActive
                )
                
                Log.d("FoodItemRepository", "Food items response code: ${response.code()}")
                
                if (response.isSuccessful && response.body() != null) {
                    val foodItems = response.body()!!.map { it.toFoodItem() }
                    Log.d("FoodItemRepository", "Successfully fetched ${foodItems.size} food items")
                    ApiResult.Success(foodItems)
                } else {
                    val errorMsg = "Failed to fetch food items: ${response.code()} - ${response.message()}"
                    Log.e("FoodItemRepository", errorMsg)
                    ApiResult.Error(errorMsg)
                }
            } catch (e: Exception) {
                ApiResult.Error("Network error: ${e.message}")
            }
        }
    }
    
    suspend fun createFoodItem(request: CreateFoodItemRequest): ApiResult<FoodItem> {
        return withContext(Dispatchers.IO) {
            try {
                val authHeader = getAuthHeader()
                if (authHeader == null) {
                    return@withContext ApiResult.Error("Not authenticated")
                }
                
                val response = apiService.createFoodItem(
                    authorization = authHeader,
                    request = request
                )
                
                if (response.isSuccessful && response.body() != null) {
                    val foodItem = response.body()!!.toFoodItem()
                    ApiResult.Success(foodItem)
                } else {
                    ApiResult.Error("Failed to create food item: ${response.message()}")
                }
            } catch (e: Exception) {
                ApiResult.Error("Network error: ${e.message}")
            }
        }
    }
    
    suspend fun markSurplus(
        itemId: String,
        surplusQuantity: Int,
        discountPercentage: Double,
        surplusPrice: Double? = null
    ): ApiResult<FoodItem> {
        return withContext(Dispatchers.IO) {
            try {
                val authHeader = getAuthHeader()
                if (authHeader == null) {
                    return@withContext ApiResult.Error("Not authenticated")
                }
                
                val request = SurplusMarkingRequest(
                    surplus_quantity = surplusQuantity,
                    discount_percentage = discountPercentage,
                    surplus_price = surplusPrice
                )
                
                val response = apiService.markSurplus(
                    authorization = authHeader,
                    itemId = itemId,
                    request = request
                )
                
                if (response.isSuccessful && response.body() != null) {
                    val foodItem = response.body()!!.toFoodItem()
                    ApiResult.Success(foodItem)
                } else {
                    ApiResult.Error("Failed to mark surplus: ${response.message()}")
                }
            } catch (e: Exception) {
                ApiResult.Error("Network error: ${e.message}")
            }
        }
    }
    
    suspend fun updateInventory(
        itemId: String,
        newTotalQuantity: Int,
        changeType: String,
        reason: String? = null
    ): ApiResult<FoodItem> {
        return withContext(Dispatchers.IO) {
            try {
                val authHeader = getAuthHeader()
                if (authHeader == null) {
                    return@withContext ApiResult.Error("Not authenticated")
                }
                
                val request = InventoryUpdateRequest(
                    new_total_quantity = newTotalQuantity,
                    change_type = changeType,
                    reason = reason
                )
                
                val response = apiService.updateInventory(
                    authorization = authHeader,
                    itemId = itemId,
                    request = request
                )
                
                if (response.isSuccessful && response.body() != null) {
                    val foodItem = response.body()!!.toFoodItem()
                    ApiResult.Success(foodItem)
                } else {
                    ApiResult.Error("Failed to update inventory: ${response.message()}")
                }
            } catch (e: Exception) {
                ApiResult.Error("Network error: ${e.message}")
            }
        }
    }
    
    suspend fun deleteFoodItem(itemId: String): ApiResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val authHeader = getAuthHeader()
                if (authHeader == null) {
                    return@withContext ApiResult.Error("Not authenticated")
                }
                
                val response = apiService.deleteFoodItem(
                    authorization = authHeader,
                    itemId = itemId
                )
                
                if (response.isSuccessful) {
                    ApiResult.Success(Unit)
                } else {
                    ApiResult.Error("Failed to delete food item: ${response.message()}")
                }
            } catch (e: Exception) {
                ApiResult.Error("Network error: ${e.message}")
            }
        }
    }
    
    suspend fun logout() {
        Log.d("FoodItemRepository", "Performing logout - clearing all user data via TokenManager")
        tokenManager.clearTokens()
        // Also clear SharedPreferences for backwards compatibility
        sharedPreferences.edit().clear().apply()
        Log.d("FoodItemRepository", "Logout completed - all authentication data cleared")
    }
    
    suspend fun isLoggedIn(): Boolean {
        return getAuthToken() != null
    }
    
    // Debug methods to check authentication status
    suspend fun getCurrentUser(): ApiResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val authHeader = getAuthHeader()
                Log.d("FoodItemRepository", "Current auth header: ${authHeader?.take(50)}...")
                
                if (authHeader == null) {
                    Log.e("FoodItemRepository", "No auth token available")
                    return@withContext ApiResult.Error("Not authenticated - no token")
                }
                
                // You can add a call to get current user info here if the endpoint exists
                // For now, just check if token exists
                ApiResult.Success(Unit)
            } catch (e: Exception) {
                Log.e("FoodItemRepository", "Error checking auth status: ${e.message}")
                ApiResult.Error("Auth check error: ${e.message}")
            }
        }
    }
    
    suspend fun debugAuthToken() {
        val token = getAuthToken()
        Log.d("FoodItemRepository", "Stored token: ${token?.take(50)}...")
        Log.d("FoodItemRepository", "Token exists: ${token != null}")
        Log.d("FoodItemRepository", "Is logged in: ${isLoggedIn()}")
    }
    
    // Surprise Bag related methods
    suspend fun getSurpriseBags(): ApiResult<List<SurpriseBagResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("FoodItemRepository", "Fetching surprise bags...")
                val authHeader = getAuthHeader()
                if (authHeader == null) {
                    Log.e("FoodItemRepository", "Not authenticated")
                    return@withContext ApiResult.Error("Not authenticated")
                }
                
                val response = apiService.getSurpriseBags(authorization = authHeader)
                if (response.isSuccessful && response.body() != null) {
                    Log.d("FoodItemRepository", "Successfully fetched surprise bags")
                    val surpriseBagList = response.body()!!
                    Log.d("FoodItemRepository", "Total surprise bags: ${surpriseBagList.count}, items: ${surpriseBagList.data.size}")
                    ApiResult.Success(surpriseBagList.data)
                } else {
                    val errorMsg = "Failed to fetch surprise bags: ${response.message()}"
                    Log.e("FoodItemRepository", errorMsg)
                    ApiResult.Error(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Network error: ${e.message}"
                Log.e("FoodItemRepository", errorMsg, e)
                ApiResult.Error(errorMsg)
            }
        }
    }
    
    suspend fun createSurpriseBag(request: CreateSurpriseBagRequest): ApiResult<SurpriseBagResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("FoodItemRepository", "Creating surprise bag: ${request.name}")
                var authHeader = getAuthHeader()
                Log.d("FoodItemRepository", "Auth header for surprise bag creation: ${authHeader?.take(50)}...")
                
                if (authHeader == null) {
                    Log.e("FoodItemRepository", "Not authenticated - no token for surprise bag creation")
                    return@withContext ApiResult.Error("Not authenticated")
                }
                
                // Log the exact request being sent
                Log.d("FoodItemRepository", "Request details:")
                Log.d("FoodItemRepository", "  name: ${request.name}")
                Log.d("FoodItemRepository", "  description: ${request.description}")
                Log.d("FoodItemRepository", "  bag_type: ${request.bag_type}")
                Log.d("FoodItemRepository", "  original_value: ${request.original_value}")
                Log.d("FoodItemRepository", "  discounted_price: ${request.discounted_price}")
                Log.d("FoodItemRepository", "  discount_percentage: ${request.discount_percentage}")
                Log.d("FoodItemRepository", "  quantity_available: ${request.quantity_available}")
                Log.d("FoodItemRepository", "  max_per_customer: ${request.max_per_customer}")
                Log.d("FoodItemRepository", "  available_from: ${request.available_from}")
                Log.d("FoodItemRepository", "  available_until: ${request.available_until}")
                Log.d("FoodItemRepository", "  pickup_start_time: ${request.pickup_start_time}")
                Log.d("FoodItemRepository", "  pickup_end_time: ${request.pickup_end_time}")
                Log.d("FoodItemRepository", "  is_active: ${request.is_active}")
                Log.d("FoodItemRepository", "  is_auto_generated: ${request.is_auto_generated}")
                
                Log.d("FoodItemRepository", "Making API call to create surprise bag...")
                var response = apiService.createSurpriseBag(
                    authorization = authHeader,
                    request = request
                )
                
                Log.d("FoodItemRepository", "Create surprise bag response code: ${response.code()}")
                Log.d("FoodItemRepository", "Response message: ${response.message()}")
                
                // Handle token expiration (401/403 with credential validation error)
                if (response.code() == 403 || response.code() == 401) {
                    try {
                        val errorBody = response.errorBody()?.string()
                        Log.e("FoodItemRepository", "Error response body: $errorBody")
                        
                        if (errorBody?.contains("Could not validate credentials") == true || 
                            errorBody?.contains("Invalid token") == true) {
                            Log.w("FoodItemRepository", "Token appears to be expired/invalid, need to re-authenticate")
                            return@withContext ApiResult.Error("Token expired - please login again")
                        }
                    } catch (e: Exception) {
                        Log.e("FoodItemRepository", "Could not read error body: ${e.message}")
                    }
                }
                
                if (response.isSuccessful && response.body() != null) {
                    Log.d("FoodItemRepository", "Surprise bag created successfully")
                    ApiResult.Success(response.body()!!)
                } else {
                    val errorMsg = "Failed to create surprise bag: ${response.code()} - ${response.message()}"
                    Log.e("FoodItemRepository", errorMsg)
                    
                    // Additional debug info for 403 errors
                    if (response.code() == 403) {
                        Log.e("FoodItemRepository", "403 Forbidden - Check if user has vendor role and store")
                        Log.e("FoodItemRepository", "Auth header being sent: ${authHeader?.take(100)}...")
                    }
                    
                    ApiResult.Error(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Network error: ${e.message}"
                Log.e("FoodItemRepository", errorMsg, e)
                ApiResult.Error(errorMsg)
            }
        }
    }
    
    suspend fun updateSurpriseBag(bagId: String, request: UpdateSurpriseBagRequest): ApiResult<SurpriseBagResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("FoodItemRepository", "Updating surprise bag: $bagId")
                val authHeader = getAuthHeader()
                if (authHeader == null) {
                    Log.e("FoodItemRepository", "Not authenticated")
                    return@withContext ApiResult.Error("Not authenticated")
                }
                
                val response = apiService.updateSurpriseBag(
                    authorization = authHeader,
                    bagId = bagId,
                    request = request
                )
                
                if (response.isSuccessful && response.body() != null) {
                    Log.d("FoodItemRepository", "Surprise bag updated successfully")
                    ApiResult.Success(response.body()!!)
                } else {
                    val errorMsg = "Failed to update surprise bag: ${response.message()}"
                    Log.e("FoodItemRepository", errorMsg)
                    ApiResult.Error(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Network error: ${e.message}"
                Log.e("FoodItemRepository", errorMsg, e)
                ApiResult.Error(errorMsg)
            }
        }
    }
    
    suspend fun deleteSurpriseBag(bagId: String): ApiResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("FoodItemRepository", "Deleting surprise bag: $bagId")
                val authHeader = getAuthHeader()
                if (authHeader == null) {
                    Log.e("FoodItemRepository", "Not authenticated")
                    return@withContext ApiResult.Error("Not authenticated")
                }
                
                val response = apiService.deleteSurpriseBag(
                    authorization = authHeader,
                    bagId = bagId
                )
                
                if (response.isSuccessful) {
                    Log.d("FoodItemRepository", "Surprise bag deleted successfully")
                    ApiResult.Success(Unit)
                } else {
                    val errorMsg = "Failed to delete surprise bag: ${response.message()}"
                    Log.e("FoodItemRepository", errorMsg)
                    ApiResult.Error(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Network error: ${e.message}"
                Log.e("FoodItemRepository", errorMsg, e)
                ApiResult.Error(errorMsg)
            }
        }
    }
}

// Extension functions to convert API responses to domain models
private fun FoodItemResponse.toFoodItem(): FoodItem {
    return FoodItem(
        id = id,
        name = name,
        description = description,
        sku = sku,
        imageUrl = image_url,
        standardPrice = standard_price,
        costPrice = cost_price,
        isFresh = is_fresh,
        totalQuantity = total_quantity,
        surplusQuantity = surplus_quantity,
        reservedQuantity = reserved_quantity,
        availableQuantity = available_quantity,
        isMarkedForSurplus = is_marked_for_surplus,
        surplusDiscountPercentage = surplus_discount_percentage,
        surplusPrice = surplus_price,
        ingredients = ingredients,
        allergens = allergens,
        weight = weight,
        unit = unit,
        isAvailable = is_available,
        isActive = is_active,
        categoryId = category_id,
        categoryName = category?.name
    )
}

private fun CategoryResponse.toCategory(): Category {
    return Category(
        id = id,
        name = name,
        parentCategoryId = parent_category_id,
        description = description,
        isActive = is_active
    )
}
