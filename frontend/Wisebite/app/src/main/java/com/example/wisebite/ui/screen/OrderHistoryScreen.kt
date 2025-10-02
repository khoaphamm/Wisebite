package com.example.wisebite.ui.screen

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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wisebite.data.model.Order
import com.example.wisebite.data.model.OrderStatus
import com.example.wisebite.ui.component.SimpleHeader
import com.example.wisebite.ui.component.OrderStatusChipCircle
import com.example.wisebite.ui.theme.*
import com.example.wisebite.ui.viewmodel.OrderViewModel
import com.example.wisebite.util.ViewModelFactory
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToOrderDetails: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: OrderViewModel = viewModel(
        factory = ViewModelFactory.getInstance(context)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadOrders()
    }

    // Handle error messages
    uiState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            // Could show snackbar here
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        SimpleHeader(
            title = "Lịch sử đơn hàng", 
            onBackClick = onNavigateBack
        )

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Green500)
            }
        } else if (uiState.orders.isEmpty()) {
            EmptyOrderHistoryState()
        } else {
            OrderHistoryList(
                orders = uiState.orders,
                onOrderClick = onNavigateToOrderDetails
            )
        }
    }
}

@Composable
private fun EmptyOrderHistoryState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.History,
            contentDescription = "No Order History",
            modifier = Modifier.size(120.dp),
            tint = WarmGrey400
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Chưa có đơn hàng nào",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = WarmGrey700,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Lịch sử các đơn hàng của bạn sẽ hiển thị ở đây",
            fontSize = 16.sp,
            color = WarmGrey600,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun OrderHistoryList(
    orders: List<Order>,
    onOrderClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(orders) { order ->
            OrderHistoryCard(
                order = order,
                onClick = { onOrderClick(order.id) }
            )
        }
    }
}

@Composable
private fun OrderHistoryCard(
    order: Order,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Order header with status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Đơn hàng #${order.id.take(8)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarmGrey800
                )
                
                OrderStatusChipCircle(status = order.status)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Store information
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Store,
                    contentDescription = "Store",
                    tint = WarmGrey600,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = order.store?.name ?: "Cửa hàng không xác định",
                    fontSize = 14.sp,
                    color = WarmGrey600,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Pickup time if available
            order.preferredPickupTime?.let { pickupTime ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Pickup Time",
                        tint = Green600,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Nhận lúc: ${order.pickupTimeDisplay}",
                        fontSize = 14.sp,
                        color = Green600,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Order items summary
            Text(
                text = "${order.items.size} món được đặt",
                fontSize = 14.sp,
                color = WarmGrey600
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Order total and date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
                        .format(order.totalAmount),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Green600
                )
                
                Text(
                    text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        .format(order.createdAt),
                    fontSize = 12.sp,
                    color = WarmGrey600
                )
            }
        }
    }
}