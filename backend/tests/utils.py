"""
Test utilities for WiseBite Backend tests.
"""
import uuid
from typing import Dict, Any
from fastapi.testclient import TestClient


def create_random_user_data() -> Dict[str, Any]:
    """Generate random user data for testing."""
    random_id = str(uuid.uuid4())[:8]
    return {
        "full_name": f"Test User {random_id}",
        "email": f"test{random_id}@example.com",
        "phone": f"091234{random_id[:4]}",
        "password": "testpassword123"
    }


def create_random_store_data() -> Dict[str, Any]:
    """Generate random store data for testing."""
    random_id = str(uuid.uuid4())[:8]
    return {
        "name": f"Test Store {random_id}",
        "description": f"A test store for {random_id}",
        "address": f"123 Test Street {random_id}",
        "phone": f"091234{random_id[:4]}",
        "email": f"store{random_id}@example.com",
        "latitude": 10.7769,
        "longitude": 106.7009
    }


def create_random_food_item_data() -> Dict[str, Any]:
    """Generate random food item data for testing."""
    random_id = str(uuid.uuid4())[:8]
    return {
        "name": f"Test Food {random_id}",
        "description": f"A delicious test food item {random_id}",
        "original_price": 50000.0,
        "discounted_price": 25000.0,
        "quantity": 10,
        "category": "main_course",
        "expires_at": "2024-12-31T23:59:59"
    }


def get_auth_headers(client: TestClient, user_data: Dict[str, Any]) -> Dict[str, str]:
    """Get authentication headers for a user."""
    # First register the user
    register_response = client.post("/api/v1/auth/register", json=user_data)
    
    # Then login to get token
    login_data = {
        "username": user_data["email"],
        "password": user_data["password"]
    }
    login_response = client.post("/api/v1/auth/login", data=login_data)
    
    if login_response.status_code == 200:
        token = login_response.json()["access_token"]
        return {"Authorization": f"Bearer {token}"}
    
    return {}


def assert_status_code(response, expected_status: int):
    """Assert response status code with helpful error message."""
    if response.status_code != expected_status:
        print(f"Expected status {expected_status}, got {response.status_code}")
        print(f"Response body: {response.text}")
    assert response.status_code == expected_status


def assert_response_contains_fields(response_data: Dict[str, Any], required_fields: list):
    """Assert that response contains required fields."""
    for field in required_fields:
        assert field in response_data, f"Field '{field}' not found in response"
