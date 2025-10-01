package com.example.wisebitemerchant.data.manager

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Date

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "merchant_auth")

class TokenManager private constructor(private val context: Context) {
    
    companion object {
        @Volatile
        private var INSTANCE: TokenManager? = null
        
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val TOKEN_EXPIRY_KEY = stringPreferencesKey("token_expiry")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val STORE_ID_KEY = stringPreferencesKey("store_id")
        
        fun getInstance(context: Context): TokenManager {
            return INSTANCE ?: synchronized(this) {
                val instance = TokenManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
    
    suspend fun saveTokens(
        accessToken: String,
        refreshToken: String? = null,
        expiresIn: Int? = null,
        userId: String? = null,
        storeId: String? = null
    ) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
            refreshToken?.let { preferences[REFRESH_TOKEN_KEY] = it }
            userId?.let { preferences[USER_ID_KEY] = it }
            storeId?.let { preferences[STORE_ID_KEY] = it }
            
            // Calculate expiry time (default 7 days if not provided)
            val expiryTime = System.currentTimeMillis() + ((expiresIn ?: 604800) * 1000L)
            preferences[TOKEN_EXPIRY_KEY] = expiryTime.toString()
        }
    }
    
    fun getToken(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[ACCESS_TOKEN_KEY]
        }
    }
    
    fun getRefreshToken(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[REFRESH_TOKEN_KEY]
        }
    }
    
    fun getUserId(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[USER_ID_KEY]
        }
    }
    
    fun getStoreId(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[STORE_ID_KEY]
        }
    }
    
    suspend fun getAuthToken(): String? {
        // Check if token is expired before using it
        if (isTokenExpired()) {
            android.util.Log.w("TokenManager", "Token is expired")
            return null
        }
        
        val token = getToken().first()
        return if (token != null) "Bearer $token" else null
    }
    
    suspend fun isTokenExpired(): Boolean {
        val expiryString = context.dataStore.data.first()[TOKEN_EXPIRY_KEY]
        if (expiryString == null) return true
        
        return try {
            val expiryTime = expiryString.toLongOrNull() ?: return true
            val now = System.currentTimeMillis()
            // Add 1-minute buffer to prevent edge cases
            now > (expiryTime - 60000)
        } catch (e: Exception) {
            android.util.Log.e("TokenManager", "Error parsing expiry time", e)
            true
        }
    }
    
    suspend fun clearTokens() {
        context.dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN_KEY)
            preferences.remove(REFRESH_TOKEN_KEY)
            preferences.remove(TOKEN_EXPIRY_KEY)
            preferences.remove(USER_ID_KEY)
            preferences.remove(STORE_ID_KEY)
        }
    }
}