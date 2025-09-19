"""
Test food item endpoints for WiseBite Backend.
"""
import pytest
from fastapi.testclient import TestClient
from sqlmodel import Session

from tests.utils import (
    create_random_user_data,
    create_random_store_data, 
    create_random_food_item_data,
    create_authenticated_client,
    assert_status_code,
    assert_response_contains_fields,
    assert_response_error,
    assert_valid_uuid,
    assert_pagination_response
)


@pytest.mark.integration
def test_create_food_item_success(client: TestClient):
    """Test successful food item creation by vendor."""
    # Create vendor and store
    vendor_client, vendor_data, vendor_token = create_authenticated_client(client, "vendor")
    
    # Create store first
    store_data = create_random_store_data()
    store_response = vendor_client.post("/api/v1/stores/", json=store_data)
    assert_status_code(store_response, 201)
    store_id = store_response.json()["id"]
    
    # Create food item
    food_data = create_random_food_item_data(store_id)
    response = vendor_client.post("/api/v1/food-items", json=food_data)
    
    assert_status_code(response, 201)
    response_data = response.json()
    assert_response_contains_fields(response_data, [
        "id", "name", "description", "original_price", "quantity", 
        "category", "store_id", "expires_at"
    ])
    assert response_data["name"] == food_data["name"]
    assert response_data["store_id"] == store_id
    assert_valid_uuid(response_data["id"])


@pytest.mark.integration
def test_create_food_item_customer_forbidden(client: TestClient):
    """Test that customers cannot create food items."""
    customer_client, _, _ = create_authenticated_client(client, "customer")
    
    food_data = create_random_food_item_data()
    response = customer_client.post("/api/v1/food-items", json=food_data)
    
    assert_status_code(response, 403)


@pytest.mark.integration
def test_create_food_item_unauthenticated(client: TestClient):
    """Test that unauthenticated users cannot create food items."""
    food_data = create_random_food_item_data()
    response = client.post("/api/v1/food-items", json=food_data)
    
    assert_status_code(response, 401)


@pytest.mark.integration
def test_create_food_item_invalid_data(client: TestClient):
    """Test food item creation with invalid data."""
    vendor_client, _, _ = create_authenticated_client(client, "vendor")
    
    # Create store first
    store_data = create_random_store_data()
    store_response = vendor_client.post("/api/v1/stores/", json=store_data)
    assert_status_code(store_response, 201)
    
    invalid_data = {
        "name": "",  # Empty name
        "original_price": -100,  # Negative price
        "quantity": -1,  # Negative quantity
        "category": "invalid_category"
    }
    
    response = vendor_client.post("/api/v1/food-items", json=invalid_data)
    assert_status_code(response, 422)


@pytest.mark.integration
def test_get_food_items_public(client: TestClient):
    """Test getting food items without authentication."""
    response = client.get("/api/v1/food-items")
    
    assert_status_code(response, 200)
    response_data = response.json()
    assert_pagination_response(response_data)


@pytest.mark.integration
def test_get_food_items_with_pagination(client: TestClient):
    """Test food items pagination."""
    # Create some food items first
    vendor_client, _, _ = create_authenticated_client(client, "vendor")
    
    # Create store
    store_data = create_random_store_data()
    store_response = vendor_client.post("/api/v1/stores/", json=store_data)
    store_id = store_response.json()["id"]
    
    # Create multiple food items
    for i in range(5):
        food_data = create_random_food_item_data(store_id)
        vendor_client.post("/api/v1/food-items", json=food_data)
    
    # Test pagination
    response = client.get("/api/v1/food-items?skip=0&limit=3")
    assert_status_code(response, 200)
    
    response_data = response.json()
    assert_pagination_response(response_data)
    assert len(response_data["data"]) <= 3


@pytest.mark.integration
def test_get_food_item_by_id(client: TestClient):
    """Test getting specific food item by ID."""
    # Create food item
    vendor_client, _, _ = create_authenticated_client(client, "vendor")
    
    store_data = create_random_store_data()
    store_response = vendor_client.post("/api/v1/stores/", json=store_data)
    store_id = store_response.json()["id"]
    
    food_data = create_random_food_item_data(store_id)
    create_response = vendor_client.post("/api/v1/food-items", json=food_data)
    food_id = create_response.json()["id"]
    
    # Get food item by ID
    response = client.get(f"/api/v1/food-items/{food_id}")
    
    assert_status_code(response, 200)
    response_data = response.json()
    assert response_data["id"] == food_id
    assert response_data["name"] == food_data["name"]


@pytest.mark.integration
def test_get_food_item_nonexistent(client: TestClient):
    """Test getting nonexistent food item returns 404."""
    import uuid
    fake_id = str(uuid.uuid4())
    
    response = client.get(f"/api/v1/food-items/{fake_id}")
    assert_status_code(response, 404)


@pytest.mark.integration
def test_update_food_item_success(client: TestClient):
    """Test successful food item update by owner."""
    vendor_client, _, _ = create_authenticated_client(client, "vendor")
    
    # Create store and food item
    store_data = create_random_store_data()
    store_response = vendor_client.post("/api/v1/stores/", json=store_data)
    store_id = store_response.json()["id"]
    
    food_data = create_random_food_item_data(store_id)
    create_response = vendor_client.post("/api/v1/food-items", json=food_data)
    food_id = create_response.json()["id"]
    
    # Update food item
    update_data = {
        "name": "Updated Food Name",
        "original_price": 75000.0,
        "quantity": 15
    }
    
    response = vendor_client.put(f"/api/v1/food-items/{food_id}", json=update_data)
    
    assert_status_code(response, 200)
    response_data = response.json()
    assert response_data["name"] == "Updated Food Name"
    assert response_data["original_price"] == 75000.0
    assert response_data["quantity"] == 15


