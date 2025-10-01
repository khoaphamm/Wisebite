package com.example.wisebitemerchant.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

@Composable
fun DashboardScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Store,
            contentDescription = "Store",
            modifier = Modifier.size(80.dp),
            tint = Orange600
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Chào mừng đến với WiseBite Merchant!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Orange600,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Dashboard sẽ được phát triển tiếp trong các phiên bản sau",
            fontSize = 16.sp,
            color = WarmGrey600,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Tính năng sắp ra mắt:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                val features = listOf(
                    "• Quản lý cửa hàng",
                    "• Tạo và quản lý surprise bags",
                    "• Xem đơn hàng",
                    "• Thống kê doanh thu",
                    "• Quản lý thông báo"
                )
                
                features.forEach { feature ->
                    Text(
                        text = feature,
                        fontSize = 14.sp,
                        color = WarmGrey600,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    WisebiteMerchantTheme {
        DashboardScreen()
    }
}