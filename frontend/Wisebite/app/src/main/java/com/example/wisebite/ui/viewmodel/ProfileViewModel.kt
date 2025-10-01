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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null,
    val isUploadingAvatar: Boolean = false,
    val uploadSuccess: String? = null
)

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val imageUploadRepository: ImageUploadRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    init {
        // Add a small delay to ensure token is ready before making API calls
        viewModelScope.launch {
            kotlinx.coroutines.delay(500) // Small delay to let authentication complete
            loadUserData()
        }
    }
    
    private fun loadUserData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            // First check if user is authenticated
            val tokenManager = com.example.wisebite.data.repository.TokenManager.getInstance(android.app.Application())
            val token = tokenManager.getToken().first()
            if (token == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "User not authenticated"
                )
                return@launch
            }
            
            // First try to get stored user data for immediate display
            val storedUser = authRepository.getStoredUser()
            if (storedUser != null) {
                _uiState.value = _uiState.value.copy(user = storedUser, isLoading = false)
            }
            
            // Then fetch fresh data from the API
            authRepository.getCurrentUser()
                .onSuccess { user ->
                    _uiState.value = _uiState.value.copy(
                        user = user,
                        isLoading = false,
                        errorMessage = null
                    )
                }
                .onFailure { exception ->
                    android.util.Log.e("ProfileViewModel", "loadUserData failed", exception)
                    // If we have stored data, keep it but stop loading
                    // If no stored data, show error
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
    
    fun refreshUserData() {
        loadUserData()
    }
    
    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun logout(onLogout: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onLogout()
        }
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
                    _uiState.value = _uiState.value.copy(
                        isUploadingAvatar = false,
                        user = response.user ?: _uiState.value.user,
                        uploadSuccess = "Avatar uploaded successfully!"
                    )
                    
                    // Refresh user data from server to ensure UI is in sync
                    refreshUserData()
                    
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

class ProfileViewModelFactory(
    private val authRepository: AuthRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            val imageUploadRepository = ImageUploadRepository.getInstance(context)
            return ProfileViewModel(authRepository, imageUploadRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}