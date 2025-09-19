# WiseBite Backend - Comprehensive Test Suite

## 🎯 Overview

This comprehensive test suite provides thorough testing coverage for the WiseBite Backend API, including:

- **Authentication & Authorization Tests**
- **User Management Tests** 
- **Store Management Tests**
- **Food Item CRUD Tests**
- **Surprise Bag Tests**
- **Order Lifecycle Tests**
- **Transaction & Payment Tests**
- **Integration Tests**

## 🚀 Quick Start

### 1. Setup Test Environment

```bash
# Copy test environment template
cp .env.test.example .env.test

# Start test database (if using Docker)
docker-compose -f docker-compose.test.yml up -d

# Install test dependencies
pip install -r requirements-test.txt
```

### 2. Run All Tests

```bash
# Run comprehensive test suite
python run_comprehensive_tests.py

# Or run with pytest directly
pytest tests/ -v
```

## 📋 Test Coverage Summary

✅ **Authentication Tests** - Complete signup, login, JWT validation  
✅ **Food Item Tests** - CRUD operations, search, filtering  
✅ **Surprise Bag Tests** - Creation, booking, time validation  
✅ **Order Tests** - Complete lifecycle, status management  
✅ **Transaction Tests** - Payment processing, refunds  
✅ **Integration Tests** - End-to-end workflows  

## 🛠️ Enhanced Test Utilities

- **Data Factories**: Random test data generation
- **Authentication Helpers**: Simplified auth setup
- **Assertion Helpers**: Comprehensive validation functions
- **Error Testing**: Proper error response validation

## 📊 Running Specific Tests

```bash
# Authentication tests
pytest tests/api/endpoints/test_auth.py -v

# Food item tests
pytest tests/api/endpoints/test_food_item.py -v

# Order tests
pytest tests/api/endpoints/test_order_comprehensive.py -v

# Transaction tests
pytest tests/api/endpoints/test_transaction.py -v
```

Your comprehensive test suite ensures the WiseBite backend is robust, reliable, and ready for production! 🚀
