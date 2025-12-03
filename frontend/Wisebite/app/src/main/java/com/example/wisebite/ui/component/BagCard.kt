package com.example.wisebite.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wisebite.ui.components.ProfessionalAsyncImage
import com.example.wisebite.ui.theme.*

// Professional helper components
@Composable
private fun ProfessionalImagePlaceholder(
    modifier: Modifier = Modifier,
    height: Int = 200
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF4CAF50).copy(alpha = 0.3f),
                        Color(0xFF81C784).copy(alpha = 0.2f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Gradient overlay for depth
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.1f)
                        ),
                        startY = 0f,
                        endY = 300f
                    )
                )
        )
        
        // Food icon with background circle
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    Color.White.copy(alpha = 0.9f),
                    CircleShape
                )
                .shadow(4.dp, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Fastfood,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(40.dp)
            )
        }
        
        // "Surprise Bag" badge in top right
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .background(
                    Color(0xFFFF6B35).copy(alpha = 0.9f),
                    RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "Surprise Bag",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ProfessionalInfoCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8F9FA)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = WarmGrey900,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
private fun ProfessionalBadge(
    text: String,
    icon: ImageVector,
    backgroundColor: Color = Color(0xFF4CAF50),
    contentColor: Color = Color.White
) {
    Row(
        modifier = Modifier
            .background(
                backgroundColor.copy(alpha = 0.1f),
                RoundedCornerShape(8.dp)
            )
            .border(
                1.dp,
                backgroundColor.copy(alpha = 0.3f),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = backgroundColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = backgroundColor
        )
    }
}

data class BagInfo(
    val title: String = "Blind bag",
    val originalPrice: String = "30.000đ",
    val discountedPrice: String = "18.000đ",
    val location: String = "45 Lê Lợi, Quận 1",
    val pickupTime: String = "14:00 - 16:00",
    val quantity: String = "Còn 3 túi",
    val description: String = "Blind bag từ Cơm Tấm Sài Gòn chứa đựng những món ăn ngon với giá ưu đãi. Nội dung túi sẽ là bất ngờ khi bạn nhận được!",
    val storeName: String = "Cơm Tấm Sài Gòn",
    val imageUrl: String? = null
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
            .padding(16.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp // Using custom shadow instead
        )
    ) {
        Column {
            // Hero Image Section - Using real images now!
            ProfessionalAsyncImage(
                imageUrl = bagInfo.imageUrl,
                contentDescription = bagInfo.title,
                height = 180.dp
            )
            
            // Content Section
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Store and Location Info with professional badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        // Store badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(
                                        Color(0xFF4CAF50).copy(alpha = 0.1f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Store,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = bagInfo.storeName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = WarmGrey900
                            )
                        }
                        
                        // Location
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = WarmGrey500,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = bagInfo.location,
                                fontSize = 12.sp,
                                color = WarmGrey600,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    
                    // Discount badge
                    val discountPercent = "40%"
                    Box(
                        modifier = Modifier
                            .background(
                                Color(0xFFFF6B35),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "-$discountPercent",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title with enhanced typography
                Text(
                    text = bagInfo.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarmGrey900,
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Price section with savings highlight
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color(0xFF4CAF50).copy(alpha = 0.05f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = bagInfo.discountedPrice,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = bagInfo.originalPrice,
                                fontSize = 16.sp,
                                color = WarmGrey500,
                                textDecoration = TextDecoration.LineThrough
                            )
                        }
                        Text(
                            text = "Tiết kiệm 12.000đ",
                            fontSize = 12.sp,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Information badges section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProfessionalBadge(
                        text = bagInfo.pickupTime,
                        icon = Icons.Default.AccessTime,
                        backgroundColor = Color(0xFF2196F3)
                    )
                    ProfessionalBadge(
                        text = bagInfo.quantity,
                        icon = Icons.Default.People,
                        backgroundColor = Color(0xFFFF9800)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Description in info card
                ProfessionalInfoCard(
                    title = "📝 Thông tin Surprise Bag"
                ) {
                    Text(
                        text = bagInfo.description,
                        fontSize = 14.sp,
                        color = WarmGrey700,
                        lineHeight = 20.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Pickup method in info card
                ProfessionalInfoCard(
                    title = "🚚 Phương thức nhận hàng"
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Self pickup button
                        ProfessionalPickupButton(
                            text = "Tự đến lấy",
                            icon = Icons.Default.Store,
                            isSelected = selectedPickupMethod == "Tự đến lấy",
                            onClick = { selectedPickupMethod = "Tự đến lấy" },
                            modifier = Modifier.weight(1f)
                        )

                        // Delivery button
                        ProfessionalPickupButton(
                            text = "Giao hàng",
                            icon = Icons.Default.DeliveryDining,
                            isSelected = selectedPickupMethod == "Giao hàng",
                            onClick = { selectedPickupMethod = "Giao hàng" },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Payment method in info card
                ProfessionalInfoCard(
                    title = "💳 Phương thức thanh toán"
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProfessionalPaymentOption(
                            text = "Thanh toán khi nhận hàng",
                            icon = Icons.Default.CreditCard,
                            isSelected = selectedPaymentMethod == "Thanh toán khi nhận hàng",
                            onClick = { selectedPaymentMethod = "Thanh toán khi nhận hàng" }
                        )
                        ProfessionalPaymentOption(
                            text = "Thanh toán bằng ngân hàng",
                            icon = Icons.Default.CreditCard,
                            isSelected = selectedPaymentMethod == "Thanh toán bằng ngân hàng",
                            onClick = { selectedPaymentMethod = "Thanh toán bằng ngân hàng" }
                        )
                        ProfessionalPaymentOption(
                            text = "Thanh toán bằng ví MoMo",
                            icon = Icons.Default.Wallet,
                            isSelected = selectedPaymentMethod == "Thanh toán bằng ví MoMo",
                            onClick = { selectedPaymentMethod = "Thanh toán bằng ví MoMo" }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Enhanced bottom section with total and order button
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4CAF50).copy(alpha = 0.05f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Tổng thanh toán",
                                fontSize = 12.sp,
                                color = WarmGrey600,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = bagInfo.discountedPrice,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                            Text(
                                text = "Đã bao gồm phí giao hàng",
                                fontSize = 10.sp,
                                color = WarmGrey500
                            )
                        }

                        Button(
                            onClick = onOrderClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50),
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 8.dp
                            ),
                            modifier = Modifier
                                .height(56.dp)
                                .widthIn(min = 140.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalOffer,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Đặt ngay",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
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

@Composable
private fun ProfessionalPickupButton(
    text: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (isSelected) Color.White else Color(0xFF4CAF50),
            containerColor = if (isSelected) Color(0xFF4CAF50) else Color.Transparent
        ),
        border = BorderStroke(
            2.dp, 
            if (isSelected) Color(0xFF4CAF50) else Color(0xFF4CAF50).copy(alpha = 0.3f)
        ),
        elevation = if (isSelected) ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp
        ) else null
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ProfessionalPaymentOption(
    text: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF4CAF50).copy(alpha = 0.1f) else Color.Transparent
        ),
        border = BorderStroke(
            1.dp,
            if (isSelected) Color(0xFF4CAF50) else WarmGrey300
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color(0xFF4CAF50),
                    unselectedColor = WarmGrey400
                )
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color(0xFF4CAF50) else WarmGrey500,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = text,
                fontSize = 14.sp,
                color = if (isSelected) WarmGrey900 else WarmGrey700,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )
        }
    }
}