package com.example.wisebite.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wisebite.data.repository.AuthRepository
import com.example.wisebite.ui.viewmodel.LoginViewModel
import com.example.wisebite.ui.viewmodel.SignupViewModel

object ViewModelFactory {
    
    fun createLoginViewModelFactory(authRepository: AuthRepository): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                    return LoginViewModel(authRepository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
    
    fun createSignupViewModelFactory(authRepository: AuthRepository): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(SignupViewModel::class.java)) {
                    return SignupViewModel(authRepository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}