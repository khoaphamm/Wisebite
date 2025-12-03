package com.example.wisebite.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wisebite.data.model.SurpriseBag
import com.example.wisebite.data.repository.ApiResult
import com.example.wisebite.data.repository.SurpriseBagRepository
import com.example.wisebite.ui.theme.*
import com.example.wisebite.ui.component.ProfessionalSurpriseBagCard
import com.example.wisebite.ui.component.ProfessionalSurpriseBagCardLarge
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SurpriseBagListUiState(
    val isLoading: Boolean = false,
    val surpriseBags: List<SurpriseBag> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String = "Tất cả",
    val selectedCity: String = "TP. Hồ Chí Minh",
    val maxPrice: Double? = null,
    val errorMessage: String? = null
)

class SurpriseBagListViewModel(
    private val repository: SurpriseBagRepository,
    private val storeId: String? = null
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SurpriseBagListUiState())
    val uiState: StateFlow<SurpriseBagListUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            android.util.Log.d("SurpriseBagListViewModel", "Loading surprise bags data for storeId: $storeId")
            val categoriesResult = repository.getAvailableCategories()
            
            val bagsResult = if (storeId != null) {
                // Load bags for specific store
                android.util.Log.d("SurpriseBagListViewModel", "Loading bags for store: $storeId")
                repository.getStoreSurpriseBags(
                    storeId = storeId,
                    category = if (_uiState.value.selectedCategory == "Tất cả") null else _uiState.value.selectedCategory
                )
            } else {
                // Load all bags
                android.util.Log.d("SurpriseBagListViewModel", "Loading all surprise bags")
                repository.getAllSurpriseBags(
                    category = if (_uiState.value.selectedCategory == "Tất cả") null else _uiState.value.selectedCategory,
                    maxPrice = _uiState.value.maxPrice
                )
            }
            
            android.util.Log.d("SurpriseBagListViewModel", "Categories result: $categoriesResult")
            android.util.Log.d("SurpriseBagListViewModel", "Bags result: $bagsResult")
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                categories = if (categoriesResult is ApiResult.Success) 
                    listOf("Tất cả") + categoriesResult.data else 
                    listOf("Tất cả", "Combo", "Thịt/Cá", "Rau/Củ", "Trái cây", "Bánh mì"),
                surpriseBags = if (bagsResult is ApiResult.Success) {
                    android.util.Log.d("SurpriseBagListViewModel", "Successfully loaded ${bagsResult.data.size} surprise bags")
                    bagsResult.data.forEach { bag ->
                        android.util.Log.d("SurpriseBagListViewModel", "Bag: ${bag.name} - Store: ${bag.store?.name}")
                    }
                    enhanceBagsWithImages(bagsResult.data)
                } else {
                    android.util.Log.e("SurpriseBagListViewModel", "Failed to load surprise bags: $bagsResult")
                    emptyList()
                },
                errorMessage = if (bagsResult is ApiResult.Error) 
                    bagsResult.message else null
            )
        }
    }
    
    fun selectCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        loadData()
    }
    
    fun setMaxPrice(price: Double?) {
        _uiState.value = _uiState.value.copy(maxPrice = price)
        loadData()
    }
    
    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    // Enhanced function to add professional food images to surprise bags
    private fun enhanceBagsWithImages(bags: List<SurpriseBag>): List<SurpriseBag> {
        val foodImages = mapOf(
            // Vietnamese Food Categories
            "combo" to listOf(
                "https://images.unsplash.com/photo-1504674900247-0877df9cc836?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80", // Delicious meal
                "https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80", // Mixed food platter
                "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80",  // Healthy food combo
                "https://images.unsplash.com/photo-1565299624946-b28f40a0ca4b?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80" // Food bowl
            ),
            "thịt/cá" to listOf(
                "https://images.unsplash.com/photo-1546833999-b9f581a1996d?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80", // Grilled meat
                "https://images.unsplash.com/photo-1544943910-4c1dc44aab44?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80", // Fresh fish
                "https://images.unsplash.com/photo-1529692236671-f1f6cf9683ba?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80", // Meat platter
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

            android.util.Log.d("SurpriseBagListViewModel", "Assigning image to bag '${bag.name}' (bagType: ${bag.bagType}): $imageUrl")

            bag.copy(imageUrl = imageUrl)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurpriseBagListScreen(
    storeId: String? = null, // If provided, show bags for specific store
    onBackClick: () -> Unit = {},
    onBagClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val repository = SurpriseBagRepository.getInstance(context)
    val viewModel = remember(storeId) { SurpriseBagListViewModel(repository, storeId) }
    val uiState by viewModel.uiState.collectAsState()
    
    // Show error dialog
    uiState.errorMessage?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.clearErrorMessage() },
            title = { Text("Lỗi") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearErrorMessage() }) {
                    Text("OK")
                }
            }
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = if (storeId != null) {
                        // Try to get store name from the first bag, otherwise use generic title
                        val storeName = uiState.surpriseBags.firstOrNull()?.store?.name
                        if (storeName != null) "Surprise Bags - $storeName" else "Surprise Bags cửa hàng"
                    } else {
                        "Tất cả Surprise Bags"
                    },
                    fontWeight = FontWeight.Bold,
                    color = Green700
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Green700
                    )
                }
            },
            actions = {
                IconButton(onClick = { /* TODO: Open filter dialog */ }) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filters",
                        tint = Green700
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )
        
        // Category filters
        LazyRow(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.categories) { category ->
                val isSelected = category == uiState.selectedCategory
                
                FilterChip(
                    onClick = { viewModel.selectCategory(category) },
                    label = {
                        Text(
                            text = category,
                            fontSize = 14.sp,
                            color = if (isSelected) Color.White else WarmGrey700
                        )
                    },
                    selected = isSelected,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Green500,
                        selectedLabelColor = Color.White,
                        containerColor = WarmGrey200,
                        labelColor = WarmGrey700
                    )
                )
            }
        }
        
        // Content
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Green500)
                }
            }
            uiState.surpriseBags.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { viewModel.loadData() }
                    ) {
                        Text(
                            text = if (uiState.errorMessage != null) {
                                "Lỗi tải dữ liệu: ${uiState.errorMessage}"
                            } else {
                                "Không có Surprise Bag nào"
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (uiState.errorMessage != null) Red500 else WarmGrey600
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (uiState.errorMessage != null) {
                                "Nhấn để thử lại"
                            } else {
                                "Hãy thử tìm kiếm với bộ lọc khác"
                            },
                            fontSize = 14.sp,
                            color = if (uiState.errorMessage != null) Green500 else WarmGrey500
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Featured Bags Section (first 3 bags with large cards)
                    if (uiState.surpriseBags.isNotEmpty()) {
                        item {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "✨ Nổi bật",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                    
                                    // Featured badge
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                Color(0xFFFFD700).copy(alpha = 0.2f),
                                                RoundedCornerShape(12.dp)
                                            )
                                            .border(
                                                1.dp,
                                                Color(0xFFFFD700).copy(alpha = 0.4f),
                                                RoundedCornerShape(12.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "⭐ HOT",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFE65100)
                                        )
                                    }
                                }
                                
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp)
                                ) {
                                    items(
                                        items = uiState.surpriseBags.take(3),
                                        key = { bag -> "featured_${bag.id}" }
                                    ) { bag ->
                                        ProfessionalSurpriseBagCardLarge(
                                            bag = bag,
                                            onClick = { onBagClick(bag.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // All Bags Section (excluding featured bags)
                    val remainingBags = if (uiState.surpriseBags.size > 3) {
                        uiState.surpriseBags.drop(3)
                    } else {
                        emptyList()
                    }
                    
                    if (remainingBags.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🍽️ Tất cả Surprise Bags",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                                
                                // Count badge
                                Box(
                                    modifier = Modifier
                                        .background(
                                            Color(0xFF4CAF50).copy(alpha = 0.1f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .border(
                                            1.dp,
                                            Color(0xFF4CAF50).copy(alpha = 0.3f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${remainingBags.size} túi",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                            }
                        }
                        
                        items(
                            items = remainingBags,
                            key = { bag -> bag.id }
                        ) { bag ->
                            ProfessionalSurpriseBagCard(
                                bag = bag,
                                onClick = { onBagClick(bag.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Old SurpriseBagListItem removed - now using ProfessionalSurpriseBagCard