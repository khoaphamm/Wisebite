# WiseBite Backend Health Check Scr# Check API endpoint
Write-Host "`n🌐 Checking API endpoints..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8000/api/v1/surprise-bag/" -Headers @{"accept"="application/json"} -TimeoutSec 5
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ API endpoint responding correctly" -ForegroundColor Green
        Write-Host "   Response: $($response.Content)" -ForegroundColor Gray
    }
} catch {
    Write-Host "❌ API endpoint not responding. Check if containers are running." -ForegroundColor Red
    exit 1
}cript verifies that the backend setup is working correctly

Write-Host "🔍 Checking WiseBite Backend Health..." -ForegroundColor Green

# Check if containers are running
Write-Host "`n📦 Checking Docker containers..." -ForegroundColor Yellow
$containers = docker-compose ps --services --filter "status=running"
if ($containers -contains "app" -and $containers -contains "db") {
    Write-Host "✅ All containers are running" -ForegroundColor Green
} else {
    Write-Host "❌ Some containers are not running. Run: docker-compose up -d" -ForegroundColor Red
    exit 1
}

# Check database connection
Write-Host "`n🗄️ Checking database connection..." -ForegroundColor Yellow
docker-compose exec -T db psql -U postgres -d wisebite_db -c "SELECT 1;" 2>$null | Out-Null
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Database connection successful" -ForegroundColor Green
} else {
    Write-Host "❌ Cannot connect to database" -ForegroundColor Red
    exit 1
}

# Check migration status
Write-Host "`n📋 Checking migration status..." -ForegroundColor Yellow
$migrationOutput = docker-compose exec -T app uv run alembic current 2>$null
if ($LASTEXITCODE -eq 0 -and $migrationOutput -like "*add_categories_inventory*") {
    Write-Host "✅ Database migrations are up to date" -ForegroundColor Green
} else {
    Write-Host "❌ Database migrations needed. Run: docker-compose exec app uv run alembic upgrade head" -ForegroundColor Red
    exit 1
}

# Check API endpoint
Write-Host "`n🌐 Checking API endpoints..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8000/api/v1/surprise-bag/" -Headers @{"accept"="application/json"} -TimeoutSec 5
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ API endpoint responding correctly" -ForegroundColor Green
        $content = $response.Content | ConvertFrom-Json
        Write-Host "   Response: $($response.Content)" -ForegroundColor Gray
    }
} catch {
    Write-Host "❌ API endpoint not responding. Check if containers are running." -ForegroundColor Red
    exit 1
}

# Check surprise bag table structure
Write-Host "`n📋 Checking surprise bag table structure..." -ForegroundColor Yellow
$tableCheck = docker-compose exec -T db psql -U postgres -d wisebite_db -c "\d surprisebag" 2>$null
if ($LASTEXITCODE -eq 0 -and $tableCheck -like "*bag_type*") {
    Write-Host "✅ Surprise bag table has correct structure" -ForegroundColor Green
} else {
    Write-Host "❌ Surprise bag table missing columns. Run migrations." -ForegroundColor Red
    exit 1
}

Write-Host "`n🎉 All checks passed! Your WiseBite backend is ready!" -ForegroundColor Green
Write-Host "📝 Backend URL: http://localhost:8000" -ForegroundColor Cyan
Write-Host "📚 API Documentation: http://localhost:8000/docs" -ForegroundColor Cyan
Write-Host "🗄️ Database: localhost:5432" -ForegroundColor Cyan