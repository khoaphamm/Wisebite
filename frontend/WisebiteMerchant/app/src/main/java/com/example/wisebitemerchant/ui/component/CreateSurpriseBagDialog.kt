package com.example.wisebitemerchant.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Money
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.wisebitemerchant.data.model.SurpriseBagCategories
import com.example.wisebitemerchant.data.model.SurpriseBagTimeWindows
import com.example.wisebitemerchant.ui.theme.Orange600
import com.example.wisebitemerchant.ui.theme.WarmGrey600
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSurpriseBagDialog(
    isCreating: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        description: String?,
        category: String,
        originalValue: Double,
        discountedPrice: Double,
        quantity: Int,
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory: String by remember { mutableStateOf(SurpriseBagCategories.COMBO) }
    var originalValueText by remember { mutableStateOf("") }
    var discountedPriceText by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("1") }
    var pickupStartHour by remember { mutableIntStateOf(14) } // 2 PM
    var pickupEndHour by remember { mutableIntStateOf(20) }   // 8 PM
    
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"))
    
    // Calculate discount percentage
    val originalValue = originalValueText.toDoubleOrNull() ?: 0.0
    val discountedPrice = discountedPriceText.toDoubleOrNull() ?: 0.0
    val discountPercentage = if (originalValue > 0) {
        ((originalValue - discountedPrice) / originalValue) * 100
    } else 0.0
    
    // Validation
    val isValid = name.isNotBlank() &&
            originalValue > 0 &&
            discountedPrice > 0 &&
            discountedPrice < originalValue &&
            discountPercentage >= SurpriseBagTimeWindows.MIN_DISCOUNT_PERCENTAGE &&
            quantityText.toIntOrNull() != null &&
            quantityText.toInt() > 0
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Text(
                    text = "Tạo Surprise Bag",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên túi bất ngờ") },
                    placeholder = { Text("VD: Túi Fresh Combo") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Description field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Mô tả (không bắt buộc)") },
                    placeholder = { Text("VD: Combo tươi sống từ cửa hàng") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Category selection
                Text(
                    text = "Danh mục",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                SurpriseBagCategories.ALL_CATEGORIES.forEach { category ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (category == selectedCategory),
                                onClick = { selectedCategory = category }
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (category == selectedCategory),
                            onClick = { selectedCategory = category }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = SurpriseBagCategories.getCategoryDisplayName(category),
                            fontSize = 14.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Pricing section
                Text(
                    text = "Giá cả",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = originalValueText,
                        onValueChange = { originalValueText = it },
                        label = { Text("Giá gốc") },
                        placeholder = { Text("100000") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        suffix = { Text("₫") },
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = discountedPriceText,
                        onValueChange = { discountedPriceText = it },
                        label = { Text("Giá bán") },
                        placeholder = { Text("50000") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        suffix = { Text("₫") },
                        singleLine = true
                    )
                }
                
                // Discount information
                if (originalValue > 0 && discountedPrice > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (discountPercentage >= SurpriseBagTimeWindows.MIN_DISCOUNT_PERCENTAGE) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.errorContainer
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "Giảm giá: ${discountPercentage.toInt()}%",
                                fontWeight = FontWeight.Medium,
                                color = if (discountPercentage >= SurpriseBagTimeWindows.MIN_DISCOUNT_PERCENTAGE) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onErrorContainer
                                }
                            )
                            if (discountPercentage < SurpriseBagTimeWindows.MIN_DISCOUNT_PERCENTAGE) {
                                Text(
                                    text = "Cần giảm tối thiểu ${SurpriseBagTimeWindows.MIN_DISCOUNT_PERCENTAGE.toInt()}%",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Quantity
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text("Số lượng") },
                    placeholder = { Text("1") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Time selection
                Text(
                    text = "Thời gian nhận hàng",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Start time
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Từ",
                            fontSize = 12.sp,
                            color = WarmGrey600
                        )
                        
                        var expanded by remember { mutableStateOf(false) }
                        
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = "${pickupStartHour}:00",
                                onValueChange = { },
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor()
                            )
                            
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                (SurpriseBagTimeWindows.PICKUP_START_HOUR..SurpriseBagTimeWindows.PICKUP_END_HOUR-1).forEach { hour ->
                                    DropdownMenuItem(
                                        text = { Text("${hour}:00") },
                                        onClick = {
                                            pickupStartHour = hour
                                            if (pickupEndHour <= hour) {
                                                pickupEndHour = minOf(hour + 1, SurpriseBagTimeWindows.PICKUP_END_HOUR)
                                            }
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    Text(
                        text = "-",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    // End time
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Đến",
                            fontSize = 12.sp,
                            color = WarmGrey600
                        )
                        
                        var expanded by remember { mutableStateOf(false) }
                        
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = "${pickupEndHour}:00",
                                onValueChange = { },
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor()
                            )
                            
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                (pickupStartHour+1..SurpriseBagTimeWindows.PICKUP_END_HOUR).forEach { hour ->
                                    DropdownMenuItem(
                                        text = { Text("${hour}:00") },
                                        onClick = {
                                            pickupEndHour = hour
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !isCreating
                    ) {
                        Text("Hủy")
                    }
                    
                    Button(
                        onClick = {
                            val startTime = LocalDateTime.of(
                                LocalDateTime.now().toLocalDate(),
                                LocalTime.of(pickupStartHour, 0)
                            )
                            val endTime = LocalDateTime.of(
                                LocalDateTime.now().toLocalDate(),
                                LocalTime.of(pickupEndHour, 0)
                            )
                            
                            onConfirm(
                                name.trim(),
                                if (description.isBlank()) null else description.trim(),
                                selectedCategory,
                                originalValue,
                                discountedPrice,
                                quantityText.toInt(),
                                startTime,
                                endTime
                            )
                        },
                        modifier = Modifier.weight(1f),
                        enabled = isValid && !isCreating
                    ) {
                        if (isCreating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Tạo")
                        }
                    }
                }
            }
        }
    }
}