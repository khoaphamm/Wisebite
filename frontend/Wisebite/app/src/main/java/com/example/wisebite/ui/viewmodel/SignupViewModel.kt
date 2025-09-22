package com.example.wisebite.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wisebite.data.model.SignupUiState
import com.example.wisebite.data.repository.AuthRepository
import com.example.wisebite.ui.component.CountryCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SignupViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState: StateFlow<SignupUiState> = _uiState.asStateFlow()
    
    private val _isSignupSuccessful = MutableStateFlow(false)
    val isSignupSuccessful: StateFlow<Boolean> = _isSignupSuccessful.asStateFlow()
    
    fun updateFullName(fullName: String) {
        _uiState.value = _uiState.value.copy(
            fullName = fullName, 
            fullNameError = null,
            errorMessage = null
        )
    }
    
    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email, 
            emailError = null,
            errorMessage = null
        )
    }
    
    fun updatePhoneNumber(phoneNumber: String) {
        _uiState.value = _uiState.value.copy(
            phoneNumber = phoneNumber, 
            phoneNumberError = null,
            errorMessage = null
        )
    }
    
    fun updateCountry(country: CountryCode) {
        _uiState.value = _uiState.value.copy(
            selectedCountry = country,
            phoneNumberError = null,
            errorMessage = null
        )
    }
    
    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password, 
            passwordError = null,
            errorMessage = null
        )
    }
    
    fun updateConfirmPassword(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(
            confirmPassword = confirmPassword, 
            confirmPasswordError = null,
            errorMessage = null
        )
    }
    
    fun updateAddress(address: String) {
        _uiState.value = _uiState.value.copy(address = address)
    }
    
    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(isPasswordVisible = !_uiState.value.isPasswordVisible)
    }
    
    fun toggleConfirmPasswordVisibility() {
        _uiState.value = _uiState.value.copy(isConfirmPasswordVisible = !_uiState.value.isConfirmPasswordVisible)
    }
    
    fun signup() {
        val currentState = _uiState.value
        
        // Validate all fields
        val validationResult = validateForm(currentState)
        if (!validationResult.isValid) {
            _uiState.value = validationResult.stateWithErrors
            return
        }
        
        _uiState.value = currentState.copy(isLoading = true, errorMessage = null)
        
        viewModelScope.launch {
            try {
                val result = authRepository.signup(
                    fullName = currentState.fullName.trim(),
                    email = currentState.email.trim(),
                    phoneNumber = formatPhoneNumber(currentState.phoneNumber.trim(), currentState.selectedCountry),
                    password = currentState.password
                )
                
                if (result.isSuccess) {
                    _uiState.value = currentState.copy(isLoading = false)
                    _isSignupSuccessful.value = true
                } else {
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Signup failed"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = "Network error. Please check your connection."
                )
            }
        }
    }
    
    private fun validateForm(state: SignupUiState): ValidationResult {
        var isValid = true
        var updatedState = state
        
        // Validate full name
        if (state.fullName.isBlank()) {
            updatedState = updatedState.copy(fullNameError = "Please enter your full name")
            isValid = false
        } else if (state.fullName.length < 2) {
            updatedState = updatedState.copy(fullNameError = "Name must be at least 2 characters")
            isValid = false
        }
        
        // Validate email
        if (state.email.isBlank()) {
            updatedState = updatedState.copy(emailError = "Please enter your email")
            isValid = false
        } else if (!isValidEmail(state.email)) {
            updatedState = updatedState.copy(emailError = "Please enter a valid email address")
            isValid = false
        }
        
        // Validate phone number
        if (state.phoneNumber.isBlank()) {
            updatedState = updatedState.copy(phoneNumberError = "Please enter your phone number")
            isValid = false
        } else if (!isValidPhoneNumber(state.phoneNumber)) {
            updatedState = updatedState.copy(phoneNumberError = "Please enter a valid phone number")
            isValid = false
        }
        
        // Validate password
        if (state.password.isBlank()) {
            updatedState = updatedState.copy(passwordError = "Please enter a password")
            isValid = false
        } else if (state.password.length < 6) {
            updatedState = updatedState.copy(passwordError = "Password must be at least 6 characters")
            isValid = false
        }
        
        // Validate confirm password
        if (state.confirmPassword.isBlank()) {
            updatedState = updatedState.copy(confirmPasswordError = "Please confirm your password")
            isValid = false
        } else if (state.password != state.confirmPassword) {
            updatedState = updatedState.copy(confirmPasswordError = "Passwords do not match")
            isValid = false
        }
        
        return ValidationResult(isValid, updatedState)
    }
    
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        // Remove any non-digit characters for validation
        val cleanNumber = phoneNumber.replace(Regex("[^0-9]"), "")
        
        // Basic validation - should be 7-15 digits (covers most international formats)
        return cleanNumber.length in 7..15
    }
    
    private fun formatPhoneNumber(phoneNumber: String, country: CountryCode): String {
        // Remove any existing country code or special characters
        val cleanNumber = phoneNumber.replace(Regex("[^0-9]"), "")
        
        // If the number already starts with the country code (without +), remove it
        val countryCodeDigits = country.dialCode.substring(1) // Remove the +
        val finalNumber = if (cleanNumber.startsWith(countryCodeDigits)) {
            cleanNumber.substring(countryCodeDigits.length)
        } else {
            cleanNumber
        }
        
        // Return the formatted number with country code
        return "${country.dialCode}$finalNumber"
    }
    
    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    private data class ValidationResult(
        val isValid: Boolean,
        val stateWithErrors: SignupUiState
    )
}