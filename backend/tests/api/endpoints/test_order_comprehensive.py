"""
Test order management endpoints for WiseBite Backend.
"""
import pytest
from fastapi.testclient import TestClient
from sqlmodel import Session

from tests.utils import (
    create_random_user_data,
    create_random_store_data, 
    create_random_food_item_data,
    create_random_order_data,
    create_authenticated_client,
    assert_status_code,
    assert_response_contains_fields,
    assert_response_error,
    assert_valid_uuid,
    assert_valid_datetime,
    assert_pagination_response
)


@pytest.mark.integration
def test_create_order_success(client: TestClient):
    """Test successful order creation by customer."""
    # Create vendor, store, and food items
    vendor_client, _, _ = create_authenticated_client(client, "vendor")
    
    store_data = create_random_store_data()
    store_response = vendor_client.post("/api/v1/stores/", json=store_data)
    store_id = store_response.json()["id"]
    
    # Create food items
    food_items = []
    for i in range(2):
        food_data = create_random_food_item_data(store_id)
        food_data["quantity"] = 10  # Ensure sufficient quantity
        food_response = vendor_client.post("/api/v1/food-items/", json=food_data)
        food_items.append(food_response.json())
    
    # Create customer and place order
    customer_client, _, _ = create_authenticated_client(client, "customer")
    
    order_data = {
        "items": [
            {"food_item_id": food_items[0]["id"], "quantity": 2},
            {"food_item_id": food_items[1]["id"], "quantity": 1}
        ],
        "store_id": store_id,
        "delivery_address": "123 Test Street, Ho Chi Minh City",
        "notes": "Please handle with care"
    }
    
    response = customer_client.post("/api/v1/orders/", json=order_data)
    
    assert_status_code(response, 201)
    response_data = response.json()
    assert_response_contains_fields(response_data, [
        "id", "customer_id", "items", "total_amount",
        "status", "created_at", "delivery_address"
    ])
    assert response_data["status"] == "pending"
    assert len(response_data["items"]) == 2
    assert_valid_uuid(response_data["id"])
    assert_valid_datetime(response_data["created_at"])


@pytest.mark.integration
def test_create_order_insufficient_quantity(client: TestClient):
    """Test order creation with insufficient food item quantity."""
    vendor_client, _, _ = create_authenticated_client(client, "vendor")
    
    # Create store and food item with limited quantity
    store_data = create_random_store_data()
    store_response = vendor_client.post("/api/v1/stores/", json=store_data)
    store_id = store_response.json()["id"]
    
    food_data = create_random_food_item_data(store_id)
    food_data["quantity"] = 2  # Only 2 available
    food_response = vendor_client.post("/api/v1/food-items/", json=food_data)
    food_id = food_response.json()["id"]
    
    # Try to order more than available
    customer_client, _, _ = create_authenticated_client(client, "customer")
    
    order_data = {
        "items": [{"food_item_id": food_id, "quantity": 5}],  # More than available
        "store_id": store_id,
        "delivery_address": "123 Test Street"
    }
    
    response = customer_client.post("/api/v1/orders/", json=order_data)
    assert_response_error(response, 400, "not enough stock")


@pytest.mark.integration
def test_create_order_nonexistent_food_item(client: TestClient):
    """Test order creation with nonexistent food item."""
    import uuid
    
    vendor_client, _, _ = create_authenticated_client(client, "vendor")
    customer_client, _, _ = create_authenticated_client(client, "customer")
    
    # Create store
    store_data = create_random_store_data()
    store_response = vendor_client.post("/api/v1/stores/", json=store_data)
    store_id = store_response.json()["id"]
    
    fake_food_id = str(uuid.uuid4())
    order_data = {
        "items": [{"food_item_id": fake_food_id, "quantity": 1}],
        "store_id": store_id,
        "delivery_address": "123 Test Street"
    }
    
    response = customer_client.post("/api/v1/orders/", json=order_data)
    assert_response_error(response, 404, "Food item with id")


@pytest.mark.integration
def test_create_order_unauthenticated(client: TestClient):
    """Test that unauthenticated users cannot create orders."""
    order_data = create_random_order_data()
    response = client.post("/api/v1/orders/", json=order_data)
    
    assert_status_code(response, 401)


