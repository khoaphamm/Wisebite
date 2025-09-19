# WiseBite API Documentation for Android Development

## 📱 API Overview

**Base URL:** `http://localhost:8000/api/v1`  
**API Version:** 1.0.0  
**Authentication:** JWT Bearer Token  

## 🔐 Authentication Flow

### 1. User Signup
- **Endpoint:** `POST /auth/signup`
- **Content-Type:** `application/json`
- **Body:**
```json
{
  "email": "user@example.com",
  "phone_number": "1234567890",
  "full_name": "John Doe",
  "gender": "male",
  "birth_date": "1990-01-01",
  "role": "customer",
  "password": "securepassword123"
}
```
- **Response:** User profile with generated UUID

### 2. User Login
- **Endpoint:** `POST /auth/login`
- **Content-Type:** `application/x-www-form-urlencoded`
- **Body:** `username=1234567890&password=securepassword123`
- **Note:** Username field expects phone number, not email
- **Response:**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### 3. Using JWT Token
For all protected endpoints, include in headers:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## 👤 User Management

### Get Current User Profile
- **Endpoint:** `GET /user/me`
- **Auth Required:** Yes
- **Response:** Complete user profile

### Update User Profile
- **Endpoint:** `PATCH /user/me`
- **Auth Required:** Yes
- **Body:** Partial user update object

### Upload Avatar
- **Endpoint:** `POST /user/upload/avatar`
- **Auth Required:** Yes
- **Content-Type:** `multipart/form-data`
- **Body:** Image file

### Change Password
- **Endpoint:** `PATCH /user/me/password`
- **Auth Required:** Yes
- **Body:**
```json
{
  "old_password": "currentpassword",
  "new_password": "newpassword123"
}
```

## 🏪 Store Management (Vendor Only)

### Create Store
- **Endpoint:** `POST /store`
- **Auth Required:** Yes (Vendor role)

### Get User's Stores
- **Endpoint:** `GET /store/me`
- **Auth Required:** Yes (Vendor role)

### Update Store
- **Endpoint:** `PATCH /store/{store_id}`
- **Auth Required:** Yes (Vendor role)

## 🍔 Food Items

### Get All Food Items
- **Endpoint:** `GET /food-item`
- **Auth Required:** No
- **Query Params:** `skip`, `limit`, location filters

### Create Food Item (Vendor)
- **Endpoint:** `POST /food-item`
- **Auth Required:** Yes (Vendor role)

### Get Food Item Details
- **Endpoint:** `GET /food-item/{item_id}`
- **Auth Required:** No

### Search Food Items
- **Endpoint:** `GET /food-item/search`
- **Query Params:** `query`, `category`, `location`

## 🎒 Surprise Bags

### Get All Surprise Bags
- **Endpoint:** `GET /surprise-bag`
- **Auth Required:** No

### Create Surprise Bag (Vendor)
- **Endpoint:** `POST /surprise-bag`
- **Auth Required:** Yes (Vendor role)

### Get Surprise Bag Details
- **Endpoint:** `GET /surprise-bag/{bag_id}`
- **Auth Required:** No

## 🛍️ Orders

### Create Order
- **Endpoint:** `POST /order`
- **Auth Required:** Yes
- **Body:**
```json
{
  "items": [
    {
      "food_item_id": "uuid",
      "quantity": 2
    }
  ],
  "store_id": "uuid"
}
```

### Get User Orders
- **Endpoint:** `GET /order/me`
- **Auth Required:** Yes

### Get Order Details
- **Endpoint:** `GET /order/{order_id}`
- **Auth Required:** Yes

### Update Order Status (Vendor)
- **Endpoint:** `PATCH /order/{order_id}/status`
- **Auth Required:** Yes (Vendor role)

## 💰 Transactions

### Get User Transactions
- **Endpoint:** `GET /transaction/me`
- **Auth Required:** Yes

### Create Payment
- **Endpoint:** `POST /transaction`
- **Auth Required:** Yes

## 🔔 Notifications

### Get User Notifications
- **Endpoint:** `GET /user/me/notifications`
- **Auth Required:** Yes

### Mark Notification as Read
- **Endpoint:** `POST /user/read/{notification_id}`
- **Auth Required:** Yes

## 💬 Chat System

### Get User Chats
- **Endpoint:** `GET /chat/me`
- **Auth Required:** Yes

### Send Message
- **Endpoint:** `POST /chat/{chat_id}/message`
- **Auth Required:** Yes

## 🌍 Location & Search

### Search by Location
Many endpoints support location-based filtering with parameters:
- `latitude`: User's current latitude
- `longitude`: User's current longitude
- `radius`: Search radius in kilometers

## 📋 Data Models

### User Roles
- `customer`: Regular app users who purchase food
- `vendor`: Store owners who sell food
- `admin`: System administrators

### Order Status
- `pending`: Order placed, awaiting vendor confirmation
- `confirmed`: Vendor accepted the order
- `preparing`: Food is being prepared
- `ready`: Order ready for pickup
- `completed`: Order completed successfully
- `cancelled`: Order cancelled

### Gender Options
- `male`
- `female`
- `other`

## 🚀 Android Implementation Tips

### 1. HTTP Client Setup
Use Retrofit or OkHttp with:
- Base URL: `http://localhost:8000/api/v1`
- JSON converter (Gson/Moshi)
- JWT token interceptor

### 2. Token Management
- Store JWT token securely (SharedPreferences encrypted or Keystore)
- Implement automatic token refresh if needed
- Handle 401/403 responses by redirecting to login

### 3. Image Handling
- Use Glide/Picasso for avatar and food item images
- Implement image compression for uploads
- Handle multipart form data for avatar uploads

### 4. Location Services
- Request location permissions
- Use GPS/Network for location-based features
- Implement location caching

### 5. Error Handling
- Parse API error responses
- Show user-friendly error messages
- Handle network connectivity issues

## 📝 Example Android Retrofit Interface

```kotlin
interface WiseBiteApi {
    @FormUrlEncoded
    @POST("auth/login")
    suspend fun login(
        @Field("username") phoneNumber: String,
        @Field("password") password: String
    ): Response<TokenResponse>
    
    @POST("auth/signup")
    suspend fun signup(@Body user: UserCreateRequest): Response<UserResponse>
    
    @GET("user/me")
    suspend fun getCurrentUser(): Response<UserResponse>
    
    @GET("food-item")
    suspend fun getFoodItems(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 20,
        @Query("latitude") lat: Double? = null,
        @Query("longitude") lng: Double? = null
    ): Response<FoodItemsResponse>
    
    @POST("order")
    suspend fun createOrder(@Body order: CreateOrderRequest): Response<OrderResponse>
}
```

## 📄 Complete API Specification

The complete OpenAPI 3.1 specification is available in `api-documentation.json` which contains:
- All endpoint details
- Request/response schemas
- Validation rules
- Example values

Import this file into tools like Postman or use it to generate client SDKs.

---

**Generated from WiseBite Backend API v1.0.0**  
**For Android Development - Complete Standalone Documentation**
