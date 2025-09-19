# WiseBite Backend Testing Guide

This guide provides comprehensive instructions for setting up and running tests for the WiseBite backend API, ensuring that anyone pulling from GitHub can test the system like the development team.

## Prerequisites

### System Requirements
- **Python**: 3.11 or higher
- **PostgreSQL**: 12 or higher
- **Docker**: For containerized testing (recommended)
- **Git**: For cloning the repository

### Required Tools
- `uv` (Python package manager) - [Installation guide](https://docs.astral.sh/uv/)
- `docker` and `docker-compose` - [Installation guide](https://docs.docker.com/get-docker/)
- `pytest` (installed via project dependencies)

## Quick Start

### 1. Clone the Repository
```bash
git clone <repository-url>
cd Wisebite/backend
```

### 2. Set Up Python Environment
```bash
# Install uv if not already installed
pip install uv

# Create virtual environment and install dependencies
uv sync
```

### 3. Set Up Test Database (Docker Method - Recommended)
```bash
# Start test database container
docker-compose -f docker-compose.test.yml up -d

# Wait for database to be ready (usually takes 30-60 seconds)
```

### 4. Run All Tests
```bash
# Activate virtual environment (Windows)
.venv\Scripts\activate

# Set test database URL
$env:TEST_DATABASE_URL = "postgresql+psycopg2://test_user:test_password@localhost:5433/wisebite_test"

# Run all tests
python -m pytest tests/ -v
```

## Detailed Setup Instructions

### Environment Setup

#### Option 1: Using Docker (Recommended)
```bash
# 1. Start the test database
docker-compose -f docker-compose.test.yml up -d

# 2. Verify database is running
docker ps | grep postgres

# Expected output should show a container with port 5433:5432
```

#### Option 2: Local PostgreSQL Installation
```bash
# 1. Install PostgreSQL locally
# 2. Create test database and user
psql -U postgres -c "CREATE USER test_user WITH PASSWORD 'test_password';"
psql -U postgres -c "CREATE DATABASE wisebite_test OWNER test_user;"
psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE wisebite_test TO test_user;"

# 3. Enable PostGIS extension
psql -U test_user -d wisebite_test -c "CREATE EXTENSION IF NOT EXISTS postgis;"
```

### Dependencies Installation

#### Using uv (Recommended)
```bash
# Install all dependencies including dev dependencies
uv sync

# If you need to add new dependencies
uv add package_name
uv add --dev test_package_name  # for test-only dependencies
```

#### Using pip (Alternative)
```bash
# Create virtual environment
python -m venv .venv

# Activate virtual environment
# Windows:
.venv\Scripts\activate
# Linux/Mac:
source .venv/bin/activate

# Install dependencies
pip install -r requirements-test.txt
```

## Running Tests

### Environment Variables
Always set the test database URL before running tests:

**Windows PowerShell:**
```powershell
$env:TEST_DATABASE_URL = "postgresql+psycopg2://test_user:test_password@localhost:5433/wisebite_test"
```

**Windows Command Prompt:**
```cmd
set TEST_DATABASE_URL=postgresql+psycopg2://test_user:test_password@localhost:5433/wisebite_test
```

**Linux/Mac:**
```bash
export TEST_DATABASE_URL="postgresql+psycopg2://test_user:test_password@localhost:5433/wisebite_test"
```

### Test Execution Options

#### Run All Tests
```bash
python -m pytest tests/ -v
```

#### Run Specific Test Modules
```bash
# Transaction tests only
python -m pytest tests/api/endpoints/test_transaction.py -v

# User tests only
python -m pytest tests/api/endpoints/test_user.py -v

# Store tests only
python -m pytest tests/api/endpoints/test_store.py -v

# Auth tests only
python -m pytest tests/api/endpoints/test_auth.py -v

# Order tests only
python -m pytest tests/api/endpoints/test_order_comprehensive.py -v
```

#### Run Specific Test Functions
```bash
# Run a single test
python -m pytest tests/api/endpoints/test_transaction.py::test_create_payment_transaction_success -v

# Run tests matching a pattern
python -m pytest tests/ -k "transaction" -v
```

#### Run Tests with Additional Options
```bash
# Show print statements
python -m pytest tests/ -v -s

# Stop on first failure
python -m pytest tests/ -v -x

# Generate coverage report
python -m pytest tests/ --cov=app --cov-report=html

# Run tests in parallel (install pytest-xdist first)
python -m pytest tests/ -v -n auto
```

### Automated Test Scripts

The repository includes convenience scripts for testing:

#### Windows
```bash
# Run simple tests (PowerShell script)
.\simple-test.ps1

# Run comprehensive tests (PowerShell script)
.\run-tests.ps1
```

#### Linux/Mac
```bash
# Make scripts executable
chmod +x run-tests.sh
chmod +x simple-test.sh

# Run tests
./run-tests.sh
```

## Test Structure

### Test Organization
```
tests/
├── __init__.py                     # Test package initialization
├── conftest.py                     # Pytest configuration and fixtures
├── conftest_simple.py              # Simplified test configuration
├── test_setup.py                   # Basic setup tests
├── utils.py                        # Test utility functions
└── api/                            # API endpoint tests
    ├── __init__.py
    └── endpoints/                  # Individual endpoint test files
        ├── __init__.py
        ├── test_auth.py            # Authentication tests (13 tests)
        ├── test_user.py            # User management tests (7 tests)
        ├── test_store.py           # Store management tests (8 tests)
        ├── test_order_comprehensive.py  # Order tests (11 tests)
        └── test_transaction.py     # Transaction tests (12 tests)
```

### Test Coverage Summary
- **Authentication Tests**: 13 tests covering signup, login, JWT validation
- **User Tests**: 7 tests covering profile management and authorization
- **Store Tests**: 8 tests covering store CRUD operations
- **Order Tests**: 11 tests covering order lifecycle management
- **Transaction Tests**: 12 tests covering payment, refund, and financial operations

**Total**: 51 comprehensive tests

## Database Management

### Test Database Schema
The test database automatically creates all required tables and relationships:
- Users (customers, vendors, admins)
- Stores (restaurant/vendor locations)
- Food Items
- Orders and Order Items
- Transactions (payments, refunds)
- Surprise Bags
- Notifications, Reviews, etc.

### Database Reset
The test database is automatically reset between test runs. If you need to manually reset:

```bash
# Stop and restart the test database container
docker-compose -f docker-compose.test.yml down
docker-compose -f docker-compose.test.yml up -d

# Wait for database to be ready
sleep 30
```

### Database Inspection
```bash
# Connect to test database to inspect data
docker exec -it wisebite_test_db psql -U test_user -d wisebite_test

# Common inspection queries
\dt                          # List all tables
SELECT * FROM users LIMIT 5; # View sample user data
SELECT * FROM transactions;   # View transaction data
\q                           # Quit
```

## Troubleshooting

### Common Issues and Solutions

#### 1. Database Connection Issues
**Problem**: `sqlalchemy.exc.OperationalError: could not connect to server`

**Solutions:**
```bash
# Check if database container is running
docker ps | grep postgres

# Restart database container
docker-compose -f docker-compose.test.yml down
docker-compose -f docker-compose.test.yml up -d

# Check database logs
docker logs wisebite_test_db

# Verify connection manually
docker exec -it wisebite_test_db psql -U test_user -d wisebite_test -c "SELECT 1;"
```

#### 2. Import Errors
**Problem**: `ModuleNotFoundError: No module named 'app'`

**Solutions:**
```bash
# Ensure you're in the backend directory
cd Wisebite/backend

# Verify virtual environment is activated
which python  # Should point to .venv/Scripts/python (Windows) or .venv/bin/python (Linux)

# Reinstall dependencies
uv sync
```

#### 3. Test Failures Due to Environment
**Problem**: Tests pass individually but fail when run together

**Solutions:**
```bash
# Run tests in isolated mode
python -m pytest tests/ --forked

# Check for test isolation issues
python -m pytest tests/ -v --tb=short

# Reset database between problematic tests
docker-compose -f docker-compose.test.yml restart
```

#### 4. Permission Issues (Linux/Mac)
**Problem**: Permission denied errors

**Solutions:**
```bash
# Fix script permissions
chmod +x *.sh

# Fix docker permissions (add user to docker group)
sudo usermod -aG docker $USER
newgrp docker
```

#### 5. Port Conflicts
**Problem**: `Port 5433 is already in use`

**Solutions:**
```bash
# Check what's using the port
netstat -tulpn | grep 5433  # Linux
netstat -an | findstr 5433  # Windows

# Kill the process or change the port in docker-compose.test.yml
# Or use a different port:
# Edit docker-compose.test.yml and change "5433:5432" to "5434:5432"
# Then update TEST_DATABASE_URL accordingly
```

### Performance Tips

1. **Use Docker for Testing**: Containerized database is faster to reset and more consistent
2. **Run Subset of Tests**: During development, run only relevant test modules
3. **Parallel Testing**: Install `pytest-xdist` for parallel test execution
4. **Test Database Persistence**: Keep the test database container running between test sessions

### Debug Mode

Enable debug logging for more detailed output:

```bash
# Set debug environment variable
$env:DEBUG = "1"  # Windows
export DEBUG=1    # Linux/Mac

# Run tests with debug output
python -m pytest tests/ -v -s --log-cli-level=DEBUG
```

## Continuous Integration

### GitHub Actions Integration
The repository should include a `.github/workflows/test.yml` file for automated testing:

```yaml
name: Tests
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgis/postgis:13-3.1
        env:
          POSTGRES_PASSWORD: test_password
          POSTGRES_USER: test_user
          POSTGRES_DB: wisebite_test
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
    steps:
    - uses: actions/checkout@v2
    - name: Set up Python
      uses: actions/setup-python@v2
      with:
        python-version: '3.11'
    - name: Install uv
      run: pip install uv
    - name: Install dependencies
      run: uv sync
    - name: Run tests
      run: |
        export TEST_DATABASE_URL="postgresql+psycopg2://test_user:test_password@localhost:5432/wisebite_test"
        python -m pytest tests/ -v
```

## Contributing Test Guidelines

When contributing new features or fixes:

1. **Write Tests First**: Follow TDD principles
2. **Maintain Coverage**: Ensure new code has appropriate test coverage
3. **Test Isolation**: Each test should be independent
4. **Clear Test Names**: Use descriptive test function names
5. **Documentation**: Update this guide when adding new test patterns

### Adding New Tests

1. **Create Test File**: Add new test files in appropriate directories
2. **Use Fixtures**: Leverage existing fixtures in `conftest.py`
3. **Follow Patterns**: Match existing test structure and naming conventions
4. **Test Edge Cases**: Include error conditions and boundary cases
5. **Update Documentation**: Add new tests to this guide's coverage summary

This testing guide ensures that anyone can clone the repository and run the complete test suite successfully, maintaining code quality and reliability across all development environments.