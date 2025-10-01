# WiseBite Backend API

WiseBite is a comprehensive food delivery and ordering platform that connects customers with local vendors, featuring real-time ordering, payment processing, and transaction management.

## 🚀 Technologies Used

- **FastAPI** - Modern, fast web framework with automatic API documentation
- **SQLModel** - ORM based on SQLAlchemy and Pydantic with type safety
- **PostgreSQL** - Relational database with PostGIS for geospatial data
- **JWT** - Secure authentication and authorization
- **Bcrypt** - Password encryption and security
- **Docker** - Containerization for easy deployment
- **Pytest** - Comprehensive testing framework
- **UV** - Fast Python package manager

## 📋 System Requirements

- Python 3.11+
- PostgreSQL 12+
- Docker & Docker Compose
- UV package manager (recommended)

## ⚙️ Quick Start

### Option 1: Automated Setup (Recommended)

For new developers, use the automated setup script:

```bash
# Windows (PowerShell - Recommended)
.\setup-db.ps1

# Windows (Command Prompt)
setup-db.bat

# Linux/Mac
./setup-db.sh
```

This script will:
- ✅ Check Docker status
- 📦 Build and start all containers
- 🗄️ Run database migrations
- 🌱 Populate initial data
- 🧪 Test API connectivity

### Option 2: Manual Setup

#### 1. Clone and Setup

```bash
git clone <repository-url>
cd Wisebite/backend

# Install UV if not already installed
pip install uv

# Install dependencies
uv sync
```

#### 2. Database Setup

```bash
# Build and start containers (includes database migration)
docker-compose build
docker-compose up -d

# Wait for database to be ready (15-30 seconds)
# Then run migrations
docker-compose exec app uv run alembic upgrade head

# Optional: Populate initial data
docker-compose exec app uv run python -c "
from app.initial_db import populate_store_and_categories, create_initial_superuser
populate_store_and_categories()
create_initial_superuser()
"
```

#### 3. Verify Setup

```bash
# Test API endpoint
curl http://localhost:8000/api/v1/surprise-bag/
# Expected: {"data":[],"count":0}

# Access API documentation
# http://localhost:8000/docs (Swagger UI)
# http://localhost:8000/redoc (ReDoc)
```

## 🧪 Testing

The project includes a comprehensive test suite with **51 tests** covering all major functionality:

### Test Coverage
- **Authentication**: 13 tests (signup, login, JWT validation)
- **Users**: 7 tests (profile management, authorization)
- **Stores**: 8 tests (CRUD operations, vendor management)
- **Orders**: 11 tests (order lifecycle, validations)
- **Transactions**: 12 tests (payments, refunds, financial operations)

### Running Tests

```bash
# Quick test with provided script
.\run-tests.ps1  # Windows
./run-tests.sh   # Linux/Mac

# Run specific test modules
python -m pytest tests/api/endpoints/test_transaction.py -v
python -m pytest tests/api/endpoints/test_auth.py -v
python -m pytest tests/api/endpoints/test_user.py -v

# Run with coverage report
python -m pytest tests/ --cov=app --cov-report=html -v
```

**📖 For detailed testing instructions, see [docs/TESTING_GUIDE.md](docs/TESTING_GUIDE.md)**

## 📚 API Documentation

### Interactive Documentation
- **Swagger UI**: `http://localhost:8000/docs`
- **ReDoc**: `http://localhost:8000/redoc`
- **OpenAPI JSON**: `http://localhost:8000/openapi.json`

### Comprehensive API Guide
**📖 For complete API documentation with examples, see [docs/API_DOCUMENTATION.md](docs/API_DOCUMENTATION.md)**

## 🏗️ Project Structure

