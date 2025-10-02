package com.example.wisebitemerchant.util

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wisebitemerchant.data.manager.TokenManager
import com.example.wisebitemerchant.data.remote.RetrofitClient
import com.example.wisebitemerchant.data.repository.AuthRepository
import com.example.wisebitemerchant.ui.viewmodel.LoginViewModel
import com.example.wisebitemerchant.ui.viewmodel.ProfileViewModel
import com.example.wisebitemerchant.ui.viewmodel.SignupViewModel

class ViewModelFactory private constructor(private val context: Context) : ViewModelProvider.Factory {
    
    companion object {
        @Volatile
        private var INSTANCE: ViewModelFactory? = null
        
        fun getInstance(context: Context): ViewModelFactory {
            return INSTANCE ?: synchronized(this) {
                val instance = ViewModelFactory(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
    
    private val tokenManager = TokenManager.getInstance(context)
    private val authRepository = AuthRepository.getInstance(RetrofitClient.apiService, tokenManager)
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(authRepository) as T
            }
            modelClass.isAssignableFrom(SignupViewModel::class.java) -> {
                SignupViewModel(authRepository) as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(authRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}