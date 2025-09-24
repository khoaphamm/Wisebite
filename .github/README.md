# WiseBite CI/CD Setup

This directory contains the continuous integration and deployment workflows for the WiseBite project.

## Workflows

### Backend CI (`ci.yml`)
- **Triggers**: Push/PR to main/develop branches
- **Purpose**: Tests the FastAPI backend using the docker-compose.test.yml configuration
- **Features**:
  - PostgreSQL with PostGIS database setup using Docker Compose
  - Python 3.12 and uv dependency management
  - Comprehensive test suite execution with coverage reporting
  - Dockerized test runner validation
  - Codecov integration for coverage reports

### Android CI (`android.yml`)
- **Triggers**: Push/PR to main/develop branches (when frontend files change)
- **Purpose**: Builds and tests the Android frontend application
- **Features**:
  - JDK 17 setup
  - Gradle caching for faster builds
  - Unit test execution
  - Debug APK build and artifact upload

## Backend CI Details

The backend CI uses the existing `backend/docker-compose.test.yml` file which provides:

- **Test Database**: PostgreSQL 15 with PostGIS 3.4
- **Port**: 5433 (to avoid conflicts with local instances)
- **Credentials**: test_user/test_password
- **Database**: wisebite_test
- **Features**: Health checks, tmpfs for fast execution

### Environment Variables
The backend tests use environment variables defined in `backend/.env.test`:
- `TEST_DATABASE_URL`: Connection string for the test database
- `SECRET_KEY`: Test-only JWT secret
- Various configuration flags for testing mode

### Test Execution Steps
1. Start PostgreSQL database container
2. Wait for database health check
3. Install dependencies using uv
4. Run pytest with coverage
5. Upload coverage reports
6. Test Docker Compose test runner
7. Clean up containers

## Local Testing

To test the CI setup locally:

```bash
cd backend
docker compose -f docker-compose.test.yml up -d test_db
export TEST_DATABASE_URL="postgresql+psycopg2://test_user:test_password@localhost:5433/wisebite_test"
uv sync --extra test
source .venv/bin/activate
python -m pytest tests/ -v --cov=app
docker compose -f docker-compose.test.yml down -v
```

## Coverage Reporting

Coverage reports are generated in XML format and uploaded to Codecov for tracking test coverage over time.