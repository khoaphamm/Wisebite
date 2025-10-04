package com.example.wisebitemerchant.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.wisebitemerchant.MainActivity
import com.example.wisebitemerchant.R
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest

/**
 * Background service that keeps the notification system running even when app is closed
 * This ensures merchants receive notifications 24/7 for new orders
 */
class MerchantBackgroundService : Service() {
    
    companion object {
        private const val SERVICE_ID = 1001
        private const val CHANNEL_ID = "wisebite_background_service"
        private const val CHANNEL_NAME = "WiseBite Background Service"
        
        fun startService(context: Context, authToken: String) {
            val intent = Intent(context, MerchantBackgroundService::class.java).apply {
                putExtra("auth_token", authToken)
                action = "START_BACKGROUND_NOTIFICATIONS"
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stopService(context: Context) {
            val intent = Intent(context, MerchantBackgroundService::class.java).apply {
                action = "STOP_BACKGROUND_NOTIFICATIONS"
            }
            context.stopService(intent)
        }
    }
    
    private var serviceScope: CoroutineScope? = null
    private var notificationService: MerchantNotificationService? = null
    private var authToken: String? = null
    
    override fun onCreate() {
        super.onCreate()
        
        createServiceNotificationChannel()
        serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        
        // Get notification service instance
        notificationService = MerchantNotificationService.getInstance(this)
        
        android.util.Log.d("BackgroundService", "Merchant background service created")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "START_BACKGROUND_NOTIFICATIONS" -> {
                authToken = intent.getStringExtra("auth_token")
                startForegroundService()
                startNotificationListening()
            }
            "STOP_BACKGROUND_NOTIFICATIONS" -> {
                stopSelf()
            }
        }
        
        // Return START_STICKY to restart service if killed by system
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        
        // Clean up notification service
        notificationService?.stopNotificationService()
        
        // Cancel all coroutines
        serviceScope?.cancel()
        
        android.util.Log.d("BackgroundService", "Merchant background service destroyed")
    }
    
    private fun createServiceNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW // Low importance for persistent notification
            ).apply {
                description = "Keeps WiseBite merchant notifications running in background"
                setShowBadge(false)
                enableVibration(false)
                enableLights(false)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun startForegroundService() {
        val notification = createServiceNotification()
        startForeground(SERVICE_ID, notification)
        android.util.Log.d("BackgroundService", "Started foreground service for merchant notifications")
    }
    
    private fun createServiceNotification(): Notification {
        // Intent to open app when notification is tapped
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("WiseBite Merchant")
            .setContentText("Listening for new orders...")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true) // Make it persistent
            .setSilent(true) // No sound for service notification
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }
    
    private fun startNotificationListening() {
        authToken?.let { token ->
            // Start the notification service
            notificationService?.startNotificationService(token)
            
            // Listen for new notifications and update service notification
            serviceScope?.launch {
                notificationService?.notifications?.collectLatest { notification ->
                    updateServiceNotification(notification)
                }
            }
            
            // Monitor unread count
            serviceScope?.launch {
                notificationService?.unreadCount?.collectLatest { count ->
                    updateServiceNotificationWithUnreadCount(count)
                }
            }
            
            android.util.Log.d("BackgroundService", "Started notification listening with token")
        } ?: run {
            android.util.Log.e("BackgroundService", "No auth token provided")
            stopSelf()
        }
    }
    
    private fun updateServiceNotification(notification: MerchantNotification) {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val updatedNotification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("WiseBite Merchant")
            .setContentText("Latest: ${notification.title}")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(SERVICE_ID, updatedNotification)
    }
    
    private fun updateServiceNotificationWithUnreadCount(unreadCount: Int) {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val contentText = if (unreadCount > 0) {
            "🔔 $unreadCount unread notifications"
        } else {
            "Listening for new orders..."
        }
        
        val updatedNotification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("WiseBite Merchant")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setNumber(unreadCount) // Show badge count
            .build()
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(SERVICE_ID, updatedNotification)
    }
}