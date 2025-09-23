package com.example.wisebite.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wisebite.ui.component.BagCard
import com.example.wisebite.ui.component.BagInfo
import com.example.wisebite.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BagDetailsScreen(
    onBackClick: () -> Unit = {},
    onOrderClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Users",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Green700,
                    fontSize = 28.sp
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Green700
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            BagCard(
                bagInfo = BagInfo(
                    title = "Blind bag",
                    originalPrice = "30.000đ",
                    discountedPrice = "18.000đ",
                    location = "45 Lê Lợi, Quận 1",
                    pickupTime = "14:00 - 16:00",
                    quantity = "Còn 3 túi",
                    description = "Blind bag từ Cơm Tấm Sài Gòn chứa đựng những món ăn ngon với giá ưu đãi. Nội dung túi sẽ là bất ngờ khi bạn nhận được!"
                ),
                onOrderClick = onOrderClick
            )
            
            // Add some bottom padding
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}