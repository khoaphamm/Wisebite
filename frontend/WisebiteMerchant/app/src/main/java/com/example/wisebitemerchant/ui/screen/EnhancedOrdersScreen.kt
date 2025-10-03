package com.example.wisebitemerchant.ui.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wisebitemerchant.data.model.MerchantOrderStatus
import com.example.wisebitemerchant.data.model.Order
import com.example.wisebitemerchant.service.MerchantNotificationService
import com.example.wisebitemerchant.ui.theme.*
import com.example.wisebitemerchant.ui.viewmodel.OrderViewModel
import com.example.wisebitemerchant.ui.viewmodel.OrderViewModelFactory
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedOrdersScreen() {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val notificationService = remember { MerchantNotificationService.getInstance(context) }
    
    // Use real OrderViewModel instead of mock data
    val orderViewModel: OrderViewModel = viewModel(
        factory = OrderViewModelFactory(context)
    )
    val uiState by orderViewModel.uiState.collectAsStateWithLifecycle()
    
    // Listen for real-time notifications
    val notifications by notificationService.notifications.collectAsStateWithLifecycle(initialValue = null)
    
    // Show error dialog if there's an error
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            // You can show a snackbar or toast here
        }
        AlertDialog(
            onDismissRequest = { orderViewModel.clearErrorMessage() },
            title = { Text("Lỗi") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { orderViewModel.clearErrorMessage() }) {
                    Text("OK")
                }
            }
        )
    }
    
    // Show loading dialog when updating orders
    if (uiState.isUpdatingOrder) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Đang cập nhật") },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Orange600
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Đang cập nhật trạng thái đơn hàng...")
                }
            },
            confirmButton = { }
        )
    }
    
    // Get filtered orders for each tab
    val newOrders = orderViewModel.getNewOrders()
    val processingOrders = orderViewModel.getProcessingOrders()
    val readyOrders = orderViewModel.getReadyOrders()
    val completedOrders = orderViewModel.getCompletedOrders()
    
    val tabs = listOf(
        "Mới (${newOrders.size})",
        "Đang xử lý (${processingOrders.size})",
        "Sẵn sàng (${readyOrders.size})",
        "Hoàn thành (${completedOrders.size})"
    )
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar with notification indicator
        TopAppBar(
            title = {
                Text(
                    text = "Quản lý đơn hàng",
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                Box {
                    IconButton(onClick = { 
                        // Refresh orders
                        orderViewModel.loadOrders()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh orders"
                        )
                    }
                }
                
                Box {
                    IconButton(onClick = { 
                        // Test notification
                        notificationService.simulateNewOrderNotification(
                            "TEST001", "Khách hàng mới", 2, 50000.0
                        )
                    }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications"
                        )
                    }
                    // Red dot for new notifications  
                    if (newOrders.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color.Red, CircleShape)
                                .align(Alignment.TopEnd)
                        )
                    }
                }
            }
        )
        
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.fillMaxWidth(),
            containerColor = Color.White,
            contentColor = Orange600
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { 
                        Text(
                            title,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                        ) 
                    }
                )
            }
        }
        
        // Content based on selected tab
        when (selectedTabIndex) {
            0 -> NewOrdersContent(
                orders = newOrders,
                onAccept = { orderId -> orderViewModel.acceptOrder(orderId) },
                onReject = { orderId -> orderViewModel.rejectOrder(orderId) }
            )
            1 -> ProcessingOrdersContent(
                orders = processingOrders,
                onMarkReady = { orderId -> orderViewModel.markOrderReady(orderId) }
            )
            2 -> ReadyOrdersContent(
                orders = readyOrders,
                onMarkCompleted = { orderId -> orderViewModel.markOrderCompleted(orderId) }
            )
            3 -> CompletedOrdersContent(completedOrders)
        }
    }
}

@Composable
fun NewOrdersContent(
    orders: List<Order>,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit
) {
    if (orders.isEmpty()) {
        EmptyOrdersState(
            icon = Icons.Default.Receipt,
            message = "Chưa có đơn hàng mới",
            subtitle = "Đơn hàng mới sẽ xuất hiện ở đây"
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(orders) { order ->
                NewOrderCard(
                    order = order,
                    onAccept = { onAccept(order.id) },
                    onReject = { onReject(order.id) }
                )
            }
        }
    }
}

@Composable
fun ProcessingOrdersContent(
    orders: List<Order>,
    onMarkReady: (String) -> Unit
) {
    if (orders.isEmpty()) {
        EmptyOrdersState(
            icon = Icons.Default.Timelapse,
            message = "Không có đơn hàng đang xử lý",
            subtitle = "Đơn hàng đang chuẩn bị sẽ hiển thị ở đây"
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(orders) { order ->
                ProcessingOrderCard(
                    order = order,
                    onMarkReady = { onMarkReady(order.id) }
                )
            }
        }
    }
}

