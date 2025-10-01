@echo off
REM WiseBite Backend Database Setup Script for Windows
REM This script sets up the database for new developers

echo 🚀 Setting up WiseBite Backend Database...

REM Check if Docker is running
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Docker is not running. Please start Docker and try again.
    exit /b 1
)

REM Stop existing containers
echo 📦 Stopping existing containers...
docker-compose down

REM Build and start containers
echo 🔨 Building and starting containers...
docker-compose build
docker-compose up -d

REM Wait for database to be ready
echo ⏳ Waiting for database to be ready...
timeout /t 15 /nobreak >nul

REM Run migrations
echo 🗄️ Running database migrations...
docker-compose exec app uv run alembic upgrade head

REM Optional: Populate initial data
echo 🌱 Populating initial data...
docker-compose exec app uv run python -c "from app.initial_db import populate_store_and_categories, create_initial_superuser; populate_store_and_categories(); create_initial_superuser(); print('✅ Initial data populated successfully!')"

echo ✅ Database setup complete!
echo 📝 Your backend is now running at: http://localhost:8000
echo 📚 API documentation available at: http://localhost:8000/docs
pause