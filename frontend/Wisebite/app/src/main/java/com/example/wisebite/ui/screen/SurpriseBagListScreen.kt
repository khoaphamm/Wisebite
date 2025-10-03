package com.example.wisebite.ui.screen

import androidx.compose.foundation.background
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
                    bagsResult.data
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.surpriseBags,
                        key = { bag -> bag.id }
                    ) { bag ->
                        SurpriseBagListItem(
                            bag = bag,
                            onClick = { onBagClick(bag.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SurpriseBagListItem(
    bag: SurpriseBag,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with store name and category
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = bag.store?.name ?: "Unknown Store",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = bag.categoryDisplayName,
                        fontSize = 12.sp,
                        color = Orange600,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Discount badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Red500.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "-${bag.formattedDiscountPercentage}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Red500
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Bag name and description
            Text(
                text = bag.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            bag.description?.let { desc ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = desc,
                    fontSize = 12.sp,
                    color = WarmGrey600,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Price and availability info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = bag.formattedDiscountedPrice,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Green600
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = bag.formattedOriginalPrice,
                            fontSize = 12.sp,
                            color = WarmGrey500,
                            style = androidx.compose.ui.text.TextStyle(
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                            )
                        )
                    }
                    
                    Text(
                        text = bag.quantityDisplay,
                        fontSize = 12.sp,
                        color = if (bag.quantityAvailable > 0) WarmGrey600 else Red500,
                        fontWeight = if (bag.quantityAvailable > 0) FontWeight.Normal else FontWeight.Medium
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Pickup time",
                            modifier = Modifier.size(14.dp),
                            tint = WarmGrey600
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = bag.pickupTimeDisplay,
                            fontSize = 12.sp,
                            color = WarmGrey600
                        )
                    }
                    
                    bag.store?.let { store ->
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                modifier = Modifier.size(12.dp),
                                tint = WarmGrey500
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = store.displayAddress,
                                fontSize = 11.sp,
                                color = WarmGrey500,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}