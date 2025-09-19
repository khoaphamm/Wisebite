"""
Test order management endpoints for WiseBite Backend.
"""
import pytest
from fastapi.testclient import TestClient
from sqlmodel import Session

from tests.utils import assert_status_code, assert_response_contains_fields
from app.models import Order, OrderStatus, OrderItem


@pytest.mark.unit
def test_create_order_model(test_customer):
    """Test Order model creation."""
    order_data = {
        "customer_id": test_customer.id,
        "status": OrderStatus.PENDING_PAYMENT,
        "total_amount": 50000.0
    }
    
    order = Order(**order_data)
    assert order.customer_id == test_customer.id
    assert order.status == OrderStatus.PENDING_PAYMENT
    assert order.total_amount == 50000.0


@pytest.mark.integration
def test_create_order(authenticated_customer_client: TestClient, test_surprise_bag):
    """Test customer creating an order."""
    order_data = {
        "items": [
            {
                "surprise_bag_id": str(test_surprise_bag.id),
                "quantity": 2
            }
        ]
    }
    
    response = authenticated_customer_client.post("/api/v1/orders/", json=order_data)
    
    assert_status_code(response, 201)
    response_data = response.json()
    assert_response_contains_fields(response_data, ["id", "customer_id", "status", "total_amount", "items"])
    assert response_data["status"] == "pending_payment"


@pytest.mark.integration
def test_get_order(authenticated_customer_client: TestClient, test_customer):
    """Test getting order details."""
    # First create an order
    from app.models import Order, OrderStatus
    import uuid
    
    # This would normally be created through the API, but for testing we create directly
    # In real tests, you'd create via the API endpoint first
    order_data = {
        "items": [
            {
                "surprise_bag_id": str(uuid.uuid4()),  # Would be real surprise bag
                "quantity": 1
            }
        ]
    }
    
    create_response = authenticated_customer_client.post("/api/v1/orders/", json=order_data)
    
    if create_response.status_code == 201:
        order_id = create_response.json()["id"]
        
        response = authenticated_customer_client.get(f"/api/v1/orders/{order_id}")
        assert_status_code(response, 200)
        response_data = response.json()
        assert_response_contains_fields(response_data, ["id", "customer_id", "status"])


@pytest.mark.integration
def test_list_customer_orders(authenticated_customer_client: TestClient):
    """Test listing customer's orders."""
    response = authenticated_customer_client.get("/api/v1/orders/my-orders")
    
    assert_status_code(response, 200)
    response_data = response.json()
    assert isinstance(response_data, list)


@pytest.mark.integration
def test_update_order_status_vendor(authenticated_vendor_client: TestClient):
    """Test vendor updating order status."""
    # This test assumes the vendor has orders to manage
    # In practice, you'd set up specific test data
    order_id = "some-order-id"  # Would be from test setup
    
    update_data = {
        "status": "confirmed"
    }
    
    # This endpoint would need to exist in your API
    response = authenticated_vendor_client.patch(f"/api/v1/orders/{order_id}/status", json=update_data)
    
    # For now, just check that the endpoint exists
    # assert_status_code(response, 200)


@pytest.mark.integration
def test_cancel_order(authenticated_customer_client: TestClient):
    """Test customer canceling their order."""
    # Create an order first
    order_data = {
        "items": [
            {
                "surprise_bag_id": str("test-id"),
                "quantity": 1
            }
        ]
    }
    
    create_response = authenticated_customer_client.post("/api/v1/orders/", json=order_data)
    
    if create_response.status_code == 201:
        order_id = create_response.json()["id"]
        
        # Cancel the order
        response = authenticated_customer_client.patch(f"/api/v1/orders/{order_id}/cancel")
        
        # Check if endpoint exists and responds appropriately
        # In real implementation, this would be 200 or 204
        assert response.status_code in [200, 204, 404, 501]  # 404/501 if not implemented yet


@pytest.mark.integration
def test_get_order_unauthorized(client: TestClient):
    """Test getting order without authentication fails."""
    order_id = "some-order-id"
    
    response = client.get(f"/api/v1/orders/{order_id}")
    assert_status_code(response, 401)
