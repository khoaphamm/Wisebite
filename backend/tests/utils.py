"""
Test utilities for WiseBite Backend tests.
"""
import uuid
import random
from typing import Dict, Any, Optional
from datetime import datetime, timedelta
from fastapi.testclient import TestClient


def create_random_user_data(role: str = "customer") -> Dict[str, Any]:
    """Generate random user data for testing."""
    random_id = str(uuid.uuid4())[:8]
    return {
        "full_name": f"Test User {random_id}",
        "email": f"test{random_id}@example.com",
        "phone_number": f"091234{random_id[:4]}",
        "password": "testpassword123",
        "role": role,
        "gender": random.choice(["male", "female", "other"]),
        "birth_date": "1990-01-01"
    }


def create_random_store_data() -> Dict[str, Any]:
    """Generate random store data for testing."""
    random_id = str(uuid.uuid4())[:8]
    return {
        "name": f"Test Store {random_id}",
        "description": f"A test store for {random_id}",
        "address": f"123 Test Street {random_id}, Ho Chi Minh City",
        "latitude": 10.7769 + random.uniform(-0.01, 0.01),  # Small variation around Ho Chi Minh City
        "longitude": 106.7009 + random.uniform(-0.01, 0.01)
    }


def create_random_food_item_data(store_id: Optional[str] = None) -> Dict[str, Any]:
    """Generate random food item data for testing."""
    random_id = str(uuid.uuid4())[:8]
    future_time = datetime.now() + timedelta(hours=random.randint(2, 24))
    
    data = {
        "name": f"Test Food {random_id}",
        "description": f"A delicious test food item {random_id}",
        "original_price": round(random.uniform(10000, 100000), 2),
        "quantity": random.randint(1, 20),
        "category": random.choice(["main_course", "dessert", "drink", "snack"]),
        "expires_at": future_time.isoformat(),
        "ingredients": f"Test ingredients for {random_id}",
        "allergens": random.choice(["nuts", "dairy", "gluten", None])
    }
    
    if store_id:
        data["store_id"] = store_id
    
    return data


def create_random_surprise_bag_data(store_id: Optional[str] = None) -> Dict[str, Any]:
    """Generate random surprise bag data for testing."""
    random_id = str(uuid.uuid4())[:8]
    now = datetime.now()
    # Create a pickup window that's currently active for testing
    pickup_start = now - timedelta(hours=random.randint(1, 2))  # Started 1-2 hours ago
    pickup_end = now + timedelta(hours=random.randint(4, 8))     # Ends 4-8 hours from now
    
    # Ensure pricing constraint: discounted_price < original_value
    original_value = round(random.uniform(30000, 150000), 2)
    # Set discount price to be 50-80% of original value to ensure it's always less
    discount_percentage = random.uniform(0.5, 0.8)
    discounted_price = round(original_value * discount_percentage, 2)
    
    data = {
        "name": f"Test Surprise Bag {random_id}",
        "description": f"Surprise bag with mystery items {random_id}",
        "original_value": original_value,
        "discounted_price": discounted_price,
        "quantity_available": random.randint(1, 10),
        "pickup_start_time": pickup_start.isoformat(),
        "pickup_end_time": pickup_end.isoformat()
    }
    
    if store_id:
        data["store_id"] = store_id
    
    return data


def create_random_order_data(items: list = None) -> Dict[str, Any]:
    """Generate random order data for testing."""
    if items is None:
        items = [
            {
                "food_item_id": str(uuid.uuid4()),
                "quantity": random.randint(1, 5)
            }
        ]
    
    return {
        "items": items,
        "delivery_address": "123 Delivery Street, Ho Chi Minh City",
        "notes": "Test order notes"
    }


def create_random_chat_message(content: Optional[str] = None) -> Dict[str, Any]:
    """Generate random chat message data for testing."""
    random_id = str(uuid.uuid4())[:8]
    return {
        "content": content or f"Test message {random_id}",
        "message_type": "text"
    }


def get_auth_headers(client: TestClient, user_data: Dict[str, Any]) -> Dict[str, str]:
    """Get authentication headers for a user."""
    # First register the user
    register_response = client.post("/api/v1/auth/signup", json=user_data)
    
    # Then login to get token (using phone_number as username)
    login_data = {
        "username": user_data["phone_number"],
        "password": user_data["password"]
    }
    login_response = client.post(
        "/api/v1/auth/login", 
        data=login_data,
        headers={"Content-Type": "application/x-www-form-urlencoded"}
    )
    
    if login_response.status_code == 200:
        token = login_response.json()["access_token"]
        return {"Authorization": f"Bearer {token}"}
    
    return {}


def create_user_and_get_token(client: TestClient, role: str = "customer") -> tuple[Dict[str, Any], str]:
    """Create a user and return user data and auth token."""
    user_data = create_random_user_data(role)
    
    # Register user
    register_response = client.post("/api/v1/auth/signup", json=user_data)
    assert register_response.status_code == 201
    
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
    assert login_response.status_code == 200
    
    token = login_response.json()["access_token"]
    return user_data, token


def create_authenticated_client(client: TestClient, role: str = "customer") -> tuple[TestClient, Dict[str, Any], str]:
    """Create an authenticated test client with user data and token."""
    user_data, token = create_user_and_get_token(client, role)
    
    # Create a new client instance to avoid header conflicts
    from fastapi.testclient import TestClient
    from app.main import app
    authenticated_client = TestClient(app)
    authenticated_client.headers.update({"Authorization": f"Bearer {token}"})
    
    return authenticated_client, user_data, token


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


def assert_valid_uuid(value: str):
    """Assert that a string is a valid UUID."""
    try:
        uuid.UUID(value)
    except ValueError:
        assert False, f"'{value}' is not a valid UUID"


def assert_valid_datetime(value: str):
    """Assert that a string is a valid ISO datetime."""
    try:
        datetime.fromisoformat(value.replace('Z', '+00:00'))
    except ValueError:
        assert False, f"'{value}' is not a valid ISO datetime"


def assert_response_error(response, expected_status: int, expected_detail: str = None):
    """Assert error response format and content."""
    assert_status_code(response, expected_status)
    response_data = response.json()
    assert "detail" in response_data
    
    if expected_detail:
        assert expected_detail.lower() in response_data["detail"].lower()


def assert_pagination_response(response_data: Dict[str, Any], expected_fields: list = None):
    """Assert pagination response structure."""
    required_fields = ["data", "count"]
    if expected_fields:
        required_fields.extend(expected_fields)
    
    assert_response_contains_fields(response_data, required_fields)
    assert isinstance(response_data["data"], list)
    assert isinstance(response_data["count"], int)
    assert response_data["count"] >= 0


def cleanup_test_data(session, models_to_delete: list):
    """Clean up test data after tests."""
    for model in models_to_delete:
        session.delete(model)
    session.commit()
