package com.example.wisebitemerchant.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wisebitemerchant.ui.theme.Orange600
import com.example.wisebitemerchant.ui.theme.WarmGrey600
import com.example.wisebitemerchant.ui.theme.WisebiteMerchantTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen() {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Mới", "Đang xử lý", "Hoàn thành", "Đã hủy")
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Quản lý đơn hàng",
                    fontWeight = FontWeight.Bold
                )
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
        when (selectedTabIndex) {
            0 -> NewOrdersContent()
            1 -> ProcessingOrdersContent()
            2 -> CompletedOrdersContent()
            3 -> CancelledOrdersContent()
        }
    }
}

@Composable
fun NewOrdersContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Receipt,
                contentDescription = "Orders",
                modifier = Modifier.size(64.dp),
                tint = Orange600
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Chưa có đơn hàng mới",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Orange600
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Đơn hàng mới sẽ xuất hiện ở đây",
                fontSize = 14.sp,
                color = WarmGrey600,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ProcessingOrdersContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Đơn hàng đang xử lý",
            fontSize = 16.sp,
            color = WarmGrey600
        )
    }
}

@Composable
fun CompletedOrdersContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Đơn hàng đã hoàn thành",
            fontSize = 16.sp,
            color = WarmGrey600
        )
    }
}

@Composable
fun CancelledOrdersContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Đơn hàng đã hủy",
            fontSize = 16.sp,
            color = WarmGrey600
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OrdersScreenPreview() {
    WisebiteMerchantTheme {
        OrdersScreen()
    }
}