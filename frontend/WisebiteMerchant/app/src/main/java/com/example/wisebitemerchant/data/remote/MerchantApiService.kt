package com.example.wisebitemerchant.data.remote

import com.example.wisebitemerchant.data.model.*
import com.example.wisebitemerchant.model.GoogleSignInRequest
import retrofit2.Response
import retrofit2.http.*

interface MerchantApiService {
    
    @FormUrlEncoded
    @POST("auth/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<LoginResponse>
    
    @POST("auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<UserResponse>
    
    @POST("auth/google")
    suspend fun signInWithGoogle(@Body request: GoogleSignInRequest): Response<LoginResponse>
    
    @GET("user/me")
    suspend fun getCurrentUser(@Header("Authorization") token: String): Response<MerchantUser>
    
    @GET("stores/me")
    suspend fun getMyStore(@Header("Authorization") token: String): Response<Store>
    
    @POST("stores")
    suspend fun createStore(
        @Header("Authorization") token: String,
        @Body request: StoreCreateRequest
    ): Response<Store>
    
    @PATCH("user/me")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: MerchantUser
    ): Response<MerchantUser>
    
    @PATCH("stores/me")
    suspend fun updateStore(
        @Header("Authorization") token: String,
        @Body request: Store
    ): Response<Store>
    
    // Order management endpoints will be added later
    
    // Surprise bag management endpoints will be added later
}