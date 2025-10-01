# WiseBite Backend Database Setup Script for PowerShell
# This script sets up the database for new developers

Write-Host "🚀 Setting up WiseBite Backend Database..." -ForegroundColor Green

# Check if Docker is running
try {
    docker info | Out-Null
    Write-Host "✅ Docker is running" -ForegroundColor Green
} catch {
    Write-Host "❌ Docker is not running. Please start Docker and try again." -ForegroundColor Red
    exit 1
}

# Stop existing containers
Write-Host "📦 Stopping existing containers..." -ForegroundColor Yellow
docker-compose down

# Build and start containers
Write-Host "🔨 Building and starting containers..." -ForegroundColor Yellow
docker-compose build
docker-compose up -d

# Wait for database to be ready
Write-Host "⏳ Waiting for database to be ready..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

# Run migrations
Write-Host "🗄️ Running database migrations..." -ForegroundColor Yellow
docker-compose exec app uv run alembic upgrade head

# Check if migration was successful
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Database migrations completed successfully!" -ForegroundColor Green
} else {
    Write-Host "❌ Database migration failed. Please check the logs." -ForegroundColor Red
    exit 1
}

# Optional: Populate initial data
Write-Host "🌱 Populating initial data..." -ForegroundColor Yellow
$initScript = @"
from app.initial_db import populate_store_and_categories, create_initial_superuser
try:
    populate_store_and_categories()
    create_initial_superuser()
    print('✅ Initial data populated successfully!')
except Exception as e:
    print(f'⚠️ Warning: Could not populate initial data: {e}')
"@

docker-compose exec app uv run python -c $initScript

Write-Host "✅ Database setup complete!" -ForegroundColor Green
Write-Host "📝 Your backend is now running at: http://localhost:8000" -ForegroundColor Cyan
Write-Host "📚 API documentation available at: http://localhost:8000/docs" -ForegroundColor Cyan

# Test API
Write-Host "🧪 Testing API..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8000/api/v1/surprise-bag/" -Headers @{"accept"="application/json"} -TimeoutSec 10
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ API is responding correctly!" -ForegroundColor Green
    }
} catch {
    Write-Host "⚠️ Warning: Could not test API. It might still be starting up." -ForegroundColor Yellow
}

Write-Host "`nPress any key to continue..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")