#!/usr/bin/env pwsh
# Database Reset Script for WiseBite Backend
# This script will completely reset the database and restart the services

Write-Host "🔄 Starting WiseBite Database Reset..." -ForegroundColor Yellow

# Stop all running containers
Write-Host "🛑 Stopping Docker containers..." -ForegroundColor Blue
docker-compose down

# Remove database volumes to completely wipe data
Write-Host "🗑️  Removing database volumes..." -ForegroundColor Blue
docker volume rm backend_postgres_data 2>$null
docker volume prune -f

# Remove any orphaned containers
Write-Host "🧹 Cleaning up orphaned containers..." -ForegroundColor Blue
docker-compose down --remove-orphans

# Optional: Remove images to force rebuild (uncomment if needed)
# Write-Host "🔄 Removing Docker images for fresh build..." -ForegroundColor Blue
# docker-compose down --rmi all

# Start services with fresh database
Write-Host "🚀 Starting fresh Docker containers..." -ForegroundColor Green
docker-compose up -d

# Wait for database to be ready
Write-Host "⏳ Waiting for database to initialize..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

# Check if containers are running
Write-Host "📊 Checking container status:" -ForegroundColor Cyan
docker-compose ps

# Show logs to verify startup
Write-Host "📋 Recent container logs:" -ForegroundColor Cyan
docker-compose logs --tail=20

Write-Host ""
Write-Host "✅ Database reset complete!" -ForegroundColor Green
Write-Host "🌐 API should be available at: http://localhost:8000" -ForegroundColor Green
Write-Host "📚 API documentation: http://localhost:8000/docs" -ForegroundColor Green
Write-Host "📱 For Android device use: http://192.168.2.23:8000" -ForegroundColor Green
Write-Host ""
Write-Host "🧪 Test the API with:" -ForegroundColor Yellow
Write-Host "   Invoke-RestMethod -Uri 'http://localhost:8000/docs' -Method GET" -ForegroundColor Gray