```
backend/
├── app/
│   ├── main.py              # FastAPI application entry point
│   ├── models.py            # SQLModel database models
│   ├── crud.py              # Database CRUD operations
│   ├── api/
│   │   ├── deps.py          # Dependency injection
│   │   ├── router.py        # API route aggregation
│   │   └── endpoints/       # Individual API endpoint modules
│   │       ├── auth.py      # Authentication endpoints
│   │       ├── user.py      # User management
│   │       ├── store.py     # Store operations
│   │       ├── order.py     # Order processing
│   │       ├── transaction.py # Payment & financial operations
│   │       └── ...          # Other endpoint modules
│   ├── core/
│   │   ├── config.py        # Application configuration
│   │   ├── db.py            # Database connection & session
│   │   └── security.py      # Security utilities (JWT, passwords)
│   ├── schemas/             # Pydantic request/response schemas
│   │   ├── auth.py          # Authentication schemas
│   │   ├── user.py          # User schemas
│   │   ├── transaction.py   # Transaction schemas
│   │   └── ...              # Other schema modules
│   └── services/            # Business logic services
│       ├── email.py         # Email service
│       ├── upload.py        # File upload service
│       └── mapbox.py        # Location services
├── tests/                   # Comprehensive test suite
│   ├── conftest.py          # Pytest configuration & fixtures
│   ├── utils.py             # Test utility functions
│   └── api/endpoints/       # API endpoint tests
├── docs/                    # Documentation
│   ├── API_DOCUMENTATION.md # Complete API reference
│   └── TESTING_GUIDE.md     # Testing setup guide
├── docker-compose.yml       # Main application containers
├── docker-compose.test.yml  # Test environment containers
├── pyproject.toml          # Project configuration & dependencies
├── pytest.ini             # Test runner configuration
└── uv.lock                 # Dependency lock file
```

## 🔧 Development Workflow

### Local Development Setup

```bash
# 1. Install dependencies
uv sync

# 2. Start development database
docker-compose up -d db

# 3. Run application with auto-reload
uvicorn app.main:app --reload

# 4. Run tests during development
python -m pytest tests/api/endpoints/test_transaction.py -v
```

## 🛠️ Troubleshooting

### Database Setup Issues

#### "could not translate host name 'db'" Error
This happens when running Alembic outside Docker. Solution:
```bash
# Run migrations inside Docker container
docker-compose exec app uv run alembic upgrade head
```

#### "relation already exists" Error
If migration fails due to existing tables:
```bash
# Mark current state as migrated
docker-compose exec app uv run alembic stamp add_categories_inventory

# Then run future migrations normally
docker-compose exec app uv run alembic upgrade head
```

#### "API returns 500 Internal Server Error"
Usually indicates missing database columns:
```bash
# Check migration status
docker-compose exec app uv run alembic current

# Run pending migrations
docker-compose exec app uv run alembic upgrade head
```

#### Reset Database (Clean Start)
```bash
# Stop containers and remove volumes
docker-compose down -v

# Rebuild and restart
docker-compose build
docker-compose up -d

# Run setup script
.\setup-db.ps1  # Windows
./setup-db.sh   # Linux/Mac
```

### Docker Issues

#### "Docker is not running"
1. Start Docker Desktop
2. Wait for Docker to fully initialize
3. Run setup script again

#### "Port already in use"
```bash
# Check what's using the port
netstat -ano | findstr :8000  # Windows
lsof -i :8000                 # Linux/Mac

# Stop conflicting services or change port in docker-compose.yml
```

### Environment Configuration

Create `.env` file for local development:

```env
# Database
DATABASE_URL=postgresql://user:password@localhost:5432/wisebite_db

# Security
SECRET_KEY=your-secret-key-here
ALGORITHM=HS256
ACCESS_TOKEN_EXPIRE_MINUTES=30

# Third-party services (optional)
CLOUDINARY_CLOUD_NAME=your-cloud-name
CLOUDINARY_API_KEY=your-api-key
CLOUDINARY_API_SECRET=your-api-secret
MAPBOX_ACCESS_TOKEN=your-mapbox-token
```

## 🌐 Core API Features

### Authentication & Authorization
- JWT-based authentication with secure token handling
- Role-based access control (Customer, Vendor, Admin)
- Protected endpoints with automatic user identification

### Transaction Processing
- **Payment Processing**: Complete payment workflows with validation
- **Refund Management**: Full refund capabilities with audit trails
- **Financial Reporting**: Vendor revenue summaries and analytics
- **Multi-payment Methods**: Credit card, debit card, cash, e-wallet support

