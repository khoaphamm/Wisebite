package com.example.wisebitemerchant.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.wisebitemerchant.R
import com.example.wisebitemerchant.data.repository.NotificationRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class MerchantNotification(
    val id: String,
    val title: String,
    val message: String,
    val type: MerchantNotificationType = MerchantNotificationType.NEW_ORDER,
    val isImportant: Boolean = false,
    val orderId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MerchantNotificationType {
    NEW_ORDER,
    ORDER_ACCEPTED,
    ORDER_CANCELLED,
    PICKUP_READY,
    SYSTEM_UPDATE,
    PAYMENT_RECEIVED
}

class MerchantNotificationService private constructor(private val context: Context) {
    companion object {
        private const val CHANNEL_ID = "wisebite_merchant_notifications"
        private const val CHANNEL_NAME = "WiseBite Merchant Notifications"
        private const val WS_URL = "wss://nondiabolic-twanna-unsensitive.ngrok-free.dev/api/v1/ws/merchant/notifications"
        
        @Volatile
        private var INSTANCE: MerchantNotificationService? = null
        
        fun getInstance(context: Context): MerchantNotificationService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MerchantNotificationService(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private var webSocket: WebSocket? = null
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val _notifications = MutableSharedFlow<MerchantNotification>()
    val notifications: SharedFlow<MerchantNotification> = _notifications.asSharedFlow()
    
    private val _notificationList = MutableStateFlow<List<MerchantNotification>>(emptyList())
    val notificationList: StateFlow<List<MerchantNotification>> = _notificationList.asStateFlow()
    
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isConnected = false
    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 5
    
    // Backend API integration
    private val notificationRepository = NotificationRepository.getInstance()
    private var authToken: String? = null
    
    init {
        createNotificationChannel()
        
        // Periodically load notifications from API (fallback when WebSocket is not available)
        serviceScope.launch {
            while (true) {
                authToken?.let { token ->
                    loadNotificationsFromApi(token)
                }
                delay(30000) // Check every 30 seconds
            }
        }
    }
    
    fun startNotificationService(authToken: String) {
        this.authToken = authToken
        serviceScope.launch {
            // Load initial notifications from API
            loadNotificationsFromApi(authToken)
            // Connect WebSocket for real-time updates
            connectWebSocket(authToken)
        }
    }
    
    fun stopNotificationService() {
        webSocket?.close(1000, "Service stopped")
        webSocket = null
        isConnected = false
        reconnectAttempts = 0
        authToken = null
    }
    
    /**
     * Load notifications from backend API using direct approach to avoid ApiResult conflicts
     */
    private suspend fun loadNotificationsFromApi(authToken: String) {
        try {
            val result = notificationRepository.loadNotifications(authToken)
            
            // Handle the result using when expression with proper type checking
            when {
                result.javaClass.simpleName.contains("Success") -> {
                    // Use reflection to safely get the data field
                    try {
                        val dataField = result.javaClass.getDeclaredField("data")
                        dataField.isAccessible = true
                        @Suppress("UNCHECKED_CAST")
                        val notifications = dataField.get(result) as List<MerchantNotification>
                        
                        _notificationList.value = notifications
                        _unreadCount.value = notifications.count { notification ->
                            // For merchant notifications, we consider unread if timestamp is recent
                            System.currentTimeMillis() - notification.timestamp < 24 * 60 * 60 * 1000 // 24 hours
                        }
                    } catch (e: Exception) {
                        println("Error accessing success data: ${e.message}")
                    }
                }
                result.javaClass.simpleName.contains("Error") -> {
                    try {
                        val messageField = result.javaClass.getDeclaredField("message")
                        messageField.isAccessible = true
                        val errorMessage = messageField.get(result) as String
                        println("Failed to load notifications from API: $errorMessage")
                    } catch (e: Exception) {
                        println("Failed to load notifications from API: Unknown error")
                    }
                }
                else -> {
                    // Loading or other state
                    println("Loading notifications...")
                }
            }
        } catch (e: Exception) {
            println("Error loading notifications: ${e.message}")
        }
    }
    
    /**
     * Mark notification as read using backend API with direct approach
     */
    fun markNotificationAsRead(notificationId: String) {
        authToken?.let { token ->
            serviceScope.launch {
                try {
                    val result = notificationRepository.markNotificationAsRead(token, notificationId)
                    
                    // Handle the result using when expression with proper type checking  
                    when {
                        result.javaClass.simpleName.contains("Success") -> {
                            // Update local state - reduce unread count
                            val currentUnread = _unreadCount.value
                            if (currentUnread > 0) {
                                _unreadCount.value = currentUnread - 1
                            }
                        }
                        result.javaClass.simpleName.contains("Error") -> {
                            try {
                                val messageField = result.javaClass.getDeclaredField("message")
                                messageField.isAccessible = true
                                val errorMessage = messageField.get(result) as String
                                println("Failed to mark notification as read: $errorMessage")
                            } catch (e: Exception) {
                                println("Failed to mark notification as read: Unknown error")
                            }
                        }
                        else -> {
                            // Loading or other state
                            println("Marking notification as read...")
                        }
                    }
                } catch (e: Exception) {
                    println("Error marking notification as read: ${e.message}")
                }
            }
        }
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
                println("Merchant WebSocket connected for notifications")
            }
            
            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    val notification = MerchantNotification(
                        id = json.optString("id", ""),
                        title = json.getString("title"),
                        message = json.getString("message"),
                        type = MerchantNotificationType.valueOf(
                            json.optString("type", "NEW_ORDER")
                        ),
                        isImportant = json.optBoolean("is_important", false),
                        orderId = json.optString("order_id", null)
                    )
                    
                    serviceScope.launch {
                        _notifications.emit(notification)
                        // Also add to persistent list
                        val currentList = _notificationList.value.toMutableList()
                        currentList.add(0, notification) // Add to beginning
                        _notificationList.value = currentList
                        
                        // Update unread count
                        if (notification.isImportant || notification.type == MerchantNotificationType.NEW_ORDER) {
                            _unreadCount.value = _unreadCount.value + 1
                        }
                        
                        showPushNotification(notification)
                    }
                } catch (e: Exception) {
                    println("Error parsing merchant notification: ${e.message}")
                }
            }
            
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                isConnected = false
                println("Merchant WebSocket failed: ${t.message}")
                
                // Attempt to reconnect with exponential backoff
                if (reconnectAttempts < maxReconnectAttempts) {
                    serviceScope.launch {
                        delay((1000 * Math.pow(2.0, reconnectAttempts.toDouble())).toLong())
                        reconnectAttempts++
                        authToken?.let { token ->
                            connectWebSocket(token)
                        }
                    }
                }
            }
            
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                isConnected = false
                println("Merchant WebSocket closed: $reason")
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
                description = "Order notifications for WiseBite merchants"
                enableVibration(true)
                setShowBadge(true)
                enableLights(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun showPushNotification(notification: MerchantNotification) {
        val sound = when (notification.type) {
            MerchantNotificationType.NEW_ORDER, MerchantNotificationType.ORDER_ACCEPTED -> android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
            else -> null
        }
        
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(notification.title)
            .setContentText(notification.message)
            .setPriority(if (notification.isImportant) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setVibrate(
                when (notification.type) {
                    MerchantNotificationType.NEW_ORDER -> longArrayOf(0, 500, 200, 500, 200, 500)
                    MerchantNotificationType.ORDER_ACCEPTED -> longArrayOf(0, 300, 100, 300)
                    else -> longArrayOf(0, 200)
                }
            )
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
        
        if (sound != null) {
            notificationBuilder.setSound(sound)
        }
        
        try {
            with(NotificationManagerCompat.from(context)) {
                notify(notification.id.hashCode(), notificationBuilder.build())
            }
        } catch (e: SecurityException) {
            println("Permission denied for showing merchant notification: ${e.message}")
        }
    }
    
    // Simulate real-time notifications for testing (fallback when WebSocket is not available)
    fun simulateNewOrderNotification(orderId: String, customerName: String, itemCount: Int, totalAmount: Double) {
        serviceScope.launch {
            val notification = MerchantNotification(
                id = "new_order_$orderId",
                title = "🛒 Đơn hàng mới!",
                message = "$customerName đã đặt $itemCount món. Tổng: ${String.format("%,.0f", totalAmount)}đ",
                type = MerchantNotificationType.NEW_ORDER,
                isImportant = true,
                orderId = orderId
            )
            
            _notifications.emit(notification)
            // Also add to persistent list for consistency
            val currentList = _notificationList.value.toMutableList()
            currentList.add(0, notification)
            _notificationList.value = currentList
            
            // Update unread count
            if (notification.isImportant) {
                _unreadCount.value = _unreadCount.value + 1
            }
            
            showPushNotification(notification)
        }
    }
    
    fun simulateOrderStatusUpdate(orderId: String, status: String) {
        serviceScope.launch {
            val notification = MerchantNotification(
                id = "status_$orderId",
                title = "Cập nhật đơn hàng",
                message = "Đơn hàng #${orderId.take(8)} đã được $status",
                type = when (status) {
                    "hủy" -> MerchantNotificationType.ORDER_CANCELLED
                    "sẵn sàng" -> MerchantNotificationType.PICKUP_READY
                    else -> MerchantNotificationType.SYSTEM_UPDATE
                },
                isImportant = status == "hủy",
                orderId = orderId
            )
            
            _notifications.emit(notification)
            // Also add to persistent list for consistency
            val currentList = _notificationList.value.toMutableList()
            currentList.add(0, notification)
            _notificationList.value = currentList
            
            // Update unread count
            if (notification.isImportant) {
                _unreadCount.value = _unreadCount.value + 1
            }
            
            showPushNotification(notification)
        }
    }
    
    fun simulatePaymentReceived(orderId: String, amount: Double) {
        serviceScope.launch {
            val notification = MerchantNotification(
                id = "payment_$orderId",
                title = "💰 Thanh toán nhận được",
                message = "Đã nhận ${String.format("%,.0f", amount)}đ cho đơn hàng #${orderId.take(8)}",
                type = MerchantNotificationType.PAYMENT_RECEIVED,
                isImportant = false,
                orderId = orderId
            )
            
            _notifications.emit(notification)
            // Also add to persistent list for consistency
            val currentList = _notificationList.value.toMutableList()
            currentList.add(0, notification)
            _notificationList.value = currentList
            
            showPushNotification(notification)
        }
    }
    
    fun simulateOrderAccepted(orderId: String, customerName: String, itemCount: Int) {
        serviceScope.launch {
            val notification = MerchantNotification(
                id = "accepted_$orderId",
                title = "✅ Đơn hàng đã được chấp nhận",
                message = "Bạn đã chấp nhận đơn hàng #${orderId.take(8)} của $customerName ($itemCount món)",
                type = MerchantNotificationType.ORDER_ACCEPTED,
                isImportant = true,
                orderId = orderId
            )
            
            _notifications.emit(notification)
            // Also add to persistent list for consistency
            val currentList = _notificationList.value.toMutableList()
            currentList.add(0, notification)
            _notificationList.value = currentList
            
            // Update unread count
            if (notification.isImportant) {
                _unreadCount.value = _unreadCount.value + 1
            }
            
            showPushNotification(notification)
        }
    }
}