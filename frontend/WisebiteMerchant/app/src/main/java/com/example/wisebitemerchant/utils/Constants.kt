package com.example.wisebitemerchant.utils

/**
 * Application constants and configuration
 */
object Constants {
    // API Configuration
    const val BASE_URL = "https://nondiabolic-twanna-unsensitive.ngrok-free.dev/"
    const val API_VERSION = "api/v1/"
    
    // SharedPreferences keys
    const val PREF_NAME = "wisebite_merchant"
    const val PREF_ACCESS_TOKEN = "access_token"
    const val PREF_USER_ID = "user_id"
    const val PREF_USER_EMAIL = "user_email"
    const val PREF_STORE_ID = "store_id"
    
    // API Endpoints
    object Endpoints {
        const val LOGIN = "auth/login"
        const val MERCHANT_FOOD_ITEMS = "merchant/food-items/"
        const val CATEGORIES_HIERARCHY = "merchant/food-items/categories/hierarchy"
    }
    
    // Request timeout configuration
    const val CONNECT_TIMEOUT = 30L // seconds
    const val READ_TIMEOUT = 30L // seconds
    const val WRITE_TIMEOUT = 30L // seconds
}