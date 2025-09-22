package com.example.wisebite.data.remote

import com.example.wisebite.data.model.LoginRequest
import com.example.wisebite.data.model.LoginResponse
import com.example.wisebite.data.model.SignupRequest
import com.example.wisebite.data.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface WisebiteApiService {
    
    @FormUrlEncoded
    @POST("auth/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<LoginResponse>
    
    @POST("auth/signup")
    suspend fun signup(@Body signupRequest: SignupRequest): Response<User>
    
    @GET("user/me")
    suspend fun getCurrentUser(@Header("Authorization") token: String): Response<User>
    
    companion object {
        // For physical device connected to same WiFi network - using your computer's IP
        const val BASE_URL = "http://192.168.2.23:8000/api/v1/"
        
        // For Android Emulator, use this instead:
        // const val BASE_URL = "http://10.0.2.2:8000/api/v1/"
        
        // Note: Make sure your phone and computer are on the same WiFi network!
        // If this IP doesn't work, run "ipconfig" and check your Wi-Fi adapter's IPv4 Address
    }
}