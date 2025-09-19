"""
Test user management endpoints for WiseBite Backend.
"""
import pytest
from fastapi.testclient import TestClient
from sqlmodel import Session

from tests.utils import create_random_user_data, assert_status_code, assert_response_contains_fields
from app.models import User, UserRole


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
    assert user.role == UserRole.CUSTOMER


@pytest.mark.integration
def test_get_user_profile(authenticated_customer_client: TestClient, test_customer):
    """Test getting user profile."""
    response = authenticated_customer_client.get(f"/api/v1/users/{test_customer.id}")
    
    assert_status_code(response, 200)
    response_data = response.json()
    assert_response_contains_fields(response_data, ["id", "full_name", "email", "phone_number", "role"])
    assert response_data["id"] == str(test_customer.id)
    assert response_data["email"] == test_customer.email


@pytest.mark.integration
def test_update_user_profile(authenticated_customer_client: TestClient, test_customer):
    """Test updating user profile."""
    update_data = {
        "full_name": "Updated Name",
        "phone_number": "0999888777"
    }
    
    response = authenticated_customer_client.put(f"/api/v1/users/{test_customer.id}", json=update_data)
    
    assert_status_code(response, 200)
    response_data = response.json()
    assert response_data["full_name"] == "Updated Name"
    assert response_data["phone_number"] == "0999888777"


@pytest.mark.integration
def test_get_user_profile_unauthorized(client: TestClient, test_customer):
    """Test getting user profile without authentication fails."""
    response = client.get(f"/api/v1/users/{test_customer.id}")
    assert_status_code(response, 401)


@pytest.mark.integration
def test_get_nonexistent_user(authenticated_customer_client: TestClient):
    """Test getting nonexistent user returns 404."""
    import uuid
    fake_id = uuid.uuid4()
    
    response = authenticated_customer_client.get(f"/api/v1/users/{fake_id}")
    assert_status_code(response, 404)


@pytest.mark.integration 
def test_list_users_admin_only(authenticated_admin_client: TestClient, authenticated_customer_client: TestClient):
    """Test that only admins can list all users."""
    # Admin should be able to list users
    admin_response = authenticated_admin_client.get("/api/v1/users/")
    assert_status_code(admin_response, 200)
    
    # Customer should not be able to list users
    customer_response = authenticated_customer_client.get("/api/v1/users/")
    assert_status_code(customer_response, 403)
