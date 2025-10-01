package com.example.wisebitemerchant.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.wisebitemerchant.data.api.CreateFoodItemRequest
import com.example.wisebitemerchant.data.model.Category
import com.example.wisebitemerchant.data.model.FoodItem
import com.example.wisebitemerchant.data.repository.ApiResult
import com.example.wisebitemerchant.data.repository.FoodItemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddFoodItemUiState(
    val isLoading: Boolean = false,
    val categories: List<Category> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isItemCreated: Boolean = false
)

class AddFoodItemViewModel(
    private val repository: FoodItemRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AddFoodItemUiState())
    val uiState: StateFlow<AddFoodItemUiState> = _uiState.asStateFlow()
    
    init {
        loadCategories()
    }
    
    private fun loadCategories() {
        viewModelScope.launch {
            Log.d("AddFoodItemViewModel", "Starting to load categories...")
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            when (val result = repository.getCategoryHierarchy()) {
                is ApiResult.Success -> {
                    Log.d("AddFoodItemViewModel", "Categories loaded successfully: ${result.data.size} items")
                    result.data.forEach { category ->
                        Log.d("AddFoodItemViewModel", "Category: ${category.name} (ID: ${category.id}, Parent: ${category.parentCategoryId})")
                    }
                    _uiState.value = _uiState.value.copy(
                        categories = result.data, // These are now subcategories from the repository
                        isLoading = false
                    )
                }
                is ApiResult.Error -> {
                    Log.e("AddFoodItemViewModel", "Failed to load categories: ${result.message}")
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Failed to load categories: ${result.message}",
                        isLoading = false
                    )
                }
                is ApiResult.Loading -> {
                    Log.d("AddFoodItemViewModel", "Loading categories...")
                    // Already handled above
                }
            }
        }
    }
    
    fun createFoodItem(
        name: String,
        description: String?,
        sku: String?,
        standardPrice: Double,
        costPrice: Double?,
        totalQuantity: Int,
        isFresh: Boolean,
        categoryId: String?,
        ingredients: String?,
        allergens: String?,
        weight: Double?,
        unit: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            val request = CreateFoodItemRequest(
                name = name,
                description = description,
                sku = sku,
                standard_price = standardPrice,
                cost_price = costPrice,
                total_quantity = totalQuantity,
                is_fresh = isFresh,
                category_id = categoryId,
                ingredients = ingredients,
                allergens = allergens,
                weight = weight,
                unit = unit
            )
            
            when (val result = repository.createFoodItem(request)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Food item created successfully",
                        isItemCreated = true
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
    
    fun resetCreationState() {
        _uiState.value = _uiState.value.copy(isItemCreated = false)
    }
    
    fun getSubcategories(): List<Category> {
        return _uiState.value.categories.filter { it.parentCategoryId != null }
    }
}

class AddFoodItemViewModelFactory(
    private val repository: FoodItemRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddFoodItemViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddFoodItemViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}