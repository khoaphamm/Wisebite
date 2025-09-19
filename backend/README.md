# WiseBite Backend API

WiseBite is a food waste reduction platform that connects customers with local vendors offering discounted surprise bags of food items.

## 🚀 Technologies Used

- **FastAPI** - Modern and fast web framework
- **SQLModel** - ORM based on SQLAlchemy and Pydantic
- **PostgreSQL** - Relational database with PostGIS for location data
- **Alembic** - Database migration tool (planned)
- **JWT** - Authentication and authorization
- **Bcrypt** - Password encryption
- **Cloudinary** - Image storage and management
- **Docker** - Containerization

## 📋 System Requirements

- Python 3.12+
- PostgreSQL 15+
- Docker & Docker Compose

## ⚙️ Installation and Setup

### 1. Clone the repository

```bash
git clone <repository-url>
cd WiseBite/backend
```

### 2. Environment Configuration

Copy and configure the environment file:

```bash
cp .env.example .env
```

Edit `.env` with your configuration:
- Database credentials
- JWT secret key
- Third-party API keys (Cloudinary, Mapbox)

### 3. Start with Docker (Recommended)

```bash
# Start the application and database
docker-compose up --build -d

# Check if services are running
docker-compose ps
```

### 4. Manual Setup (Alternative)

```bash
# Install dependencies
pip install -r requirements.txt
pip install -r requirements-test.txt

# Start PostgreSQL separately
# Then run the application
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

## 🧪 Testing

### Quick Test Setup

```bash
# Start test database
docker-compose -f docker-compose.test.yml up -d test_db

# Run tests using PowerShell script
.\run-tests.ps1

# Or run tests with pytest directly
pytest --cov=app --cov-report=html -v
```

### Test Options

```bash
# Run specific test types
.\run-tests.ps1 -TestType unit
.\run-tests.ps1 -TestType integration
.\run-tests.ps1 -TestType auth

# Install dependencies and run tests
.\run-tests.ps1 -Install

# Run without coverage
.\run-tests.ps1 -Coverage:$false
```

See [tests/README.md](tests/README.md) for detailed testing documentation.

## 📚 API Documentation

After running the application, you can access:

- **Swagger UI**: `http://localhost:8000/docs`
- **ReDoc**: `http://localhost:8000/redoc`
- **OpenAPI JSON**: `http://localhost:8000/openapi.json`

## 🏗️ Project Structure

```
backend/
├── app/
│   ├── __init__.py
│   ├── main.py              # FastAPI app entry point
│   ├── models.py            # SQLModel database models
│   ├── crud.py              # Database operations
│   ├── api/
│   │   ├── deps.py          # Dependencies
│   │   ├── router.py        # API routes
│   │   └── endpoints/       # API endpoint modules
│   ├── core/
│   │   ├── config.py        # Settings and configuration
│   │   ├── db.py            # Database connection
│   │   └── security.py      # Security utilities
│   ├── schemas/             # Pydantic schemas
│   └── services/            # Business logic services
├── tests/                   # Test suite
├── docker-compose.yml       # Main Docker setup
├── docker-compose.test.yml  # Test environment
├── Dockerfile              # Docker image configuration
├── requirements.txt         # Python dependencies
├── requirements-test.txt    # Test dependencies
├── pyproject.toml          # Project configuration
├── pytest.ini             # Test configuration
├── run-tests.ps1           # PowerShell test runner
├── .env                    # Environment variables
└── .env.test              # Test environment variables
```

## 🔧 Development

### Database Migrations (Planned)

```bash
# When Alembic is set up:
alembic revision --autogenerate -m "Description"
alembic upgrade head
```

### Running in Development

```bash
# With auto-reload
uvicorn app.main:app --reload

# Or with Docker for development
docker-compose up app
```

### Code Quality

```bash
# Run tests with coverage
pytest --cov=app --cov-report=html

# Format code (when configured)
black app/
isort app/

# Lint code (when configured)
flake8 app/
```

## 🌐 API Endpoints

### Authentication
- `POST /api/v1/auth/register` - User registration
- `POST /api/v1/auth/login` - User login
- `GET /api/v1/auth/me` - Get current user

### Users
- `GET /api/v1/users/{user_id}` - Get user profile
- `PUT /api/v1/users/{user_id}` - Update user profile

### Stores
- `GET /api/v1/stores/` - List all stores
- `POST /api/v1/stores/` - Create store (vendors only)
- `GET /api/v1/stores/{store_id}` - Get store details
- `PUT /api/v1/stores/{store_id}` - Update store

### Surprise Bags
- `GET /api/v1/surprise-bags/` - List available surprise bags
- `POST /api/v1/surprise-bags/` - Create surprise bag (vendors only)
- `GET /api/v1/surprise-bags/{bag_id}` - Get surprise bag details

### Orders
- `POST /api/v1/orders/` - Create order
- `GET /api/v1/orders/my-orders` - Get customer's orders
- `GET /api/v1/orders/{order_id}` - Get order details

## 🔐 Authentication

The API uses JWT (JSON Web Tokens) for authentication:

1. Register or login to get an access token
2. Include the token in the Authorization header: `Bearer <token>`
3. Tokens expire after 30 minutes (configurable)

## 🌍 Environment Variables

Key environment variables:

```env
# Database
POSTGRES_HOST=db
POSTGRES_PORT=5432
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
POSTGRES_DB=wisebite_db

# Security
SECRET_KEY=your-secret-key-here

# Third-party services
CLOUDINARY_CLOUD_NAME=your-cloud-name
CLOUDINARY_API_KEY=your-api-key
CLOUDINARY_API_SECRET=your-api-secret
MAPBOX_ACCESS_TOKEN=your-mapbox-token
```

## 🐳 Docker Commands

```bash
# Build and start all services
docker-compose up --build -d

# View logs
docker-compose logs -f app

# Stop services
docker-compose down

# Reset database (removes all data)
docker-compose down -v
docker-compose up --build -d

# Run tests in Docker
docker-compose -f docker-compose.test.yml up --build test_runner
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Write tests for your changes
4. Ensure all tests pass
5. Submit a pull request

## 📝 License

This project is licensed under the MIT License.

## 📞 Support

For questions or issues, please open an issue on the repository or contact the development team.
