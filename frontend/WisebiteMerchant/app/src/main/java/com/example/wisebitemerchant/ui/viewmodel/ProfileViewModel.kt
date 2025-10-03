package com.example.wisebitemerchant.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wisebitemerchant.data.model.MerchantUser
import com.example.wisebitemerchant.data.model.Store
import com.example.wisebitemerchant.data.repository.AuthRepository
import com.example.wisebitemerchant.data.repository.StoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: MerchantUser? = null,
    val store: Store? = null,
    val errorMessage: String? = null
)

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val storeRepository: StoreRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    init {
        loadUserData()
    }
    
    private fun loadUserData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            // Fetch user data
            authRepository.getCurrentUser()
                .onSuccess { user ->
                    _uiState.value = _uiState.value.copy(user = user)
                    // After getting user, fetch store data
                    loadStoreData()
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to load user data"
                    )
                }
        }
    }
    
    private fun loadStoreData() {
        viewModelScope.launch {
            storeRepository.getMyStore()
                .onSuccess { store ->
                    _uiState.value = _uiState.value.copy(
                        store = store,
                        isLoading = false,
                        errorMessage = null
                    )
                }
                .onFailure { exception ->
                    // Store data is optional, so just log and continue
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = null  // Don't show error if store not found
                    )
                }
        }
    }
    
    fun refreshUserData() {
        loadUserData()
    }
    
    fun logout(onLogout: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onLogout()
        }
    }
    
    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
