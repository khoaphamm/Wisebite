package com.example.wisebite.service

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.CancellationException

data class GoogleSignInResult(
    val success: Boolean,
    val idToken: String? = null,
    val email: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val errorMessage: String? = null
)

class GoogleSignInService(private val context: Context) {
    
    private val credentialManager = CredentialManager.create(context)
    
    // Using your actual WEB Client ID from Google Cloud Console
    // This is the same ID used in your backend .env file
    private val webClientId = "1066643707737-l8mkrv7beu7k19hnn23reqnqh936k5b3.apps.googleusercontent.com"
    
    suspend fun signIn(): GoogleSignInResult {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(true)
                .build()
            
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
            
            val result = credentialManager.getCredential(
                request = request,
                context = context,
            )
            
            handleSignInResult(result)
            
        } catch (e: GetCredentialException) {
            Log.e("GoogleSignIn", "GetCredentialException: ${e.message}", e)
            GoogleSignInResult(
                success = false,
                errorMessage = "Đăng nhập Google thất bại: ${e.message}"
            )
        } catch (e: CancellationException) {
            Log.e("GoogleSignIn", "Sign-in was cancelled", e)
            GoogleSignInResult(
                success = false,
                errorMessage = "Đăng nhập đã bị hủy"
            )
        } catch (e: Exception) {
            Log.e("GoogleSignIn", "Unexpected error during sign-in", e)
            GoogleSignInResult(
                success = false,
                errorMessage = "Có lỗi xảy ra: ${e.message}"
            )
        }
    }
    
    private fun handleSignInResult(result: GetCredentialResponse): GoogleSignInResult {
        return try {
            val credential = result.credential
            
            if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                
                GoogleSignInResult(
                    success = true,
                    idToken = googleIdTokenCredential.idToken,
                    email = googleIdTokenCredential.id,
                    displayName = googleIdTokenCredential.displayName,
                    photoUrl = googleIdTokenCredential.profilePictureUri?.toString()
                )
            } else {
                Log.e("GoogleSignIn", "Unexpected credential type: ${credential.type}")
                GoogleSignInResult(
                    success = false,
                    errorMessage = "Loại credential không được hỗ trợ"
                )
            }
        } catch (e: GoogleIdTokenParsingException) {
            Log.e("GoogleSignIn", "Invalid Google ID token", e)
            GoogleSignInResult(
                success = false,
                errorMessage = "Token Google không hợp lệ"
            )
        } catch (e: Exception) {
            Log.e("GoogleSignIn", "Error parsing credential", e)
            GoogleSignInResult(
                success = false,
                errorMessage = "Lỗi xử lý thông tin đăng nhập"
            )
        }
    }
}