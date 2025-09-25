"""
Test store travel information endpoints for WiseBite Backend.
"""
import pytest
from unittest.mock import Mock, patch
from fastapi.testclient import TestClient

from tests.utils import create_random_store_data, assert_status_code, assert_response_contains_fields


@pytest.mark.integration
@patch('app.services.mapbox.get_travel_info_multi_profile')
def test_list_stores_with_travel_info_success(mock_mapbox, authenticated_vendor_client: TestClient):
    """Test vendor getting stores with travel information."""
    # Mock Mapbox response
    mock_mapbox.return_value = {
        "driving": [
            {"duration": 900, "distance": 5000},  # 15 minutes, 5km
            {"duration": 1200, "distance": 7000}  # 20 minutes, 7km
        ],
        "walking": [
            {"duration": 3600, "distance": 4500},  # 1 hour, 4.5km
            {"duration": 4800, "distance": 6500}   # 1.33 hours, 6.5km
        ]
    }
    
    # Ho Chi Minh City coordinates (vendor location)
    vendor_lat = 10.7769
    vendor_lng = 106.7009
    
    response = authenticated_vendor_client.get(
        f"/api/v1/stores/with-travel-info?vendor_latitude={vendor_lat}&vendor_longitude={vendor_lng}&radius=20"
    )
    
    assert_status_code(response, 200)
    response_data = response.json()
    assert isinstance(response_data, list)
    
    # Check that each store in response has travel information
    for store in response_data:
        expected_fields = [
            "id", "name", "address", "distance_km",
            "travel_time_driving_seconds", "travel_time_walking_seconds",
            "travel_distance_driving_meters", "travel_distance_walking_meters"
        ]
        assert_response_contains_fields(store, expected_fields)
        
        # Check travel time values are reasonable (if present)
        if store.get("travel_time_driving_seconds"):
            assert store["travel_time_driving_seconds"] > 0
        if store.get("travel_time_walking_seconds"):
            assert store["travel_time_walking_seconds"] > 0


@pytest.mark.integration
@patch('app.services.mapbox.get_travel_info_multi_profile')
def test_list_stores_with_travel_info_mapbox_failure(mock_mapbox, authenticated_vendor_client: TestClient):
    """Test handling of Mapbox API failure."""
    # Mock Mapbox failure
    mock_mapbox.return_value = None
    
    vendor_lat = 10.7769
    vendor_lng = 106.7009
    
    response = authenticated_vendor_client.get(
        f"/api/v1/stores/with-travel-info?vendor_latitude={vendor_lat}&vendor_longitude={vendor_lng}&radius=20"
    )
    
    assert_status_code(response, 200)
    response_data = response.json()
    assert isinstance(response_data, list)
    
    # Should still return stores but without travel information
    for store in response_data:
        assert "id" in store
        assert "name" in store
        # Travel info fields should be None or missing when Mapbox fails
        assert store.get("travel_time_driving_seconds") is None
        assert store.get("travel_time_walking_seconds") is None


@pytest.mark.integration
def test_list_stores_with_travel_info_missing_coordinates(authenticated_vendor_client: TestClient):
    """Test endpoint with missing vendor coordinates."""
    response = authenticated_vendor_client.get(
        "/api/v1/stores/with-travel-info?radius=20"
    )
    
    # Should return validation error for missing required parameters
    assert_status_code(response, 422)


@pytest.mark.integration
def test_list_stores_with_travel_info_invalid_radius(authenticated_vendor_client: TestClient):
    """Test endpoint with invalid radius values."""
    vendor_lat = 10.7769
    vendor_lng = 106.7009
    
    # Test negative radius
    response = authenticated_vendor_client.get(
        f"/api/v1/stores/with-travel-info?vendor_latitude={vendor_lat}&vendor_longitude={vendor_lng}&radius=-1"
    )
    assert_status_code(response, 422)  # Validation error
    
    # Test radius too large
    response = authenticated_vendor_client.get(
        f"/api/v1/stores/with-travel-info?vendor_latitude={vendor_lat}&vendor_longitude={vendor_lng}&radius=200"
    )
    assert_status_code(response, 422)  # Validation error


@pytest.mark.integration
def test_list_stores_with_travel_info_customer_forbidden(authenticated_customer_client: TestClient):
    """Test that customers cannot access vendor-specific travel info endpoint."""
    vendor_lat = 10.7769
    vendor_lng = 106.7009
    
    response = authenticated_customer_client.get(
        f"/api/v1/stores/with-travel-info?vendor_latitude={vendor_lat}&vendor_longitude={vendor_lng}&radius=20"
    )
    
    # Should return forbidden for customers
    assert_status_code(response, 403)


@pytest.mark.unit
@patch('app.services.mapbox.get_travel_info_from_mapbox')
async def test_get_travel_info_multi_profile(mock_single_profile):
    """Test the multi-profile travel info function."""
    from app.services.mapbox import get_travel_info_multi_profile
    
    # Mock individual profile calls
    mock_single_profile.side_effect = [
        [{"duration": 900, "distance": 5000}],  # driving
        [{"duration": 3600, "distance": 4500}]  # walking
    ]
    
    origin = (106.7009, 10.7769)  # (lng, lat)
    destinations = [(106.7109, 10.7869)]  # One destination
    
    result = await get_travel_info_multi_profile(origin, destinations)
    
    assert result is not None
    assert "driving" in result
    assert "walking" in result
    assert len(result["driving"]) == 1
    assert len(result["walking"]) == 1
    assert result["driving"][0]["duration"] == 900
    assert result["walking"][0]["duration"] == 3600
    
    # Verify both profiles were called
    assert mock_single_profile.call_count == 2


@pytest.mark.unit
async def test_get_travel_info_multi_profile_empty_destinations():
    """Test multi-profile function with empty destinations."""
    from app.services.mapbox import get_travel_info_multi_profile
    
    origin = (106.7009, 10.7769)
    destinations = []
    
    result = await get_travel_info_multi_profile(origin, destinations)
    
    assert result == {"driving": [], "walking": []}