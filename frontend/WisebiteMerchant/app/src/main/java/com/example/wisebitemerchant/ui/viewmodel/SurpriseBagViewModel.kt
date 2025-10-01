package com.example.wisebitemerchant.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.wisebitemerchant.data.api.CreateSurpriseBagRequest
import com.example.wisebitemerchant.data.api.SurpriseBagResponse
import com.example.wisebitemerchant.data.api.UpdateSurpriseBagRequest
import com.example.wisebitemerchant.data.model.SurpriseBag
import com.example.wisebitemerchant.data.model.SurpriseBagCategories
import com.example.wisebitemerchant.data.model.SurpriseBagTimeWindows
import com.example.wisebitemerchant.data.repository.ApiResult
import com.example.wisebitemerchant.data.repository.FoodItemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class SurpriseBagUiState(
    val isLoading: Boolean = false,
    val activeBags: List<SurpriseBag> = emptyList(),
    val soldBags: List<SurpriseBag> = emptyList(),
    val draftBags: List<SurpriseBag> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isCreatingBag: Boolean = false,
    val isTokenExpired: Boolean = false
)

class SurpriseBagViewModel(
    private val repository: FoodItemRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SurpriseBagUiState())
    val uiState: StateFlow<SurpriseBagUiState> = _uiState.asStateFlow()
    
    init {
        loadSurpriseBags()
    }
    
    fun loadSurpriseBags() {
        viewModelScope.launch {
            Log.d("SurpriseBagViewModel", "Loading surprise bags...")
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, isTokenExpired = false)
            
            when (val result = repository.getSurpriseBags()) {
                is ApiResult.Success -> {
                    Log.d("SurpriseBagViewModel", "Surprise bags loaded: ${result.data.size}")
                    val bags = result.data.map { it.toSurpriseBag() }
                    
                    _uiState.value = _uiState.value.copy(
                        activeBags = bags.filter { it.isActive && it.quantityAvailable > 0 },
                        soldBags = bags.filter { it.isActive && it.quantityAvailable == 0 },
                        draftBags = bags.filter { !it.isActive },
                        isLoading = false,
                        isTokenExpired = false
                    )
                }
                is ApiResult.Error -> {
                    Log.e("SurpriseBagViewModel", "Failed to load surprise bags: ${result.message}")
                    
                    if (result.message.contains("Token expired") || 
                        result.message.contains("please login again")) {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.",
                            isLoading = false,
                            isTokenExpired = true
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Failed to load surprise bags: ${result.message}",
                            isLoading = false,
                            isTokenExpired = false
                        )
                    }
                }
                is ApiResult.Loading -> {
                    // Already handled above
                }
            }
        }
    }
    
    fun createSurpriseBag(
        name: String,
        description: String?,
        category: String,
        originalValue: Double,
        discountedPrice: Double,
        quantity: Int,
        pickupStartTime: LocalDateTime,
        pickupEndTime: LocalDateTime
    ) {
        viewModelScope.launch {
            Log.d("SurpriseBagViewModel", "Creating surprise bag: $name")
            _uiState.value = _uiState.value.copy(isCreatingBag = true, errorMessage = null)
            
            // Calculate discount percentage (as decimal for API, percentage for validation)
            val discountPercentageValue = ((originalValue - discountedPrice) / originalValue)
            val discountPercentageDisplay = discountPercentageValue * 100
            
            // Validate discount percentage
            if (discountPercentageDisplay < SurpriseBagTimeWindows.MIN_DISCOUNT_PERCENTAGE) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Giảm giá tối thiểu phải là ${SurpriseBagTimeWindows.MIN_DISCOUNT_PERCENTAGE}%",
                    isCreatingBag = false
                )
                return@launch
            }
            
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
            
            val request = CreateSurpriseBagRequest(
                name = name,
                description = description,
                bag_type = category,
                original_value = originalValue,
                discounted_price = discountedPrice,
                discount_percentage = discountPercentageValue, // Use decimal value (0.0 to 1.0)
                quantity_available = quantity,
                max_per_customer = 1,
                available_from = LocalDateTime.now().format(formatter), // Available from now
                available_until = pickupStartTime.minusHours(2).format(formatter), // Available until 2h before pickup
                pickup_start_time = pickupStartTime.format(formatter),
                pickup_end_time = pickupEndTime.format(formatter),
                is_active = true,
                is_auto_generated = false
            )
            
            when (val result = repository.createSurpriseBag(request)) {
                is ApiResult.Success -> {
                    Log.d("SurpriseBagViewModel", "Surprise bag created successfully")
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Surprise bag đã được tạo thành công!",
                        isCreatingBag = false,
                        isTokenExpired = false
                    )
                    loadSurpriseBags() // Refresh the list
                }
                is ApiResult.Error -> {
                    Log.e("SurpriseBagViewModel", "Failed to create surprise bag: ${result.message}")
                    
                    if (result.message.contains("Token expired") || 
                        result.message.contains("please login again")) {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.",
                            isCreatingBag = false,
                            isTokenExpired = true
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Không thể tạo surprise bag: ${result.message}",
                            isCreatingBag = false,
                            isTokenExpired = false
                        )
                    }
                }
                is ApiResult.Loading -> {
                    // Already handled above
                }
            }
        }
    }
    
    fun updateSurpriseBag(
        bagId: String,
        name: String? = null,
        description: String? = null,
        originalValue: Double? = null,
        discountedPrice: Double? = null,
        quantity: Int? = null,
        pickupStartTime: LocalDateTime? = null,
        pickupEndTime: LocalDateTime? = null,
        isActive: Boolean? = null
    ) {
        viewModelScope.launch {
            Log.d("SurpriseBagViewModel", "Updating surprise bag: $bagId")
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
            val request = UpdateSurpriseBagRequest(
                name = name,
                description = description,
                original_value = originalValue,
                discounted_price = discountedPrice,
                quantity_available = quantity,
                pickup_start_time = pickupStartTime?.format(formatter),
                pickup_end_time = pickupEndTime?.format(formatter),
                is_active = isActive
            )
            
            when (val result = repository.updateSurpriseBag(bagId, request)) {
                is ApiResult.Success -> {
                    Log.d("SurpriseBagViewModel", "Surprise bag updated successfully")
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Surprise bag đã được cập nhật!",
                        isLoading = false
                    )
                    loadSurpriseBags() // Refresh the list
                }
                is ApiResult.Error -> {
                    Log.e("SurpriseBagViewModel", "Failed to update surprise bag: ${result.message}")
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Không thể cập nhật surprise bag: ${result.message}",
                        isLoading = false
                    )
                }
                is ApiResult.Loading -> {
                    // Already handled above
                }
            }
        }
    }
    
    fun deactivateSurpriseBag(bagId: String) {
        updateSurpriseBag(bagId, isActive = false)
    }
    
    fun activateSurpriseBag(bagId: String) {
        updateSurpriseBag(bagId, isActive = true)
    }
    
    fun deleteSurpriseBag(bagId: String) {
        viewModelScope.launch {
            Log.d("SurpriseBagViewModel", "Deleting surprise bag: $bagId")
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            when (val result = repository.deleteSurpriseBag(bagId)) {
                is ApiResult.Success -> {
                    Log.d("SurpriseBagViewModel", "Surprise bag deleted successfully")
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Surprise bag đã được xóa!",
                        isLoading = false
                    )
                    loadSurpriseBags() // Refresh the list
                }
                is ApiResult.Error -> {
                    Log.e("SurpriseBagViewModel", "Failed to delete surprise bag: ${result.message}")
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Không thể xóa surprise bag: ${result.message}",
                        isLoading = false
                    )
                }
                is ApiResult.Loading -> {
                    // Already handled above
                }
            }
        }
    }
    
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null,
            isTokenExpired = false
        )
    }
    
    fun clearTokenExpiredState() {
        _uiState.value = _uiState.value.copy(isTokenExpired = false)
    }
    
    fun clearStoredToken() {
        viewModelScope.launch {
            repository.clearAuthToken()
        }
    }
    
    suspend fun isAuthenticated(): Boolean {
        return repository.isAuthenticated()
    }
    
    suspend fun reAuthenticate(idToken: String): Boolean {
        return when (val result = repository.googleSignIn(idToken)) {
            is ApiResult.Success -> {
                clearTokenExpiredState()
                true
            }
            is ApiResult.Error -> {
                Log.e("SurpriseBagViewModel", "Re-authentication failed: ${result.message}")
                false
            }
            is ApiResult.Loading -> false
        }
    }
    
    fun refreshData() {
        loadSurpriseBags()
    }
    
    // Helper functions for price calculation
    fun calculateMinimumPrice(originalValue: Double): Double {
        val maxAllowedPrice = originalValue * (1.0 - SurpriseBagTimeWindows.MIN_DISCOUNT_PERCENTAGE / 100.0)
        return maxAllowedPrice
    }
    
    fun calculateDiscountPercentage(originalValue: Double, discountedPrice: Double): Double {
        return ((originalValue - discountedPrice) / originalValue) * 100
    }
    
    fun validateDiscountPrice(originalValue: Double, discountedPrice: Double): Boolean {
        val discountPercentage = calculateDiscountPercentage(originalValue, discountedPrice)
        return discountPercentage >= SurpriseBagTimeWindows.MIN_DISCOUNT_PERCENTAGE
    }
}

class SurpriseBagViewModelFactory(
    private val repository: FoodItemRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SurpriseBagViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SurpriseBagViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// Extension function to convert API response to domain model
private fun SurpriseBagResponse.toSurpriseBag(): SurpriseBag {
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    return SurpriseBag(
        id = id,
        name = name,
        description = description,
        bagType = bag_type,
        category = bag_type, // Use bag_type as category for now
        originalValue = original_value,
        discountedPrice = discounted_price,
        quantityAvailable = quantity_available,
        pickupStartTime = LocalDateTime.parse(pickup_start_time, formatter),
        pickupEndTime = LocalDateTime.parse(pickup_end_time, formatter),
        isActive = is_active,
        createdAt = LocalDateTime.parse(created_at, formatter),
        updatedAt = LocalDateTime.parse(updated_at, formatter),
        storeId = store_id
    )
}