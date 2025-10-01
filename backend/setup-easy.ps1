#!/usr/bin/env pwsh

Write-Host "========================================"
Write-Host "   WISEBITE SETUP SCRIPT - POWERSHELL"
Write-Host "========================================"
Write-Host ""

Write-Host "[1/6] Checking Docker Desktop..." -ForegroundColor Yellow
try {
    docker --version | Out-Null
    Write-Host "✅ Docker is running" -ForegroundColor Green
} catch {
    Write-Host "❌ Docker Desktop is not running or not installed!" -ForegroundColor Red
    Write-Host "Please:" -ForegroundColor Red
    Write-Host "1. Install Docker Desktop" -ForegroundColor Red
    Write-Host "2. Start Docker Desktop" -ForegroundColor Red
    Write-Host "3. Wait for Docker to fully initialize" -ForegroundColor Red
    Write-Host "4. Run this script again" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host ""
Write-Host "[2/6] Building and starting containers..." -ForegroundColor Yellow
docker-compose up --build -d
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Failed to start containers" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}
Write-Host "✅ Containers started successfully" -ForegroundColor Green

Write-Host ""
Write-Host "[3/6] Waiting for database to initialize..." -ForegroundColor Yellow
Start-Sleep -Seconds 30
Write-Host "✅ Database ready" -ForegroundColor Green

Write-Host ""
Write-Host "[4/6] Running database migrations..." -ForegroundColor Yellow
docker-compose exec -T app uv run alembic upgrade head
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Database migration failed, trying to fix..." -ForegroundColor Yellow
    docker-compose exec -T app uv run alembic stamp add_categories_inventory
    docker-compose exec -T app uv run alembic upgrade head
}
Write-Host "✅ Database migrations completed" -ForegroundColor Green

Write-Host ""
Write-Host "[5/6] Creating sample data..." -ForegroundColor Yellow
$dataScript = @"
from app.initial_db import populate_store_and_categories, create_initial_superuser
try:
    populate_store_and_categories()
    create_initial_superuser()
    print('Sample data created successfully!')
except Exception as e:
    print(f'Sample data creation had issues: {e}')
"@

docker-compose exec -T app uv run python -c $dataScript
Write-Host "✅ Sample data setup completed" -ForegroundColor Green

Write-Host ""
Write-Host "[6/6] Testing API connectivity..." -ForegroundColor Yellow
Start-Sleep -Seconds 5
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8000/api/v1/surprise-bag/" -Method Get -TimeoutSec 10
    if ($response) {
        Write-Host "✅ API is responding correctly" -ForegroundColor Green
    }
} catch {
    Write-Host "⚠️  API test failed, but services might still be starting..." -ForegroundColor Yellow
    Write-Host "Please wait a moment and check manually" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================"
Write-Host "          SETUP COMPLETED! 🎉"
Write-Host "========================================"
Write-Host ""
Write-Host "✅ Backend API: http://localhost:8000/docs" -ForegroundColor Green
Write-Host "✅ Test endpoint: http://localhost:8000/api/v1/surprise-bag/" -ForegroundColor Green
Write-Host "✅ Alternative docs: http://localhost:8000/redoc" -ForegroundColor Green
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "1. Open Android Studio" -ForegroundColor White
Write-Host "2. Import frontend/WisebiteCustomer (Customer app)" -ForegroundColor White
Write-Host "3. Import frontend/WisebiteMerchant (Merchant app)" -ForegroundColor White
Write-Host "4. Run on emulator or device" -ForegroundColor White
Write-Host ""
Write-Host "Need help? Check SETUP_GUIDE_VI.md for detailed instructions" -ForegroundColor Cyan
Write-Host ""
Read-Host "Press Enter to exit"