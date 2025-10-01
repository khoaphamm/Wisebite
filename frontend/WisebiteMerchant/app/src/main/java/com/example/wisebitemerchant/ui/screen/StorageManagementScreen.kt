package com.example.wisebitemerchant.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wisebitemerchant.data.model.FoodItem
import com.example.wisebitemerchant.data.repository.FoodItemRepository
import com.example.wisebitemerchant.ui.component.FoodItemCard
import com.example.wisebitemerchant.ui.component.SurplusMarkingDialog
import com.example.wisebitemerchant.ui.theme.Orange50
import com.example.wisebitemerchant.ui.theme.Orange600
import com.example.wisebitemerchant.ui.theme.WarmGrey600
import com.example.wisebitemerchant.ui.theme.WisebiteMerchantTheme
import com.example.wisebitemerchant.ui.viewmodel.StorageManagementViewModel
import com.example.wisebitemerchant.ui.viewmodel.StorageManagementViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageManagementScreen(
    viewModel: StorageManagementViewModel? = null,
    onNavigateToAddItem: () -> Unit
) {
    val context = LocalContext.current
    val repository = FoodItemRepository.getInstance(context)
    val actualViewModel: StorageManagementViewModel = viewModel ?: viewModel(
        factory = StorageManagementViewModelFactory(repository)
    )
    
    val uiState by actualViewModel.uiState.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Tất cả", "Tươi sống", "Đóng gói", "Surplus")
    
    var showSurplusDialog by remember { mutableStateOf(false) }
    var selectedItemForSurplus by remember { mutableStateOf<FoodItem?>(null) }
    
    // Show error/success messages
    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        // Here you could show a snackbar or toast
        // For now, we'll just clear the messages after showing them
        if (uiState.errorMessage != null || uiState.successMessage != null) {
            kotlinx.coroutines.delay(3000)
            actualViewModel.clearMessages()
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Quản lý kho hàng",
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                IconButton(
                    onClick = onNavigateToAddItem
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Thêm sản phẩm"
                    )
                }
            }
        )
        
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }
        
        // Content based on selected tab
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTabIndex) {
                0 -> AllProductsContent(
                    foodItems = uiState.foodItems,
                    onNavigateToAddItem = onNavigateToAddItem,
                    onMarkSurplus = { item ->
                        selectedItemForSurplus = item
                        showSurplusDialog = true
                    },
                    onEdit = { /* TODO: Navigate to edit */ },
                    onUpdateInventory = { /* TODO: Show inventory dialog */ },
                    onDelete = { item -> actualViewModel.deleteFoodItem(item.id) }
                )
                1 -> FreshProductsContent(
                    foodItems = actualViewModel.getFreshItems(),
                    onMarkSurplus = { item ->
                        selectedItemForSurplus = item
                        showSurplusDialog = true
                    },
                    onEdit = { /* TODO: Navigate to edit */ },
                    onUpdateInventory = { /* TODO: Show inventory dialog */ },
                    onDelete = { item -> actualViewModel.deleteFoodItem(item.id) }
                )
                2 -> PackagedProductsContent(
                    foodItems = actualViewModel.getPackagedItems(),
                    onMarkSurplus = { item ->
                        selectedItemForSurplus = item
                        showSurplusDialog = true
                    },
                    onEdit = { /* TODO: Navigate to edit */ },
                    onUpdateInventory = { /* TODO: Show inventory dialog */ },
                    onDelete = { item -> actualViewModel.deleteFoodItem(item.id) }
                )
                3 -> SurplusProductsContent(
                    foodItems = actualViewModel.getSurplusItems(),
                    onMarkSurplus = { item ->
                        selectedItemForSurplus = item
                        showSurplusDialog = true
                    },
                    onEdit = { /* TODO: Navigate to edit */ },
                    onUpdateInventory = { /* TODO: Show inventory dialog */ },
                    onDelete = { item -> actualViewModel.deleteFoodItem(item.id) }
                )
            }
            
            // Loading overlay
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Orange600)
                }
            }
            
            // Error message
            uiState.errorMessage?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            
            // Success message
            uiState.successMessage?.let { success ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = success,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
    
    // Surplus Marking Dialog
    if (showSurplusDialog && selectedItemForSurplus != null) {
        SurplusMarkingDialog(
            foodItem = selectedItemForSurplus!!,
            onDismiss = {
                showSurplusDialog = false
                selectedItemForSurplus = null
            },
            onConfirm = { surplusQuantity, discountPercentage, surplusPrice ->
                actualViewModel.markSurplus(
                    itemId = selectedItemForSurplus!!.id,
                    surplusQuantity = surplusQuantity,
                    discountPercentage = discountPercentage,
                    surplusPrice = surplusPrice
                )
                showSurplusDialog = false
                selectedItemForSurplus = null
            }
        )
    }
}

@Composable
fun AllProductsContent(
    foodItems: List<FoodItem>,
    onNavigateToAddItem: () -> Unit,
    onMarkSurplus: (FoodItem) -> Unit,
    onEdit: (FoodItem) -> Unit,
    onUpdateInventory: (FoodItem) -> Unit,
    onDelete: (FoodItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Orange50
            ),
            onClick = onNavigateToAddItem
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add new item",
                    tint = Orange600
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Thêm sản phẩm mới",
                    color = Orange600,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (foodItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Chưa có sản phẩm nào",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(foodItems) { item ->
                    FoodItemCard(
                        foodItem = item,
                        onMarkSurplus = { onMarkSurplus(item) },
                        onEdit = { onEdit(item) },
                        onUpdateInventory = { onUpdateInventory(item) },
                        onDelete = { onDelete(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun FreshProductsContent(
    foodItems: List<FoodItem>,
    onMarkSurplus: (FoodItem) -> Unit,
    onEdit: (FoodItem) -> Unit,
    onUpdateInventory: (FoodItem) -> Unit,
    onDelete: (FoodItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (foodItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Chưa có sản phẩm tươi sống nào",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(foodItems) { item ->
                    FoodItemCard(
                        foodItem = item,
                        onMarkSurplus = { onMarkSurplus(item) },
                        onEdit = { onEdit(item) },
                        onUpdateInventory = { onUpdateInventory(item) },
                        onDelete = { onDelete(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun PackagedProductsContent(
    foodItems: List<FoodItem>,
    onMarkSurplus: (FoodItem) -> Unit,
    onEdit: (FoodItem) -> Unit,
    onUpdateInventory: (FoodItem) -> Unit,
    onDelete: (FoodItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (foodItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Chưa có sản phẩm đóng gói nào",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(foodItems) { item ->
                    FoodItemCard(
                        foodItem = item,
                        onMarkSurplus = { onMarkSurplus(item) },
                        onEdit = { onEdit(item) },
                        onUpdateInventory = { onUpdateInventory(item) },
                        onDelete = { onDelete(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun SurplusProductsContent(
    foodItems: List<FoodItem>,
    onMarkSurplus: (FoodItem) -> Unit,
    onEdit: (FoodItem) -> Unit,
    onUpdateInventory: (FoodItem) -> Unit,
    onDelete: (FoodItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (foodItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Chưa có sản phẩm thặng dư nào",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(foodItems) { item ->
                    FoodItemCard(
                        foodItem = item,
                        onMarkSurplus = { onMarkSurplus(item) },
                        onEdit = { onEdit(item) },
                        onUpdateInventory = { onUpdateInventory(item) },
                        onDelete = { onDelete(item) }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StorageManagementScreenPreview() {
    WisebiteMerchantTheme {
        StorageManagementScreen(
            onNavigateToAddItem = {}
        )
    }
}