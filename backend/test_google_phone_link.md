# Google Account + Phone Number Login Test Guide

## 🎯 How It Works (Option 2 Implementation)

Users who sign up with Google can now also set up phone number + password for traditional login.

### Scenario: User Flow

1. **User signs up with Google Sign-In**
   - Creates account with email: `khoapgh235@gmail.com`
   - Gets placeholder phone: `google_user_123456`
   - Password: `google_oauth` (placeholder)

2. **User links phone number for traditional login**
   - Calls `/api/v1/auth/link-phone-number`
   - Sets real phone: `0369833705`
   - Sets real password: `mypassword123`

3. **User can now login two ways:**
   - **Option A**: Google Sign-In (using `id_token`)
   - **Option B**: Traditional login with phone `0369833705` + password `mypassword123`
   - **Option C**: Traditional login with email `khoapgh235@gmail.com` + password `mypassword123`

## 📋 API Endpoints

### 1. Google Sign-In (Existing)
```http
POST /api/v1/auth/google-signin
Content-Type: application/json

{
  "id_token": "google_jwt_token_here"
}
```

### 2. Link Phone Number (NEW)
```http
POST /api/v1/auth/link-phone-number
Authorization: Bearer your_access_token
Content-Type: application/json

{
  "phone_number": "0369833705",
  "password": "mypassword123"
}
```

### 3. Flexible Login (Enhanced)
```http
POST /api/v1/auth/login
Content-Type: application/x-www-form-urlencoded

username=0369833705&password=mypassword123
```

Or with email:
```http
POST /api/v1/auth/login
Content-Type: application/x-www-form-urlencoded

username=khoapgh235@gmail.com&password=mypassword123
```

## ✅ Benefits

- **Flexibility**: Users can choose their preferred login method
- **Backup**: If Google Sign-In is unavailable, users can still login
- **Familiarity**: Some users prefer traditional phone + password
- **Security**: Each method is independent and secure

## 🔧 Implementation Details

### Backend Changes Made:
1. Added `LinkPhoneNumberRequest` schema
2. Enhanced `authenticate_flexible()` function in `crud.py`
3. Updated `/login` endpoint to accept email OR phone number
4. Added `/link-phone-number` endpoint for Google users
5. Maintained backward compatibility

### Frontend Integration:
To use this in your Android app, add:

1. **Link Phone Number Screen** (for Google users)
2. **Login Options**: Show both Google Sign-In and traditional login
3. **Flexible Login Form**: Accept email OR phone number as username

This gives users complete flexibility in how they want to access their account!