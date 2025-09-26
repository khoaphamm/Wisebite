package com.example.wisebite.data.model

import com.google.gson.annotations.SerializedName

import com.example.wisebite.ui.component.CountryCode
import java.util.Date

data class User(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("full_name")
    val fullName: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("phone_number")
    val phoneNumber: String,
    
    @SerializedName("role")
    val role: String, // "customer" or "vendor" - matches backend UserRole enum
    
    @SerializedName("avt_url") 
    val avatarUrl: String?,
    
    @SerializedName("created_at")
    val createdAt: String
)

data class LoginRequest(
    @SerializedName("phone_number")
    val phoneNumber: String,
    
    @SerializedName("password")
    val password: String
)

data class SignupRequest(
    @SerializedName("full_name")
    val fullName: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("phone_number")
    val phoneNumber: String,
    
    @SerializedName("password")
    val password: String,
    
    @SerializedName("role")
    val role: String = "customer" // Backend expects "role" not "user_type"
)

data class UserUpdateRequest(
    @SerializedName("full_name")
    val fullName: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("phone_number")
    val phoneNumber: String
)

data class LoginResponse(
    @SerializedName("access_token")
    val accessToken: String,
    
    @SerializedName("token_type")
    val tokenType: String = "bearer"
)

data class ApiResponse<T>(
    @SerializedName("data")
    val data: T?,
    
    @SerializedName("message")
    val message: String?,
    
    @SerializedName("detail")
    val detail: String? // For error responses
)

// UI State models
data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null
)

data class LoginUiState(
    val phoneNumber: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isPasswordVisible: Boolean = false
)


data class SignupUiState(
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val selectedCountry: CountryCode = CountryCode("Vietnam", "VN", "🇻🇳", "+84"),
    val password: String = "",
    val confirmPassword: String = "",
    val address: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val fullNameError: String? = null,
    val emailError: String? = null,
    val phoneNumberError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null
)