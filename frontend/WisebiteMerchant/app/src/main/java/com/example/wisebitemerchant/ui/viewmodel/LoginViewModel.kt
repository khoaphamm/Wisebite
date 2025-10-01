package com.example.wisebitemerchant.ui.viewmodel

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wisebitemerchant.data.model.LoginResponse
import com.example.wisebitemerchant.data.repository.AuthRepository
import com.example.wisebitemerchant.service.GoogleSignInService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val phoneNumber: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isPasswordVisible: Boolean = false
)

class LoginViewModel(
    private val authRepository: AuthRepository,
    // Bạn không cần context trong constructor cho Google Sign-In nữa
    // context: Context
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
            _uiState.value = currentState.copy(errorMessage = "Vui lòng nhập số điện thoại")
            return
        }

        if (currentState.password.isBlank()) {
            _uiState.value = currentState.copy(errorMessage = "Vui lòng nhập mật khẩu")
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
                        errorMessage = result.exceptionOrNull()?.message ?: "Đăng nhập thất bại"
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

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    // Sửa đổi phương thức này để nhận Activity
    fun signInWithGoogle(activity: Activity) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                // Khởi tạo GoogleSignInService ở đây với Activity context
                val googleSignInService = GoogleSignInService(activity)
                val googleResult = googleSignInService.signIn()

                if (googleResult.success && googleResult.idToken != null) {
                    // Send Google ID token to backend
                    val result = authRepository.signInWithGoogle(googleResult.idToken)

                    if (result.isSuccess) {
                        _uiState.value = currentState.copy(isLoading = false)
                        _isLoginSuccessful.value = true
                    } else {
                        _uiState.value = currentState.copy(
                            isLoading = false,
                            errorMessage = result.exceptionOrNull()?.message ?: "Đăng nhập Google thất bại"
                        )
                    }
                } else {
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        errorMessage = googleResult.errorMessage ?: "Đăng nhập Google thất bại"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = "Có lỗi xảy ra khi đăng nhập Google: ${e.message}"
                )
            }
        }
    }

    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        // Vietnamese phone number validation
        return phoneNumber.matches(Regex("^[0-9]{10,11}$"))
    }
}