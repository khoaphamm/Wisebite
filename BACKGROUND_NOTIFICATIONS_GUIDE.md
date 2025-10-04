# 🔔 Background Notifications Implementation Guide

## ✅ **Complete Background Notification System**

Your WiseBite Merchant app now supports **receiving notifications even when the app is closed**! Here's how it works:

### **🏗️ Architecture Overview**

1. **MerchantBackgroundService** - Foreground service that runs 24/7
2. **MerchantNotificationService** - WebSocket + API notification handler
3. **BootReceiver** - Auto-starts service after device reboot
4. **MainActivity** - Permission handling and service management

### **📱 User Experience**

#### **When App is Running:**
- Real-time WebSocket notifications ✨
- Instant order updates
- Sound + vibration alerts

#### **When App is Closed:**
- Background service keeps running 🚀
- Push notifications with custom sounds
- Persistent notification showing "WiseBite Merchant - Listening for orders..."
- Auto-restart after device reboot

### **🎛️ Controls Available**

#### **In EnhancedOrdersScreen:**
- **🔔 Test Notification** - Test notification button
- **🔘 Background Toggle** - Enable/disable background notifications
  - Green = Background notifications ON
  - Gray = Background notifications OFF

#### **Automatic Behaviors:**
- **First Launch**: Requests notification permission
- **After Login**: Auto-starts background service
- **After Logout**: Stops background service
- **Device Boot**: Auto-starts if previously enabled

### **🔧 How to Use**

#### **For Merchants:**
1. **Grant Permission**: Allow notifications when prompted
2. **Login**: Background service starts automatically
3. **Toggle Control**: Use toggle button to enable/disable background notifications
4. **Stay Connected**: Service runs even when app is closed

#### **For Testing:**
1. Use "Test Notification" button to verify notifications work
2. Close the app completely
3. Send test notification from backend
4. Should receive notification even when app is closed

### **⚡ Technical Features**

#### **Permissions Required:**
- `POST_NOTIFICATIONS` - Show notifications (Android 13+)
- `VIBRATE` - Notification vibration
- `WAKE_LOCK` - Keep service alive
- `FOREGROUND_SERVICE` - Background operation
- `FOREGROUND_SERVICE_DATA_SYNC` - Sync notifications
- `RECEIVE_BOOT_COMPLETED` - Auto-start after reboot

#### **Service Lifecycle:**
- **Foreground Service**: Runs with persistent notification
- **START_STICKY**: Auto-restarts if killed by system
- **WebSocket Connection**: Real-time order updates
- **API Fallback**: Polls every 30 seconds if WebSocket fails

#### **Notification Types:**
- 🛒 **NEW_ORDER**: Urgent, sound + vibration
- ✅ **ORDER_ACCEPTED**: Confirmation sound
- ❌ **ORDER_CANCELLED**: Important vibration
- 📦 **PICKUP_READY**: Standard notification
- 💰 **PAYMENT_RECEIVED**: Success sound
- ℹ️ **SYSTEM_UPDATE**: Silent notification

### **🚀 Integration Points**

#### **Backend Requirements:**
- WebSocket endpoint: `/api/v1/ws/merchant/notifications`
- REST API: `/api/v1/merchant/notifications`
- Auth token in headers: `Authorization: Bearer {token}`

#### **Frontend Integration:**
```kotlin
// Start service after login
MainActivity.startNotificationServiceAfterLogin(authToken)

// Stop service after logout  
MainActivity.stopNotificationServiceAfterLogout()

// Test notification
notificationService.simulateNewOrderNotification(orderId, customerName, itemCount, totalAmount)
```

### **🔍 Troubleshooting**

#### **Notifications Not Working:**
1. Check notification permission in phone settings
2. Verify background app restrictions are disabled
3. Ensure battery optimization is disabled for WiseBite Merchant
4. Check if background toggle is enabled (green)

#### **Service Not Starting:**
1. Check auth token is saved properly
2. Verify foreground service permissions
3. Look for logs in Android Studio logcat
4. Test with manual service start

#### **WebSocket Issues:**
1. Check network connectivity
2. Verify backend WebSocket endpoint is running
3. Service falls back to API polling automatically
4. Check auth token validity

### **📊 Monitoring**

#### **Service Status:**
- Persistent notification shows current status
- Unread count badge in notification
- Real-time connection status in logs

#### **Debug Logs:**
- `BackgroundService`: Service lifecycle events
- `MerchantNotificationService`: WebSocket connection, notification parsing
- `BootReceiver`: Auto-start behavior
- `MainActivity`: Permission handling, service management

### **🎯 Performance**

#### **Battery Optimized:**
- Uses efficient WebSocket connection
- Minimal CPU usage when idle
- Smart reconnection with exponential backoff
- Foreground service prevents system kills

#### **Network Efficient:**
- WebSocket for real-time updates
- API polling only as fallback
- Connection pooling with OkHttp
- Automatic retry logic

### **🔒 Security**

#### **Auth Token Management:**
- Secure storage in SharedPreferences
- Automatic token refresh (if implemented)
- Clean token removal on logout
- Bearer token authentication

---

## 🎉 **Result: 24/7 Merchant Notifications!**

Your merchants will never miss an order again, even when the app is completely closed. The system is robust, battery-efficient, and user-friendly with simple toggle controls.

**Test it now**: Close the app completely and send a notification from your backend - it should still appear! 🚀