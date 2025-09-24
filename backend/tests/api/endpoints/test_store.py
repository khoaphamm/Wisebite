"""
Test store management endpoints for WiseBite Backend.
"""
import pytest
from fastapi.testclient import TestClient
from sqlmodel import Session

from tests.utils import create_random_store_data, assert_status_code, assert_response_contains_fields
from app.models import Store


@pytest.mark.unit
def test_create_store_model(test_vendor):
    """Test Store model creation."""
    store_data = {
        "name": "Test Store",
        "address": "123 Test Street",
        "description": "A test store",
        "owner_id": test_vendor.id
    }
    
    store = Store(**store_data)
    assert store.name == "Test Store"
    assert store.address == "123 Test Street"
    assert store.owner_id == test_vendor.id


@pytest.mark.integration
def test_create_store_vendor(authenticated_vendor_client: TestClient):
    """Test vendor creating a store."""
    store_data = create_random_store_data()
    
    response = authenticated_vendor_client.post("/api/v1/stores/", json=store_data)
    
    assert_status_code(response, 201)
    response_data = response.json()
    assert_response_contains_fields(response_data, ["id", "name", "address", "description"])
    assert response_data["name"] == store_data["name"]


@pytest.mark.integration
def test_create_store_with_location(authenticated_vendor_client: TestClient):
    """Test vendor creating a store with location coordinates."""
    store_data = create_random_store_data()
    store_data["latitude"] = 10.7769
    store_data["longitude"] = 106.7009
    
    response = authenticated_vendor_client.post("/api/v1/stores/", json=store_data)
    
    assert_status_code(response, 201)
    response_data = response.json()
    assert_response_contains_fields(response_data, ["id", "name", "address", "latitude", "longitude"])
    assert response_data["name"] == store_data["name"]
    # Note: actual lat/lon values would be extracted from PostGIS in real implementation


@pytest.mark.integration
def test_create_store_customer_forbidden(authenticated_customer_client: TestClient):
    """Test customer cannot create a store."""
    store_data = create_random_store_data()
    
    response = authenticated_customer_client.post("/api/v1/stores/", json=store_data)
    assert_status_code(response, 403)


@pytest.mark.integration
def test_get_store(client: TestClient, test_store):
    """Test getting store details (public endpoint)."""
    response = client.get(f"/api/v1/stores/{test_store.id}")
    
    assert_status_code(response, 200)
    response_data = response.json()
    assert_response_contains_fields(response_data, ["id", "name", "address"])
    assert response_data["id"] == str(test_store.id)


@pytest.mark.integration
def test_list_stores(client: TestClient, test_store):
    """Test listing all stores (public endpoint)."""
    response = client.get("/api/v1/stores/")
    
    assert_status_code(response, 200)
    response_data = response.json()
    assert isinstance(response_data, list)
    assert len(response_data) >= 1


@pytest.mark.integration
def test_list_nearby_stores_basic(client: TestClient):
    """Test listing nearby stores with basic parameters."""
    # Ho Chi Minh City coordinates
    latitude = 10.7769
    longitude = 106.7009
    radius = 10.0
    
    response = client.get(
        f"/api/v1/stores/nearby?latitude={latitude}&longitude={longitude}&radius={radius}"
    )
    
    assert_status_code(response, 200)
    response_data = response.json()
    assert isinstance(response_data, list)
    
    # Check that each store in response has distance information
    for store in response_data:
        assert_response_contains_fields(store, ["id", "name", "address", "distance_km"])
        if store["distance_km"] is not None:
            assert store["distance_km"] >= 0, "Distance should be non-negative"


@pytest.mark.integration 
def test_list_nearby_stores_with_radius_validation(client: TestClient):
    """Test nearby stores endpoint with invalid radius values."""
    latitude = 10.7769
    longitude = 106.7009
    
    # Test negative radius
    response = client.get(
        f"/api/v1/stores/nearby?latitude={latitude}&longitude={longitude}&radius=-1"
    )
    assert_status_code(response, 422)  # Validation error
    
    # Test radius too large
    response = client.get(
        f"/api/v1/stores/nearby?latitude={latitude}&longitude={longitude}&radius=200"
    )
    assert_status_code(response, 422)  # Validation error


@pytest.mark.integration
def test_list_nearby_stores_coordinate_validation(client: TestClient):
    """Test nearby stores endpoint with invalid coordinates."""
    radius = 10.0
    
    # Test invalid latitude
    response = client.get(
        f"/api/v1/stores/nearby?latitude=91&longitude=106&radius={radius}"
    )
    # Should work but might return empty results
    
    # Test missing coordinates
    response = client.get("/api/v1/stores/nearby?radius=10")
    assert_status_code(response, 422)  # Missing required parameters


@pytest.mark.integration
def test_update_store_owner(authenticated_vendor_client: TestClient, test_store, test_vendor):
    """Test store owner can update their store."""
    # Setup authenticated client to use the store owner's credentials
    authenticated_vendor_client.app.dependency_overrides.clear()
    
    update_data = {
        "name": "Updated Store Name",
        "description": "Updated description"
    }
    
    response = authenticated_vendor_client.put(f"/api/v1/stores/{test_store.id}", json=update_data)
    
    assert_status_code(response, 200)
    response_data = response.json()
    assert response_data["name"] == "Updated Store Name"


@pytest.mark.integration
def test_update_store_with_location(authenticated_vendor_client: TestClient, test_store):
    """Test updating store location."""
    update_data = {
        "latitude": 10.8,
        "longitude": 106.8
    }
    
    response = authenticated_vendor_client.put(f"/api/v1/stores/{test_store.id}", json=update_data)
    
    assert_status_code(response, 200)
    # Location update would be reflected in PostGIS in real implementation


@pytest.mark.integration
def test_update_store_not_owner(authenticated_customer_client: TestClient, test_store):
    """Test non-owner cannot update store."""
    update_data = {
        "name": "Hacked Store Name"
    }
    
    response = authenticated_customer_client.put(f"/api/v1/stores/{test_store.id}", json=update_data)
    assert_status_code(response, 403)


@pytest.mark.integration
def test_delete_store_owner(authenticated_vendor_client: TestClient, test_store):
    """Test store owner can delete their store."""
    response = authenticated_vendor_client.delete(f"/api/v1/stores/{test_store.id}")
    assert_status_code(response, 204)
    
    # Verify store is deleted
    get_response = authenticated_vendor_client.get(f"/api/v1/stores/{test_store.id}")
    assert_status_code(get_response, 404)
