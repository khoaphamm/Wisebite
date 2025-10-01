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
    @SerializedName("store_id") val storeId: Int,
    @SerializedName("merchant_id") val merchantId: Int,
    @SerializedName("store_name") val storeName: String,
    @SerializedName("store_description") val storeDescription: String? = null,
    @SerializedName("store_address") val storeAddress: String,
    @SerializedName("store_phone") val storePhone: String? = null,
    @SerializedName("store_image") val storeImage: String? = null,
    @SerializedName("business_hours") val businessHours: String? = null,
    @SerializedName("cuisine_type") val cuisineType: String? = null,
    @SerializedName("is_active") val isActive: Boolean = true,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerializedName("created_at") val createdAt: String? = null
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