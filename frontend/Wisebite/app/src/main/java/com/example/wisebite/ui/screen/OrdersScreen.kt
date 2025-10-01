package com.example.wisebite.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingBag
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
import com.example.wisebite.data.model.Order
import com.example.wisebite.data.model.OrderStatus
import com.example.wisebite.ui.component.SimpleHeader
import com.example.wisebite.ui.theme.*
import com.example.wisebite.ui.viewmodel.OrderViewModel
import com.example.wisebite.util.ViewModelFactory
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    onNavigateToOrderDetails: (String) -> Unit = {},
    onNavigateToHome: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: OrderViewModel = viewModel(
        factory = ViewModelFactory.getInstance(context)
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadOrders()
    }

    // Handle error messages
    uiState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            // Show snackbar or toast here if needed
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        SimpleHeader(title = "Hoạt động")

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Green500)
            }
        } else if (uiState.orders.isEmpty()) {
            EmptyOrdersState(onStartShopping = onNavigateToHome)
        } else {
            OrdersList(
                orders = uiState.orders,
                onOrderClick = onNavigateToOrderDetails
            )
        }
    }
}

@Composable
private fun EmptyOrdersState(onStartShopping: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ShoppingBag,
            contentDescription = "No Orders",
            modifier = Modifier.size(120.dp),
            tint = WarmGrey400
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "No orders yet",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = WarmGrey700,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "When you place orders, they will appear here.",
            fontSize = 16.sp,
            color = WarmGrey600,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onStartShopping,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Green500,
                contentColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text(
                text = "Start Shopping",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun OrdersList(
    orders: List<Order>,
    onOrderClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(orders) { order ->
            OrderCard(
                order = order,
                onClick = { onOrderClick(order.id) }
            )
        }
    }
}

@Composable
private fun OrderCard(
    order: Order,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    text = "Order #${order.id.take(8)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarmGrey800
                )
                
                OrderStatusChip(status = order.status)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Store name
            Text(
                text = order.store?.name ?: "Unknown Store",
                fontSize = 14.sp,
                color = WarmGrey600,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Order items summary
            Text(
                text = "${order.items.size} item${if (order.items.size > 1) "s" else ""}",
                fontSize = 14.sp,
                color = WarmGrey600
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Total and date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = formatPrice(order.totalAmount),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Green500
                )
                
                Text(
                    text = formatDate(order.createdAt),
                    fontSize = 12.sp,
                    color = WarmGrey500
                )
            }
        }
    }
}

@Composable
fun OrderStatusChip(status: OrderStatus) {
    val (backgroundColor, textColor) = when (status) {
        OrderStatus.PENDING -> Pair(Orange100, Orange600)
        OrderStatus.CONFIRMED -> Pair(Blue100, Blue600)
        OrderStatus.PREPARING -> Pair(Purple100, Purple600)
        OrderStatus.READY_FOR_PICKUP -> Pair(Green100, Green600)
        OrderStatus.COMPLETED -> Pair(Green100, Green700)
        OrderStatus.CANCELLED -> Pair(Red100, Red600)
        OrderStatus.PENDING_PAYMENT -> Pair(Orange100, Orange600)
        OrderStatus.AWAITING_PICKUP -> Pair(Blue100, Blue600)
    }
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.displayName,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

private fun formatPrice(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    return formatter.format(amount)
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}
