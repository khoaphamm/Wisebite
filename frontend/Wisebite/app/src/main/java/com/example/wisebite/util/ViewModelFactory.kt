package com.example.wisebite.util

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wisebite.data.repository.AuthRepository
import com.example.wisebite.data.repository.OrderRepository
import com.example.wisebite.ui.viewmodel.ForgotPasswordViewModel
import com.example.wisebite.ui.viewmodel.LoginViewModel
import com.example.wisebite.ui.viewmodel.OrderViewModel
import com.example.wisebite.ui.viewmodel.SignupViewModel

class ViewModelFactory private constructor(
    private val authRepository: AuthRepository,
    private val orderRepository: OrderRepository,
    private val context: Context
) : ViewModelProvider.Factory {

    companion object {
        @Volatile
        private var INSTANCE: ViewModelFactory? = null
        
        fun getInstance(context: Context): ViewModelFactory {
            return INSTANCE ?: synchronized(this) {
                val authRepository = AuthRepository.getInstance(context)
                val orderRepository = OrderRepository.getInstance(context)
                INSTANCE ?: ViewModelFactory(
                    authRepository, 
                    orderRepository, 
                    context
                ).also { INSTANCE = it }
            }
        }
    }
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(authRepository, context) as T
            }
            modelClass.isAssignableFrom(SignupViewModel::class.java) -> {
                SignupViewModel(authRepository) as T
            }
            modelClass.isAssignableFrom(ForgotPasswordViewModel::class.java) -> {
                ForgotPasswordViewModel(authRepository) as T
            }
            modelClass.isAssignableFrom(OrderViewModel::class.java) -> {
                OrderViewModel(orderRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}