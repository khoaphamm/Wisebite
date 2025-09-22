#!/usr/bin/env pwsh
# Test login with existing users

Write-Host "Testing login for existing users..." -ForegroundColor Yellow
Write-Host ""

# Try the test user (should work)
Write-Host "1. Testing with test user (1234567890)..." -ForegroundColor Blue
try {
    $body = "username=1234567890&password=testpass123"
    $response = Invoke-RestMethod -Uri "http://192.168.2.23:8000/api/v1/auth/login" -Method POST -ContentType "application/x-www-form-urlencoded" -Body $body
    Write-Host "   ✅ SUCCESS: Test user login works" -ForegroundColor Green
    Write-Host "   Token: $($response.access_token.Substring(0,20))..." -ForegroundColor Gray
} catch {
    Write-Host "   ❌ FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# Try the Android signup user with common passwords
Write-Host ""
Write-Host "2. Testing your Android signup user (+840369833705)..." -ForegroundColor Blue

$commonPasswords = @("password123", "123456", "password", "123456789", "qwerty123")

foreach ($password in $commonPasswords) {
    Write-Host "   Trying password: $password" -ForegroundColor Gray
    try {
        $body = "username=+840369833705&password=$password"
        $response = Invoke-RestMethod -Uri "http://192.168.2.23:8000/api/v1/auth/login" -Method POST -ContentType "application/x-www-form-urlencoded" -Body $body
        Write-Host "   ✅ SUCCESS: Password '$password' works!" -ForegroundColor Green
        Write-Host "   Token: $($response.access_token.Substring(0,20))..." -ForegroundColor Gray
        break
    } catch {
        Write-Host "   ❌ Failed with '$password'" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "For Android App Testing:" -ForegroundColor Cyan
Write-Host "   Working credentials:" -ForegroundColor Gray
Write-Host "   - Phone: 1234567890" -ForegroundColor White
Write-Host "   - Password: testpass123" -ForegroundColor White
Write-Host ""
Write-Host "Solutions for your signup user:" -ForegroundColor Yellow
Write-Host "   1. Use the working test credentials above" -ForegroundColor Gray
Write-Host "   2. Reset database and signup again with the new phone picker" -ForegroundColor Gray
Write-Host "   3. Check what password you used during Android signup" -ForegroundColor Gray