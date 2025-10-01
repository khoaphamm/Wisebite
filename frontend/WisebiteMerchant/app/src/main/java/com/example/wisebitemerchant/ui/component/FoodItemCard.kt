package com.example.wisebitemerchant.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wisebitemerchant.data.model.FoodItem
import com.example.wisebitemerchant.ui.theme.*
import java.text.NumberFormat
import java.util.*

@Composable
fun FoodItemCard(
    foodItem: FoodItem,
    onEdit: () -> Unit = {},
    onMarkSurplus: () -> Unit = {},
    onUpdateInventory: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    var showDropdownMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Orange50),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with name and menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = foodItem.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (foodItem.sku != null) {
                        Text(
                            text = "SKU: ${foodItem.sku}",
                            fontSize = 12.sp,
                            color = WarmGrey600
                        )
                    }
                }
                
                Box {
                    IconButton(
                        onClick = { showDropdownMenu = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = WarmGrey600
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showDropdownMenu,
                        onDismissRequest = { showDropdownMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Chỉnh sửa") },
                            onClick = {
                                showDropdownMenu = false
                                onEdit()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Đánh dấu Surplus") },
                            onClick = {
                                showDropdownMenu = false
                                onMarkSurplus()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Cập nhật kho") },
                            onClick = {
                                showDropdownMenu = false
                                onUpdateInventory()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Xóa", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showDropdownMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }
            
            // Description
            if (foodItem.description != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = foodItem.description,
                    fontSize = 14.sp,
                    color = WarmGrey600,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Category and Type
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (foodItem.categoryName != null) {
                    Badge(
                        containerColor = Orange100,
                        contentColor = Orange600
                    ) {
                        Text(
                            text = foodItem.categoryName,
                            fontSize = 10.sp
                        )
                    }
                }
                
                Badge(
                    containerColor = if (foodItem.isFresh) Green100 else Blue100,
                    contentColor = if (foodItem.isFresh) Green600 else Blue600
                ) {
                    Text(
                        text = if (foodItem.isFresh) "Tươi sống" else "Đóng gói",
                        fontSize = 10.sp
                    )
                }
                
                if (foodItem.isMarkedForSurplus) {
                    Badge(
                        containerColor = Red100,
                        contentColor = Red600
                    ) {
                        Text(
                            text = "Surplus",
                            fontSize = 10.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Price and Inventory Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Giá bán",
                        fontSize = 12.sp,
                        color = WarmGrey600
                    )
                    Text(
                        text = formatPrice(foodItem.standardPrice),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Orange600
                    )
                    
                    if (foodItem.isMarkedForSurplus && foodItem.surplusPrice != null) {
                        Text(
                            text = "Surplus: ${formatPrice(foodItem.surplusPrice)}",
                            fontSize = 12.sp,
                            color = Red600,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Kho hàng",
                        fontSize = 12.sp,
                        color = WarmGrey600
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Available quantity indicator
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        foodItem.availableQuantity <= 0 -> Red500
                                        foodItem.availableQuantity <= 5 -> Orange600
                                        else -> Green500
                                    }
                                )
                        )
                        
                        Text(
                            text = "${foodItem.availableQuantity}/${foodItem.totalQuantity} ${foodItem.unit}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    if (foodItem.surplusQuantity > 0) {
                        Text(
                            text = "Surplus: ${foodItem.surplusQuantity} ${foodItem.unit}",
                            fontSize = 11.sp,
                            color = Red600
                        )
                    }
                }
            }
        }
    }
}

private fun formatPrice(price: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    return formatter.format(price).replace("₫", "đ")
}

@Preview(showBackground = true)
@Composable
fun FoodItemCardPreview() {
    WisebiteMerchantTheme {
        Column {
            FoodItemCard(
                foodItem = FoodItem(
                    id = "1",
                    name = "Thịt bò tươi Úc",
                    description = "Thịt bò tươi cao cấp nhập khẩu từ Úc, chất lượng A1",
                    sku = "BEEF001",
                    standardPrice = 250000.0,
                    totalQuantity = 10,
                    availableQuantity = 7,
                    surplusQuantity = 3,
                    unit = "kg",
                    categoryName = "Thịt",
                    isFresh = true,
                    isMarkedForSurplus = true,
                    surplusPrice = 175000.0
                )
            )
            
            FoodItemCard(
                foodItem = FoodItem(
                    id = "2",
                    name = "Bánh quy socola",
                    description = "Bánh quy socola thơm ngon",
                    standardPrice = 35000.0,
                    totalQuantity = 50,
                    availableQuantity = 45,
                    unit = "pack",
                    categoryName = "Bánh/Snack",
                    isFresh = false
                )
            )
        }
    }
}