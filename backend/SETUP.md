# WiseBite Backend Setup Guide

This guide helps new developers set up the WiseBite backend quickly and correctly.

## 🚀 Quick Setup (Recommended)

### Prerequisites
- Docker Desktop installed and running
- Git installed
- PowerShell (Windows) or Bash (Linux/Mac)

### One-Command Setup

```bash
# Windows (PowerShell)
.\setup-db.ps1

# Windows (Command Prompt)
setup-db.bat

# Linux/Mac
./setup-db.sh
```

This will automatically:
1. ✅ Check Docker status
2. 📦 Build and start containers
3. 🗄️ Run database migrations  
4. 🌱 Populate initial data
5. 🧪 Test API connectivity

## 📋 Verification

After setup, run the health check:

```bash
# Windows PowerShell
.\check-health.ps1

# Manual verification
curl http://localhost:8000/api/v1/surprise-bag/
# Expected: {"data":[],"count":0}
```

## 🌐 Access Points

- **API Documentation**: http://localhost:8000/docs
- **Alternative Docs**: http://localhost:8000/redoc
- **API Base URL**: http://localhost:8000/api/v1
- **Database**: localhost:5432 (user: postgres, password: postgres, db: wisebite_db)

## 🛠️ Manual Setup (If Automated Setup Fails)

### 1. Clone Repository
```bash
git clone <repository-url>
cd Wisebite/backend
```

### 2. Start Services
```bash
docker-compose build
docker-compose up -d
```

### 3. Wait and Run Migrations
```bash
# Wait 15-30 seconds for database to start
docker-compose exec app uv run alembic upgrade head
```

### 4. Populate Initial Data (Optional)
```bash
docker-compose exec app uv run python -c "
from app.initial_db import populate_store_and_categories, create_initial_superuser
populate_store_and_categories()
create_initial_superuser()
"
```

## 🐛 Common Issues

### "Docker is not running"
- Start Docker Desktop
- Wait for Docker to fully initialize
- Run setup script again

### "could not translate host name 'db'"
- You're running Alembic outside Docker
- **Solution**: Run inside container: `docker-compose exec app uv run alembic upgrade head`

### "relation already exists" Error
- Previous migration partially completed
- **Solution**: Mark as complete: `docker-compose exec app uv run alembic stamp add_categories_inventory`

### API Returns 500 Error
- Database schema is incomplete
- **Solution**: Run migrations: `docker-compose exec app uv run alembic upgrade head`

### Port Already in Use
- Another service is using port 8000
- **Solution**: Change port in `docker-compose.yml` or stop conflicting service

## 🔄 Reset Everything

If you need a completely clean start:

```bash
# Stop containers and remove data
docker-compose down -v

# Remove images (optional)
docker-compose down --rmi all

# Start fresh
.\setup-db.ps1  # Windows
./setup-db.sh   # Linux/Mac
```

## 📚 Next Steps

1. **API Testing**: Visit http://localhost:8000/docs
2. **Read Documentation**: Check README.md for detailed API information
3. **Run Tests**: Use `docker-compose exec app uv run pytest tests/ -v`
4. **Development**: Use `uvicorn app.main:app --reload` for live reloading

## 🆘 Need Help?

1. **Check Health**: Run `.\check-health.ps1`
2. **View Logs**: `docker-compose logs -f app`
3. **Database Logs**: `docker-compose logs -f db`
4. **Reset and Retry**: Follow "Reset Everything" section above

## 📊 Expected Test Results

When setup is complete, these should work:

- ✅ http://localhost:8000/docs (Swagger UI loads)
- ✅ http://localhost:8000/api/v1/surprise-bag/ returns `{"data":[],"count":0}`
- ✅ Database contains tables: `user`, `store`, `surprisebag`, `category`, etc.
- ✅ Health check script passes all tests