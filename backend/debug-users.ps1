#!/usr/bin/env pwsh
# User Debug Script for WiseBite Backend
# Shows all users and helps debug login issues

Write-Host "🔍 WiseBite User Debug Tool" -ForegroundColor Yellow
Write-Host ""

# Test API connectivity first
Write-Host "1️⃣  Testing API connectivity..." -ForegroundColor Blue
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8000/docs" -Method GET -TimeoutSec 5
    Write-Host "   ✅ API is accessible" -ForegroundColor Green
} catch {
    Write-Host "   ❌ API is not accessible: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Get all users via API (need admin token)
Write-Host "2️⃣  Getting admin token..." -ForegroundColor Blue
try {
    # Login as admin (created during init)
    $adminLoginResponse = Invoke-RestMethod -Uri "http://localhost:8000/api/v1/auth/login" -Method POST -ContentType "application/x-www-form-urlencoded" -Body "username=0123456789&password=changeme"
    $adminToken = $adminLoginResponse.access_token
    Write-Host "   ✅ Admin login successful" -ForegroundColor Green
} catch {
    Write-Host "   ❌ Admin login failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "   ℹ️  Admin credentials might have changed" -ForegroundColor Yellow
}

# Show all users in database using SQL
Write-Host "3️⃣  Querying database directly..." -ForegroundColor Blue
try {
    $result = docker exec backend-db-1 psql -U postgres -d wisebite_db -t -c "SELECT phone_number, email, full_name, role FROM public.user ORDER BY created_at;"
    
    if ($result) {
        Write-Host "   📋 Users in database:" -ForegroundColor Cyan
        Write-Host "   Phone Number     | Email                    | Full Name      | Role" -ForegroundColor Gray
        Write-Host "   -----------------|--------------------------|----------------|----------" -ForegroundColor Gray
        
        $result | ForEach-Object {
            $line = $_.Trim()
            if ($line -and $line -ne "") {
                Write-Host "   $line" -ForegroundColor White
            }
        }
    } else {
        Write-Host "   ❌ No users found or query failed" -ForegroundColor Red
    }
} catch {
    Write-Host "   ❌ Database query failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "4️⃣  Testing login for each user..." -ForegroundColor Blue

# Test common credentials
$testCredentials = @(
    @{phone="1234567890"; password="testpass123"; name="Test Script User"}
    @{phone="0123456789"; password="changeme"; name="Default Admin"}
    @{phone="1234567891"; password="password123"; name="Possible Android User"}
)

foreach ($cred in $testCredentials) {
    Write-Host "   Testing $($cred.name) ($($cred.phone))..." -ForegroundColor Gray
    try {
        $loginResponse = Invoke-RestMethod -Uri "http://localhost:8000/api/v1/auth/login" -Method POST -ContentType "application/x-www-form-urlencoded" -Body "username=$($cred.phone)&password=$($cred.password)"
        Write-Host "   ✅ Login successful for $($cred.phone)" -ForegroundColor Green
    } catch {
        Write-Host "   ❌ Login failed for $($cred.phone)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "💡 Troubleshooting Tips:" -ForegroundColor Yellow
Write-Host "   • Check the phone number you used during Android signup" -ForegroundColor Gray
Write-Host "   • Make sure password meets requirements (if any)" -ForegroundColor Gray
Write-Host "   • Try logging in with the phone numbers shown above" -ForegroundColor Gray
Write-Host "   • Use .\reset-database.ps1 to start fresh if needed" -ForegroundColor Gray