@Composable
fun ReadyOrdersContent(
    orders: List<Order>,
    onMarkCompleted: (String) -> Unit
) {
    if (orders.isEmpty()) {
        EmptyOrdersState(
            icon = Icons.Default.CheckCircle,
            message = "Không có đơn hàng sẵn sàng",
            subtitle = "Đơn hàng sẵn sàng nhận sẽ hiển thị ở đây"
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(orders) { order ->
                ReadyOrderCard(
                    order = order,
                    onMarkCompleted = { onMarkCompleted(order.id) }
                )
            }
        }
    }
}

@Composable
fun CompletedOrdersContent(orders: List<Order>) {
    if (orders.isEmpty()) {
        EmptyOrdersState(
            icon = Icons.Default.Done,
            message = "Chưa có đơn hàng hoàn thành",
            subtitle = "Đơn hàng đã hoàn thành sẽ hiển thị ở đây"
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(orders) { order ->
                CompletedOrderCard(order = order)
            }
        }
    }
}

@Composable
fun EmptyOrdersState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String,
    subtitle: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
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
                text = subtitle,
                fontSize = 14.sp,
                color = WarmGrey600,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun NewOrderCard(
    order: Order,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    // Log order details when rendering
    Log.d("EnhancedOrdersScreen", "Rendering NewOrderCard for order: ${order.id}")
    Log.d("EnhancedOrdersScreen", "  Customer: ${order.customer}")
    Log.d("EnhancedOrdersScreen", "  Customer name: ${order.customerName}")
    Log.d("EnhancedOrdersScreen", "  Items: ${order.items}")
    Log.d("EnhancedOrdersScreen", "  Items display: ${order.itemsDisplay}")
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with urgency indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color.Red, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ĐƠN HÀNG MỚI",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                }
                Text(
                    text = "#${order.id}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = WarmGrey600
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Customer info
            Text(
                text = order.customerName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = WarmGrey800
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Order items
            Text(
                text = order.itemsDisplay,
                fontSize = 14.sp,
                color = WarmGrey600,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Pickup time and amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                order.formattedPickupTime?.let { time ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = Orange600,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Nhận lúc $time",
                            fontSize = 14.sp,
                            color = Orange600,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Text(
                    text = order.formattedTotalAmount,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Green600
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Red
                    )
                ) {
                    Text("Từ chối")
                }
                
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Orange600
                    )
                ) {
                    Text("Chấp nhận")
                }
            }
        }
    }
}

@Composable
fun ProcessingOrderCard(
    order: Order,
    onMarkReady: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            OrderCardHeader(order, "ĐANG CHUẨN BỊ", Orange600)
            OrderCardContent(order)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onMarkReady,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green600
                )
            ) {
                Text("Đánh dấu sẵn sàng")
            }
        }
    }
}

@Composable
fun ReadyOrderCard(
    order: Order,
    onMarkCompleted: () -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            OrderCardHeader(order, "SẴN SÀNG NHẬN", Green600)
            OrderCardContent(order)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { showConfirmDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Blue600
                )
            ) {
                Text("Xác nhận đã nhận")
            }
        }
    }
    
    // Confirmation Dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text(
                    text = "Xác nhận hoàn thành đơn hàng",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Bạn có chắc chắn muốn đánh dấu đơn hàng #${order.id} đã được khách hàng nhận không?",
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Khách hàng: ${order.customerName}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = WarmGrey800
                    )
                    Text(
                        text = "Tổng tiền: ${NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(order.totalAmount)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Green600
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onMarkCompleted()
                        showConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Blue600
                    )
                ) {
                    Text("Xác nhận")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showConfirmDialog = false }
                ) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
fun CompletedOrderCard(order: Order) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            OrderCardHeader(order, "HOÀN THÀNH", WarmGrey600)
            OrderCardContent(order)
        }
    }
}

@Composable
fun OrderCardHeader(order: Order, statusText: String, statusColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = statusText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = statusColor
        )
        Text(
            text = "#${order.id}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = WarmGrey600
        )
    }
}

@Composable
fun OrderCardContent(order: Order) {
    Spacer(modifier = Modifier.height(12.dp))
    
    Text(
        text = order.customerName,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = WarmGrey800
    )
    
    Spacer(modifier = Modifier.height(8.dp))
    
    Text(
        text = order.itemsDisplay,
        fontSize = 14.sp,
        color = WarmGrey600
    )
    
    Spacer(modifier = Modifier.height(8.dp))
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        order.formattedPickupTime?.let { time ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = Orange600,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Nhận lúc $time",
                    fontSize = 14.sp,
                    color = Orange600
                )
            }
        }
        
        Text(
            text = order.formattedTotalAmount,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Green600
        )
    }
}