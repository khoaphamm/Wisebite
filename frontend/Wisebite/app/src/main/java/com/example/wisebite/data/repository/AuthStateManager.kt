package com.example.wisebite.data.repository

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Central authentication state manager that handles token expiration
 * and provides logout events across the app
 */
class AuthStateManager private constructor(
    private val context: Context
) {
    companion object {
        @Volatile
        private var INSTANCE: AuthStateManager? = null
        
        fun getInstance(context: Context): AuthStateManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AuthStateManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val authRepository = AuthRepository.getInstance(context)
    
    private val _authExpiredEvents = MutableSharedFlow<String>()
    val authExpiredEvents: SharedFlow<String> = _authExpiredEvents.asSharedFlow()
    
    /**
     * Call this when an API call fails due to authentication issues
     */
    suspend fun handleAuthenticationFailure(reason: String = "Token expired") {
        android.util.Log.w("AuthStateManager", "Authentication failure: $reason")
        
        // Clear stored tokens
        authRepository.logout()
        
        // Notify all observers that authentication has expired
        _authExpiredEvents.emit(reason)
    }
    
    /**
     * Check if current session is valid
     */
    suspend fun isSessionValid(): Boolean {
        return try {
            val tokenManager = TokenManager.getInstance(context)
            val token = tokenManager.getToken().first()
            token != null && !tokenManager.isTokenExpired()
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Extension for ViewModels to handle authentication expiration
 */
fun ViewModel.handleAuthExpired(authStateManager: AuthStateManager, onLogout: () -> Unit) {
    viewModelScope.launch {
        authStateManager.authExpiredEvents.collect { reason ->
            android.util.Log.d("AuthViewModel", "Auth expired: $reason")
            onLogout()
        }
    }
}