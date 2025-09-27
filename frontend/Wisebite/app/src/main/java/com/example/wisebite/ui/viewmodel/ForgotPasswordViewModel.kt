package com.example.wisebite.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wisebite.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun requestResetCode(email: String) {
        if (!validateEmail(email)) {
            _uiState.value = _uiState.value.copy(error = "Email không hợp lệ")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            authRepository.requestPasswordReset(email)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isCodeSent = true,
                        email = email,
                        step = ForgotPasswordStep.CODE_ENTRY,
                        codeExpirationTime = System.currentTimeMillis() + (15 * 60 * 1000) // 15 minutes
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Có lỗi xảy ra"
                    )
                }
        }
    }

    fun verifyResetCode(email: String, resetCode: String) {
        if (resetCode.length != 6) {
            _uiState.value = _uiState.value.copy(error = "Mã xác thực phải có 6 số")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // Store the code for later use in password reset
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                step = ForgotPasswordStep.NEW_PASSWORD,
                resetCode = resetCode
            )
        }
    }

    fun resetPassword(email: String, resetCode: String, newPassword: String) {
        if (!validatePassword(newPassword)) {
            _uiState.value = _uiState.value.copy(error = "Mật khẩu phải có ít nhất 8 ký tự")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            authRepository.resetPassword(email, resetCode, newPassword)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isPasswordResetSuccessful = true,
                        step = ForgotPasswordStep.SUCCESS
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Có lỗi xảy ra khi đặt lại mật khẩu"
                    )
                }
        }
    }

    fun resendCode(email: String) {
        requestResetCode(email)
    }

    fun validateEmail(email: String): Boolean {
        return email.isNotBlank() && 
               email.contains("@") && 
               email.contains(".") &&
               android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun validatePassword(password: String): Boolean {
        return password.length >= 8
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class ForgotPasswordUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val step: ForgotPasswordStep = ForgotPasswordStep.EMAIL_ENTRY,
    val email: String = "",
    val resetCode: String = "",
    val codeExpirationTime: Long? = null,
    val isCodeSent: Boolean = false,
    val isPasswordResetSuccessful: Boolean = false
)

enum class ForgotPasswordStep {
    EMAIL_ENTRY,
    CODE_ENTRY,
    NEW_PASSWORD,
    SUCCESS
}