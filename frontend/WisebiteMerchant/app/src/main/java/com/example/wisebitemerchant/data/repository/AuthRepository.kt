package com.example.wisebitemerchant.data.repository

import android.util.Log
import com.example.wisebitemerchant.data.manager.TokenManager
import com.example.wisebitemerchant.data.model.*
import com.example.wisebitemerchant.data.remote.MerchantApiService
import com.example.wisebitemerchant.model.GoogleSignInRequest
import kotlinx.coroutines.flow.first

class AuthRepository private constructor(
    private val apiService: MerchantApiService,
    private val tokenManager: TokenManager
) {
    
    companion object {
        @Volatile
        private var INSTANCE: AuthRepository? = null
        
        fun getInstance(apiService: MerchantApiService, tokenManager: TokenManager): AuthRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = AuthRepository(apiService, tokenManager)
                INSTANCE = instance
                instance
            }
        }
    }
    
    suspend fun login(phoneNumber: String, password: String): Result<LoginResponse> {
        return try {
            val response = apiService.login(phoneNumber, password)
            
            if (response.isSuccessful) {
                response.body()?.let { loginResponse ->
                    // Save tokens with simplified response
                    tokenManager.saveTokens(
                        accessToken = loginResponse.accessToken
                    )
                    Result.success(loginResponse)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Số điện thoại hoặc mật khẩu không chính xác"
                    403 -> "Tài khoản đã bị khóa"
                    404 -> "Tài khoản không tồn tại"
                    500 -> "Lỗi server. Vui lòng thử lại sau."
                    else -> "Đăng nhập thất bại"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Login error", e)
            Result.failure(Exception("Lỗi mạng. Vui lòng kiểm tra kết nối internet."))
        }
    }
    
    suspend fun signup(
        fullName: String,
        email: String,
        phoneNumber: String,
        password: String,
        storeName: String,
        storeAddress: String,
        cuisineType: String? = null
    ): Result<LoginResponse> {
        return try {
            // First, create the user account
            val userRequest = SignupRequest(
                fullName = fullName,
                email = email,
                phoneNumber = phoneNumber,
                password = password
            )
            
            val signupResponse = apiService.signup(userRequest)
            
            if (signupResponse.isSuccessful) {
                signupResponse.body()?.let { userResponse ->
                    // After successful signup, login to get tokens using email
                    val loginResponse = apiService.login(email, password)
                    
                    if (loginResponse.isSuccessful) {
                        loginResponse.body()?.let { loginData ->
                            // Save tokens with simplified response
                            tokenManager.saveTokens(
                                accessToken = loginData.accessToken
                            )
                            
                            Result.success(loginData)
                        } ?: Result.failure(Exception("Empty login response"))
                    } else {
                        val errorBody = loginResponse.errorBody()?.string()
                        Result.failure(Exception("Login failed after signup: $errorBody"))
                    }
                } ?: Result.failure(Exception("Empty signup response"))
            } else {
                val errorBody = signupResponse.errorBody()?.string()
                Log.e("AuthRepository", "Signup failed with code ${signupResponse.code()}: $errorBody")
                
                val errorMessage = when (signupResponse.code()) {
                    400 -> "Dữ liệu không hợp lệ"
                    409 -> "Số điện thoại hoặc email đã được sử dụng"
                    500 -> "Lỗi server. Vui lòng thử lại sau."
                    else -> "Đăng ký thất bại"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Signup error", e)
            Result.failure(Exception("Lỗi mạng: ${e.message}"))
        }
    }
    
    suspend fun signInWithGoogle(idToken: String): Result<LoginResponse> {
        return try {
            Log.d("AuthRepository", "Attempting Google sign-in for merchant")
            val response = apiService.signInWithGoogle(
                GoogleSignInRequest(id_token = idToken)
            )
            Log.d("AuthRepository", "Google sign-in response code: ${response.code()}")
            
            if (response.isSuccessful) {
                val loginResponse = response.body()
                if (loginResponse != null) {
                    Log.d("AuthRepository", "Google sign-in successful, saving token: ${loginResponse.accessToken.take(20)}...")
                    
                    // Save tokens with simplified response
                    tokenManager.saveTokens(
                        accessToken = loginResponse.accessToken
                    )
                    
                    Log.d("AuthRepository", "Token saved successfully")
                    Result.success(loginResponse)
                } else {
                    Log.e("AuthRepository", "Google sign-in response body is null")
                    Result.failure(Exception("Empty response from server"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("AuthRepository", "Google sign-in failed with code ${response.code()}: $errorBody")
                
                val errorMessage = when (response.code()) {
                    400 -> "Token Google không hợp lệ"
                    401 -> "Xác thực Google thất bại"
                    403 -> "Tài khoản không có quyền merchant"
                    404 -> "Tài khoản chưa được đăng ký"
                    500 -> "Lỗi server. Vui lòng thử lại sau."
                    else -> "Đăng nhập Google thất bại"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: java.net.UnknownHostException) {
            Log.e("AuthRepository", "Unknown host during Google sign-in", e)
            Result.failure(Exception("Không thể kết nối đến server. Vui lòng kiểm tra mạng."))
        } catch (e: Exception) {
            Log.e("AuthRepository", "Google sign-in error", e)
            Result.failure(Exception("Lỗi mạng: ${e.message}"))
        }
    }
    
    suspend fun getCurrentUser(): Result<MerchantUser> {
        return try {
            val authToken = tokenManager.getAuthToken()
            if (authToken == null) {
                return Result.failure(Exception("Vui lòng đăng nhập lại"))
            }
            
            val response = apiService.getCurrentUser(authToken)
            
            if (response.isSuccessful) {
                response.body()?.let { user ->
                    Result.success(user)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Phiên đăng nhập đã hết hạn"
                    403 -> "Không có quyền truy cập"
                    404 -> "Người dùng không tồn tại"
                    else -> "Không thể lấy thông tin người dùng"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Get user error", e)
            Result.failure(Exception("Lỗi mạng. Vui lòng kiểm tra kết nối."))
        }
    }
    
    suspend fun logout(): Result<Unit> {
        return try {
            tokenManager.clearTokens()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Logout error", e)
            Result.failure(e)
        }
    }
    
    suspend fun isAuthenticated(): Boolean {
        return try {
            val token = tokenManager.getToken().first()
            !token.isNullOrEmpty() && !tokenManager.isTokenExpired()
        } catch (e: Exception) {
            Log.e("AuthRepository", "Check authentication error", e)
            false
        }
    }
}