package com.example.wisebite.data.remote

import com.example.wisebite.data.model.ForgotPasswordRequest
import com.example.wisebite.data.model.GoogleSignInRequest
import com.example.wisebite.data.model.LoginRequest
import com.example.wisebite.data.model.LoginResponse
import com.example.wisebite.data.model.MessageResponse
import com.example.wisebite.data.model.ResetPasswordRequest
import com.example.wisebite.data.model.SignupRequest
import com.example.wisebite.data.model.User
import com.example.wisebite.data.model.UserUpdateRequest
import com.example.wisebite.data.model.UploadResponse
import com.example.wisebite.data.model.CreateOrderRequest
import com.example.wisebite.data.model.Order
import com.example.wisebite.data.model.OrdersResponse
import com.example.wisebite.data.model.OrderStatusUpdateRequest
import com.example.wisebite.data.model.Store
import com.example.wisebite.data.model.SurpriseBag
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
import retrofit2.http.Query

interface WisebiteApiService {
    
    @FormUrlEncoded
    @POST("auth/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<LoginResponse>
    
    @POST("auth/signup")
    suspend fun signup(@Body signupRequest: SignupRequest): Response<User>
    
    @POST("auth/google-signin")
    suspend fun signInWithGoogle(@Body request: GoogleSignInRequest): Response<LoginResponse>
    
    @GET("user/me")
    suspend fun getCurrentUser(@Header("Authorization") token: String): Response<User>
    
    @PATCH("user/me")
    suspend fun updateCurrentUser(
        @Header("Authorization") token: String,
        @Body updateRequest: UserUpdateRequest
    ): Response<User>
    
    @POST("auth/forgot-password")
    suspend fun requestPasswordReset(@Body request: ForgotPasswordRequest): Response<MessageResponse>
    
    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<MessageResponse>
    
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
    
    // Order Endpoints
    @POST("orders/")
    suspend fun createOrder(
        @Header("Authorization") token: String,
        @Body orderRequest: CreateOrderRequest
    ): Response<Order>
    
    @GET("orders/me")
    suspend fun getUserOrders(
        @Header("Authorization") token: String
    ): Response<OrdersResponse>
    
    @GET("orders/{orderId}")
    suspend fun getOrder(
        @Header("Authorization") token: String,
        @Path("orderId") orderId: String
    ): Response<Order>
    
    @PATCH("orders/{orderId}/status")
    suspend fun updateOrderStatus(
        @Header("Authorization") token: String,
        @Path("orderId") orderId: String,
        @Body statusUpdate: OrderStatusUpdateRequest
    ): Response<Order>
    
    @POST("orders/{orderId}/cancel")
    suspend fun cancelOrder(
        @Header("Authorization") token: String,
        @Path("orderId") orderId: String
    ): Response<Order>
    
    // Customer Endpoints for browsing stores and surprise bags
    @GET("customer/stores")
    suspend fun getAvailableStores(
        @Query("city") city: String? = null
    ): Response<List<Store>>
    
    @GET("customer/stores/{store_id}/surprise-bags")
    suspend fun getStoreSurpriseBags(
        @Path("store_id") storeId: String,
        @Query("category") category: String? = null
    ): Response<List<SurpriseBag>>
    
    @GET("customer/surprise-bags")
    suspend fun getAllSurpriseBags(
        @Query("category") category: String? = null,
        @Query("city") city: String? = null,
        @Query("available_from") availableFrom: String? = null,
        @Query("available_until") availableUntil: String? = null,
        @Query("max_price") maxPrice: Double? = null
    ): Response<List<SurpriseBag>>
    
    @GET("customer/surprise-bags/{bag_id}")
    suspend fun getSurpriseBagDetails(
        @Path("bag_id") bagId: String
    ): Response<SurpriseBag>
    
    @GET("customer/categories")
    suspend fun getAvailableCategories(): Response<List<String>>
    
    companion object {
        // For demo usage with ngrok - Update this URL when you have a new ngrok session
        const val BASE_URL = "https://nondiabolic-twanna-unsensitive.ngrok-free.dev/api/v1/"
        
        // For local development (when not using ngrok)
        // const val BASE_URL = "http://192.168.2.23:8000/api/v1/"
        
        // For Android Emulator (when not using ngrok):
        // const val BASE_URL = "http://10.0.2.2:8000/api/v1/"
    }
}