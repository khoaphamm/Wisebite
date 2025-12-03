package com.example.wisebite.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wisebite.data.model.SurpriseBag
import com.example.wisebite.ui.theme.*
import com.example.wisebite.ui.components.ProfessionalAsyncImage

@Composable
fun ImagePlaceholder(
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.ImageSearch,
    backgroundColor: Color = Color(0xFFF5F5F5),
    iconColor: Color = Color(0xFFBDBDBD),
    cornerRadius: Int = 12
) {
    Box(
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor,
                        backgroundColor.copy(alpha = 0.8f)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius.dp)
            )
            .clip(RoundedCornerShape(cornerRadius.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Image placeholder",
            modifier = Modifier.size(32.dp),
            tint = iconColor
        )
    }
}

@Composable
fun ProfessionalBadge(
    text: String,
    backgroundColor: Color = Red500.copy(alpha = 0.1f),
    textColor: Color = Red500,
    icon: ImageVector? = null
) {
    Row(
        modifier = Modifier
            .background(
                backgroundColor,
                RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = textColor
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun CompactInfoRow(
    icon: ImageVector,
    text: String,
    iconColor: Color = WarmGrey600,
    textColor: Color = WarmGrey600
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = iconColor
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            fontSize = 11.sp,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ProfessionalSurpriseBagCard(
    bag: SurpriseBag,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black.copy(alpha = 0.08f)
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // Image Section with Overlay Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                // Real Image with fallback
                ProfessionalAsyncImage(
                    imageUrl = bag.imageUrl,
                    contentDescription = bag.name,
                    height = 140.dp
                )
                
                // Top-right discount badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                ) {
                    ProfessionalBadge(
                        text = "-${bag.formattedDiscountPercentage}",
                        backgroundColor = Red500,
                        textColor = Color.White,
                        icon = Icons.Default.LocalOffer
                    )
                }
                
                // Store info in bottom-left
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Store logo placeholder
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                Color.White,
                                CircleShape
                            )
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = "Store logo",
                            modifier = Modifier.size(20.dp),
                            tint = Green600
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Column {
                        Text(
                            text = bag.store?.name ?: "Unknown Store",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = androidx.compose.ui.text.TextStyle(
                                shadow = androidx.compose.ui.graphics.Shadow(
                                    color = Color.Black.copy(alpha = 0.5f),
                                    offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                                    blurRadius = 2f
                                )
                            )
                        )
                        
                        ProfessionalBadge(
                            text = bag.categoryDisplayName,
                            backgroundColor = Orange600.copy(alpha = 0.9f),
                            textColor = Color.White
                        )
                    }
                }
            }
            
            // Content Section
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Bag name and description
                Text(
                    text = bag.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
                
                bag.description?.let { desc ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = desc,
                        fontSize = 12.sp,
                        color = WarmGrey600,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Price Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = bag.formattedDiscountedPrice,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Green600
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = bag.formattedOriginalPrice,
                                fontSize = 12.sp,
                                color = WarmGrey500,
                                style = androidx.compose.ui.text.TextStyle(
                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                )
                            )
                        }
                        
                        Text(
                            text = "Tiết kiệm ${String.format("%,.0f", bag.originalValue - bag.discountedPrice)}đ",
                            fontSize = 10.sp,
                            color = Green600,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .background(
                                    Green100,
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        if (bag.quantityAvailable <= 5 && bag.quantityAvailable > 0) {
                            ProfessionalBadge(
                                text = "Sắp hết!",
                                backgroundColor = Orange100,
                                textColor = Orange600,
                                icon = Icons.Default.Warning
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        
                        Text(
                            text = bag.quantityDisplay,
                            fontSize = 11.sp,
                            color = if (bag.quantityAvailable > 0) WarmGrey600 else Red500,
                            fontWeight = if (bag.quantityAvailable > 0) FontWeight.Normal else FontWeight.Medium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Info Section
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CompactInfoRow(
                        icon = Icons.Default.AccessTime,
                        text = bag.pickupTimeDisplay,
                        iconColor = Orange600
                    )
                    
                    bag.store?.let { store ->
                        CompactInfoRow(
                            icon = Icons.Default.LocationOn,
                            text = store.displayAddress,
                            iconColor = Red500
                        )
                    }
                }
            }
        }
    }
}

// Large version for featured bags
@Composable
fun ProfessionalSurpriseBagCardLarge(
    bag: SurpriseBag,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(280.dp)
            .clickable { onClick() }
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color.Black.copy(alpha = 0.1f)
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // Hero Image Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                // Real Image with fallback
                ProfessionalAsyncImage(
                    imageUrl = bag.imageUrl,
                    contentDescription = bag.name,
                    height = 180.dp
                )
                
                // Discount badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    ProfessionalBadge(
                        text = "-${bag.formattedDiscountPercentage}",
                        backgroundColor = Red500,
                        textColor = Color.White,
                        icon = Icons.Default.LocalOffer
                    )
                }
                
                // Store info overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White, CircleShape)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = "Store logo",
                                modifier = Modifier.size(24.dp),
                                tint = Green600
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column {
                            Text(
                                text = bag.store?.name ?: "Unknown Store",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                style = androidx.compose.ui.text.TextStyle(
                                    shadow = androidx.compose.ui.graphics.Shadow(
                                        color = Color.Black.copy(alpha = 0.5f),
                                        offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                                        blurRadius = 2f
                                    )
                                )
                            )
                            ProfessionalBadge(
                                text = bag.categoryDisplayName,
                                backgroundColor = Orange600.copy(alpha = 0.9f),
                                textColor = Color.White
                            )
                        }
                    }
                }
            }
            
            // Content
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = bag.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 22.sp
                )
                
                bag.description?.let { desc ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = desc,
                        fontSize = 14.sp,
                        color = WarmGrey600,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = bag.formattedDiscountedPrice,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Green600
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = bag.formattedOriginalPrice,
                                fontSize = 14.sp,
                                color = WarmGrey500,
                                style = androidx.compose.ui.text.TextStyle(
                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                )
                            )
                        }
                        Text(
                            text = "Tiết kiệm ${String.format("%,.0f", bag.originalValue - bag.discountedPrice)}đ",
                            fontSize = 11.sp,
                            color = Green600,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .background(Green100, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.End) {
                        if (bag.quantityAvailable <= 5 && bag.quantityAvailable > 0) {
                            ProfessionalBadge(
                                text = "Sắp hết!",
                                backgroundColor = Orange100,
                                textColor = Orange600
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        Text(
                            text = bag.quantityDisplay,
                            fontSize = 12.sp,
                            color = if (bag.quantityAvailable > 0) WarmGrey600 else Red500
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CompactInfoRow(
                        icon = Icons.Default.AccessTime,
                        text = bag.pickupTimeDisplay,
                        iconColor = Orange600
                    )
                    
                    bag.store?.let { store ->
                        CompactInfoRow(
                            icon = Icons.Default.LocationOn,
                            text = store.displayAddress,
                            iconColor = Red500
                        )
                    }
                }
            }
        }
    }
}