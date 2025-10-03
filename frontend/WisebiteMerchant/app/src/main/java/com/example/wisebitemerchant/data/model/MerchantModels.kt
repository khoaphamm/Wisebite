package com.example.wisebitemerchant.data.model

import com.google.gson.annotations.SerializedName

data class MerchantUser(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("full_name") val fullName: String,
    val email: String,
    @SerializedName("phone_number") val phoneNumber: String,
    @SerializedName("role") val role: String = "vendor", // Backend expects "vendor" for merchants
    @SerializedName("profile_picture") val profilePicture: String? = null,
    @SerializedName("is_verified") val isVerified: Boolean = false,
    @SerializedName("created_at") val createdAt: String? = null
)

data class Store(
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("address") val address: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("logo_url") val logoUrl: String? = null,
    @SerializedName("owner_id") val ownerId: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerializedName("cuisine_type") val cuisineType: String? = null,
    @SerializedName("is_active") val isActive: Boolean = true,
    @SerializedName("created_at") val createdAt: String? = null
)

data class StoreCreateRequest(
    val name: String,
    val description: String? = null,
    val address: String,
    @SerializedName("logo_url") val logoUrl: String? = null,
    @SerializedName("cuisine_type") val cuisineType: String? = null
)

data class LoginRequest(
    @SerializedName("phone_number") val phoneNumber: String,
    val password: String
)

data class SignupRequest(
    @SerializedName("full_name") val fullName: String,
    val email: String,
    @SerializedName("phone_number") val phoneNumber: String,
    val password: String,
    val role: String = "vendor" // Backend expects "vendor" for merchants
)

data class UserResponse(
    val id: String,
    val email: String,
    @SerializedName("phone_number") val phoneNumber: String,
    @SerializedName("full_name") val fullName: String,
    val role: String,
    @SerializedName("avt_url") val avatarUrl: String?,
    val gender: String?,
    @SerializedName("birth_date") val birthDate: String?,
    @SerializedName("is_google_user") val isGoogleUser: Boolean = false
)

data class LoginResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String = "bearer"
)

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val error: String? = null
)