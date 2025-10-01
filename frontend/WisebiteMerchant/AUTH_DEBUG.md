# Authentication Debug Guide for WiseBite Merchant App

## Problem: 403 Forbidden when creating surprise bags

Your logs show:
- ✅ Google auth works: `POST /api/v1/auth/google` - 200 OK 
- ❌ Surprise bag creation fails: `POST /api/v1/surprise-bag/` - 403 Forbidden

## Debugging Steps

### 1. Check Token Storage
Add this code to test if token is properly saved after Google auth:

```kotlin
// In your AuthViewModel or wherever you handle Google sign in
viewModelScope.launch {
    val result = repository.googleSignIn(idToken)
    when (result) {
        is ApiResult.Success -> {
            // Debug: Check if token was saved
            repository.debugAuthToken()
            
            // Test if you can make authenticated calls
            val userCheck = repository.getCurrentUser()
            Log.d("AuthDebug", "User check result: $userCheck")
        }
        is ApiResult.Error -> {
            Log.e("AuthDebug", "Google sign in failed: ${result.message}")
        }
    }
}
```

### 2. Check Before Creating Surprise Bag
Add this to your SurpriseBagViewModel before calling createSurpriseBag:

```kotlin
fun createSurpriseBag(...) {
    viewModelScope.launch {
        // Debug authentication before creating
        Log.d("SurpriseBagDebug", "About to create surprise bag")
        repository.debugAuthToken()
        
        val authCheck = repository.getCurrentUser()
        Log.d("SurpriseBagDebug", "Auth check: $authCheck")
        
        // Your existing createSurpriseBag code...
    }
}
```

### 3. Check User Role and Store
The 403 error suggests:
- User doesn't have vendor role, OR
- User doesn't have a store created

## Quick Fix Steps

1. **Add debug logging** (already done in updated code)
2. **Ensure Google auth saves token** (added googleSignIn method)
3. **Check user role** - Your user might be created as "customer" instead of "vendor"
4. **Check if store exists** - Vendors need a store to create surprise bags

## Testing

Run your app and look for these log messages:
- "Google sign in successful, token received"
- "Token saved to SharedPreferences" 
- "Auth header for surprise bag creation"
- "403 Forbidden - Check if user has vendor role and store"

The logs will tell you exactly where the authentication is failing.