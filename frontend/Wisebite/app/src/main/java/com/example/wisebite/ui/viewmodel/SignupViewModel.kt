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
        
        // Real-time validation
        validateFullNameField(fullName)
    }
    
    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email, 
            emailError = null,
            errorMessage = null
        )
        
        // Real-time validation
        validateEmailField(email)
    }
    
    fun updatePhoneNumber(phoneNumber: String) {
        _uiState.value = _uiState.value.copy(
            phoneNumber = phoneNumber, 
            phoneNumberError = null,
            errorMessage = null
        )
        
        // Real-time validation for phone
        validatePhoneField(phoneNumber)
    }
    
    fun updateCountry(country: CountryCode) {
        _uiState.value = _uiState.value.copy(
            selectedCountry = country,
            phoneNumberError = null,
            errorMessage = null
        )
        
        // Re-validate phone with new country
        validatePhoneField(_uiState.value.phoneNumber)
    }
    
    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password, 
            passwordError = null,
            errorMessage = null
        )
        
        // Real-time validation
        validatePasswordField(password)
    }
    
    fun updateConfirmPassword(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(
            confirmPassword = confirmPassword, 
            confirmPasswordError = null,
            errorMessage = null
        )
        
        // Real-time validation
        validateConfirmPasswordField(confirmPassword)
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
                        errorMessage = result.exceptionOrNull()?.message ?: "Đăng ký thất bại. Vui lòng thử lại."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = "Lỗi mạng. Vui lòng kiểm tra kết nối internet."
                )
            }
        }
    }
    
    private fun validateForm(state: SignupUiState): ValidationResult {
        var isValid = true
        var updatedState = state
        
        // Validate full name
        if (state.fullName.isBlank()) {
            updatedState = updatedState.copy(fullNameError = "Vui lòng nhập họ và tên")
            isValid = false
        } else if (state.fullName.length < 2) {
            updatedState = updatedState.copy(fullNameError = "Họ và tên phải có ít nhất 2 ký tự")
            isValid = false
        } else if (state.fullName.length > 100) {
            updatedState = updatedState.copy(fullNameError = "Họ và tên không được quá 100 ký tự")
            isValid = false
        }
        
        // Validate email
        if (state.email.isBlank()) {
            updatedState = updatedState.copy(emailError = "Vui lòng nhập địa chỉ email")
            isValid = false
        } else if (!isValidEmail(state.email)) {
            updatedState = updatedState.copy(emailError = "Vui lòng nhập địa chỉ email hợp lệ")
            isValid = false
        }
        
        // Validate phone number
        if (state.phoneNumber.isBlank()) {
            updatedState = updatedState.copy(phoneNumberError = "Vui lòng nhập số điện thoại")
            isValid = false
        } else if (!isValidPhoneNumber(state.phoneNumber)) {
            updatedState = updatedState.copy(phoneNumberError = "Vui lòng nhập số điện thoại hợp lệ")
            isValid = false
        }
        
        // Validate password
        if (state.password.isBlank()) {
            updatedState = updatedState.copy(passwordError = "Vui lòng nhập mật khẩu")
            isValid = false
        } else if (state.password.length < 6) {
            updatedState = updatedState.copy(passwordError = "Mật khẩu phải có ít nhất 6 ký tự")
            isValid = false
        } else if (state.password.length > 50) {
            updatedState = updatedState.copy(passwordError = "Mật khẩu không được quá 50 ký tự")
            isValid = false
        }
        
        // Validate confirm password
        if (state.confirmPassword.isBlank()) {
            updatedState = updatedState.copy(confirmPasswordError = "Vui lòng xác nhận mật khẩu")
            isValid = false
        } else if (state.password != state.confirmPassword) {
            updatedState = updatedState.copy(confirmPasswordError = "Mật khẩu xác nhận không khớp")
            isValid = false
        }
        
        return ValidationResult(isValid, updatedState)
    }
    
    // Individual field validation functions for real-time feedback
    private fun validateFullNameField(fullName: String) {
        if (fullName.isBlank()) return // Don't show error for empty field while typing
        
        when {
            fullName.length < 2 -> {
                _uiState.value = _uiState.value.copy(fullNameError = "Họ và tên phải có ít nhất 2 ký tự")
            }
            fullName.length > 100 -> {
                _uiState.value = _uiState.value.copy(fullNameError = "Họ và tên không được quá 100 ký tự")
            }
        }
    }
    
    private fun validateEmailField(email: String) {
        if (email.isBlank()) return // Don't show error for empty field while typing
        
        if (!isValidEmail(email)) {
            _uiState.value = _uiState.value.copy(emailError = "Vui lòng nhập địa chỉ email hợp lệ")
        }
    }
    
    private fun validatePhoneField(phoneNumber: String) {
        if (phoneNumber.isBlank()) return // Don't show error for empty field while typing
        
        if (!isValidPhoneNumber(phoneNumber)) {
            _uiState.value = _uiState.value.copy(phoneNumberError = "Vui lòng nhập số điện thoại hợp lệ")
        }
    }
    
    private fun validatePasswordField(password: String) {
        if (password.isBlank()) return // Don't show error for empty field while typing
        
        when {
            password.length < 6 -> {
                _uiState.value = _uiState.value.copy(passwordError = "Mật khẩu phải có ít nhất 6 ký tự")
            }
            password.length > 50 -> {
                _uiState.value = _uiState.value.copy(passwordError = "Mật khẩu không được quá 50 ký tự")
            }
        }
    }
    
    private fun validateConfirmPasswordField(confirmPassword: String) {
        if (confirmPassword.isBlank()) return // Don't show error for empty field while typing
        
        val currentPassword = _uiState.value.password
        if (currentPassword.isNotBlank() && confirmPassword != currentPassword) {
            _uiState.value = _uiState.value.copy(confirmPasswordError = "Mật khẩu xác nhận không khớp")
        }
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