@pytest.mark.integration
def test_get_customer_orders(client: TestClient):
    """Test getting customer's orders."""
    # Create order first
    vendor_client, _, _ = create_authenticated_client(client, "vendor")
    customer_client, _, _ = create_authenticated_client(client, "customer")
    
    # Setup store and food item
    store_data = create_random_store_data()
    store_response = vendor_client.post("/api/v1/stores/", json=store_data)
    store_id = store_response.json()["id"]
    
    food_data = create_random_food_item_data(store_id)
    food_response = vendor_client.post("/api/v1/food-items/", json=food_data)
    food_id = food_response.json()["id"]
    
    # Create order
    order_data = {
        "items": [{"food_item_id": food_id, "quantity": 1}],
        "store_id": store_id,
        "delivery_address": "123 Test Street"
    }
    customer_client.post("/api/v1/orders/", json=order_data)
    
    # Get customer orders
    response = customer_client.get("/api/v1/orders/me")
    
    assert_status_code(response, 200)
    response_data = response.json()
    assert_pagination_response(response_data)
    assert len(response_data["data"]) >= 1


@pytest.mark.integration
def test_get_vendor_orders(client: TestClient):
    """Test getting vendor's orders."""
    vendor_client, _, _ = create_authenticated_client(client, "vendor")
    customer_client, _, _ = create_authenticated_client(client, "customer")
    
    # Setup store and food item
    store_data = create_random_store_data()
    store_response = vendor_client.post("/api/v1/stores/", json=store_data)
    store_id = store_response.json()["id"]
    
    food_data = create_random_food_item_data(store_id)
    food_response = vendor_client.post("/api/v1/food-items/", json=food_data)
    food_id = food_response.json()["id"]
    
    # Customer creates order
    order_data = {
        "items": [{"food_item_id": food_id, "quantity": 1}],
        "store_id": store_id,
        "delivery_address": "123 Test Street"
    }
    customer_client.post("/api/v1/orders/", json=order_data)
    
    # Vendor gets orders for their store
    response = vendor_client.get(f"/api/v1/orders/store/{store_id}")
    
    assert_status_code(response, 200)
    response_data = response.json()
    assert_pagination_response(response_data)
    assert len(response_data["data"]) >= 1


@pytest.mark.integration
def test_get_order_by_id(client: TestClient):
    """Test getting specific order by ID."""
    vendor_client, _, _ = create_authenticated_client(client, "vendor")
    customer_client, _, _ = create_authenticated_client(client, "customer")
    
    # Setup and create order
    store_data = create_random_store_data()
    store_response = vendor_client.post("/api/v1/stores/", json=store_data)
    store_id = store_response.json()["id"]
    
    food_data = create_random_food_item_data(store_id)
    food_response = vendor_client.post("/api/v1/food-items/", json=food_data)
    food_id = food_response.json()["id"]
    
    order_data = {
        "items": [{"food_item_id": food_id, "quantity": 1}],
        "store_id": store_id,
        "delivery_address": "123 Test Street"
    }
    order_response = customer_client.post("/api/v1/orders/", json=order_data)
    order_id = order_response.json()["id"]
    
    # Get order by ID (customer can access their own order)
    response = customer_client.get(f"/api/v1/orders/{order_id}")
    
    assert_status_code(response, 200)
    response_data = response.json()
    assert response_data["id"] == order_id


@pytest.mark.integration
def test_update_order_status_by_vendor(client: TestClient):
    """Test order status update by vendor."""
    vendor_client, _, _ = create_authenticated_client(client, "vendor")
    customer_client, _, _ = create_authenticated_client(client, "customer")
    
    # Setup and create order
    store_data = create_random_store_data()
    store_response = vendor_client.post("/api/v1/stores/", json=store_data)
    store_id = store_response.json()["id"]
    
    food_data = create_random_food_item_data(store_id)
    food_response = vendor_client.post("/api/v1/food-items/", json=food_data)
    food_id = food_response.json()["id"]
    
    order_data = {
        "items": [{"food_item_id": food_id, "quantity": 1}],
        "store_id": store_id,
        "delivery_address": "123 Test Street"
    }
    order_response = customer_client.post("/api/v1/orders/", json=order_data)
    order_id = order_response.json()["id"]
    
    # Vendor updates order status
    status_update = {"status": "confirmed"}
    response = vendor_client.patch(f"/api/v1/orders/{order_id}/status", json=status_update)
    
    assert_status_code(response, 200)
    response_data = response.json()
    assert response_data["status"] == "confirmed"


