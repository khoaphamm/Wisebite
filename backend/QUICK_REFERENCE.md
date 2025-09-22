# WiseBite Backend - Quick Commands Reference

## Database Management

### Reset Database (Complete Wipe)
```powershell
.\reset-database.ps1
```
- Stops all containers
- Removes database volumes 
- Restarts with fresh database
- Creates default admin user

### Quick API Test
```powershell
.\test-api.ps1
```
- Tests connectivity
- Tests signup/login flow
- Verifies authenticated endpoints

## Manual Commands

### Start Services
```powershell
docker-compose up -d
```

### Stop Services
```powershell
docker-compose down
```

### View Logs
```powershell
docker-compose logs -f
```

### Check Container Status
```powershell
docker-compose ps
```

## API Endpoints

### Base URLs
- **Local Development**: http://localhost:8000
- **Android Device**: http://192.168.2.23:8000
- **API Documentation**: http://localhost:8000/docs

### Key Endpoints
- `POST /api/v1/auth/signup` - User registration
- `POST /api/v1/auth/login` - User login (form data)
- `GET /api/v1/user/me` - Get current user (authenticated)

## Test Credentials (After Reset)
- **Phone**: 1234567890
- **Password**: testpass123
- **Email**: testuser@example.com

## Android Configuration
- **Base URL**: `http://192.168.2.23:8000/api/v1/`
- **Network Security**: Configured for cleartext HTTP
- **Login Format**: Form-encoded data (username/password)

## Troubleshooting

### If Android can't connect:
1. Check both devices are on same WiFi
2. Verify IP address with `ipconfig`
3. Update BASE_URL in `WisebiteApiService.kt`
4. Rebuild Android app

### If API is not responding:
1. Check Docker containers: `docker-compose ps`
2. View logs: `docker-compose logs`
3. Reset database: `.\reset-database.ps1`

### Common Issues:
- **422 Unprocessable Content**: Check request format (JSON vs form data)
- **Network Error**: Check IP address and network security config
- **401 Unauthorized**: Check token format and expiration