package com.example.wisebitemerchant.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wisebitemerchant.data.model.FoodItem
import com.example.wisebitemerchant.ui.theme.*
import java.text.NumberFormat
import java.util.*

@Composable
fun SurplusMarkingDialog(
    foodItem: FoodItem,
    onDismiss: () -> Unit,
    onConfirm: (surplusQuantity: Int, discountPercentage: Double, surplusPrice: Double) -> Unit
) {
    var surplusQuantity by remember { mutableStateOf("") }
    var discountPercentage by remember { mutableStateOf("30") }
    var calculatedPrice by remember { mutableStateOf(0.0) }
    
    // Calculate surplus price when discount changes
    LaunchedEffect(discountPercentage) {
        val discount = discountPercentage.toDoubleOrNull() ?: 0.0
        calculatedPrice = foodItem.standardPrice * (1 - discount / 100)
    }
    
    val maxAvailable = foodItem.availableQuantity
    val surplusQty = surplusQuantity.toIntOrNull() ?: 0
    val discountPct = discountPercentage.toDoubleOrNull() ?: 0.0
    
    val isValidQuantity = surplusQty > 0 && surplusQty <= maxAvailable
    val isValidDiscount = discountPct > 0 && discountPct <= 100
    val isFormValid = isValidQuantity && isValidDiscount
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Đánh dấu Surplus",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Product info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Orange50),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = foodItem.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Giá gốc: ${formatPrice(foodItem.standardPrice)}",
                            fontSize = 14.sp,
                            color = WarmGrey600
                        )
                        Text(
                            text = "Có sẵn: ${foodItem.availableQuantity} ${foodItem.unit}",
                            fontSize = 14.sp,
                            color = WarmGrey600
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Surplus Quantity Input
                OutlinedTextField(
                    value = surplusQuantity,
                    onValueChange = { surplusQuantity = it },
                    label = { Text("Số lượng surplus") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    suffix = { Text(foodItem.unit) },
                    singleLine = true,
                    isError = surplusQuantity.isNotBlank() && !isValidQuantity,
                    supportingText = {
                        if (surplusQuantity.isNotBlank() && !isValidQuantity) {
                            Text(
                                text = "Phải từ 1 đến $maxAvailable ${foodItem.unit}",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp
                            )
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Discount Percentage Input
                OutlinedTextField(
                    value = discountPercentage,
                    onValueChange = { discountPercentage = it },
                    label = { Text("Phần trăm giảm giá") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    suffix = { Text("%") },
                    singleLine = true,
                    isError = discountPercentage.isNotBlank() && !isValidDiscount,
                    supportingText = {
                        if (discountPercentage.isNotBlank() && !isValidDiscount) {
                            Text(
                                text = "Phải từ 1% đến 100%",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp
                            )
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Calculated Price Display
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Green50),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Giá surplus",
                            fontSize = 14.sp,
                            color = WarmGrey600
                        )
                        Text(
                            text = formatPrice(calculatedPrice),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Green600
                        )
                        if (discountPct > 0) {
                            Text(
                                text = "Tiết kiệm: ${formatPrice(foodItem.standardPrice - calculatedPrice)}",
                                fontSize = 12.sp,
                                color = Green600
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Summary
                if (isFormValid) {
                    Text(
                        text = "• $surplusQty ${foodItem.unit} sẽ được đánh dấu surplus\n" +
                                "• Giá giảm từ ${formatPrice(foodItem.standardPrice)} xuống ${formatPrice(calculatedPrice)}\n" +
                                "• Khách hàng tiết kiệm ${discountPct.toInt()}%",
                        fontSize = 12.sp,
                        color = WarmGrey600,
                        textAlign = TextAlign.Start
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(surplusQty, discountPct / 100, calculatedPrice)
                },
                enabled = isFormValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Orange600
                )
            ) {
                Text("Xác nhận")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

private fun formatPrice(price: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    return formatter.format(price).replace("₫", "đ")
}

@Preview(showBackground = true)
@Composable
fun SurplusMarkingDialogPreview() {
    WisebiteMerchantTheme {
        SurplusMarkingDialog(
            foodItem = FoodItem(
                id = "1",
                name = "Thịt bò tươi Úc",
                standardPrice = 250000.0,
                totalQuantity = 10,
                availableQuantity = 7,
                unit = "kg"
            ),
            onDismiss = {},
            onConfirm = { _, _, _ -> }
        )
    }
}