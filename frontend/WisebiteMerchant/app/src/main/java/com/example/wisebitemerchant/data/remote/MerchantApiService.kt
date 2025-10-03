package com.example.wisebitemerchant.data.remote

import com.example.wisebitemerchant.data.model.*
import com.example.wisebitemerchant.model.GoogleSignInRequest
import retrofit2.Response
import retrofit2.http.*

interface MerchantApiService {
    
    @FormUrlEncoded
    @POST("api/v1/auth/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<LoginResponse>
    
    @POST("api/v1/auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<UserResponse>
    
    @POST("api/v1/auth/google")
    suspend fun signInWithGoogle(@Body request: GoogleSignInRequest): Response<LoginResponse>
    
    @GET("api/v1/user/me")
    suspend fun getCurrentUser(@Header("Authorization") token: String): Response<MerchantUser>
    
    @GET("api/v1/stores/me")
    suspend fun getMyStore(@Header("Authorization") token: String): Response<Store>
    
    @POST("api/v1/stores")
    suspend fun createStore(
        @Header("Authorization") token: String,
        @Body request: StoreCreateRequest
    ): Response<Store>
    
    @PATCH("api/v1/user/me")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: MerchantUser
    ): Response<MerchantUser>
    
    @PATCH("api/v1/stores/me")
    suspend fun updateStore(
        @Header("Authorization") token: String,
        @Body request: Store
    ): Response<Store>
    
    // Order management endpoints will be added later
    
    // Surprise bag management endpoints will be added later
}