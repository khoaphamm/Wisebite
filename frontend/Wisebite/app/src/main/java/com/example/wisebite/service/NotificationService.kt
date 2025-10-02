package com.example.wisebite.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.wisebite.R
import com.example.wisebite.data.repository.NotificationRepository
import com.example.wisebite.data.model.Notification as ApiNotification
import com.example.wisebite.data.model.ApiResult
import com.example.wisebite.data.model.toWisebiteNotification
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class WisebiteNotification(
    val id: String,
    val title: String,
    val message: String,
    val type: NotificationType = NotificationType.ORDER_UPDATE,
    val isImportant: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

enum class NotificationType {
    ORDER_UPDATE,
    PROMOTION,
    SYSTEM,
    PICKUP_REMINDER
}

class NotificationService private constructor(private val context: Context) {
    companion object {
        private const val CHANNEL_ID = "wisebite_notifications"
        private const val CHANNEL_NAME = "WiseBite Notifications"
        private const val WS_URL = "wss://nondiabolic-twanna-unsensitive.ngrok-free.dev/api/v1/ws/notifications"
        
        @Volatile
        private var INSTANCE: NotificationService? = null
        
        fun getInstance(context: Context): NotificationService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NotificationService(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private var webSocket: WebSocket? = null
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val notificationRepository = NotificationRepository.getInstance()
    
    // Authentication token for API calls
    private var authToken: String? = null
    
    // Real-time notifications from WebSocket
    private val _realtimeNotifications = MutableSharedFlow<WisebiteNotification>()
    val realtimeNotifications: SharedFlow<WisebiteNotification> = _realtimeNotifications.asSharedFlow()
    
    // All notifications (API + real-time)
    private val _allNotifications = MutableStateFlow<List<WisebiteNotification>>(emptyList())
    val allNotifications: StateFlow<List<WisebiteNotification>> = _allNotifications.asStateFlow()
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isConnected = false
    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 5
    
    init {
        createNotificationChannel()
    }
    
    fun startNotificationService(authToken: String) {
        this.authToken = authToken
        serviceScope.launch {
            // Load existing notifications from API
            loadNotificationsFromApi(authToken)
            // Connect to WebSocket for real-time updates
            connectWebSocket(authToken)
        }
    }
    
    private suspend fun loadNotificationsFromApi(authToken: String) {
        when (val result = notificationRepository.loadNotifications(authToken)) {
            is ApiResult.Success -> {
                val notifications = result.data.map { apiNotification ->
                    apiNotification.toWisebiteNotification()
                }
                _allNotifications.value = notifications
            }
            is ApiResult.Error -> {
                println("Failed to load notifications from API: ${result.message}")
            }
            is ApiResult.Loading -> {
                // Handle loading state if needed
            }
        }
    }
    
    suspend fun markNotificationAsRead(notificationId: String): Boolean {
        authToken?.let { token ->
            when (val result = notificationRepository.markNotificationAsRead(token, notificationId)) {
                is ApiResult.Success -> {
                    // Update local state
                    val currentNotifications = _allNotifications.value.toMutableList()
                    val index = currentNotifications.indexOfFirst { it.id == notificationId }
                    if (index != -1) {
                        currentNotifications[index] = currentNotifications[index].copy(isRead = true)
                        _allNotifications.value = currentNotifications
                    }
                    return true
                }
                is ApiResult.Error -> {
                    println("Failed to mark notification as read: ${result.message}")
                    return false
                }
                is ApiResult.Loading -> {
                    return false
                }
            }
        }
        return false
    }
    
    fun refreshNotifications() {
        authToken?.let { token ->
            serviceScope.launch {
                loadNotificationsFromApi(token)
            }
        }
    }
    
    fun stopNotificationService() {
        webSocket?.close(1000, "Service stopped")
        webSocket = null
        isConnected = false
        reconnectAttempts = 0
    }
    
    private fun connectWebSocket(authToken: String) {
        val request = Request.Builder()
            .url(WS_URL)
            .addHeader("Authorization", "Bearer $authToken")
            .build()
        
        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                isConnected = true
                reconnectAttempts = 0
                println("WebSocket connected for notifications")
            }
            
            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    val notification = WisebiteNotification(
                        id = json.optString("id", ""),
                        title = json.getString("title"),
                        message = json.getString("message"),
                        type = NotificationType.valueOf(
                            json.optString("type", "ORDER_UPDATE")
                        ),
                        isImportant = json.optBoolean("is_important", false)
                    )
                    
                    serviceScope.launch {
                        _realtimeNotifications.emit(notification)
                        
                        // Add to all notifications list
                        val currentNotifications = _allNotifications.value.toMutableList()
                        currentNotifications.add(0, notification) // Add to beginning
                        _allNotifications.value = currentNotifications
                        
                        showPushNotification(notification)
                    }
                } catch (e: Exception) {
                    println("Error parsing notification: ${e.message}")
                }
            }
            
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                isConnected = false
                println("WebSocket failed: ${t.message}")
                
                // Attempt to reconnect with exponential backoff
                if (reconnectAttempts < maxReconnectAttempts) {
                    serviceScope.launch {
                        delay((1000 * Math.pow(2.0, reconnectAttempts.toDouble())).toLong())
                        reconnectAttempts++
                        connectWebSocket(authToken)
                    }
                }
            }
            
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                isConnected = false
                println("WebSocket closed: $reason")
            }
        })
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications from WiseBite app"
                enableVibration(true)
                setShowBadge(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun showPushNotification(notification: WisebiteNotification) {
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // You'll need to add this icon
            .setContentTitle(notification.title)
            .setContentText(notification.message)
            .setPriority(if (notification.isImportant) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setVibrate(if (notification.isImportant) longArrayOf(0, 300, 200, 300) else longArrayOf(0, 200))
        
        try {
            with(NotificationManagerCompat.from(context)) {
                notify(notification.id.hashCode(), notificationBuilder.build())
            }
        } catch (e: SecurityException) {
            println("Permission denied for showing notification: ${e.message}")
        }
    }
    
    // Simulate real-time notifications for testing (fallback when WebSocket is not available)
    fun simulateOrderNotification(orderId: String, status: String) {
        serviceScope.launch {
            val notification = WisebiteNotification(
                id = "order_$orderId",
                title = "Cập nhật đơn hàng",
                message = "Đơn hàng #${orderId.take(8)} đã được $status",
                type = NotificationType.ORDER_UPDATE,
                isImportant = true
            )
            
            _realtimeNotifications.emit(notification)
            showPushNotification(notification)
        }
    }
    
    fun simulatePickupReminder(orderId: String, pickupTime: String) {
        serviceScope.launch {
            val notification = WisebiteNotification(
                id = "pickup_$orderId",
                title = "⏰ Nhắc nhở nhận hàng",
                message = "Đến giờ nhận đơn hàng #${orderId.take(8)} lúc $pickupTime!",
                type = NotificationType.PICKUP_REMINDER,
                isImportant = true
            )
            
            _realtimeNotifications.emit(notification)
            showPushNotification(notification)
        }
    }
}