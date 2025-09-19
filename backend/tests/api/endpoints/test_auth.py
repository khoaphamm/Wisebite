"""
Test authentication endpoints for WiseBite Backend.
"""
import pytest
from fastapi.testclient import TestClient
from sqlmodel import Session

from tests.utils import (
    create_random_user_data, 
    assert_status_code, 
    assert_response_contains_fields,
    assert_response_error,
    assert_valid_uuid
)


@pytest.mark.integration
def test_signup_customer_success(client: TestClient):
    """Test successful customer registration."""
    user_data = create_random_user_data(role="customer")
    
    response = client.post("/api/v1/auth/signup", json=user_data)
    
    assert_status_code(response, 201)
    response_data = response.json()
    assert_response_contains_fields(response_data, [
        "id", "full_name", "email", "phone_number", "role", "avt_url"
    ])
    assert response_data["email"] == user_data["email"]
    assert response_data["phone_number"] == user_data["phone_number"]
    assert response_data["role"] == "customer"
    assert_valid_uuid(response_data["id"])


@pytest.mark.integration
def test_signup_vendor_success(client: TestClient):
    """Test successful vendor registration."""
    user_data = create_random_user_data(role="vendor")
    
    response = client.post("/api/v1/auth/signup", json=user_data)
    
    assert_status_code(response, 201)
    response_data = response.json()
    assert response_data["role"] == "vendor"


@pytest.mark.integration
def test_signup_duplicate_email(client: TestClient):
    """Test registration with duplicate email fails."""
    user_data = create_random_user_data()
    
    # Register first user
    response1 = client.post("/api/v1/auth/signup", json=user_data)
    assert_status_code(response1, 201)
    
    # Try to register with same email
    user_data2 = create_random_user_data()
    user_data2["email"] = user_data["email"]  # Same email
    response2 = client.post("/api/v1/auth/signup", json=user_data2)
    assert_response_error(response2, 400, "email already exists")


@pytest.mark.integration
def test_signup_duplicate_phone(client: TestClient):
    """Test registration with duplicate phone number fails."""
    user_data = create_random_user_data()
    
    # Register first user
    response1 = client.post("/api/v1/auth/signup", json=user_data)
    assert_status_code(response1, 201)
    
    # Try to register with same phone
    user_data2 = create_random_user_data()
    user_data2["phone_number"] = user_data["phone_number"]  # Same phone
    response2 = client.post("/api/v1/auth/signup", json=user_data2)
    assert_response_error(response2, 400, "phone number already exists")


@pytest.mark.integration
def test_signup_invalid_data(client: TestClient):
    """Test registration with invalid data fails."""
    invalid_data = {
        "email": "invalid-email",  # Invalid email format
        "phone_number": "123",     # Too short
        "full_name": "",           # Empty name
        "password": "123",         # Too short
        "role": "invalid_role"     # Invalid role
    }
    
    response = client.post("/api/v1/auth/signup", json=invalid_data)
    assert_status_code(response, 422)  # Validation error


@pytest.mark.integration
def test_login_success_with_phone(client: TestClient):
    """Test successful login with phone number."""
    user_data = create_random_user_data()
    
    # Register user first
    register_response = client.post("/api/v1/auth/signup", json=user_data)
    assert_status_code(register_response, 201)
    
    # Login with phone number as username
    login_data = {
        "username": user_data["phone_number"],
        "password": user_data["password"]
    }
    login_response = client.post(
        "/api/v1/auth/login", 
        data=login_data,
        headers={"Content-Type": "application/x-www-form-urlencoded"}
    )
    
    assert_status_code(login_response, 200)
    response_data = login_response.json()
    assert_response_contains_fields(response_data, ["access_token"])
    assert isinstance(response_data["access_token"], str)
    assert len(response_data["access_token"]) > 10  # JWT token should be long


