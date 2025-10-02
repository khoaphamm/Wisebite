package com.example.wisebite.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wisebite.data.model.Order
import com.example.wisebite.data.model.OrderItem
import com.example.wisebite.data.model.OrderStatus
import com.example.wisebite.ui.component.OrderStatusChip
import com.example.wisebite.ui.theme.*
import com.example.wisebite.ui.viewmodel.OrderViewModel
import com.example.wisebite.util.ViewModelFactory
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsScreen(
    orderId: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: OrderViewModel = viewModel(
        factory = ViewModelFactory.getInstance(context)
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(orderId) {
        viewModel.getOrderById(orderId)
    }

    // Show confirmation dialog for cancellation
    var showCancelDialog by remember { mutableStateOf(false) }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Order") },
            text = { Text("Are you sure you want to cancel this order? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.cancelOrder(orderId)
                        showCancelDialog = false
                    }
                ) {
                    Text("Cancel Order", color = Red600)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Keep Order")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    uiState.selectedOrder?.let { order ->
                        if (order.status == OrderStatus.PENDING || order.status == OrderStatus.CONFIRMED) {
                            IconButton(onClick = { showCancelDialog = true }) {
                                Icon(
                                    Icons.Default.Cancel,
                                    contentDescription = "Cancel Order",
                                    tint = Red600
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = WarmGrey800
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Green500)
            }
        } else {
            uiState.selectedOrder?.let { order ->
                OrderDetailsContent(
                    order = order,
                    modifier = Modifier.padding(paddingValues)
                )
            } ?: run {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Order not found",
                        fontSize = 18.sp,
                        color = WarmGrey600
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderDetailsContent(
    order: Order,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Order Status Card
        item {
            OrderStatusCard(order = order)
        }

        // Store Information
        item {
            StoreInfoCard(order = order)
        }

        // Order Items
        item {
            Text(
                text = "Order Items",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = WarmGrey800
            )
        }

        items(order.items) { item ->
            OrderItemCard(item = item)
        }

        // Order Summary
        item {
            OrderSummaryCard(order = order)
        }

        // Add some bottom padding
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun OrderStatusCard(order: Order) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Order #${order.id.take(8)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarmGrey800
                    )
                    Text(
                        text = formatDate(order.createdAt),
                        fontSize = 14.sp,
                        color = WarmGrey600
                    )
                }
                
                OrderStatusChip(status = order.status)
            }
        }
    }
}

@Composable
private fun StoreInfoCard(order: Order) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Store Information",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = WarmGrey800
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = order.store?.name ?: "Unknown Store",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Green600
            )
            
            order.store?.address?.let { address ->
                Text(
                    text = address,
                    fontSize = 14.sp,
                    color = WarmGrey600,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun OrderItemCard(item: OrderItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                item.surpriseBag?.let { bag ->
                    Text(
                        text = bag.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = WarmGrey800,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = "Surprise Bag",
                        fontSize = 14.sp,
                        color = WarmGrey600
                    )
                }
                
                item.foodItem?.let { food ->
                    Text(
                        text = food.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = WarmGrey800,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = "Food Item",
                        fontSize = 14.sp,
                        color = WarmGrey600
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Qty: ${item.quantity}",
                    fontSize = 14.sp,
                    color = WarmGrey600
                )
                
                Text(
                    text = formatPrice(item.unitPrice),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Green600
                )
            }
        }
    }
}

@Composable
private fun OrderSummaryCard(order: Order) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Order Summary",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = WarmGrey800
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total Items:",
                    fontSize = 14.sp,
                    color = WarmGrey600
                )
                Text(
                    text = "${order.items.size}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = WarmGrey800
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total Amount:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarmGrey800
                )
                Text(
                    text = formatPrice(order.totalAmount),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Green600
                )
            }
        }
    }
}

private fun formatPrice(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    return formatter.format(amount)
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}