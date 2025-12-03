package com.example.wisebitemerchant.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wisebitemerchant.data.model.MerchantUser
import com.example.wisebitemerchant.data.model.Store
import com.example.wisebitemerchant.data.model.StoreCreateRequest
import com.example.wisebitemerchant.data.repository.AuthRepository
import com.example.wisebitemerchant.data.repository.StoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MerchantEditProfileUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    
    // Personal Information
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val profileImageUrl: String? = null,
    
    // Store Information
    val storeName: String = "",
    val storeDescription: String = "",
    val storeAddress: String = "",
    val storeImageUrl: String? = null,
    
    // UI States
    val isPersonalInfoExpanded: Boolean = true,
    val isStoreInfoExpanded: Boolean = true,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    
    // Original data for comparison
    val originalUser: MerchantUser? = null,
    val originalStore: Store? = null
)

class MerchantEditProfileViewModel(
    private val authRepository: AuthRepository,
    private val storeRepository: StoreRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MerchantEditProfileUiState())
    val uiState: StateFlow<MerchantEditProfileUiState> = _uiState.asStateFlow()
    
    init {
        loadProfileData()
    }
    
    private fun loadProfileData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            // Load user data
            authRepository.getCurrentUser()
                .onSuccess { user ->
                    _uiState.value = _uiState.value.copy(
                        originalUser = user,
                        fullName = user.fullName,
                        email = user.email,
                        phoneNumber = user.phoneNumber,
                        profileImageUrl = user.profilePicture
                    )
                    // Load store data after user data
                    loadStoreData()
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to load profile: ${exception.message}"
                    )
                }
        }
    }
    
    private fun loadStoreData() {
        viewModelScope.launch {
            storeRepository.getMyStore()
                .onSuccess { store ->
                    _uiState.value = _uiState.value.copy(
                        originalStore = store,
                        storeName = store.name ?: "",
                        storeDescription = store.description ?: "",
                        storeAddress = store.address ?: "",
                        storeImageUrl = store.logoUrl,
                        isLoading = false
                    )
                }
                .onFailure { exception ->
                    // Handle "Store not found" as a normal case for new merchants
                    if (exception.message?.contains("Store not found") == true || 
                        exception.message?.contains("404") == true ||
                        exception.message?.contains("Cửa hàng không tồn tại") == true) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            originalStore = null,
                            // Keep default empty values for store fields
                            errorMessage = null
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Failed to load store data: ${exception.message}"
                        )
                    }
                }
        }
    }
    
    // Personal Info Field Updates
    fun updateFullName(fullName: String) {
        _uiState.value = _uiState.value.copy(fullName = fullName)
    }
    
    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }
    
    fun updatePhoneNumber(phoneNumber: String) {
        _uiState.value = _uiState.value.copy(phoneNumber = phoneNumber)
    }
    
    fun updateProfileImage(imageUrl: String?) {
        _uiState.value = _uiState.value.copy(profileImageUrl = imageUrl)
    }
    
    // Store Info Field Updates
    fun updateStoreName(storeName: String) {
        _uiState.value = _uiState.value.copy(storeName = storeName)
    }
    
    fun updateStoreDescription(description: String) {
        _uiState.value = _uiState.value.copy(storeDescription = description)
    }
    
    fun updateStoreAddress(address: String) {
        _uiState.value = _uiState.value.copy(storeAddress = address)
    }
    
    fun updateStoreImage(imageUrl: String?) {
        _uiState.value = _uiState.value.copy(storeImageUrl = imageUrl)
    }
    
    // UI State Updates
    fun togglePersonalInfoExpansion() {
        _uiState.value = _uiState.value.copy(
            isPersonalInfoExpanded = !_uiState.value.isPersonalInfoExpanded
        )
    }
    
    fun toggleStoreInfoExpansion() {
        _uiState.value = _uiState.value.copy(
            isStoreInfoExpanded = !_uiState.value.isStoreInfoExpanded
        )
    }
    
    // Validation
    private fun validatePersonalInfo(): String? {
        val state = _uiState.value
        return when {
            state.fullName.isBlank() -> "Họ tên không được để trống"
            state.email.isBlank() -> "Email không được để trống"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches() -> 
                "Email không hợp lệ"
            state.phoneNumber.isBlank() -> "Số điện thoại không được để trống"
            state.phoneNumber.length < 10 -> 
                "Số điện thoại phải có ít nhất 10 chữ số"
            else -> null
        }
    }
    
    private fun validateStoreInfo(): String? {
        val state = _uiState.value
        return when {
            state.storeName.isBlank() -> "Tên cửa hàng không được để trống"
            state.storeAddress.isBlank() -> "Địa chỉ cửa hàng không được để trống"
            else -> null
        }
    }
    
    fun hasChanges(): Boolean {
        val state = _uiState.value
        val originalUser = state.originalUser
        val originalStore = state.originalStore
        
        val hasPersonalChanges = originalUser?.let { user ->
            state.fullName != user.fullName ||
            state.email != user.email ||
            state.phoneNumber != user.phoneNumber ||
            state.profileImageUrl != user.profilePicture
        } == true
        
        val hasStoreChanges = if (originalStore != null) {
            // Compare with existing store
            state.storeName != (originalStore.name ?: "") ||
            state.storeDescription != (originalStore.description ?: "") ||
            state.storeAddress != (originalStore.address ?: "") ||
            state.storeImageUrl != originalStore.logoUrl
        } else {
            // No existing store - check if user has entered store data
            state.storeName.isNotBlank() || 
            state.storeDescription.isNotBlank() ||
            state.storeAddress.isNotBlank() ||
            state.storeImageUrl != null
        }
        
        return hasPersonalChanges || hasStoreChanges
    }
    
    fun saveProfile(onSuccess: () -> Unit) {
        val personalValidation = validatePersonalInfo()
        val storeValidation = validateStoreInfo()
        
        if (personalValidation != null) {
            _uiState.value = _uiState.value.copy(errorMessage = personalValidation)
            return
        }
        
        if (storeValidation != null) {
            _uiState.value = _uiState.value.copy(errorMessage = storeValidation)
            return
        }
        
        if (!hasChanges()) {
            _uiState.value = _uiState.value.copy(successMessage = "Không có thay đổi nào để lưu")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSaving = true,
                errorMessage = null,
                successMessage = null
            )
            
            val state = _uiState.value
            
            try {
                // Update personal information if changed
                val originalUser = state.originalUser
                if (originalUser != null && (
                    state.fullName != originalUser.fullName ||
                    state.email != originalUser.email ||
                    state.phoneNumber != originalUser.phoneNumber ||
                    state.profileImageUrl != originalUser.profilePicture
                )) {
                    val updatedUser = originalUser.copy(
                        fullName = state.fullName,
                        email = state.email,
                        phoneNumber = state.phoneNumber,
                        profilePicture = state.profileImageUrl
                    )
                    
                    authRepository.updateProfile(updatedUser)
                        .onFailure { exception ->
                            _uiState.value = _uiState.value.copy(
                                isSaving = false,
                                errorMessage = "Failed to update profile: ${exception.message}"
                            )
                            return@launch
                        }
                }
                
                // Handle store information (create or update)
                val originalStore = state.originalStore
                val hasStoreChanges = if (originalStore != null) {
                    // Check if existing store has changes
                    state.storeName != (originalStore.name ?: "") ||
                    state.storeDescription != (originalStore.description ?: "") ||
                    state.storeAddress != (originalStore.address ?: "") ||
                    state.storeImageUrl != originalStore.logoUrl
                } else {
                    // New store - check if required fields are filled
                    state.storeName.isNotBlank() && state.storeAddress.isNotBlank()
                }
                
                if (hasStoreChanges) {
                    if (originalStore != null) {
                        // Update existing store
                        val updatedStore = originalStore.copy(
                            name = state.storeName.trim().takeIf { it.isNotBlank() },
                            description = state.storeDescription.trim().ifBlank { null },
                            address = state.storeAddress.trim().takeIf { it.isNotBlank() },
                            logoUrl = state.storeImageUrl
                        )
                        
                        storeRepository.updateStore(updatedStore)
                            .onFailure { exception ->
                                _uiState.value = _uiState.value.copy(
                                    isSaving = false,
                                    errorMessage = "Failed to update store: ${exception.message}"
                                )
                                return@launch
                            }
                    } else {
                        // Create new store
                        val newStoreRequest = StoreCreateRequest(
                            name = state.storeName.trim(),
                            description = state.storeDescription.trim().ifBlank { null },
                            address = state.storeAddress.trim(),
                            logoUrl = state.storeImageUrl
                        )
                        
                        storeRepository.createStore(newStoreRequest)
                            .onFailure { exception ->
                                _uiState.value = _uiState.value.copy(
                                    isSaving = false,
                                    errorMessage = "Failed to create store: ${exception.message}"
                                )
                                return@launch
                            }
                    }
                }
                
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    successMessage = "Cập nhật thông tin thành công!"
                )
                
                // Reload data to get updated values
                loadProfileData()
                onSuccess()
                
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = "Unexpected error: ${exception.message}"
                )
            }
        }
    }
    
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }
}