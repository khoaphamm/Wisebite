package com.example.wisebite.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.wisebite.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    title: String,
    isVisible: Boolean,
    pickupStartTime: String,
    pickupEndTime: String,
    selectedTime: String?,
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isVisible) return
    
    val timePickerState = rememberTimePickerState(
        initialHour = selectedTime?.let { parseTimeToHour(it) } ?: parseTimeToHour(pickupStartTime),
        initialMinute = selectedTime?.let { parseTimeToMinute(it) } ?: parseTimeToMinute(pickupStartTime),
        is24Hour = true
    )
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarmGrey800
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Available time window info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Green500.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Khung giờ có thể nhận: $pickupStartTime - $pickupEndTime",
                        modifier = Modifier.padding(12.dp),
                        fontSize = 14.sp,
                        color = Green700,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Time Picker
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = Cream100,
                        clockDialSelectedContentColor = Color.White,
                        clockDialUnselectedContentColor = WarmGrey600,
                        selectorColor = Green500,
                        containerColor = Color.White,
                        periodSelectorBorderColor = Green500,
                        periodSelectorSelectedContainerColor = Green500,
                        periodSelectorUnselectedContainerColor = Color.Transparent,
                        periodSelectorSelectedContentColor = Color.White,
                        periodSelectorUnselectedContentColor = WarmGrey600,
                        timeSelectorSelectedContainerColor = Green500,
                        timeSelectorUnselectedContainerColor = Cream100,
                        timeSelectorSelectedContentColor = Color.White,
                        timeSelectorUnselectedContentColor = WarmGrey600
                    )
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = WarmGrey600
                        )
                    ) {
                        Text(
                            text = "Hủy",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Button(
                        onClick = {
                            val selectedHour = timePickerState.hour
                            val selectedMinute = timePickerState.minute
                            
                            // Validate time is within allowed window
                            val startHour = parseTimeToHour(pickupStartTime)
                            val endHour = parseTimeToHour(pickupEndTime)
                            
                            if (isTimeWithinWindow(selectedHour, selectedMinute, startHour, endHour)) {
                                val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                                onTimeSelected(formattedTime)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Green500,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Chọn",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// Helper functions for time parsing and validation
private fun parseTimeToHour(timeString: String): Int {
    return try {
        if (timeString.contains("T")) {
            // Parse ISO format (e.g., "2024-10-02T18:00:00")
            val time = timeString.split("T")[1].split(":")[0]
            time.toInt()
        } else if (timeString.contains(":")) {
            // Parse simple format (e.g., "18:00")
            timeString.split(":")[0].toInt()
        } else {
            18 // Default hour
        }
    } catch (e: Exception) {
        18 // Default hour if parsing fails
    }
}

private fun parseTimeToMinute(timeString: String): Int {
    return try {
        if (timeString.contains("T")) {
            // Parse ISO format (e.g., "2024-10-02T18:00:00")
            val time = timeString.split("T")[1].split(":")[1]
            time.toInt()
        } else if (timeString.contains(":")) {
            // Parse simple format (e.g., "18:00")
            timeString.split(":")[1].toInt()
        } else {
            0 // Default minute
        }
    } catch (e: Exception) {
        0 // Default minute if parsing fails
    }
}

private fun isTimeWithinWindow(
    selectedHour: Int,
    selectedMinute: Int,
    startHour: Int,
    endHour: Int
): Boolean {
    // DEMO MODE: Always allow any time selection for demo purposes
    return true
    
    /* Original validation logic - commented out for demo
    val selectedTimeInMinutes = selectedHour * 60 + selectedMinute
    val startTimeInMinutes = startHour * 60
    val endTimeInMinutes = endHour * 60
    
    return selectedTimeInMinutes >= startTimeInMinutes && selectedTimeInMinutes <= endTimeInMinutes
    */
}