package com.example.wisebitemerchant

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.example.wisebitemerchant.navigation.MerchantNavigation
import com.example.wisebitemerchant.ui.theme.WisebiteMerchantTheme
import com.example.wisebitemerchant.service.MerchantBackgroundService
import com.example.wisebitemerchant.receiver.BootReceiver

class MainActivity : ComponentActivity() {
    
    // Register for notification permission result
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            android.util.Log.d("MainActivity", "Notification permission granted")
        } else {
            android.util.Log.w("MainActivity", "Notification permission denied")
            // You might want to show a dialog explaining why notifications are important
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Request notification permission for Android 13+
        requestNotificationPermission()
        
        setContent {
            WisebiteMerchantTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    MerchantNavigation(navController = navController)
                }
            }
        }
    }
    
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                    android.util.Log.d("MainActivity", "Notification permission already granted")
                    // Start background service if user is logged in
                    startBackgroundServiceIfLoggedIn()
                }
                else -> {
                    // Request permission
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // For Android versions below 13, notification permission is automatically granted
            android.util.Log.d("MainActivity", "Notification permission not required for this Android version")
            // Start background service if user is logged in
            startBackgroundServiceIfLoggedIn()
        }
    }
    
    /**
     * Start background notification service if user is logged in
     * This enables notifications even when app is closed
     */
    private fun startBackgroundServiceIfLoggedIn() {
        // TODO: Replace with actual auth token from your login system
        // For now, using a placeholder - you should get this from SharedPreferences or your auth system
        val authToken = getAuthTokenFromStorage()
        
        if (!authToken.isNullOrEmpty()) {
            // Save settings for auto-start after boot
            BootReceiver.saveAutoStartSettings(this, authToken, true)
            
            // Start background service for notifications
            MerchantBackgroundService.startService(this, authToken)
            
            android.util.Log.d("MainActivity", "Background notification service started")
        } else {
            android.util.Log.d("MainActivity", "No auth token found, background service not started")
        }
    }
    
    /**
     * Get auth token from storage (replace with your actual implementation)
     */
    private fun getAuthTokenFromStorage(): String? {
        val prefs = getSharedPreferences("wisebite_merchant_prefs", MODE_PRIVATE)
        return prefs.getString("auth_token", null)
    }
    
    /**
     * Save auth token and start background service (call this after successful login)
     */
    fun startNotificationServiceAfterLogin(authToken: String) {
        // Save token
        val prefs = getSharedPreferences("wisebite_merchant_prefs", MODE_PRIVATE)
        prefs.edit().putString("auth_token", authToken).apply()
        
        // Save auto-start settings
        BootReceiver.saveAutoStartSettings(this, authToken, true)
        
        // Start background service
        MerchantBackgroundService.startService(this, authToken)
        
        android.util.Log.d("MainActivity", "Notification service started after login")
    }
    
    /**
     * Stop notification service (call this after logout)
     */
    fun stopNotificationServiceAfterLogout() {
        // Clear settings
        BootReceiver.clearAutoStartSettings(this)
        
        // Stop background service
        MerchantBackgroundService.stopService(this)
        
        android.util.Log.d("MainActivity", "Notification service stopped after logout")
    }
}