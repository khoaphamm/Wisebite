# WiseBite Backend Testing Guide

This document explains how to test the WiseBite Backend API.

## 🧪 Testing Structure

The testing structure follows modern Python testing best practices:

```
tests/
├── __init__.py
├── conftest.py          # Test configuration and fixtures
├── utils.py             # Test utilities and helpers
└── api/
    ├── __init__.py
    └── endpoints/
        ├── __init__.py
        ├── test_auth.py      # Authentication tests
        ├── test_user.py      # User management tests
        ├── test_store.py     # Store management tests
        └── test_order.py     # Order management tests
```

## 🛠️ Setup

### 1. Install Test Dependencies

```bash
# Using pip
pip install -r requirements-test.txt

# Or using the test runner with install flag
.\run-tests.ps1 -Install
```

### 2. Setup Test Database

Make sure you have a PostgreSQL test database running. You can:

1. **Use the existing Docker setup** (recommended):
   ```bash
   docker-compose up -d db
   ```

2. **Or use a separate test database instance** on port 5433.

### 3. Configure Environment

The tests use `.env.test` for configuration. Key settings:

```env
TEST_DATABASE_URL=postgresql+psycopg2://postgres:postgres@localhost:5433/wisebite_test_db
SECRET_KEY=test-secret-key-for-testing-only
```

## 🚀 Running Tests

### Using PowerShell Script (Recommended)

```bash
# Run all tests with coverage
.\run-tests.ps1

# Run specific test types
.\run-tests.ps1 -TestType unit
.\run-tests.ps1 -TestType integration
.\run-tests.ps1 -TestType auth
.\run-tests.ps1 -TestType user
.\run-tests.ps1 -TestType store
.\run-tests.ps1 -TestType order

# Run without coverage
.\run-tests.ps1 -Coverage:$false

# Run with installation of dependencies
.\run-tests.ps1 -Install
```

### Using pytest directly

```bash
# Run all tests
pytest

# Run with coverage
pytest --cov=app --cov-report=html --cov-report=term-missing

# Run specific test markers
pytest -m unit
pytest -m integration

# Run specific test files
pytest tests/api/endpoints/test_auth.py
pytest tests/api/endpoints/test_user.py -v
```

## 📊 Test Categories

### Unit Tests
- Test individual functions and models
- Fast execution
- No database dependencies
- Run with: `pytest -m unit`

### Integration Tests
- Test API endpoints
- Use test database
- Test complete request/response cycles
- Run with: `pytest -m integration`

## 🧩 Test Fixtures

The `conftest.py` provides several fixtures:

- `session`: Test database session
- `client`: FastAPI test client
- `test_customer`: Test customer user
- `test_vendor`: Test vendor user
- `test_admin`: Test admin user
- `test_store`: Test store for vendor
- `test_food_item`: Test food item
- `test_surprise_bag`: Test surprise bag
- `authenticated_customer_client`: Authenticated test client for customer
- `authenticated_vendor_client`: Authenticated test client for vendor
- `authenticated_admin_client`: Authenticated test client for admin

## 📝 Writing Tests

### Example Unit Test

```python
@pytest.mark.unit
def test_create_user_model():
    """Test User model creation."""
    user_data = {
        "full_name": "Test User",
        "phone_number": "0123456789",
        "email": "test@example.com",
        "hashed_password": "hashedpassword",
        "role": UserRole.CUSTOMER
    }
    
    user = User(**user_data)
    assert user.full_name == "Test User"
    assert user.email == "test@example.com"
```

### Example Integration Test

```python
@pytest.mark.integration
def test_register_user_success(client: TestClient):
    """Test successful user registration."""
    user_data = create_random_user_data()
    
    response = client.post("/api/v1/auth/register", json=user_data)
    
    assert_status_code(response, 201)
    response_data = response.json()
    assert_response_contains_fields(response_data, ["id", "full_name", "email"])
```

## 📈 Coverage Reports

After running tests with coverage, you can view:

- **Terminal output**: Shows coverage percentage
- **HTML Report**: Open `htmlcov/index.html` in browser for detailed coverage

## 🔧 Troubleshooting

### Common Issues

1. **Import errors**: Make sure test dependencies are installed
2. **Database connection errors**: Check if PostgreSQL is running and accessible
3. **Permission errors**: Ensure test database user has proper permissions

### Debug Mode

Run tests with more verbose output:
```bash
pytest -v -s
.\run-tests.ps1 -Verbose
```

## 🎯 Best Practices

1. **Isolate tests**: Each test should be independent
2. **Use fixtures**: Leverage the provided fixtures for setup
3. **Clear assertions**: Use descriptive assertion messages
4. **Test edge cases**: Include both success and failure scenarios
5. **Keep tests fast**: Unit tests should run quickly

## 📚 Additional Resources

- [FastAPI Testing](https://fastapi.tiangolo.com/tutorial/testing/)
- [Pytest Documentation](https://docs.pytest.org/)
- [SQLModel Testing](https://sqlmodel.tiangolo.com/tutorial/fastapi/tests/)
