package com.example.wisebite.data.remote

import com.example.wisebite.data.model.LoginRequest
import com.example.wisebite.data.model.LoginResponse
import com.example.wisebite.data.model.SignupRequest
import com.example.wisebite.data.model.User
import com.example.wisebite.data.model.UserUpdateRequest
import com.example.wisebite.data.model.UploadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

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
    
    // Image Upload Endpoints
    @Multipart
    @POST("upload/avatar")
    suspend fun uploadAvatar(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part
    ): Response<UploadResponse>
    
    @Multipart
    @POST("upload/store/{store_id}/image")
    suspend fun uploadStoreImage(
        @Path("store_id") storeId: Int,
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part
    ): Response<UploadResponse>
    
    @Multipart
    @POST("upload/food-item/{food_item_id}/image")
    suspend fun uploadFoodItemImage(
        @Path("food_item_id") foodItemId: Int,
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part
    ): Response<UploadResponse>
    
    @Multipart
    @POST("upload/surprise-bag/{bag_id}/images")
    suspend fun uploadSurpriseBagImages(
        @Path("bag_id") bagId: Int,
        @Header("Authorization") token: String,
        @Part files: List<MultipartBody.Part>
    ): Response<UploadResponse>
    
    @FormUrlEncoded
    @POST("upload/base64")
    suspend fun uploadBase64Image(
        @Header("Authorization") token: String,
        @Field("folder") folder: String,
        @Field("public_id") publicId: String,
        @Field("base64_data") base64Data: String
    ): Response<UploadResponse>
    
    companion object {
        // For demo usage with ngrok - Update this URL when you have a new ngrok session
        const val BASE_URL = "https://nondiabolic-twanna-unsensitive.ngrok-free.dev/api/v1/"
        
        // For local development (when not using ngrok)
        // const val BASE_URL = "http://192.168.2.23:8000/api/v1/"
        
        // For Android Emulator (when not using ngrok):
        // const val BASE_URL = "http://10.0.2.2:8000/api/v1/"
    }
}