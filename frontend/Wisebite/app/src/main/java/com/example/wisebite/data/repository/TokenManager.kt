package com.example.wisebite.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

class TokenManager private constructor(private val context: Context) {
    
    companion object {
        @Volatile
        private var INSTANCE: TokenManager? = null
        
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("auth_tokens")
        
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val TOKEN_EXPIRY_KEY = stringPreferencesKey("token_expiry")
        private val USER_JSON_KEY = stringPreferencesKey("user_json")
        
        fun getInstance(context: Context): TokenManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TokenManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    suspend fun saveToken(token: String) {
        saveTokens(token, null, null)
    }
    
    suspend fun saveTokens(accessToken: String, refreshToken: String?, expiresIn: Int?) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
            refreshToken?.let { preferences[REFRESH_TOKEN_KEY] = it }
            
            // Calculate expiry time if provided
            expiresIn?.let {
                val expiryTime = System.currentTimeMillis() + (it * 1000L) // Convert to milliseconds
                preferences[TOKEN_EXPIRY_KEY] = expiryTime.toString()
            }
        }
    }
    
    suspend fun saveRefreshToken(refreshToken: String) {
        context.dataStore.edit { preferences ->
            preferences[REFRESH_TOKEN_KEY] = refreshToken
        }
    }
    
    suspend fun saveUserJson(userJson: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_JSON_KEY] = userJson
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
    
    suspend fun isTokenExpired(): Boolean {
        return context.dataStore.data.map { preferences ->
            val expiryString = preferences[TOKEN_EXPIRY_KEY]
            if (expiryString != null) {
                val expiryTime = expiryString.toLongOrNull() ?: 0L
                System.currentTimeMillis() > expiryTime - 60000L // Consider expired 1 minute early
            } else {
                false // If no expiry time, assume token is valid
            }
        }.first()
    }
    
    fun getUserJson(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[USER_JSON_KEY]
        }
    }
    
    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    fun getAuthHeader(): Flow<String?> {
        return getToken().map { token ->
            token?.let { "Bearer $it" }
        }
    }
}