package com.example.wisebitemerchant.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wisebitemerchant.data.model.Category
import com.example.wisebitemerchant.ui.theme.*
import com.example.wisebitemerchant.ui.viewmodel.AddFoodItemViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodItemScreen(
    viewModel: AddFoodItemViewModel,
    onNavigateBack: () -> Unit = {},
    onSave: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var standardPrice by remember { mutableStateOf("") }
    var costPrice by remember { mutableStateOf("") }
    var totalQuantity by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("piece") }
    var ingredients by remember { mutableStateOf("") }
    var allergens by remember { mutableStateOf("") }
    var isFresh by remember { mutableStateOf(true) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showUnitPicker by remember { mutableStateOf(false) }
    
    // Handle UI state changes
    LaunchedEffect(uiState.isItemCreated) {
        if (uiState.isItemCreated) {
            viewModel.resetCreationState()
            onSave()
        }
    }
    
    // Show error or success messages
    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        // Clear messages after showing them
        if (uiState.errorMessage != null || uiState.successMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }
    
    val units = listOf("piece", "kg", "g", "liter", "ml", "pack", "box", "bó", "con", "chai", "hộp", "gói", "nải")
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Show error/success messages
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Long
            )
        }
    }
    
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Thêm sản phẩm mới",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            // Validate required fields
                            if (name.isNotBlank() && standardPrice.isNotBlank() && totalQuantity.isNotBlank()) {
                                viewModel.createFoodItem(
                                    name = name.trim(),
                                    description = description.trim().takeIf { it.isNotBlank() },
                                    sku = sku.trim().takeIf { it.isNotBlank() },
                                    standardPrice = standardPrice.toDoubleOrNull() ?: 0.0,
                                    costPrice = costPrice.toDoubleOrNull(),
                                    totalQuantity = totalQuantity.toIntOrNull() ?: 0,
                                    isFresh = isFresh,
                                    categoryId = selectedCategory?.id,
                                    ingredients = ingredients.trim().takeIf { it.isNotBlank() },
                                    allergens = allergens.trim().takeIf { it.isNotBlank() },
                                    weight = weight.toDoubleOrNull(),
                                    unit = unit
                                )
                            }
                        },
                        enabled = name.isNotBlank() && standardPrice.isNotBlank() && totalQuantity.isNotBlank() && !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Orange600,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Lưu",
                                color = if (name.isNotBlank() && standardPrice.isNotBlank() && totalQuantity.isNotBlank()) 
                                    Orange600 else WarmGrey400,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Product Image Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clickable { /* TODO: Image picker */ },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Orange50)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Add Image",
                            tint = Orange600,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Thêm ảnh sản phẩm",
                            color = Orange600,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Basic Information Section
            Text(
                text = "Thông tin cơ bản",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Product Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Tên sản phẩm *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Mô tả") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // SKU
            OutlinedTextField(
                value = sku,
                onValueChange = { sku = it },
                label = { Text("Mã SKU") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("VD: BEEF001") }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Category Selection
            Text(
                text = "Danh mục",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Category Picker
            OutlinedTextField(
                value = selectedCategory?.name ?: "",
                onValueChange = { },
                label = { Text("Chọn danh mục") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCategoryPicker = true },
                enabled = false,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown"
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = "Category"
                    )
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Fresh/Packaged Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Loại sản phẩm:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(16.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isFresh,
                        onClick = { isFresh = true }
                    )
                    Text("Tươi sống")
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    RadioButton(
                        selected = !isFresh,
                        onClick = { isFresh = false }
                    )
                    Text("Đóng gói")
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Pricing Section
            Text(
                text = "Giá cả",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Standard Price
                OutlinedTextField(
                    value = standardPrice,
                    onValueChange = { standardPrice = it },
                    label = { Text("Giá bán *") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    suffix = { Text("₫") },
                    singleLine = true
                )
                
                // Cost Price
                OutlinedTextField(
                    value = costPrice,
                    onValueChange = { costPrice = it },
                    label = { Text("Giá vốn") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    suffix = { Text("₫") },
                    singleLine = true
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Inventory Section
            Text(
                text = "Kho hàng",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Total Quantity
                OutlinedTextField(
                    value = totalQuantity,
                    onValueChange = { totalQuantity = it },
                    label = { Text("Số lượng *") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                
                // Unit
                OutlinedTextField(
                    value = unit,
                    onValueChange = { },
                    label = { Text("Đơn vị") },
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showUnitPicker = true },
                    enabled = false,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown"
                        )
                    },
                    singleLine = true
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Weight (optional)
            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Trọng lượng (g)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                placeholder = { Text("VD: 1000") }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Additional Information Section
            Text(
                text = "Thông tin bổ sung",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Ingredients
            OutlinedTextField(
                value = ingredients,
                onValueChange = { ingredients = it },
                label = { Text("Thành phần") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                placeholder = { Text("Liệt kê các thành phần chính...") }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Allergens
            OutlinedTextField(
                value = allergens,
                onValueChange = { allergens = it },
                label = { Text("Chất gây dị ứng") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("VD: Sữa, Gluten, Hạt...") }
            )
            
            // Bottom spacing for save button
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
    
    // Category Picker Dialog
    if (showCategoryPicker) {
        AlertDialog(
            onDismissRequest = { showCategoryPicker = false },
            title = { Text("Chọn danh mục") },
            text = {
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Orange600)
                    }
                } else {
                    LazyColumn {
                        items(uiState.categories) { category ->
                            TextButton(
                                onClick = {
                                    selectedCategory = category
                                    showCategoryPicker = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Text(
                                        text = category.name,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryPicker = false }) {
                    Text("Hủy")
                }
            }
        )
    }
    
    // Unit Picker Dialog
    if (showUnitPicker) {
        AlertDialog(
            onDismissRequest = { showUnitPicker = false },
            title = { Text("Chọn đơn vị") },
            text = {
                LazyColumn {
                    items(units) { unitOption ->
                        TextButton(
                            onClick = {
                                unit = unitOption
                                showUnitPicker = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Text(
                                    text = unitOption,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showUnitPicker = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AddFoodItemScreenPreview() {
    WisebiteMerchantTheme {
        // Note: Preview will show UI without ViewModel functionality
        // AddFoodItemScreen(viewModel = mockViewModel)
    }
}