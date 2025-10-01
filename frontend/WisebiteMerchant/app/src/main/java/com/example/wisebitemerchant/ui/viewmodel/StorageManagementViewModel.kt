package com.example.wisebitemerchant.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.wisebitemerchant.data.model.Category
import com.example.wisebitemerchant.data.model.FoodItem
import com.example.wisebitemerchant.data.repository.ApiResult
import com.example.wisebitemerchant.data.repository.FoodItemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StorageManagementUiState(
    val isLoading: Boolean = false,
    val foodItems: List<FoodItem> = emptyList(),
    val categories: List<Category> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class StorageManagementViewModel(
    private val repository: FoodItemRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(StorageManagementUiState())
    val uiState: StateFlow<StorageManagementUiState> = _uiState.asStateFlow()
    
    init {
        // For testing: automatically login with test credentials
        testLogin()
    }
    
    private fun testLogin() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            when (val result = repository.login("hekinglois@gmail.com", "123123")) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Login successful"
                    )
                    loadData()
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Login failed: ${result.message}"
                    )
                }
                else -> {}
            }
        }
    }
    
    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            // Load categories and food items
            loadCategories()
            loadFoodItems()
        }
    }
    
    private suspend fun loadCategories() {
        when (val result = repository.getCategoryHierarchy()) {
            is ApiResult.Success -> {
                _uiState.value = _uiState.value.copy(categories = result.data)
            }
            is ApiResult.Error -> {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to load categories: ${result.message}"
                )
            }
            is ApiResult.Loading -> {
                // Already handled in loadData
            }
        }
    }
    
    private suspend fun loadFoodItems() {
        when (val result = repository.getFoodItems()) {
            is ApiResult.Success -> {
                _uiState.value = _uiState.value.copy(
                    foodItems = result.data,
                    isLoading = false
                )
            }
            is ApiResult.Error -> {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to load food items: ${result.message}",
                    isLoading = false
                )
            }
            is ApiResult.Loading -> {
                // Already handled in loadData
            }
        }
    }
    
    fun loadFoodItemsByFilter(
        categoryId: String? = null,
        isSurplusAvailable: Boolean? = null,
        isActive: Boolean? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            when (val result = repository.getFoodItems(categoryId, isSurplusAvailable, isActive)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        foodItems = result.data,
                        isLoading = false
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = result.message,
                        isLoading = false
                    )
                }
                is ApiResult.Loading -> {
                    // Already handled above
                }
            }
        }
    }
    
    fun markSurplus(
        itemId: String,
        surplusQuantity: Int,
        discountPercentage: Double,
        surplusPrice: Double? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            when (val result = repository.markSurplus(itemId, surplusQuantity, discountPercentage, surplusPrice)) {
                is ApiResult.Success -> {
                    // Update the food item in the list
                    val updatedItems = _uiState.value.foodItems.map { item ->
                        if (item.id == itemId) result.data else item
                    }
                    _uiState.value = _uiState.value.copy(
                        foodItems = updatedItems,
                        isLoading = false,
                        successMessage = "Surplus marked successfully"
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = result.message,
                        isLoading = false
                    )
                }
                is ApiResult.Loading -> {
                    // Already handled above
                }
            }
        }
    }
    
    fun updateInventory(
        itemId: String,
        newTotalQuantity: Int,
        changeType: String,
        reason: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            when (val result = repository.updateInventory(itemId, newTotalQuantity, changeType, reason)) {
                is ApiResult.Success -> {
                    // Update the food item in the list
                    val updatedItems = _uiState.value.foodItems.map { item ->
                        if (item.id == itemId) result.data else item
                    }
                    _uiState.value = _uiState.value.copy(
                        foodItems = updatedItems,
                        isLoading = false,
                        successMessage = "Inventory updated successfully"
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = result.message,
                        isLoading = false
                    )
                }
                is ApiResult.Loading -> {
                    // Already handled above
                }
            }
        }
    }
    
    fun deleteFoodItem(itemId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            when (val result = repository.deleteFoodItem(itemId)) {
                is ApiResult.Success -> {
                    // Remove the food item from the list
                    val updatedItems = _uiState.value.foodItems.filter { it.id != itemId }
                    _uiState.value = _uiState.value.copy(
                        foodItems = updatedItems,
                        isLoading = false,
                        successMessage = "Food item deleted successfully"
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = result.message,
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
            successMessage = null
        )
    }
    
    fun refreshData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            loadFoodItems()
        }
    }
    
    fun getFreshItems(): List<FoodItem> {
        return _uiState.value.foodItems.filter { it.isFresh }
    }
    
    fun getPackagedItems(): List<FoodItem> {
        return _uiState.value.foodItems.filter { !it.isFresh }
    }
    
    fun getSurplusItems(): List<FoodItem> {
        return _uiState.value.foodItems.filter { it.isMarkedForSurplus }
    }
}

class StorageManagementViewModelFactory(
    private val repository: FoodItemRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StorageManagementViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StorageManagementViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}