### Order Management
- **Order Creation**: Multi-item orders with real-time inventory checks
- **Order Tracking**: Complete order lifecycle management
- **Status Updates**: Real-time order status notifications
- **Vendor Operations**: Order management for restaurant owners

### User Management
- **Profile Management**: Comprehensive user profile operations
- **Location Services**: Address and coordinate management
- **Access Control**: Secure user data with proper authorization

### Store Operations
- **Store Management**: Complete CRUD operations for vendor stores
- **Location Integration**: Geospatial queries for store discovery
- **Business Hours**: Flexible operating hours management

## 🔐 Security Features

- **JWT Authentication**: Secure token-based authentication
- **Password Security**: Bcrypt hashing with salt
- **Input Validation**: Pydantic schema validation for all inputs
- **SQL Injection Prevention**: SQLModel ORM protection
- **Rate Limiting**: Built-in protection against abuse
- **CORS Configuration**: Secure cross-origin request handling

## 🧪 Quality Assurance

### Testing Standards
- **Unit Tests**: Individual function and method testing
- **Integration Tests**: End-to-end API workflow testing
- **Authentication Tests**: Comprehensive security testing
- **Database Tests**: Transaction and data integrity testing
- **Error Handling Tests**: Edge case and failure scenario testing

### Code Quality
- **Type Safety**: Full type hints with SQLModel and Pydantic
- **Documentation**: Comprehensive docstrings and API docs
- **Error Handling**: Consistent error responses with proper HTTP status codes
- **Logging**: Structured logging for debugging and monitoring

## 🐳 Docker Deployment

### Production Deployment

```bash
# Build and start all services
docker-compose up --build -d

# View application logs
docker-compose logs -f app

# Scale services if needed
docker-compose up --scale app=3 -d
```

### Testing Environment

```bash
# Start isolated test environment
docker-compose -f docker-compose.test.yml up -d

# Run tests in containerized environment
docker-compose -f docker-compose.test.yml exec app python -m pytest tests/ -v

# Clean up test environment
docker-compose -f docker-compose.test.yml down -v
```

## 🤝 Contributing

### Getting Started
1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Follow the testing guide in [docs/TESTING_GUIDE.md](docs/TESTING_GUIDE.md)
4. Ensure all tests pass: `python -m pytest tests/ -v`
5. Commit changes: `git commit -m 'Add amazing feature'`
6. Push to branch: `git push origin feature/amazing-feature`
7. Open a Pull Request

### Development Standards
- **Write Tests**: All new features must include comprehensive tests
- **API Documentation**: Update API docs for any endpoint changes
- **Type Safety**: Use proper type hints throughout the codebase
- **Error Handling**: Implement proper error responses and validation
- **Security**: Follow security best practices for authentication and data handling

### Testing Requirements
- All tests must pass before submitting PR
- New features require 90%+ test coverage
- Include both positive and negative test cases
- Test error conditions and edge cases

## 📝 Recent Updates

### Transaction System Overhaul (Latest)
- ✅ Complete transaction test suite (12/12 tests passing)
- ✅ Enhanced payment processing with proper vendor determination
- ✅ Refund system with full audit trails
- ✅ Vendor financial reporting and revenue calculations
- ✅ Standardized response schemas across all endpoints

### Database Schema Improvements
- ✅ Optional order_id for refund transactions
- ✅ Proper foreign key relationships and constraints
- ✅ Enhanced data validation and type safety

### API Consistency
- ✅ Standardized response formats using TransactionPublic schema
- ✅ Consistent error handling across all endpoints
- ✅ Improved authentication and authorization flows

## 📞 Support

For questions, issues, or contributions:

1. **Issues**: Open an issue on the repository for bugs or feature requests
2. **Documentation**: Check [docs/API_DOCUMENTATION.md](docs/API_DOCUMENTATION.md) for API questions
3. **Testing**: See [docs/TESTING_GUIDE.md](docs/TESTING_GUIDE.md) for testing help
4. **Development**: Follow the contributing guidelines above

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

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
