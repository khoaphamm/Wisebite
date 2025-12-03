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
                    enhanceBagsWithImages(bagsResult.data.take(5)) // Show top 5 featured bags with images
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
                            featuredSurpriseBags = enhanceBagsWithImages(result.data.take(5))
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
                is ApiResult.Success -> onResult(enhanceBagsWithImages(result.data))
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(errorMessage = result.message)
                    onResult(emptyList())
                }
                else -> onResult(emptyList())
            }
        }
    }

    // Enhanced function to add professional food images to surprise bags
    private fun enhanceBagsWithImages(bags: List<SurpriseBag>): List<SurpriseBag> {
        val foodImages = mapOf(
            // Vietnamese Food Categories
            "combo" to listOf(
                "https://images.unsplash.com/photo-1504674900247-0877df9cc836?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80", // Delicious meal
                "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80",  // Healthy food combo
                "https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80", // Mixed food platter
                "https://images.unsplash.com/photo-1565299624946-b28f40a0ca4b?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80" // Food bowl
            ),
            "thịt/cá" to listOf(
                "https://images.unsplash.com/photo-1529692236671-f1f6cf9683ba?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80",// Meat platter
                "https://images.unsplash.com/photo-1546833999-b9f581a1996d?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80", // Grilled meat
                "https://images.unsplash.com/photo-1544943910-4c1dc44aab44?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80", // Fresh fish
                "https://images.unsplash.com/photo-1551218808-94e220e084d2?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80"  // Seafood
            ),
            "rau/củ" to listOf(
                "https://images.unsplash.com/photo-1540420773420-3366772f4999?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80", // Fresh vegetables
                "https://images.unsplash.com/photo-1590779033100-9f60a05a013d?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80", // Vegetable salad
                "https://images.unsplash.com/photo-1574316071802-0d684efa7bf5?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80", // Root vegetables
                "https://images.unsplash.com/photo-1506976785307-8732e854ad03?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80"  // Mixed vegetables
            ),
            "trái cây" to listOf(
                "https://images.unsplash.com/photo-1619566636858-adf3ef46400b?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80", // Tropical fruits
                "https://images.unsplash.com/photo-1610832958506-aa56368176cf?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80", // Fresh fruit mix
                "https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80", // Citrus fruits
                "https://images.unsplash.com/photo-1464207687429-7505649dae38?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80"  // Berry mix
            ),
            "bánh mì" to listOf(
                "https://images.unsplash.com/photo-1516684732162-798a0062be99?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80", // Vietnamese bánh mì
                "https://images.unsplash.com/photo-1549931319-a545dcf3bc73?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80", // Fresh bread
                "https://images.unsplash.com/photo-1509440159596-0249088772ff?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80", // Artisan bread
                "https://images.unsplash.com/photo-1565895405307-2da9c1b9b7b0?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80"  // Vietnamese sandwich
            ),
            "default" to listOf(
                "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80", // Gourmet food
                "https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80", // Mixed cuisine
                "https://images.unsplash.com/photo-1576618148400-f54bed99fcfd?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80", // Restaurant meal
                "https://images.unsplash.com/photo-1565958011703-44f9829ba187?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80"  // Food platter
            )
        )

        return bags.mapIndexed { index, bag ->
            // If bag already has an image, keep it
            if (!bag.imageUrl.isNullOrBlank()) {
                return@mapIndexed bag
            }

            // Determine category for image selection using bagType
            val category = when {
                bag.bagType.lowercase().contains("combo") -> "combo"
                bag.bagType.lowercase().contains("thịt") || bag.bagType.lowercase().contains("cá") -> "thịt/cá"
                bag.bagType.lowercase().contains("rau") || bag.bagType.lowercase().contains("củ") -> "rau/củ"
                bag.bagType.lowercase().contains("trái") || bag.bagType.lowercase().contains("cây") -> "trái cây"
                bag.bagType.lowercase().contains("bánh") || bag.bagType.lowercase().contains("mì") -> "bánh mì"
                else -> "default"
            }

            val categoryImages = foodImages[category] ?: foodImages["default"]!!
            val imageUrl = categoryImages[index % categoryImages.size]

            android.util.Log.d("HomeViewModel", "Assigning image to bag '${bag.name}' (bagType: ${bag.bagType}): $imageUrl")

            bag.copy(imageUrl = imageUrl)
        }
    }
}