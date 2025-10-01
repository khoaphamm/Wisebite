package com.example.wisebite.data.repository

import android.content.Context
import com.example.wisebite.data.model.GoogleSignInRequest
import com.example.wisebite.data.model.LoginRequest
import com.example.wisebite.data.model.LoginResponse
import com.example.wisebite.data.model.SignupRequest
import com.example.wisebite.data.model.User
import com.example.wisebite.data.model.UserUpdateRequest
import com.example.wisebite.data.model.ForgotPasswordRequest
import com.example.wisebite.data.model.ResetPasswordRequest
import com.example.wisebite.data.remote.RetrofitClient
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import retrofit2.Response

class AuthRepository private constructor(
    private val context: Context,
    private val tokenManager: TokenManager,
    private val gson: Gson = Gson()
) {
    
    companion object {
        @Volatile
        private var INSTANCE: AuthRepository? = null
        
        fun getInstance(context: Context): AuthRepository {
            return INSTANCE ?: synchronized(this) {
                val tokenManager = TokenManager.getInstance(context)
                INSTANCE ?: AuthRepository(context.applicationContext, tokenManager).also { INSTANCE = it }
            }
        }
    }
    
    suspend fun login(phoneNumber: String, password: String): Result<LoginResponse> {
        return try {
            android.util.Log.d("AuthRepository", "Attempting login with phone: $phoneNumber")
            val response = RetrofitClient.apiService.login(
                username = phoneNumber, // Backend expects 'username' field
                password = password
            )
            android.util.Log.d("AuthRepository", "Login response code: ${response.code()}")
            
            if (response.isSuccessful) {
                val loginResponse = response.body()
                if (loginResponse != null) {
                    // Save tokens with expiration info
                    tokenManager.saveTokens(
                        accessToken = loginResponse.accessToken,
                        refreshToken = loginResponse.refreshToken,
                        expiresIn = loginResponse.expiresIn
                    )
                    
                    // Fetch user data with the new token
                    try {
                        val userResponse = RetrofitClient.apiService.getCurrentUser("Bearer ${loginResponse.accessToken}")
                        if (userResponse.isSuccessful) {
                            val user = userResponse.body()
                            if (user != null) {
                                tokenManager.saveUserJson(gson.toJson(user))
                                android.util.Log.d("AuthRepository", "Login successful, user data saved")
                            }
                        } else {
                            android.util.Log.w("AuthRepository", "Failed to fetch user data: ${userResponse.code()}")
                        }
                    } catch (e: Exception) {
                        android.util.Log.w("AuthRepository", "Error fetching user data", e)
                    }
                    
                    Result.success(loginResponse)
                } else {
                    Result.failure(Exception("Login response is null"))
                }
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Invalid phone number or password"
                    422 -> "Please check your input and try again"
                    else -> "Login failed. Please try again."
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error. Please check your connection."))
        }
    }
    
    suspend fun signup(
        fullName: String,
        email: String,
        phoneNumber: String,
        password: String
    ): Result<User> {
        return try {
            val signupRequest = SignupRequest(
                fullName = fullName,
                email = email,
                phoneNumber = phoneNumber,
                password = password,
                role = "customer"
            )
            
            android.util.Log.d("AuthRepository", "Sending signup request: $signupRequest")
            val response = RetrofitClient.apiService.signup(signupRequest)
            android.util.Log.d("AuthRepository", "Signup response code: ${response.code()}")
            
            if (response.isSuccessful) {
                val user = response.body()
                if (user != null) {
                    android.util.Log.d("AuthRepository", "Signup successful: $user")
                    Result.success(user)
                } else {
                    android.util.Log.e("AuthRepository", "Signup response body is null")
                    Result.failure(Exception("Signup response is null"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("AuthRepository", "Signup failed with code ${response.code()}: $errorBody")
                
                val errorMessage = when (response.code()) {
                    400 -> "User with this phone number or email already exists"
                    422 -> "Please check your input and try again"
                    500 -> "Server error. Please try again later."
                    else -> "Signup failed. Please try again. (Code: ${response.code()})"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: java.net.ConnectException) {
            android.util.Log.e("AuthRepository", "Connection failed", e)
            Result.failure(Exception("Cannot connect to server. Please check:\n1. Backend is running\n2. Network connection\n3. Correct IP address"))
        } catch (e: java.net.UnknownHostException) {
            android.util.Log.e("AuthRepository", "Unknown host", e)
            Result.failure(Exception("Cannot reach server. Please check your network connection."))
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Signup error", e)
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }
    
    suspend fun signInWithGoogle(idToken: String): Result<LoginResponse> {
        return try {
            android.util.Log.d("AuthRepository", "Attempting Google sign-in")
            val response = RetrofitClient.apiService.signInWithGoogle(
                GoogleSignInRequest(id_token = idToken)
            )
            android.util.Log.d("AuthRepository", "Google sign-in response code: ${response.code()}")
            
            if (response.isSuccessful) {
                val loginResponse = response.body()
                if (loginResponse != null) {
                    // Save tokens with expiration info
                    tokenManager.saveTokens(
                        accessToken = loginResponse.accessToken,
                        refreshToken = loginResponse.refreshToken,
                        expiresIn = loginResponse.expiresIn
                    )
                    
                    // Fetch user data with the new token
                    try {
                        val userResponse = RetrofitClient.apiService.getCurrentUser("Bearer ${loginResponse.accessToken}")
                        if (userResponse.isSuccessful) {
                            val user = userResponse.body()
                            if (user != null) {
                                tokenManager.saveUserJson(gson.toJson(user))
                                android.util.Log.d("AuthRepository", "Google sign-in successful for user: ${user.email}")
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.w("AuthRepository", "Failed to fetch user data after Google sign-in", e)
                    }
                    
                    Result.success(loginResponse)
                } else {
                    Result.failure(Exception("Login response is null"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("AuthRepository", "Google sign-in failed: $errorBody")
                Result.failure(Exception(errorBody ?: "Google sign-in failed"))
            }
        } catch (e: java.net.ConnectException) {
            android.util.Log.e("AuthRepository", "Connection failed", e)
            Result.failure(Exception("Cannot connect to server"))
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Google sign-in error", e)
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }
    
    suspend fun getCurrentUser(): Result<User> {
        return executeWithTokenRefresh { authHeader ->
            RetrofitClient.apiService.getCurrentUser(authHeader)
        }.onSuccess { user ->
            // Save updated user data
            tokenManager.saveUserJson(gson.toJson(user))
            android.util.Log.d("AuthRepository", "getCurrentUser - success: ${user.email}")
        }.onFailure { exception ->
            android.util.Log.e("AuthRepository", "getCurrentUser - failed: ${exception.message}")
        }
    }
    
    suspend fun updateUserProfile(
        fullName: String,
        email: String,
        phoneNumber: String
    ): Result<User> {
        return try {
            val authHeader = tokenManager.getAuthHeader().first()
            if (authHeader != null) {
                val updateRequest = UserUpdateRequest(
                    fullName = fullName,
                    email = email,
                    phoneNumber = phoneNumber
                )
                
                val response = RetrofitClient.apiService.updateCurrentUser(authHeader, updateRequest)
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        // Update stored user data
                        tokenManager.saveUserJson(gson.toJson(user))
                        Result.success(user)
                    } else {
                        Result.failure(Exception("Updated user data is null"))
                    }
                } else {
                    val errorMessage = when (response.code()) {
                        400 -> "Email or phone number already exists"
                        422 -> "Please check your input and try again"
                        else -> "Failed to update profile. Please try again."
                    }
                    Result.failure(Exception(errorMessage))
                }
            } else {
                Result.failure(Exception("No auth token available"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error. Please check your connection."))
        }
    }
    
    suspend fun logout() {
        tokenManager.clearAll()
    }
    
    suspend fun isLoggedIn(): Boolean {
        return tokenManager.getToken().first() != null
    }
    
    /**
     * Execute an API call with automatic token refresh on authentication failure
     */
    suspend fun <T> executeWithTokenRefresh(
        apiCall: suspend (authHeader: String) -> retrofit2.Response<T>
    ): Result<T> {
        return try {
            // Check if token is expired before making the call
            if (tokenManager.isTokenExpired()) {
                android.util.Log.d("AuthRepository", "Token is expired, need to re-authenticate")
                return Result.failure(Exception("Token expired - please login again"))
            }
            
            val authHeader = tokenManager.getAuthHeader().first()
            if (authHeader == null) {
                return Result.failure(Exception("No auth token available"))
            }
            
            // Make the initial API call
            val response = apiCall(authHeader)
            
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    Result.success(body)
                } ?: Result.failure(Exception("Response body is null"))
            } else if (response.code() == 401 || response.code() == 403) {
                android.util.Log.w("AuthRepository", "Auth failure (${response.code()}), clearing tokens")
                // Token is invalid, clear stored tokens
                tokenManager.clearAll()
                
                // Notify auth state manager about the failure
                try {
                    val authStateManager = AuthStateManager.getInstance(context)
                    authStateManager.handleAuthenticationFailure("Authentication expired")
                } catch (e: Exception) {
                    android.util.Log.e("AuthRepository", "Failed to notify auth state manager", e)
                }
                
                Result.failure(Exception("Authentication expired - please login again"))
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("API call failed: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Network error in executeWithTokenRefresh", e)
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }
    
    suspend fun getStoredUser(): User? {
        return try {
            val userJson = tokenManager.getUserJson().first()
            userJson?.let { gson.fromJson(it, User::class.java) }
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun requestPasswordReset(email: String): Result<String> {
        return try {
            android.util.Log.d("AuthRepository", "Requesting password reset for: $email")
            val response = RetrofitClient.apiService.requestPasswordReset(
                ForgotPasswordRequest(email = email)
            )
            android.util.Log.d("AuthRepository", "Password reset request response code: ${response.code()}")
            
            if (response.isSuccessful) {
                val messageResponse = response.body()
                if (messageResponse != null) {
                    android.util.Log.d("AuthRepository", "Password reset request successful")
                    Result.success(messageResponse.message)
                } else {
                    Result.failure(Exception("Response is null"))
                }
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "Invalid email address"
                    422 -> "Please check your input and try again"
                    500 -> "Server error. Please try again later."
                    else -> "Failed to send reset code. Please try again."
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: java.net.ConnectException) {
            android.util.Log.e("AuthRepository", "Connection failed", e)
            Result.failure(Exception("Cannot connect to server. Please check your connection."))
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Password reset request error", e)
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }
    
    suspend fun resetPassword(email: String, resetCode: String, newPassword: String): Result<String> {
        return try {
            android.util.Log.d("AuthRepository", "Resetting password for: $email")
            val response = RetrofitClient.apiService.resetPassword(
                ResetPasswordRequest(
                    email = email,
                    reset_code = resetCode,
                    new_password = newPassword
                )
            )
            android.util.Log.d("AuthRepository", "Password reset response code: ${response.code()}")
            
            if (response.isSuccessful) {
                val messageResponse = response.body()
                if (messageResponse != null) {
                    android.util.Log.d("AuthRepository", "Password reset successful")
                    Result.success(messageResponse.message)
                } else {
                    Result.failure(Exception("Response is null"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("AuthRepository", "Password reset failed: $errorBody")
                
                val errorMessage = when (response.code()) {
                    400 -> "Invalid reset code or code has expired"
                    422 -> "Please check your input and try again"
                    500 -> "Server error. Please try again later."
                    else -> "Failed to reset password. Please try again."
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: java.net.ConnectException) {
            android.util.Log.e("AuthRepository", "Connection failed", e)
            Result.failure(Exception("Cannot connect to server. Please check your connection."))
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Password reset error", e)
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }
}