package com.example.wisebite.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wisebite.data.model.OrderStatus
import com.example.wisebite.ui.theme.*

@Composable
fun OrderStatusChip(status: OrderStatus) {
    val (backgroundColor, textColor) = when (status) {
        OrderStatus.PENDING -> Pair(Orange100, Orange600)
        OrderStatus.CONFIRMED -> Pair(Blue100, Blue600)
        OrderStatus.PREPARING -> Pair(Purple100, Purple600)
        OrderStatus.READY_FOR_PICKUP -> Pair(Green100, Green600)
        OrderStatus.COMPLETED -> Pair(Green200, Green800)
        OrderStatus.CANCELLED -> Pair(Red100, Red600)
        OrderStatus.PENDING_PAYMENT -> Pair(Orange100, Orange600)
        OrderStatus.AWAITING_PICKUP -> Pair(Green200, Green700)
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

@Composable
fun OrderStatusChipCircle(status: OrderStatus) {
    val (backgroundColor, contentColor, text) = when (status) {
        OrderStatus.PENDING -> Triple(Orange100, Orange600, "Chờ xử lý")
        OrderStatus.CONFIRMED -> Triple(Blue100, Blue600, "Đã xác nhận")
        OrderStatus.PREPARING -> Triple(Purple100, Purple600, "Đang chuẩn bị")
        OrderStatus.READY_FOR_PICKUP -> Triple(Green100, Green600, "Sẵn sàng")
        OrderStatus.AWAITING_PICKUP -> Triple(Green200, Green700, "Chờ nhận")
        OrderStatus.COMPLETED -> Triple(Green200, Green800, "Hoàn thành")
        OrderStatus.CANCELLED -> Triple(Red100, Red600, "Đã hủy")
        OrderStatus.PENDING_PAYMENT -> Triple(Orange100, Orange600, "Chờ thanh toán")
    }
    
    Surface(
        modifier = Modifier.clip(CircleShape),
        color = backgroundColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = contentColor
        )
    }
}