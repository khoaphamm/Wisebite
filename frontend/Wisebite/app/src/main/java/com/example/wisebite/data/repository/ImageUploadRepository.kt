package com.example.wisebite.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.example.wisebite.data.model.UploadResponse
import com.example.wisebite.data.remote.RetrofitClient
import com.example.wisebite.data.remote.WisebiteApiService
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class ImageUploadRepository private constructor(
    private val apiService: WisebiteApiService,
    private val tokenManager: TokenManager,
    private val context: Context
) {
    companion object {
        @Volatile
        private var INSTANCE: ImageUploadRepository? = null
        
        fun getInstance(context: Context): ImageUploadRepository {
            return INSTANCE ?: synchronized(this) {
                val apiService = RetrofitClient.apiService
                val tokenManager = TokenManager.getInstance(context)
                val instance = ImageUploadRepository(apiService, tokenManager, context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
    
    private suspend fun getAuthToken(): String {
        val token = tokenManager.getToken().first()
        return "Bearer $token"
    }
    
    private fun createImagePart(uri: Uri, partName: String): MultipartBody.Part {
        // Create a temporary file from URI
        val inputStream: InputStream = context.contentResolver.openInputStream(uri)!!
        val file = File(context.cacheDir, "${System.currentTimeMillis()}_upload.jpg")
        val outputStream = FileOutputStream(file)
        
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
        
        // Create RequestBody and MultipartBody.Part
        val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, file.name, requestBody)
    }
    
    private fun bitmapToBase64(bitmap: Bitmap, quality: Int = 85): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
    
    suspend fun uploadAvatar(imageUri: Uri): Result<UploadResponse> {
        return try {
            val imagePart = createImagePart(imageUri, "file")
            val authToken = getAuthToken()
            val response = apiService.uploadAvatar(authToken, imagePart)
            
            if (response.isSuccessful) {
                response.body()?.let { uploadResponse ->
                    Result.success(uploadResponse)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Upload failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("ImageUploadRepository", "Error uploading avatar", e)
            Result.failure(e)
        }
    }
    
    suspend fun uploadStoreImage(storeId: Int, imageUri: Uri): Result<UploadResponse> {
        return try {
            val imagePart = createImagePart(imageUri, "file")
            val authToken = getAuthToken()
            val response = apiService.uploadStoreImage(storeId, authToken, imagePart)
            
            if (response.isSuccessful) {
                response.body()?.let { uploadResponse ->
                    Result.success(uploadResponse)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Upload failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("ImageUploadRepository", "Error uploading store image", e)
            Result.failure(e)
        }
    }
    
    suspend fun uploadFoodItemImage(foodItemId: Int, imageUri: Uri): Result<UploadResponse> {
        return try {
            val imagePart = createImagePart(imageUri, "file")
            val authToken = getAuthToken()
            val response = apiService.uploadFoodItemImage(foodItemId, authToken, imagePart)
            
            if (response.isSuccessful) {
                response.body()?.let { uploadResponse ->
                    Result.success(uploadResponse)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Upload failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("ImageUploadRepository", "Error uploading food item image", e)
            Result.failure(e)
        }
    }
    
    suspend fun uploadSurpriseBagImages(bagId: Int, imageUris: List<Uri>): Result<UploadResponse> {
        return try {
            val imageParts = imageUris.mapIndexed { index, uri ->
                createImagePart(uri, "files")
            }
            
            val authToken = getAuthToken()
            val response = apiService.uploadSurpriseBagImages(bagId, authToken, imageParts)
            
            if (response.isSuccessful) {
                response.body()?.let { uploadResponse ->
                    Result.success(uploadResponse)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Upload failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("ImageUploadRepository", "Error uploading surprise bag images", e)
            Result.failure(e)
        }
    }
    
    suspend fun uploadBase64Image(
        bitmap: Bitmap, 
        folder: String, 
        publicId: String,
        quality: Int = 85
    ): Result<UploadResponse> {
        return try {
            val base64Data = bitmapToBase64(bitmap, quality)
            val authToken = getAuthToken()
            val response = apiService.uploadBase64Image(authToken, folder, publicId, base64Data)
            
            if (response.isSuccessful) {
                response.body()?.let { uploadResponse ->
                    Result.success(uploadResponse)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Upload failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("ImageUploadRepository", "Error uploading base64 image", e)
            Result.failure(e)
        }
    }
}