package com.example.wisebite.data.remote

import com.example.wisebite.data.model.LoginRequest
import com.example.wisebite.data.model.LoginResponse
import com.example.wisebite.data.model.SignupRequest
import com.example.wisebite.data.model.User
import com.example.wisebite.data.model.UserUpdateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
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
    
    @PATCH("user/me")
    suspend fun updateCurrentUser(
        @Header("Authorization") token: String,
        @Body updateRequest: UserUpdateRequest
    ): Response<User>
    
    companion object {
        // For demo usage with ngrok - Update this URL when you have a new ngrok session
        const val BASE_URL = "https://nondiabolic-twanna-unsensitive.ngrok-free.dev/api/v1/"
        
        // For local development (when not using ngrok)
        // const val BASE_URL = "http://192.168.2.23:8000/api/v1/"
        
        // For Android Emulator (when not using ngrok):
        // const val BASE_URL = "http://10.0.2.2:8000/api/v1/"
    }
}