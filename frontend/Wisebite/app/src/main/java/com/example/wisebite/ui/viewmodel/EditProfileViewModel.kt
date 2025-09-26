package com.example.wisebite.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.wisebite.data.model.User
import com.example.wisebite.data.repository.AuthRepository
import com.example.wisebite.data.repository.ImageUploadRepository
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
    val isUpdateSuccessful: Boolean = false,
    val isUploadingAvatar: Boolean = false,
    val uploadSuccess: String? = null
)

class EditProfileViewModel(
    private val authRepository: AuthRepository,
    private val imageUploadRepository: ImageUploadRepository
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
    
    fun uploadAvatar(imageUri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isUploadingAvatar = true,
                errorMessage = null,
                uploadSuccess = null
            )
            
            imageUploadRepository.uploadAvatar(imageUri)
                .onSuccess { response ->
                    val updatedUser = response.user ?: _uiState.value.user
                    _uiState.value = _uiState.value.copy(
                        isUploadingAvatar = false,
                        user = updatedUser,
                        uploadSuccess = "Avatar uploaded successfully!"
                    )
                    
                    // Refresh user data to keep UI in sync
                    loadUserData()
                    
                    // Clear success message after 3 seconds
                    kotlinx.coroutines.delay(3000)
                    _uiState.value = _uiState.value.copy(uploadSuccess = null)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isUploadingAvatar = false,
                        errorMessage = "Failed to upload avatar: ${exception.message}"
                    )
                }
        }
    }
    
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            uploadSuccess = null
        )
    }
}

class EditProfileViewModelFactory(
    private val authRepository: AuthRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditProfileViewModel::class.java)) {
            val imageUploadRepository = ImageUploadRepository.getInstance(context)
            return EditProfileViewModel(authRepository, imageUploadRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}