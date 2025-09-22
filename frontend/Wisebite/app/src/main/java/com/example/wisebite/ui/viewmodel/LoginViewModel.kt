package com.example.wisebite.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wisebite.data.model.LoginUiState
import com.example.wisebite.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    private val _isLoginSuccessful = MutableStateFlow(false)
    val isLoginSuccessful: StateFlow<Boolean> = _isLoginSuccessful.asStateFlow()
    
    fun updatePhoneNumber(phoneNumber: String) {
        _uiState.value = _uiState.value.copy(phoneNumber = phoneNumber, errorMessage = null)
    }
    
    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password, errorMessage = null)
    }
    
    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(isPasswordVisible = !_uiState.value.isPasswordVisible)
    }
    
    fun login() {
        val currentState = _uiState.value
        
        // Basic validation
        if (currentState.phoneNumber.isBlank()) {
            _uiState.value = currentState.copy(errorMessage = "Please enter your phone number")
            return
        }
        
        if (currentState.password.isBlank()) {
            _uiState.value = currentState.copy(errorMessage = "Please enter your password")
            return
        }
        
        _uiState.value = currentState.copy(isLoading = true, errorMessage = null)
        
        viewModelScope.launch {
            try {
                val result = authRepository.login(currentState.phoneNumber, currentState.password)
                
                if (result.isSuccess) {
                    _uiState.value = currentState.copy(isLoading = false)
                    _isLoginSuccessful.value = true
                } else {
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Login failed"
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
    
    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        // Basic phone number validation - adjust according to your requirements
        return phoneNumber.matches(Regex("^[+]?[0-9]{10,15}$"))
    }
    
    private fun formatPhoneNumber(phoneNumber: String): String {
        // Add country code if not present
        return if (phoneNumber.startsWith("+")) {
            phoneNumber
        } else {
            "+84$phoneNumber" // Default to Vietnam country code
        }
    }
}