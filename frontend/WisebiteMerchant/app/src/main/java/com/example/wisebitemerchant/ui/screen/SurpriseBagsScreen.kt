package com.example.wisebitemerchant.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wisebitemerchant.data.model.SurpriseBag
import com.example.wisebitemerchant.data.repository.FoodItemRepository
import com.example.wisebitemerchant.ui.component.CreateSurpriseBagDialog
import com.example.wisebitemerchant.ui.theme.Green500
import com.example.wisebitemerchant.ui.theme.Orange600
import com.example.wisebitemerchant.ui.theme.Red500
import com.example.wisebitemerchant.ui.theme.WarmGrey600
import com.example.wisebitemerchant.ui.theme.WisebiteMerchantTheme
import com.example.wisebitemerchant.ui.viewmodel.SurpriseBagViewModel
import com.example.wisebitemerchant.ui.viewmodel.SurpriseBagViewModelFactory
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurpriseBagsScreen() {
    val context = LocalContext.current
    val repository = FoodItemRepository.getInstance(context)
    val viewModel: SurpriseBagViewModel = viewModel(
        factory = SurpriseBagViewModelFactory(repository)
    )
    
    val uiState by viewModel.uiState.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showCreateDialog by remember { mutableStateOf(false) }
    val tabs = listOf("Đang hoạt động", "Đã bán", "Nháp")
    
    // Handle success/error messages
    uiState.successMessage?.let { message ->
        LaunchedEffect(message) {
            // Show success message and clear it
            viewModel.clearMessages()
        }
    }
    
    uiState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            // Show error message and clear it after 3 seconds
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }
    
    // Handle token expiration
    if (uiState.isTokenExpired) {
        AlertDialog(
            onDismissRequest = { viewModel.clearTokenExpiredState() },
            title = { Text("Phiên đăng nhập hết hạn") },
            text = { Text("Phiên đăng nhập của bạn đã hết hạn. Vui lòng đăng nhập lại để tiếp tục.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearTokenExpiredState()
                    // TODO: Navigate to login screen or trigger re-authentication
                    // You can add navigation logic here based on your app's navigation structure
                }) {
                    Text("Đăng nhập lại")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.clearTokenExpiredState() }) {
                    Text("Hủy")
                }
            }
        )
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Túi Bất Ngờ",
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                IconButton(
                    onClick = { showCreateDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Tạo Surprise Bag"
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
        
        // Loading indicator
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Orange600)
            }
        } else {
            // Content based on selected tab
            when (selectedTabIndex) {
                0 -> SurpriseBagsList(
                    bags = uiState.activeBags,
                    onToggleActive = { bag -> viewModel.deactivateSurpriseBag(bag.id) },
                    onDelete = { bag -> viewModel.deleteSurpriseBag(bag.id) },
                    emptyMessage = "Chưa có Surprise Bag nào đang hoạt động",
                    emptyDescription = "Tạo Surprise Bag để bán sản phẩm với giá ưu đãi"
                )
                1 -> SurpriseBagsList(
                    bags = uiState.soldBags,
                    onToggleActive = null, // Can't reactivate sold bags
                    onDelete = null, // Can't delete sold bags
                    emptyMessage = "Chưa có Surprise Bag nào được bán",
                    emptyDescription = "Các Surprise Bag đã bán hết sẽ hiển thị ở đây"
                )
                2 -> SurpriseBagsList(
                    bags = uiState.draftBags,
                    onToggleActive = { bag -> viewModel.activateSurpriseBag(bag.id) },
                    onDelete = { bag -> viewModel.deleteSurpriseBag(bag.id) },
                    emptyMessage = "Chưa có Surprise Bag nháp nào",
                    emptyDescription = "Các Surprise Bag chưa kích hoạt sẽ hiển thị ở đây"
                )
            }
        }
    }
    
    // Create Surprise Bag Dialog
    if (showCreateDialog) {
        CreateSurpriseBagDialog(
            isCreating = uiState.isCreatingBag,
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, description, category, originalValue, discountedPrice, quantity, startTime, endTime ->
                viewModel.createSurpriseBag(
                    name = name,
                    description = description,
                    category = category,
                    originalValue = originalValue,
                    discountedPrice = discountedPrice,
                    quantity = quantity,
                    pickupStartTime = startTime,
                    pickupEndTime = endTime
                )
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun SurpriseBagsList(
    bags: List<SurpriseBag>,
    onToggleActive: ((SurpriseBag) -> Unit)?,
    onDelete: ((SurpriseBag) -> Unit)? = null,
    emptyMessage: String,
    emptyDescription: String
) {
    if (bags.isEmpty()) {
        EmptyBagsContent(
            message = emptyMessage,
            description = emptyDescription
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(bags) { bag ->
                SurpriseBagCard(
                    bag = bag,
                    onToggleActive = onToggleActive?.let { { onToggleActive(bag) } },
                    onDelete = onDelete?.let { { onDelete(bag) } }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurpriseBagCard(
    bag: SurpriseBag,
    onToggleActive: (() -> Unit)?,
    onDelete: (() -> Unit)? = null
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"))
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: Navigate to bag details */ },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = bag.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = bag.category,
                        fontSize = 14.sp,
                        color = Orange600,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Status indicator
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when {
                                !bag.isActive -> Color.Gray.copy(alpha = 0.2f)
                                bag.quantityAvailable == 0 -> Red500.copy(alpha = 0.2f)
                                else -> Green500.copy(alpha = 0.2f)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when {
                            !bag.isActive -> "Nháp"
                            bag.quantityAvailable == 0 -> "Hết hàng"
                            else -> "Hoạt động"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = when {
                            !bag.isActive -> Color.Gray
                            bag.quantityAvailable == 0 -> Red500
                            else -> Green500
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Price information
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Giá gốc",
                        fontSize = 12.sp,
                        color = WarmGrey600
                    )
                    Text(
                        text = currencyFormatter.format(bag.originalValue),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Column {
                    Text(
                        text = "Giá bán",
                        fontSize = 12.sp,
                        color = WarmGrey600
                    )
                    Text(
                        text = currencyFormatter.format(bag.discountedPrice),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Orange600
                    )
                }
                
                Column {
                    Text(
                        text = "Giảm giá",
                        fontSize = 12.sp,
                        color = WarmGrey600
                    )
                    Text(
                        text = "${bag.discountPercentage.toInt()}%",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Green500
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Additional information
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Số lượng: ${bag.quantityAvailable}",
                    fontSize = 12.sp,
                    color = WarmGrey600
                )
                
                Text(
                    text = "Nhận hàng: ${bag.timeDisplayText}",
                    fontSize = 12.sp,
                    color = WarmGrey600
                )
            }
            
            // Action buttons
            if (onToggleActive != null || onDelete != null) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    // Delete button
                    onDelete?.let { deleteCallback ->
                        TextButton(
                            onClick = deleteCallback,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Red500
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Xóa",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Xóa",
                                fontSize = 14.sp
                            )
                        }
                    }
                    
                    // Toggle active button
                    onToggleActive?.let { toggleCallback ->
                        if (onDelete != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        TextButton(
                            onClick = toggleCallback
                        ) {
                            Icon(
                                imageVector = if (bag.isActive) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (bag.isActive) "Ẩn" else "Kích hoạt",
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyBagsContent(
    message: String,
    description: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CardGiftcard,
                contentDescription = "Surprise Bags",
                modifier = Modifier.size(64.dp),
                tint = Orange600
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = message,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Orange600
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = description,
                fontSize = 14.sp,
                color = WarmGrey600,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SurpriseBagsScreenPreview() {
    WisebiteMerchantTheme {
        SurpriseBagsScreen()
    }
}