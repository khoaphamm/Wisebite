#!/usr/bin/env pwsh
# Simple User Debug Script for WiseBite Backend

Write-Host "WiseBite User Debug Tool" -ForegroundColor Yellow
Write-Host ""

# Query database directly for users
Write-Host "Querying database for all users..." -ForegroundColor Blue
try {
    $result = docker exec backend-db-1 psql -U postgres -d wisebite_db -t -c "SELECT phone_number, email, full_name, role FROM public.user ORDER BY created_at;"
    
    if ($result) {
        Write-Host "Users in database:" -ForegroundColor Cyan
        Write-Host "==================" -ForegroundColor Gray
        
        $result | ForEach-Object {
            $line = $_.Trim()
            if ($line -and $line -ne "") {
                Write-Host $line -ForegroundColor White
            }
        }
    } else {
        Write-Host "No users found" -ForegroundColor Red
    }
} catch {
    Write-Host "Database query failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "Testing login for common credentials..." -ForegroundColor Blue

# Test credentials one by one
Write-Host "Testing admin (0123456789)..." -ForegroundColor Gray
try {
    $body = "username=0123456789" + "&" + "password=changeme"
    $response = Invoke-RestMethod -Uri "http://localhost:8000/api/v1/auth/login" -Method POST -ContentType "application/x-www-form-urlencoded" -Body $body
    Write-Host "SUCCESS: Admin login works" -ForegroundColor Green
} catch {
    Write-Host "FAILED: Admin login failed" -ForegroundColor Red
}

Write-Host "Testing test user (1234567890)..." -ForegroundColor Gray
try {
    $body = "username=1234567890" + "&" + "password=testpass123"
    $response = Invoke-RestMethod -Uri "http://localhost:8000/api/v1/auth/login" -Method POST -ContentType "application/x-www-form-urlencoded" -Body $body
    Write-Host "SUCCESS: Test user login works" -ForegroundColor Green
} catch {
    Write-Host "FAILED: Test user login failed" -ForegroundColor Red
}

Write-Host ""
Write-Host "If your Android signup user is not listed above:" -ForegroundColor Yellow
Write-Host "1. Check what phone number you entered during signup" -ForegroundColor Gray
Write-Host "2. Make sure you're using the exact same phone number for login" -ForegroundColor Gray
Write-Host "3. Try resetting the database with: .\reset-database.ps1" -ForegroundColor Gray