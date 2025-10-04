package com.example.wisebitemerchant.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.example.wisebitemerchant.service.MerchantBackgroundService

/**
 * Receiver that automatically starts the background notification service
 * when the device boots up or the app is updated
 */
class BootReceiver : BroadcastReceiver() {
    
    companion object {
        private const val PREFS_NAME = "wisebite_merchant_prefs"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_AUTO_START = "auto_start_notifications"
        
        /**
         * Save settings for auto-start functionality
         */
        fun saveAutoStartSettings(context: Context, authToken: String, autoStart: Boolean = true) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putString(KEY_AUTH_TOKEN, authToken)
                .putBoolean(KEY_AUTO_START, autoStart)
                .apply()
            
            android.util.Log.d("BootReceiver", "Saved auto-start settings: autoStart=$autoStart")
        }
        
        fun clearAutoStartSettings(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .remove(KEY_AUTH_TOKEN)
                .putBoolean(KEY_AUTO_START, false)
                .apply()
            
            android.util.Log.d("BootReceiver", "Cleared auto-start settings")
        }
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_REPLACED -> {
                android.util.Log.d("BootReceiver", "Device boot completed or app updated")
                
                // Check if user has enabled auto-start notifications
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val autoStart = prefs.getBoolean(KEY_AUTO_START, false)
                val authToken = prefs.getString(KEY_AUTH_TOKEN, null)
                
                if (autoStart && !authToken.isNullOrEmpty()) {
                    android.util.Log.d("BootReceiver", "Auto-starting merchant notification service")
                    MerchantBackgroundService.startService(context, authToken)
                } else {
                    android.util.Log.d("BootReceiver", "Auto-start disabled or no auth token")
                }
            }
        }
    }
}