#!/usr/bin/env pwsh
# Quick API Test Script for WiseBite Backend
# Tests signup, login, and user endpoints

Write-Host "🧪 Testing WiseBite API..." -ForegroundColor Yellow
Write-Host ""

# Test basic connectivity
Write-Host "1️⃣  Testing API connectivity..." -ForegroundColor Blue
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8000/docs" -Method GET -TimeoutSec 5
    Write-Host "   ✅ API is accessible" -ForegroundColor Green
} catch {
    Write-Host "   ❌ API is not accessible: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test signup
Write-Host "2️⃣  Testing user signup..." -ForegroundColor Blue
try {
    $signupData = @{
        email = "testuser@example.com"
        password = "testpass123"
        full_name = "Test User"
        phone_number = "1234567890"
        role = "customer"
    } | ConvertTo-Json

    $signupResponse = Invoke-RestMethod -Uri "http://localhost:8000/api/v1/auth/signup" -Method POST -ContentType "application/json" -Body $signupData
    Write-Host "   ✅ Signup successful - User ID: $($signupResponse.id)" -ForegroundColor Green
    $userId = $signupResponse.id
} catch {
    Write-Host "   ❌ Signup failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test login
Write-Host "3️⃣  Testing user login..." -ForegroundColor Blue
try {
    $loginResponse = Invoke-RestMethod -Uri "http://localhost:8000/api/v1/auth/login" -Method POST -ContentType "application/x-www-form-urlencoded" -Body "username=1234567890&password=testpass123"
    Write-Host "   ✅ Login successful - Token received" -ForegroundColor Green
    $token = $loginResponse.access_token
} catch {
    Write-Host "   ❌ Login failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test authenticated endpoint
Write-Host "4️⃣  Testing authenticated endpoint..." -ForegroundColor Blue
try {
    $userResponse = Invoke-RestMethod -Uri "http://localhost:8000/api/v1/user/me" -Method GET -Headers @{"Authorization" = "Bearer $token"}
    Write-Host "   ✅ User data retrieved - Name: $($userResponse.full_name)" -ForegroundColor Green
} catch {
    Write-Host "   ❌ User data retrieval failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "🎉 All tests passed! The API is working correctly." -ForegroundColor Green
Write-Host ""
Write-Host "📱 For Android testing use IP: 192.168.2.23:8000" -ForegroundColor Cyan
Write-Host "🔑 Test credentials:" -ForegroundColor Cyan
Write-Host "   Phone: 1234567890" -ForegroundColor Gray
Write-Host "   Password: testpass123" -ForegroundColor Gray