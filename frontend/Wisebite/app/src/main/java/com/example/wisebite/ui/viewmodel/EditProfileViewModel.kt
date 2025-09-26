package com.example.wisebite.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.wisebite.data.model.User
import com.example.wisebite.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EditProfileUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val errorMessage: String? = null,
    val isUpdateSuccessful: Boolean = false
)

class EditProfileViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()
    
    // Store original values to check for changes
    private var originalFullName: String = ""
    private var originalEmail: String = ""
    private var originalPhoneNumber: String = ""
    
    init {
        loadUserData()
    }
    
    private fun loadUserData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            // First try to get stored user data
            val storedUser = authRepository.getStoredUser()
            if (storedUser != null) {
                setUserData(storedUser)
            }
            
            // Then fetch fresh data from the API
            authRepository.getCurrentUser()
                .onSuccess { user ->
                    setUserData(user)
                    _uiState.value = _uiState.value.copy(
                        user = user,
                        isLoading = false,
                        errorMessage = null
                    )
                }
                .onFailure { exception ->
                    if (storedUser == null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Failed to load user data"
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                }
        }
    }
    
    private fun setUserData(user: User) {
        originalFullName = user.fullName
        originalEmail = user.email
        originalPhoneNumber = user.phoneNumber
        
        _uiState.value = _uiState.value.copy(
            user = user,
            fullName = user.fullName,
            email = user.email,
            phoneNumber = user.phoneNumber
        )
    }
    
    fun updateFullName(fullName: String) {
        _uiState.value = _uiState.value.copy(fullName = fullName, errorMessage = null)
    }
    
    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email, errorMessage = null)
    }
    
    fun updatePhoneNumber(phoneNumber: String) {
        _uiState.value = _uiState.value.copy(phoneNumber = phoneNumber, errorMessage = null)
    }
    
    fun hasChanges(): Boolean {
        val currentState = _uiState.value
        return currentState.fullName != originalFullName ||
                currentState.email != originalEmail ||
                currentState.phoneNumber != originalPhoneNumber
    }
    
    fun updateProfile() {
        if (!hasChanges()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Không có thay đổi nào để lưu"
            )
            return
        }
        
        // Validate inputs
        val currentState = _uiState.value
        if (currentState.fullName.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Họ và tên không được để trống")
            return
        }
        
        if (currentState.email.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Email không được để trống")
            return
        }
        
        if (currentState.phoneNumber.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Số điện thoại không được để trống")
            return
        }
        
        // Email validation
        val emailRegex = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex()
        if (!currentState.email.matches(emailRegex)) {
            _uiState.value = _uiState.value.copy(errorMessage = "Email không hợp lệ")
            return
        }
        
        // Phone number validation (basic)
        if (currentState.phoneNumber.length < 8) {
            _uiState.value = _uiState.value.copy(errorMessage = "Số điện thoại không hợp lệ")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            authRepository.updateUserProfile(
                fullName = currentState.fullName,
                email = currentState.email,
                phoneNumber = currentState.phoneNumber
            )
                .onSuccess { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isUpdateSuccessful = true,
                        user = user
                    )
                    
                    // Update original values
                    originalFullName = currentState.fullName
                    originalEmail = currentState.email
                    originalPhoneNumber = currentState.phoneNumber
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Cập nhật thông tin thất bại"
                    )
                }
        }
    }
    
    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

class EditProfileViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditProfileViewModel::class.java)) {
            return EditProfileViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}