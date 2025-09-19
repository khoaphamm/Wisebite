"""
Test authentication endpoints for WiseBite Backend.
"""
import pytest
from fastapi.testclient import TestClient
from sqlmodel import Session

from tests.utils import create_random_user_data, assert_status_code, assert_response_contains_fields


@pytest.mark.integration
def test_register_user_success(client: TestClient):
    """Test successful user registration."""
    user_data = create_random_user_data()
    
    response = client.post("/api/v1/auth/register", json=user_data)
    
    assert_status_code(response, 201)
    response_data = response.json()
    assert_response_contains_fields(response_data, ["id", "full_name", "email", "phone", "role"])
    assert response_data["email"] == user_data["email"]
    assert response_data["role"] == "customer"  # Default role


@pytest.mark.integration
def test_register_user_duplicate_email(client: TestClient):
    """Test registration with duplicate email fails."""
    user_data = create_random_user_data()
    
    # Register first user
    response1 = client.post("/api/v1/auth/register", json=user_data)
    assert_status_code(response1, 201)
    
    # Try to register with same email
    response2 = client.post("/api/v1/auth/register", json=user_data)
    assert_status_code(response2, 400)


@pytest.mark.integration
def test_login_success(client: TestClient):
    """Test successful user login."""
    user_data = create_random_user_data()
    
    # Register user first
    register_response = client.post("/api/v1/auth/register", json=user_data)
    assert_status_code(register_response, 201)
    
    # Login
    login_data = {
        "username": user_data["email"],
        "password": user_data["password"]
    }
    login_response = client.post("/api/v1/auth/login", data=login_data)
    
    assert_status_code(login_response, 200)
    response_data = login_response.json()
    assert_response_contains_fields(response_data, ["access_token", "token_type"])
    assert response_data["token_type"] == "bearer"


@pytest.mark.integration
def test_login_invalid_credentials(client: TestClient):
    """Test login with invalid credentials fails."""
    login_data = {
        "username": "nonexistent@example.com",
        "password": "wrongpassword"
    }
    
    response = client.post("/api/v1/auth/login", data=login_data)
    assert_status_code(response, 401)


@pytest.mark.integration
def test_get_current_user(authenticated_customer_client: TestClient, test_customer):
    """Test getting current authenticated user."""
    response = authenticated_customer_client.get("/api/v1/auth/me")
    
    assert_status_code(response, 200)
    response_data = response.json()
    assert_response_contains_fields(response_data, ["id", "full_name", "email", "role"])
    assert response_data["id"] == str(test_customer.id)


@pytest.mark.integration
def test_get_current_user_unauthenticated(client: TestClient):
    """Test getting current user without authentication fails."""
    response = client.get("/api/v1/auth/me")
    assert_status_code(response, 401)
