# WiseBite Android - Network Troubleshooting Guide

## 🔧 Fixing Network Connection Issues

### **Step 1: Verify Backend is Running**
```bash
# In your backend directory
cd backend
docker-compose ps

# You should see services running like:
# backend-app-1    Up
# backend-db-1     Up (healthy)
```

If not running:
```bash
docker-compose up -d
```

### **Step 2: Test Backend API Directly**
Open your browser and test:
- **Emulator**: http://10.0.2.2:8000/docs
- **Physical Device**: http://YOUR_COMPUTER_IP:8000/docs

Replace `YOUR_COMPUTER_IP` with your actual IP address.

### **Step 3: Find Your Computer's IP Address**

**On Windows:**
```cmd
ipconfig
```
Look for "IPv4 Address" under your WiFi adapter (usually starts with 192.168.x.x)

**On Mac/Linux:**
```bash
ifconfig | grep inet
```

### **Step 4: Update API Base URL**

If using a **physical device** (not emulator), update the base URL in:
`app/src/main/java/com/example/wisebite/data/remote/WisebiteApiService.kt`

```kotlin
companion object {
    // For physical device - replace with your computer's IP
    const val BASE_URL = "http://192.168.1.XXX:8000/api/v1/"
    
    // For emulator (default)
    // const val BASE_URL = "http://10.0.2.2:8000/api/v1/"
}
```

### **Step 5: Enable Detailed Logging**

Check Android Studio's Logcat for detailed error messages:
1. Open **View** → **Tool Windows** → **Logcat**
2. Filter by "RetrofitClient" or "AuthRepository"
3. Try signing up again and check the logs

### **Step 6: Common Solutions**

#### **For Windows Firewall Issues:**
1. Allow port 8000 through Windows Firewall
2. Or temporarily disable Windows Firewall for testing

#### **For Network Issues:**
1. Make sure your phone and computer are on the same WiFi network
2. Try accessing http://YOUR_IP:8000/docs from your phone's browser
3. If that doesn't work, the issue is network configuration

#### **For Android Network Security:**
The app is configured to allow HTTP traffic to common development IPs, but you may need to add your specific IP to:
`app/src/main/res/xml/network_security_config.xml`

### **Step 7: Test with Postman/cURL**

Test the signup endpoint directly:
```bash
curl -X POST "http://YOUR_IP:8000/api/v1/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "full_name": "Test User",
    "email": "test@example.com", 
    "phone_number": "+1234567890",
    "password": "password123",
    "user_type": "customer"
  }'
```

### **Expected Log Output (Success):**
```
D/RetrofitClient: Making request to: http://10.0.2.2:8000/api/v1/auth/signup
D/RetrofitClient: Response code: 201
D/AuthRepository: Signup successful: User(...)
```

### **Common Error Patterns:**

#### **Connection Refused:**
```
java.net.ConnectException: Connection refused
```
**Solution:** Backend not running or wrong IP address

#### **Network Unreachable:**
```
java.net.UnknownHostException
```
**Solution:** Phone can't reach your computer's IP

#### **Timeout:**
```
SocketTimeoutException
```
**Solution:** Network is slow or firewall blocking

## 🚀 Quick Test Checklist

- [ ] Backend running (`docker-compose ps` shows "Up")
- [ ] Can access http://10.0.2.2:8000/docs (emulator) or http://YOUR_IP:8000/docs (device)
- [ ] Phone and computer on same WiFi network
- [ ] Windows Firewall allows port 8000
- [ ] Correct BASE_URL in WisebiteApiService.kt
- [ ] Android logs show detailed error information

## 📱 Device-Specific Notes

**Android Emulator:**
- Always use `10.0.2.2` (maps to localhost)
- Usually works without additional configuration

**Physical Android Device:**
- Must use your computer's actual IP address
- Both devices must be on same WiFi network
- May need to adjust firewall settings

**iOS Simulator/Device:**
- Use `localhost` for simulator
- Use actual IP for physical device