@pytest.mark.integration
def test_cancel_order_by_customer(client: TestClient):
    """Test order cancellation by customer."""
    vendor_client, _, _ = create_authenticated_client(client, "vendor")
    customer_client, _, _ = create_authenticated_client(client, "customer")
    
    # Setup and create order
    store_data = create_random_store_data()
    store_response = vendor_client.post("/api/v1/stores/", json=store_data)
    store_id = store_response.json()["id"]
    
    food_data = create_random_food_item_data(store_id)
    food_response = vendor_client.post("/api/v1/food-items/", json=food_data)
    food_id = food_response.json()["id"]
    
    order_data = {
        "items": [{"food_item_id": food_id, "quantity": 1}],
        "store_id": store_id,
        "delivery_address": "123 Test Street"
    }
    order_response = customer_client.post("/api/v1/orders/", json=order_data)
    order_id = order_response.json()["id"]
    
    # Customer cancels order
    response = customer_client.post(f"/api/v1/orders/{order_id}/cancel")
    
    assert_status_code(response, 200)
    response_data = response.json()
    assert response_data["status"] == "cancelled"


@pytest.mark.integration
def test_order_total_calculation(client: TestClient):
    """Test that order total is calculated correctly."""
    vendor_client, _, _ = create_authenticated_client(client, "vendor")
    customer_client, _, _ = create_authenticated_client(client, "customer")
    
    # Setup store and food items with known prices
    store_data = create_random_store_data()
    store_response = vendor_client.post("/api/v1/stores/", json=store_data)
    store_id = store_response.json()["id"]
    
    # Create food items with specific prices
    food1_data = create_random_food_item_data(store_id)
    food1_data["original_price"] = 25000.0
    food1_response = vendor_client.post("/api/v1/food-items/", json=food1_data)
    food1_id = food1_response.json()["id"]
    
    food2_data = create_random_food_item_data(store_id)
    food2_data["original_price"] = 35000.0
    food2_response = vendor_client.post("/api/v1/food-items/", json=food2_data)
    food2_id = food2_response.json()["id"]
    
    # Create order
    order_data = {
        "items": [
            {"food_item_id": food1_id, "quantity": 2},  # 2 × 25000 = 50000
            {"food_item_id": food2_id, "quantity": 1}   # 1 × 35000 = 35000
        ],
        "store_id": store_id,
        "delivery_address": "123 Test Street"
    }
    
    response = customer_client.post("/api/v1/orders/", json=order_data)
    
    assert_status_code(response, 201)
    response_data = response.json()
    
    # Total should be 50000 + 35000 = 85000
    expected_total = 85000.0
    assert response_data["total_amount"] == expected_total


@pytest.mark.integration
def test_order_unauthorized_access(client: TestClient):
    """Test that users cannot access orders they don't own."""
    vendor_client, _, _ = create_authenticated_client(client, "vendor")
    customer1_client, _, _ = create_authenticated_client(client, "customer")
    customer2_client, _, _ = create_authenticated_client(client, "customer")
    
    # Setup order by customer1
    store_data = create_random_store_data()
    store_response = vendor_client.post("/api/v1/stores/", json=store_data)
    store_id = store_response.json()["id"]
    
    food_data = create_random_food_item_data(store_id)
    food_response = vendor_client.post("/api/v1/food-items/", json=food_data)
    food_id = food_response.json()["id"]
    
    order_data = {
        "items": [{"food_item_id": food_id, "quantity": 1}],
        "store_id": store_id,
        "delivery_address": "123 Test Street"
    }
    order_response = customer1_client.post("/api/v1/orders/", json=order_data)
    order_id = order_response.json()["id"]
    
    # Customer2 tries to access customer1's order
    response = customer2_client.get(f"/api/v1/orders/{order_id}")
    
    assert_status_code(response, 403)