@pytest.mark.integration
def test_login_invalid_phone(client: TestClient):
    """Test login with invalid phone number fails."""
    login_data = {
        "username": "9999999999",  # Non-existent phone
        "password": "wrongpassword"
    }
    
    response = client.post(
        "/api/v1/auth/login", 
        data=login_data,
        headers={"Content-Type": "application/x-www-form-urlencoded"}
    )
    assert_response_error(response, 401, "incorrect phone number or password")


@pytest.mark.integration
def test_login_invalid_password(client: TestClient):
    """Test login with invalid password fails."""
    user_data = create_random_user_data()
    
    # Register user first
    register_response = client.post("/api/v1/auth/signup", json=user_data)
    assert_status_code(register_response, 201)
    
    # Try login with wrong password
    login_data = {
        "username": user_data["phone_number"],
        "password": "wrongpassword"
    }
    response = client.post(
        "/api/v1/auth/login", 
        data=login_data,
        headers={"Content-Type": "application/x-www-form-urlencoded"}
    )
    assert_response_error(response, 401, "incorrect phone number or password")


@pytest.mark.integration
def test_login_missing_fields(client: TestClient):
    """Test login with missing fields fails."""
    # Missing password
    response1 = client.post(
        "/api/v1/auth/login", 
        data={"username": "1234567890"},
        headers={"Content-Type": "application/x-www-form-urlencoded"}
    )
    assert_status_code(response1, 422)
    
    # Missing username
    response2 = client.post(
        "/api/v1/auth/login", 
        data={"password": "password123"},
        headers={"Content-Type": "application/x-www-form-urlencoded"}
    )
    assert_status_code(response2, 422)


@pytest.mark.integration
def test_protected_endpoint_without_token(client: TestClient):
    """Test accessing protected endpoint without token fails."""
    response = client.get("/api/v1/user/me")
    assert_status_code(response, 401)


@pytest.mark.integration
def test_protected_endpoint_with_invalid_token(client: TestClient):
    """Test accessing protected endpoint with invalid token fails."""
    headers = {"Authorization": "Bearer invalid_token"}
    response = client.get("/api/v1/user/me", headers=headers)
    assert_status_code(response, 403)


@pytest.mark.integration
def test_get_current_user_success(client: TestClient):
    """Test getting current authenticated user."""
    user_data = create_random_user_data()
    
    # Register user
    register_response = client.post("/api/v1/auth/signup", json=user_data)
    assert_status_code(register_response, 201)
    
    # Login to get token
    login_data = {
        "username": user_data["phone_number"],
        "password": user_data["password"]
    }
    login_response = client.post(
        "/api/v1/auth/login", 
        data=login_data,
        headers={"Content-Type": "application/x-www-form-urlencoded"}
    )
    token = login_response.json()["access_token"]
    
    # Get current user
    headers = {"Authorization": f"Bearer {token}"}
    response = client.get("/api/v1/user/me", headers=headers)
    
    assert_status_code(response, 200)
    response_data = response.json()
    assert_response_contains_fields(response_data, [
        "id", "full_name", "email", "phone_number", "role"
    ])
    assert response_data["email"] == user_data["email"]
    assert response_data["phone_number"] == user_data["phone_number"]


@pytest.mark.integration
def test_jwt_token_expiry_structure(client: TestClient):
    """Test that JWT token has proper structure and claims."""
    import jwt
    from app.core.config import settings
    
    user_data = create_random_user_data()
    
    # Register and login
    register_response = client.post("/api/v1/auth/signup", json=user_data)
    user_id = register_response.json()["id"]
    
    login_data = {
        "username": user_data["phone_number"],
        "password": user_data["password"]
    }
    login_response = client.post(
        "/api/v1/auth/login", 
        data=login_data,
        headers={"Content-Type": "application/x-www-form-urlencoded"}
    )
    token = login_response.json()["access_token"]
    
    # Decode token (without verification for testing)
    decoded = jwt.decode(token, options={"verify_signature": False})
    
    # Check token structure
    assert "sub" in decoded  # Subject (user ID)
    assert "exp" in decoded  # Expiration time
    assert decoded["sub"] == user_id
