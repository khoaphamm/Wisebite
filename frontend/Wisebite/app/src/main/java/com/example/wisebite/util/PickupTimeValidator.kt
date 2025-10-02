package com.example.wisebite.util

import java.text.SimpleDateFormat
import java.util.*

object PickupTimeValidator {
    
    /**
     * Validates if the selected pickup time is within the allowed window - DEMO MODE: Always valid
     */
    fun validatePickupTime(
        selectedTime: String,
        pickupStartTime: String,
        pickupEndTime: String
    ): ValidationResult {
        try {
            // DEMO MODE: Always allow any pickup time for demo purposes
            return ValidationResult(isValid = true)
            
            /* Original validation logic - commented out for demo
            val selectedHour = parseTimeToHour(selectedTime)
            val selectedMinute = parseTimeToMinute(selectedTime)
            val startHour = parseTimeToHour(pickupStartTime)
            val endHour = parseTimeToHour(pickupEndTime)
            
            val selectedTimeInMinutes = selectedHour * 60 + selectedMinute
            val startTimeInMinutes = startHour * 60
            val endTimeInMinutes = endHour * 60
            
            return when {
                selectedTimeInMinutes < startTimeInMinutes -> {
                    ValidationResult(
                        isValid = false,
                        errorMessage = "Thời gian nhận phải sau ${formatTime(startHour, 0)}"
                    )
                }
                selectedTimeInMinutes > endTimeInMinutes -> {
                    ValidationResult(
                        isValid = false,
                        errorMessage = "Thời gian nhận phải trước ${formatTime(endHour, 0)}"
                    )
                }
                else -> ValidationResult(isValid = true)
            }
            */
        } catch (e: Exception) {
            return ValidationResult(
                isValid = false,
                errorMessage = "Định dạng thời gian không hợp lệ"
            )
        }
    }
    
    /**
     * Validates if pickup time is not in the past - DEMO MODE: More lenient validation
     */
    fun validateNotInPast(selectedTime: String): ValidationResult {
        try {
            // DEMO MODE: Always allow any pickup time for demo purposes
            return ValidationResult(isValid = true)
            
            /* Original validation logic - commented out for demo
            val now = Calendar.getInstance()
            val currentHour = now.get(Calendar.HOUR_OF_DAY)
            val currentMinute = now.get(Calendar.MINUTE)
            
            val selectedHour = parseTimeToHour(selectedTime)
            val selectedMinute = parseTimeToMinute(selectedTime)
            
            val currentTimeInMinutes = currentHour * 60 + currentMinute
            val selectedTimeInMinutes = selectedHour * 60 + selectedMinute
            
            // Allow 15 minutes buffer
            val bufferMinutes = 15
            
            return if (selectedTimeInMinutes < currentTimeInMinutes + bufferMinutes) {
                ValidationResult(
                    isValid = false,
                    errorMessage = "Thời gian nhận phải sau ít nhất 15 phút từ bây giờ"
                )
            } else {
                ValidationResult(isValid = true)
            }
            */
        } catch (e: Exception) {
            return ValidationResult(
                isValid = false,
                errorMessage = "Không thể xác thực thời gian"
            )
        }
    }
    
    /**
     * Gets suggested pickup times within the allowed window
     */
    fun getSuggestedPickupTimes(
        pickupStartTime: String,
        pickupEndTime: String,
        intervalMinutes: Int = 30
    ): List<String> {
        try {
            val startHour = parseTimeToHour(pickupStartTime)
            val endHour = parseTimeToHour(pickupEndTime)
            
            val suggestions = mutableListOf<String>()
            var currentHour = startHour
            var currentMinute = 0
            
            while (currentHour < endHour || (currentHour == endHour && currentMinute == 0)) {
                suggestions.add(formatTime(currentHour, currentMinute))
                
                currentMinute += intervalMinutes
                if (currentMinute >= 60) {
                    currentHour += currentMinute / 60
                    currentMinute %= 60
                }
            }
            
            return suggestions
        } catch (e: Exception) {
            return emptyList()
        }
    }
    
    /**
     * Formats pickup time display string
     */
    fun formatPickupTimeDisplay(
        selectedTime: String,
        pickupStartTime: String,
        pickupEndTime: String
    ): String {
        return try {
            val startTime = formatTimeFromIso(pickupStartTime)
            val endTime = formatTimeFromIso(pickupEndTime)
            "Nhận lúc $selectedTime (Khung giờ: $startTime - $endTime)"
        } catch (e: Exception) {
            "Thời gian nhận: $selectedTime"
        }
    }
    
    private fun parseTimeToHour(timeString: String): Int {
        return if (timeString.contains("T")) {
            // Parse ISO format (e.g., "2024-10-02T18:00:00")
            timeString.split("T")[1].split(":")[0].toInt()
        } else if (timeString.contains(":")) {
            // Parse simple format (e.g., "18:00")
            timeString.split(":")[0].toInt()
        } else {
            throw IllegalArgumentException("Invalid time format")
        }
    }
    
    private fun parseTimeToMinute(timeString: String): Int {
        return if (timeString.contains("T")) {
            // Parse ISO format (e.g., "2024-10-02T18:00:00")
            timeString.split("T")[1].split(":")[1].toInt()
        } else if (timeString.contains(":")) {
            // Parse simple format (e.g., "18:00")
            timeString.split(":")[1].toInt()
        } else {
            0
        }
    }
    
    private fun formatTime(hour: Int, minute: Int): String {
        return String.format("%02d:%02d", hour, minute)
    }
    
    private fun formatTimeFromIso(isoTime: String): String {
        return try {
            if (isoTime.contains("T")) {
                val time = isoTime.split("T")[1].split(":")
                "${time[0]}:${time[1]}"
            } else {
                isoTime
            }
        } catch (e: Exception) {
            isoTime
        }
    }
}

data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)