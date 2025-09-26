package com.example.wisebite.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wisebite.data.repository.AuthRepository

class AuthCheckViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    suspend fun checkAuthenticationStatus(): Boolean {
        return try {
            // Check if we have a valid token and user data
            val isLoggedIn = authRepository.isLoggedIn()
            if (isLoggedIn) {
                // Optionally, we could validate the token with the server here
                // For now, we'll trust that we have a valid token
                val user = authRepository.getStoredUser()
                user != null
            } else {
                false
            }
        } catch (e: Exception) {
            // If any error occurs, assume not authenticated
            false
        }
    }
}

class AuthCheckViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthCheckViewModel::class.java)) {
            return AuthCheckViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}