package com.example.wisebite.data.model

import com.google.gson.annotations.SerializedName

data class Store(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("address")
    val address: String,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("logo_url")
    val logoUrl: String? = null,
    
    @SerializedName("owner_id")
    val ownerId: String? = null,
    
    @SerializedName("latitude")
    val latitude: Double? = null,
    
    @SerializedName("longitude")
    val longitude: Double? = null
) {
    // Helper methods
    val displayName: String
        get() = name
    
    val displayAddress: String
        get() = address
    
    val displayDescription: String
        get() = description ?: "Cửa hàng uy tín, chất lượng"
}