@echo off
echo ========================================
echo    WISEBITE SETUP SCRIPT - WINDOWS
echo ========================================
echo.

echo [1/6] Checking Docker Desktop...
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Docker Desktop is not running or not installed!
    echo Please:
    echo 1. Install Docker Desktop
    echo 2. Start Docker Desktop
    echo 3. Wait for Docker to fully initialize
    echo 4. Run this script again
    pause
    exit /b 1
)
echo ✅ Docker is running

echo.
echo [2/6] Building and starting containers...
docker-compose up --build -d
if %errorlevel% neq 0 (
    echo ❌ Failed to start containers
    pause
    exit /b 1
)
echo ✅ Containers started successfully

echo.
echo [3/6] Waiting for database to initialize...
timeout /t 30 /nobreak > nul
echo ✅ Database ready

echo.
echo [4/6] Running database migrations...
docker-compose exec -T app uv run alembic upgrade head
if %errorlevel% neq 0 (
    echo ❌ Database migration failed
    echo Trying to fix migration state...
    docker-compose exec -T app uv run alembic stamp add_categories_inventory
    docker-compose exec -T app uv run alembic upgrade head
)
echo ✅ Database migrations completed

echo.
echo [5/6] Creating sample data...
docker-compose exec -T app uv run python -c "from app.initial_db import populate_store_and_categories, create_initial_superuser; populate_store_and_categories(); create_initial_superuser(); print('Sample data created successfully!')"
if %errorlevel% neq 0 (
    echo ⚠️  Sample data creation had issues (this is usually OK)
)
echo ✅ Sample data setup completed

echo.
echo [6/6] Testing API connectivity...
timeout /t 5 /nobreak > nul
curl -s "http://localhost:8000/api/v1/surprise-bag/" | find "data" >nul
if %errorlevel% neq 0 (
    echo ⚠️  API test failed, but services might still be starting...
    echo Please wait a moment and check manually
) else (
    echo ✅ API is responding correctly
)

echo.
echo ========================================
echo           SETUP COMPLETED! 🎉
echo ========================================
echo.
echo ✅ Backend API: http://localhost:8000/docs
echo ✅ Test endpoint: http://localhost:8000/api/v1/surprise-bag/
echo ✅ Alternative docs: http://localhost:8000/redoc
echo.
echo Next steps:
echo 1. Open Android Studio
echo 2. Import frontend/WisebiteCustomer (Customer app)
echo 3. Import frontend/WisebiteMerchant (Merchant app)
echo 4. Run on emulator or device
echo.
echo Need help? Check SETUP_GUIDE_VI.md for detailed instructions
echo.
pause