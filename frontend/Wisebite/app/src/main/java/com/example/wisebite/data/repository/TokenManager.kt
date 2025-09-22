package com.example.wisebite.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TokenManager private constructor(private val context: Context) {
    
    companion object {
        @Volatile
        private var INSTANCE: TokenManager? = null
        
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("auth_tokens")
        
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val USER_JSON_KEY = stringPreferencesKey("user_json")
        
        fun getInstance(context: Context): TokenManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TokenManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = token
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