@pytest.mark.integration
def test_update_food_item_unauthorized(client: TestClient):
    """Test that users cannot update food items they don't own."""
    # Create food item with vendor1
    vendor1_client, _, _ = create_authenticated_client(client, "vendor")
    
    store_data = create_random_store_data()
    store_response = vendor1_client.post("/api/v1/stores/", json=store_data)
    store_id = store_response.json()["id"]
    
    food_data = create_random_food_item_data(store_id)
    create_response = vendor1_client.post("/api/v1/food-items", json=food_data)
    food_id = create_response.json()["id"]
    
    # Try to update with vendor2
    vendor2_client, _, _ = create_authenticated_client(client, "vendor")
    
    update_data = {"name": "Unauthorized Update"}
    response = vendor2_client.put(f"/api/v1/food-items/{food_id}", json=update_data)
    
    assert_status_code(response, 403)


@pytest.mark.integration
def test_delete_food_item_success(client: TestClient):
    """Test successful food item deletion by owner."""
    vendor_client, _, _ = create_authenticated_client(client, "vendor")
    
    # Create store and food item
    store_data = create_random_store_data()
    store_response = vendor_client.post("/api/v1/stores/", json=store_data)
    store_id = store_response.json()["id"]
    
    food_data = create_random_food_item_data(store_id)
    create_response = vendor_client.post("/api/v1/food-items", json=food_data)
    food_id = create_response.json()["id"]
    
    # Delete food item
    response = vendor_client.delete(f"/api/v1/food-items/{food_id}")
    
    assert_status_code(response, 200)
    
    # Verify item is deleted
    get_response = client.get(f"/api/v1/food-items/{food_id}")
    assert_status_code(get_response, 404)


@pytest.mark.integration
def test_search_food_items(client: TestClient):
    """Test food item search functionality."""
    vendor_client, _, _ = create_authenticated_client(client, "vendor")
    
    # Create store
    store_data = create_random_store_data()
    store_response = vendor_client.post("/api/v1/stores/", json=store_data)
    store_id = store_response.json()["id"]
    
    # Create food item with specific name
    food_data = create_random_food_item_data(store_id)
    food_data["name"] = "Special Pizza"
    vendor_client.post("/api/v1/food-items", json=food_data)
    
    # Search for the food item
    response = client.get("/api/v1/food-items/search?query=Special Pizza")
    
    assert_status_code(response, 200)
    response_data = response.json()
    assert_pagination_response(response_data)
    
    # Should find the pizza
    found = any(item["name"] == "Special Pizza" for item in response_data["data"])
    assert found, "Special Pizza not found in search results"


@pytest.mark.integration
def test_filter_food_items_by_category(client: TestClient):
    """Test filtering food items by category."""
    vendor_client, _, _ = create_authenticated_client(client, "vendor")
    
    # Create store
    store_data = create_random_store_data()
    store_response = vendor_client.post("/api/v1/stores/", json=store_data)
    store_id = store_response.json()["id"]
    
    # Create food items with different categories
    for category in ["main_course", "dessert", "drink"]:
        food_data = create_random_food_item_data(store_id)
        food_data["category"] = category
        vendor_client.post("/api/v1/food-items", json=food_data)
    
    # Filter by category
    response = client.get("/api/v1/food-items?category=dessert")
    
    assert_status_code(response, 200)
    response_data = response.json()
    
    # All items should be desserts
    for item in response_data["data"]:
        assert item["category"] == "dessert"


@pytest.mark.integration
def test_food_items_location_filter(client: TestClient):
    """Test filtering food items by location."""
    vendor_client, _, _ = create_authenticated_client(client, "vendor")
    
    # Create store with specific location
    store_data = create_random_store_data()
    store_data["latitude"] = 10.7769
    store_data["longitude"] = 106.7009
    store_response = vendor_client.post("/api/v1/stores/", json=store_data)
    store_id = store_response.json()["id"]
    
    # Create food item
    food_data = create_random_food_item_data(store_id)
    vendor_client.post("/api/v1/food-items", json=food_data)
    
    # Search with location
    response = client.get(
        "/api/v1/food-items?latitude=10.7769&longitude=106.7009&radius=5"
    )
    
    assert_status_code(response, 200)
    response_data = response.json()
    assert_pagination_response(response_data)


@pytest.mark.integration
def test_get_vendor_food_items(client: TestClient):
    """Test getting food items for a specific vendor."""
    vendor_client, _, _ = create_authenticated_client(client, "vendor")
    
    # Create store
    store_data = create_random_store_data()
    store_response = vendor_client.post("/api/v1/stores/", json=store_data)
    store_id = store_response.json()["id"]
    
    # Create multiple food items
    created_items = []
    for i in range(3):
        food_data = create_random_food_item_data(store_id)
        create_response = vendor_client.post("/api/v1/food-items", json=food_data)
        created_items.append(create_response.json()["id"])
    
    # Get vendor's food items
    response = vendor_client.get("/api/v1/food-items/me")
    
    assert_status_code(response, 200)
    response_data = response.json()
    assert_pagination_response(response_data)
    
    # Should contain all created items
    returned_ids = [item["id"] for item in response_data["data"]]
    for created_id in created_items:
        assert created_id in returned_ids
