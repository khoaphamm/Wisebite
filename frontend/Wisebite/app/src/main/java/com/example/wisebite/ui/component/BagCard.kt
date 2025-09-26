package com.example.wisebite.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wisebite.ui.theme.*

data class BagInfo(
    val title: String = "Blind bag",
    val originalPrice: String = "30.000đ",
    val discountedPrice: String = "18.000đ",
    val location: String = "45 Lê Lợi, Quận 1",
    val pickupTime: String = "14:00 - 16:00",
    val quantity: String = "Còn 3 túi",
    val description: String = "Blind bag từ Cơm Tấm Sài Gòn chứa đựng những món ăn ngon với giá ưu đãi. Nội dung túi sẽ là bất ngờ khi bạn nhận được!"
)

@Composable
fun BagCard(
    bagInfo: BagInfo = BagInfo(),
    onOrderClick: () -> Unit = {}
) {
    // State management for user selections
    var selectedPickupMethod by remember { mutableStateOf("Giao hàng") }
    var selectedPaymentMethod by remember { mutableStateOf("Thanh toán bằng ví MoMo") }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header with location
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = WarmGrey600,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = bagInfo.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = WarmGrey600,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title
            Text(
                text = bagInfo.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = WarmGrey900,
                fontSize = 24.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Price section
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = bagInfo.discountedPrice,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = WarmGrey900,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = bagInfo.originalPrice,
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarmGrey500,
                    textDecoration = TextDecoration.LineThrough,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Time and quantity info
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = "Time",
                    tint = WarmGrey600,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Thời gian kết thúc: ${bagInfo.pickupTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = WarmGrey600,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.People,
                    contentDescription = "Quantity",
                    tint = WarmGrey600,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = bagInfo.quantity,
                    style = MaterialTheme.typography.bodySmall,
                    color = WarmGrey600,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description section
            Text(
                text = "Thông tin blind bag",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = WarmGrey900,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = bagInfo.description,
                style = MaterialTheme.typography.bodyMedium,
                color = WarmGrey700,
                lineHeight = 20.sp,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Pickup method section
            Text(
                text = "Phương thức nhận hàng",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = WarmGrey900,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Self pickup button
                OutlinedButton(
                    onClick = { selectedPickupMethod = "Tự đến lấy" },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (selectedPickupMethod == "Tự đến lấy") Green500 else WarmGrey700,
                        containerColor = if (selectedPickupMethod == "Tự đến lấy") Green500.copy(alpha = 0.1f) else Color.Transparent
                    ),
                    border = BorderStroke(
                        1.dp, 
                        if (selectedPickupMethod == "Tự đến lấy") Green500 else WarmGrey400
                    )
                ) {
                    Text(
                        text = "Tự đến lấy",
                        fontSize = 14.sp,
                        fontWeight = if (selectedPickupMethod == "Tự đến lấy") FontWeight.Medium else FontWeight.Normal
                    )
                }

                // Delivery button
                OutlinedButton(
                    onClick = { selectedPickupMethod = "Giao hàng" },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (selectedPickupMethod == "Giao hàng") Green500 else WarmGrey700,
                        containerColor = if (selectedPickupMethod == "Giao hàng") Green500.copy(alpha = 0.1f) else Color.Transparent
                    ),
                    border = BorderStroke(
                        1.dp, 
                        if (selectedPickupMethod == "Giao hàng") Green500 else WarmGrey400
                    )
                ) {
                    Text(
                        text = "Giao hàng",
                        fontSize = 14.sp,
                        fontWeight = if (selectedPickupMethod == "Giao hàng") FontWeight.Medium else FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Payment method section
            Text(
                text = "Phương thức thanh toán",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = WarmGrey900,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Payment options
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PaymentOptionRow(
                    text = "Thanh toán khi nhận hàng",
                    isSelected = selectedPaymentMethod == "Thanh toán khi nhận hàng",
                    onClick = { selectedPaymentMethod = "Thanh toán khi nhận hàng" }
                )
                PaymentOptionRow(
                    text = "Thanh toán bằng ngân hàng",
                    isSelected = selectedPaymentMethod == "Thanh toán bằng ngân hàng",
                    onClick = { selectedPaymentMethod = "Thanh toán bằng ngân hàng" }
                )
                PaymentOptionRow(
                    text = "Thanh toán bằng ví MoMo",
                    isSelected = selectedPaymentMethod == "Thanh toán bằng ví MoMo",
                    onClick = { selectedPaymentMethod = "Thanh toán bằng ví MoMo" }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bottom section with total and order button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Tổng cộng",
                        style = MaterialTheme.typography.bodySmall,
                        color = WarmGrey600,
                        fontSize = 12.sp
                    )
                    Text(
                        text = bagInfo.discountedPrice,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = WarmGrey900,
                        fontSize = 18.sp
                    )
                }

                Button(
                    onClick = onOrderClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green500,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(
                        text = "Đặt ngay",
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentOptionRow(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    iconColor: Color? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = Green500,
                unselectedColor = WarmGrey400
            )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = WarmGrey700,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
        if (iconColor != null) {
            // Payment method icon
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = iconColor,
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}