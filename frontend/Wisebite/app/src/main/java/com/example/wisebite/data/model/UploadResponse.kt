package com.example.wisebite.data.model

import com.google.gson.annotations.SerializedName

data class UploadResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("image_url")
    val imageUrl: String? = null,
    
    @SerializedName("image_urls")
    val imageUrls: List<String>? = null,
    
    @SerializedName("user")
    val user: User? = null
)