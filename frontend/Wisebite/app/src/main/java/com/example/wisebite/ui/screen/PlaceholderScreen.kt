package com.example.wisebite.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wisebite.ui.component.SimpleHeader
import com.example.wisebite.ui.theme.*

@Composable
fun PlaceholderScreen(
    title: String,
    subtitle: String = "Tính năng này đang được phát triển",
    onBackClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
    ) {
        SimpleHeader(
            title = title,
            onBackClick = onBackClick
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Content in center
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Construction,
                contentDescription = "Under construction",
                modifier = Modifier.size(80.dp),
                tint = Green500
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Đang phát triển",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Green500.copy(alpha = 0.1f)
                )
            ) {
                Text(
                    text = "Tính năng này sẽ sớm có mặt trong các phiên bản tiếp theo của WiseBite. Cảm ơn bạn đã kiên nhẫn chờ đợi!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Green700,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp),
                    lineHeight = 20.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
    }
}

// Specific placeholder screens
@Composable
fun NotificationsScreen(onBackClick: () -> Unit = {}) {
    PlaceholderScreen(
        title = "Thông báo",
        subtitle = "Quản lý thông báo và cài đặt thông báo của bạn",
        onBackClick = onBackClick
    )
}

@Composable
fun PaymentMethodsScreen(onBackClick: () -> Unit = {}) {
    PlaceholderScreen(
        title = "Phương thức thanh toán",
        subtitle = "Thêm và quản lý các phương thức thanh toán",
        onBackClick = onBackClick
    )
}

