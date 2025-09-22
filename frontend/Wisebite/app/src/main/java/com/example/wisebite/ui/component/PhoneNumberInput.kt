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
    
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Country Code Selector
            Card(
                modifier = Modifier
                    .clickable { showCountryPicker = true }
                    .padding(end = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedCountry.flag,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = selectedCountry.dialCode,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select country",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            // Phone Number Input
            Column(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = onPhoneNumberChange,
                    label = { Text(label) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMessage != null,
                    singleLine = true,
                    placeholder = { 
                        Text(
                            text = when (selectedCountry.code) {
                                "VN" -> "369833705"
                                "US", "CA" -> "555-0123"
                                "GB" -> "7700 900123"
                                "SG" -> "8123 4567"
                                else -> "123456789"
                            }
                        ) 
                    }
                )
                
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }
        }
        
        // Show full formatted number
        if (phoneNumber.isNotEmpty()) {
            Text(
                text = "Full number: ${selectedCountry.dialCode}${phoneNumber}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, start = 16.dp)
            )
        }
    }
    
    // Country Picker Dialog
    if (showCountryPicker) {
        Dialog(onDismissRequest = { showCountryPicker = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Column {
                    Text(
                        text = "Select Country",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
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
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = country.flag,
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = country.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = country.dialCode,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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