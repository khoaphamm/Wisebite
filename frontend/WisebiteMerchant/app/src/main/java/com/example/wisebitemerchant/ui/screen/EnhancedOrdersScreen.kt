package com.example.wisebitemerchant.ui.screen

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
import com.example.wisebitemerchant.service.MerchantNotificationService
import com.example.wisebitemerchant.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

// Mock data models - these should come from your actual data layer
data class MerchantOrder(
    val id: String,
    val customerName: String,
    val items: List<String>,
    val totalAmount: Double,
    val status: MerchantOrderStatus,
    val pickupTime: String?,
    val createdAt: Date = Date(),
    val notes: String? = null
)

enum class MerchantOrderStatus {
    NEW, CONFIRMED, PREPARING, READY, COMPLETED, CANCELLED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedOrdersScreen() {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val notificationService = remember { MerchantNotificationService.getInstance(context) }
    
    // Listen for real-time notifications
    val notifications by notificationService.notifications.collectAsStateWithLifecycle(initialValue = null)
    
    // Mock orders data - replace with actual ViewModel
    val mockOrders = remember {
        listOf(
            MerchantOrder(
                id = "ORD001",
                customerName = "Nguyễn Văn A",
                items = listOf("Combo trưa", "Nước ngọt"),
                totalAmount = 45000.0,
                status = MerchantOrderStatus.NEW,
                pickupTime = "12:30"
            ),
            MerchantOrder(
                id = "ORD002", 
                customerName = "Trần Thị B",
                items = listOf("Bánh mì"),
                totalAmount = 25000.0,
                status = MerchantOrderStatus.PREPARING,
                pickupTime = "13:00"
            ),
            MerchantOrder(
                id = "ORD003",
                customerName = "Lê Minh C",
                items = listOf("Surprise Bag"),
                totalAmount = 30000.0,
                status = MerchantOrderStatus.READY,
                pickupTime = "14:00"
            )
        )
    }
    
    // Show notification snackbar
    notifications?.let { notification ->
        LaunchedEffect(notification.id) {
            // You can show a snackbar or handle the notification here
        }
    }
    
    val tabs = listOf("Mới (${mockOrders.count { it.status == MerchantOrderStatus.NEW }})", 
                     "Đang xử lý", "Sẵn sàng", "Hoàn thành")
    
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
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color.Red, CircleShape)
                            .align(Alignment.TopEnd)
                    )
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
            0 -> NewOrdersContent(mockOrders.filter { it.status == MerchantOrderStatus.NEW })
            1 -> ProcessingOrdersContent(mockOrders.filter { 
                it.status == MerchantOrderStatus.CONFIRMED || it.status == MerchantOrderStatus.PREPARING 
            })
            2 -> ReadyOrdersContent(mockOrders.filter { it.status == MerchantOrderStatus.READY })
            3 -> CompletedOrdersContent(mockOrders.filter { it.status == MerchantOrderStatus.COMPLETED })
        }
    }
}

@Composable
fun NewOrdersContent(orders: List<MerchantOrder>) {
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
                    onAccept = { /* Handle accept */ },
                    onReject = { /* Handle reject */ }
                )
            }
        }
    }
}

@Composable
fun ProcessingOrdersContent(orders: List<MerchantOrder>) {
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
                    onMarkReady = { /* Handle mark ready */ }
                )
            }
        }
    }
}

@Composable
fun ReadyOrdersContent(orders: List<MerchantOrder>) {
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
                    onMarkCompleted = { /* Handle mark completed */ }
                )
            }
        }
    }
}

@Composable
fun CompletedOrdersContent(orders: List<MerchantOrder>) {
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
    order: MerchantOrder,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
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
                text = order.items.joinToString(", "),
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
                order.pickupTime?.let { time ->
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
                    text = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
                        .format(order.totalAmount),
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
    order: MerchantOrder,
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
    order: MerchantOrder,
    onMarkCompleted: () -> Unit
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
            OrderCardHeader(order, "SẴN SÀNG NHẬN", Green600)
            OrderCardContent(order)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onMarkCompleted,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Blue600
                )
            ) {
                Text("Xác nhận đã nhận")
            }
        }
    }
}

@Composable
fun CompletedOrderCard(order: MerchantOrder) {
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
fun OrderCardHeader(order: MerchantOrder, statusText: String, statusColor: Color) {
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
fun OrderCardContent(order: MerchantOrder) {
    Spacer(modifier = Modifier.height(12.dp))
    
    Text(
        text = order.customerName,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = WarmGrey800
    )
    
    Spacer(modifier = Modifier.height(8.dp))
    
    Text(
        text = order.items.joinToString(", "),
        fontSize = 14.sp,
        color = WarmGrey600
    )
    
    Spacer(modifier = Modifier.height(8.dp))
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        order.pickupTime?.let { time ->
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
            text = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
                .format(order.totalAmount),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Green600
        )
    }
}