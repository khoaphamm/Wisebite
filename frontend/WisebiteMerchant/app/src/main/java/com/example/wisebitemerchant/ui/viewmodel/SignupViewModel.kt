package com.example.wisebitemerchant.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wisebitemerchant.data.model.CountryCode
import com.example.wisebitemerchant.data.model.CountryCodeData
import com.example.wisebitemerchant.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SignupUiState(
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val storeName: String = "",
    val storeAddress: String = "",
    val cuisineType: String = "",
    val selectedCountry: CountryCode = CountryCodeData.defaultCountry,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    // Field-specific error messages
    val fullNameError: String? = null,
    val emailError: String? = null,
    val phoneNumberError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val storeNameError: String? = null,
    val storeAddressError: String? = null
)

class SignupViewModel(private val authRepository: AuthRepository) : ViewModel() {
    
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
        validateFullNameField(fullName)
    }
    
    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email, 
            emailError = null,
            errorMessage = null
        )
        validateEmailField(email)
    }
    
    fun updatePhoneNumber(phoneNumber: String) {
        _uiState.value = _uiState.value.copy(
            phoneNumber = phoneNumber, 
            phoneNumberError = null,
            errorMessage = null
        )
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
        validatePasswordField(password)
    }
    
    fun updateConfirmPassword(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(
            confirmPassword = confirmPassword, 
            confirmPasswordError = null,
            errorMessage = null
        )
        validateConfirmPasswordField(confirmPassword)
    }
    
    fun updateStoreName(storeName: String) {
        _uiState.value = _uiState.value.copy(
            storeName = storeName, 
            storeNameError = null,
            errorMessage = null
        )
        validateStoreNameField(storeName)
    }
    
    fun updateStoreAddress(storeAddress: String) {
        _uiState.value = _uiState.value.copy(
            storeAddress = storeAddress, 
            storeAddressError = null,
            errorMessage = null
        )
        validateStoreAddressField(storeAddress)
    }
    
    fun updateCuisineType(cuisineType: String) {
        _uiState.value = _uiState.value.copy(cuisineType = cuisineType)
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
        try {
            val validationResult = validateForm(currentState)
            if (!validationResult.isValid) {
                _uiState.value = validationResult.stateWithErrors
                return
            }
        } catch (e: Exception) {
            _uiState.value = currentState.copy(
                errorMessage = "Lỗi xác thực: ${e.message}"
            )
            return
        }
        
        _uiState.value = currentState.copy(isLoading = true, errorMessage = null)
        
        viewModelScope.launch {
            try {
                val formattedPhone = try {
                    formatPhoneNumber(currentState.phoneNumber, currentState.selectedCountry)
                } catch (e: Exception) {
                    currentState.phoneNumber // Use original if formatting fails
                }
                
                val result = authRepository.signup(
                    fullName = currentState.fullName,
                    email = currentState.email,
                    phoneNumber = formattedPhone,
                    password = currentState.password,
                    storeName = currentState.storeName,
                    storeAddress = currentState.storeAddress,
                    cuisineType = currentState.cuisineType.ifBlank { null }
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
                    errorMessage = "Lỗi: ${e.message ?: e::class.simpleName}"
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
        }
        
        // Validate confirm password
        if (state.confirmPassword.isBlank()) {
            updatedState = updatedState.copy(confirmPasswordError = "Vui lòng xác nhận mật khẩu")
            isValid = false
        } else if (state.password != state.confirmPassword) {
            updatedState = updatedState.copy(confirmPasswordError = "Mật khẩu xác nhận không khớp")
            isValid = false
        }
        
        // Store validation - make optional for now since stores are created separately
        // TODO: Remove this when implementing proper store creation flow
        /*
        // Validate store name
        if (state.storeName.isBlank()) {
            updatedState = updatedState.copy(storeNameError = "Vui lòng nhập tên cửa hàng")
            isValid = false
        }
        
        // Validate store address
        if (state.storeAddress.isBlank()) {
            updatedState = updatedState.copy(storeAddressError = "Vui lòng nhập địa chỉ cửa hàng")
            isValid = false
        }
        */
        
        return ValidationResult(isValid, updatedState)
    }
    
    // Individual field validation functions for real-time feedback
    private fun validateFullNameField(fullName: String) {
        if (fullName.isBlank()) return
        
        when {
            fullName.length < 2 -> {
                _uiState.value = _uiState.value.copy(fullNameError = "Họ và tên phải có ít nhất 2 ký tự")
            }
            else -> {
                _uiState.value = _uiState.value.copy(fullNameError = null)
            }
        }
    }
    
    private fun validateEmailField(email: String) {
        if (email.isBlank()) return
        
        if (!isValidEmail(email)) {
            _uiState.value = _uiState.value.copy(emailError = "Email không hợp lệ")
        } else {
            _uiState.value = _uiState.value.copy(emailError = null)
        }
    }
    
    private fun validatePhoneField(phoneNumber: String) {
        if (phoneNumber.isBlank()) return
        
        if (!isValidPhoneNumber(phoneNumber)) {
            _uiState.value = _uiState.value.copy(phoneNumberError = "Số điện thoại không hợp lệ")
        } else {
            _uiState.value = _uiState.value.copy(phoneNumberError = null)
        }
    }
    
    private fun validatePasswordField(password: String) {
        if (password.isBlank()) return
        
        when {
            password.length < 6 -> {
                _uiState.value = _uiState.value.copy(passwordError = "Mật khẩu phải có ít nhất 6 ký tự")
            }
            else -> {
                _uiState.value = _uiState.value.copy(passwordError = null)
            }
        }
    }
    
    private fun validateConfirmPasswordField(confirmPassword: String) {
        if (confirmPassword.isBlank()) return
        
        if (_uiState.value.password != confirmPassword) {
            _uiState.value = _uiState.value.copy(confirmPasswordError = "Mật khẩu xác nhận không khớp")
        } else {
            _uiState.value = _uiState.value.copy(confirmPasswordError = null)
        }
    }
    
    private fun validateStoreNameField(storeName: String) {
        if (storeName.isBlank()) return
        
        if (storeName.length < 2) {
            _uiState.value = _uiState.value.copy(storeNameError = "Tên cửa hàng phải có ít nhất 2 ký tự")
        } else {
            _uiState.value = _uiState.value.copy(storeNameError = null)
        }
    }
    
    private fun validateStoreAddressField(storeAddress: String) {
        if (storeAddress.isBlank()) return
        
        if (storeAddress.length < 10) {
            _uiState.value = _uiState.value.copy(storeAddressError = "Địa chỉ cửa hàng quá ngắn")
        } else {
            _uiState.value = _uiState.value.copy(storeAddressError = null)
        }
    }
    
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        // More lenient validation - allow any number with at least 7 digits
        val digitsOnly = phoneNumber.replace(Regex("[^0-9]"), "")
        return digitsOnly.length >= 7 && digitsOnly.length <= 15
    }
    
    private fun formatPhoneNumber(phoneNumber: String, country: CountryCode): String {
        return try {
            // Remove any non-digit characters
            val cleanNumber = phoneNumber.replace(Regex("[^0-9]"), "")
            
            // If no clean number, return original
            if (cleanNumber.isEmpty()) return phoneNumber
            
            // Remove leading zeros or country code if already present
            val countryCodeDigits = country.dialCode.removePrefix("+")
            val finalNumber = if (cleanNumber.startsWith(countryCodeDigits)) {
                cleanNumber.substring(countryCodeDigits.length)
            } else {
                cleanNumber.removePrefix("0")
            }
            
            // Return the formatted number with country code
            "${country.dialCode}$finalNumber"
        } catch (e: Exception) {
            // If formatting fails, return original phone number
            phoneNumber
        }
    }
    
    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    private data class ValidationResult(
        val isValid: Boolean,
        val stateWithErrors: SignupUiState
    )
}