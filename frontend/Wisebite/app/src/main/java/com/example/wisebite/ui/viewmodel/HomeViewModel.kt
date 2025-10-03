package com.example.wisebite.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wisebite.data.model.Store
import com.example.wisebite.data.model.SurpriseBag
import com.example.wisebite.data.repository.ApiResult
import com.example.wisebite.data.repository.SurpriseBagRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val stores: List<Store> = emptyList(),
    val featuredSurpriseBags: List<SurpriseBag> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String = "Tất cả",
    val errorMessage: String? = null,
    val selectedCity: String = "TP. Hồ Chí Minh"
)

class HomeViewModel(
    private val surpriseBagRepository: SurpriseBagRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            // Load stores, categories, and featured surprise bags in parallel
            val storesResult = surpriseBagRepository.getAvailableStores()
            val categoriesResult = surpriseBagRepository.getAvailableCategories()
            val bagsResult = surpriseBagRepository.getAllSurpriseBags(
                category = null,
                maxPrice = null
            )
            
            // Log results for debugging
            android.util.Log.d("HomeViewModel", "Stores result: $storesResult")
            android.util.Log.d("HomeViewModel", "Categories result: $categoriesResult")
            android.util.Log.d("HomeViewModel", "Bags result: $bagsResult")
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                stores = if (storesResult is ApiResult.Success) {
                    android.util.Log.d("HomeViewModel", "Successfully loaded ${storesResult.data.size} stores")
                    storesResult.data.forEach { store ->
                        android.util.Log.d("HomeViewModel", "Store: ${store.name} - ${store.address}")
                    }
                    storesResult.data
                } else {
                    android.util.Log.e("HomeViewModel", "Failed to load stores: $storesResult")
                    emptyList()
                },
                categories = if (categoriesResult is ApiResult.Success) 
                    listOf("Tất cả") + categoriesResult.data 
                else listOf("Tất cả", "Combo", "Thịt/Cá", "Rau/Củ", "Trái cây", "Bánh mì"),
                featuredSurpriseBags = if (bagsResult is ApiResult.Success) 
                    bagsResult.data.take(5) // Show top 5 featured bags
                else emptyList(),
                errorMessage = when {
                    storesResult is ApiResult.Error -> storesResult.message
                    categoriesResult is ApiResult.Error -> categoriesResult.message
                    bagsResult is ApiResult.Error -> bagsResult.message
                    else -> null
                }
            )
        }
    }

    fun selectCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        
        // If category is not "Tất cả", filter featured bags by category
        if (category != "Tất cả") {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                android.util.Log.d("HomeViewModel", "Filtering by category: $category")
                when (val result = surpriseBagRepository.getAllSurpriseBags(
                    category = category,
                    maxPrice = null
                )) {
                    is ApiResult.Success -> {
                        android.util.Log.d("HomeViewModel", "Filtered bags: ${result.data.size} found")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            featuredSurpriseBags = result.data.take(5)
                        )
                    }
                    is ApiResult.Error -> {
                        android.util.Log.e("HomeViewModel", "Error filtering bags: ${result.message}")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                    else -> {}
                }
            }
        } else {
            // Show all featured bags
            loadInitialData()
        }
    }

    fun changeCity(city: String) {
        _uiState.value = _uiState.value.copy(selectedCity = city)
        loadInitialData()
    }

    fun refreshData() {
        loadInitialData()
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun getStoreSurpriseBags(storeId: String, onResult: (List<SurpriseBag>) -> Unit) {
        viewModelScope.launch {
            when (val result = surpriseBagRepository.getStoreSurpriseBags(storeId)) {
                is ApiResult.Success -> onResult(result.data)
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(errorMessage = result.message)
                    onResult(emptyList())
                }
                else -> onResult(emptyList())
            }
        }
    }
}