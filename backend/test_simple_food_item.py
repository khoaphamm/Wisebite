"""
Simple test to check food item creation without dependencies.
"""
import pytest
from fastapi.testclient import TestClient
from tests.utils import create_authenticated_client, create_random_store_data, create_random_food_item_data


def test_simple_food_item_flow():
    """Test a simple food item flow from scratch."""
    from app.main import app
    
    with TestClient(app) as client:
        # Create vendor
        vendor_client, vendor_data, vendor_token = create_authenticated_client(client, "vendor")
        
        # Create store
        store_data = create_random_store_data()
        store_response = vendor_client.post("/api/v1/stores/", json=store_data)
        
        print(f"Store creation response: {store_response.status_code}")
        print(f"Store response body: {store_response.text}")
        
        if store_response.status_code != 201:
            print(f"Vendor data: {vendor_data}")
            print(f"Store data: {store_data}")
            return
        
        store_id = store_response.json()["id"]
        
        # Create food item
        food_data = create_random_food_item_data(store_id)
        response = vendor_client.post("/api/v1/food-items/", json=food_data)
        
        print(f"Food item creation response: {response.status_code}")
        print(f"Food item response body: {response.text}")


if __name__ == "__main__":
    test_simple_food_item_flow()
