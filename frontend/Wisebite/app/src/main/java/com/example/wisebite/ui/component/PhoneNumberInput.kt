package com.example.wisebite.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

data class CountryCode(
    val name: String,
    val code: String,
    val flag: String,
    val dialCode: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneNumberInput(
    label: String,
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    selectedCountry: CountryCode,
    onCountryChange: (CountryCode) -> Unit,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    var showCountryPicker by remember { mutableStateOf(false) }
    
    val commonCountries = listOf(
        CountryCode("Vietnam", "VN", "🇻🇳", "+84"),
        CountryCode("United States", "US", "🇺🇸", "+1"),
        CountryCode("United Kingdom", "GB", "🇬🇧", "+44"),
        CountryCode("Singapore", "SG", "🇸🇬", "+65"),
        CountryCode("Thailand", "TH", "🇹🇭", "+66"),
        CountryCode("Malaysia", "MY", "🇲🇾", "+60"),
        CountryCode("Australia", "AU", "🇦🇺", "+61"),
        CountryCode("Canada", "CA", "🇨🇦", "+1"),
        CountryCode("Japan", "JP", "🇯🇵", "+81"),
        CountryCode("South Korea", "KR", "🇰🇷", "+82")
    )
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Country Code Picker - matching the SignupScreen style
            Row(
                modifier = Modifier
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(androidx.compose.material3.MaterialTheme.colorScheme.surface)
                    .clickable { showCountryPicker = true }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = selectedCountry.flag, fontSize = 20.sp)
                Text(
                    text = selectedCountry.dialCode, 
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Thay đổi mã quốc gia",
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Phone Number Field - matching the WisebiteInputField style
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = onPhoneNumberChange,
                modifier = Modifier.weight(1f),
                placeholder = { 
                    Text(
                        text = when (selectedCountry.code) {
                            "VN" -> "369833705"
                            "US", "CA" -> "555-0123"
                            "GB" -> "7700 900123"
                            "SG" -> "8123 4567"
                            else -> "123456789"
                        },
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = errorMessage != null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                    focusedBorderColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    cursorColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    focusedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
                    errorBorderColor = androidx.compose.material3.MaterialTheme.colorScheme.error
                )
            )
        }
        
        // Error message
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        
        // Show full formatted number
        if (phoneNumber.isNotEmpty()) {
            Text(
                text = "Số đầy đủ: ${selectedCountry.dialCode}${phoneNumber}",
                fontSize = 11.sp,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
    
    // Country Picker Dialog
    if (showCountryPicker) {
        Dialog(onDismissRequest = { showCountryPicker = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .clip(RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = androidx.compose.ui.graphics.Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Chọn quốc gia",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    LazyColumn {
                        items(commonCountries) { country ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onCountryChange(country)
                                        showCountryPicker = false
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = country.flag,
                                    fontSize = 24.sp,
                                    modifier = Modifier.padding(end = 16.dp)
                                )
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = country.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = country.dialCode,
                                        fontSize = 14.sp,
                                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                if (selectedCountry.code == country.code) {
                                    Icon(
                                        imageVector = androidx.compose.material.icons.Icons.Default.Check,
                                        contentDescription = "Đã chọn",
                                        tint = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}