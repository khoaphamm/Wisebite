"""
Test transaction endpoints for WiseBite Backend.
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
    assert_valid_datetime,
    assert_pagination_response
)


@pytest.mark.integration
def test_create_payment_transaction_success(client: TestClient):
    """Test successful payment transaction creation."""
    # Create order first
    vendor_client, _, _ = create_authenticated_client(client, "vendor")
    customer_client, _, _ = create_authenticated_client(client, "customer")
    
    # Setup store and food item - use standard_price instead of original_price
    store_data = create_random_store_data()
    store_response = vendor_client.post("/api/v1/stores/", json=store_data)
    store_id = store_response.json()["id"]
    
    food_data = create_random_food_item_data(store_id)
    food_data["standard_price"] = 50000.0
    food_data["total_quantity"] = 10
    food_response = vendor_client.post("/api/v1/food-items/", json=food_data)
    food_id = food_response.json()["id"]
    
    # Create order
    order_data = {
        "items": [{"food_item_id": food_id, "quantity": 1}],
        "delivery_address": "123 Test Street"
    }
    print(f"Sending order data: {order_data}")
    print(f"Food ID: {food_id}")
    print(f"Store ID: {store_id}")
    order_response = customer_client.post("/api/v1/orders/", json=order_data)
    print(f"Order response status: {order_response.status_code}")
    print(f"Order response body: {order_response.json()}")
    order_id = order_response.json()["id"]
    
    # Create payment transaction
    transaction_data = {
        "order_id": order_id,
        "payment_method": "credit_card",
        "amount": 50000.0,
        "payment_details": {
            "card_last_four": "1234",
            "card_type": "visa"
        }
    }
    
    response = customer_client.post("/api/v1/transactions", json=transaction_data)
    
    assert_status_code(response, 201)
    response_data = response.json()
    assert_response_contains_fields(response_data, [
        "id", "order_id", "customer_id", "amount", "payment_method",
        "status", "created_at", "transaction_type"
    ])
    assert response_data["order_id"] == order_id
    assert response_data["amount"] == 50000.0
    assert response_data["status"] == "completed"
    assert response_data["transaction_type"] == "payment"
    assert_valid_uuid(response_data["id"])
    assert_valid_datetime(response_data["created_at"])


@pytest.mark.integration
def test_create_transaction_nonexistent_order(client: TestClient):
    """Test transaction creation with nonexistent order."""
    import uuid
    
    customer_client, _, _ = create_authenticated_client(client, "customer")
    
    fake_order_id = str(uuid.uuid4())
    transaction_data = {
        "order_id": fake_order_id,
        "payment_method": "credit_card",
        "amount": 50000.0
    }
    
    response = customer_client.post("/api/v1/transactions", json=transaction_data)
    assert_response_error(response, 404, "order not found")


@pytest.mark.integration
def test_create_transaction_unauthorized_order(client: TestClient):
    """Test transaction creation for order belonging to another user."""
    vendor_client, _, _ = create_authenticated_client(client, "vendor")
    customer1_client, _, _ = create_authenticated_client(client, "customer")
    customer2_client, _, _ = create_authenticated_client(client, "customer")
    
    # Create order with customer1 - use standard_price instead of original_price
    store_data = create_random_store_data()
    store_response = vendor_client.post("/api/v1/stores/", json=store_data)
    store_id = store_response.json()["id"]
    
    food_data = create_random_food_item_data(store_id)
    food_data["standard_price"] = 25000.0
    food_data["total_quantity"] = 10
    food_response = vendor_client.post("/api/v1/food-items/", json=food_data)
    food_id = food_response.json()["id"]
    
    order_data = {
        "items": [{"food_item_id": food_id, "quantity": 1}],
        "delivery_address": "123 Test Street"
    }
    order_response = customer1_client.post("/api/v1/orders/", json=order_data)
    order_id = order_response.json()["id"]
    
    # Customer2 tries to pay for customer1's order
    transaction_data = {
        "order_id": order_id,
        "payment_method": "credit_card",
        "amount": 25000.0
    }
    
    response = customer2_client.post("/api/v1/transactions", json=transaction_data)
    assert_status_code(response, 403)


@pytest.mark.integration
def test_create_transaction_invalid_amount(client: TestClient):
    """Test transaction creation with invalid amount."""
    vendor_client, _, _ = create_authenticated_client(client, "vendor")
    customer_client, _, _ = create_authenticated_client(client, "customer")
    
    # Create order - use standard_price instead of original_price
    store_data = create_random_store_data()
    store_response = vendor_client.post("/api/v1/stores/", json=store_data)
    store_id = store_response.json()["id"]
    
    food_data = create_random_food_item_data(store_id)
    food_data["standard_price"] = 50000.0
    food_data["total_quantity"] = 10
    food_response = vendor_client.post("/api/v1/food-items/", json=food_data)
    food_id = food_response.json()["id"]
    
    order_data = {
        "items": [{"food_item_id": food_id, "quantity": 1}],
        "delivery_address": "123 Test Street"
    }
    order_response = customer_client.post("/api/v1/orders/", json=order_data)
    order_id = order_response.json()["id"]
    
    # Try to pay wrong amount
    transaction_data = {
        "order_id": order_id,
        "payment_method": "credit_card",
        "amount": 25000.0  # Wrong amount (order is 50000)
    }
    
    response = customer_client.post("/api/v1/transactions", json=transaction_data)
    assert_response_error(response, 400, "amount mismatch")


@pytest.mark.integration
def test_create_transaction_already_paid(client: TestClient):
    """Test transaction creation for already paid order."""
    vendor_client, _, _ = create_authenticated_client(client, "vendor")
    customer_client, _, _ = create_authenticated_client(client, "customer")
    
    # Create and pay for order - use standard_price instead of original_price
    store_data = create_random_store_data()
    store_response = vendor_client.post("/api/v1/stores/", json=store_data)
    store_id = store_response.json()["id"]
    
    food_data = create_random_food_item_data(store_id)
    food_data["standard_price"] = 50000.0
    food_data["total_quantity"] = 10
    food_response = vendor_client.post("/api/v1/food-items/", json=food_data)
    food_id = food_response.json()["id"]
    
    order_data = {
        "items": [{"food_item_id": food_id, "quantity": 1}],
        "delivery_address": "123 Test Street"
    }
    order_response = customer_client.post("/api/v1/orders/", json=order_data)
    order_id = order_response.json()["id"]
    
    # First payment
    transaction_data = {
        "order_id": order_id,
        "payment_method": "credit_card",
        "amount": 50000.0
    }
    customer_client.post("/api/v1/transactions", json=transaction_data)
    
    # Try to pay again
    response = customer_client.post("/api/v1/transactions", json=transaction_data)
    assert_response_error(response, 400, "already paid")


@pytest.mark.integration
def test_get_user_transactions(client: TestClient):
    """Test getting user's transaction history."""
    vendor_client, _, _ = create_authenticated_client(client, "vendor")
    customer_client, _, _ = create_authenticated_client(client, "customer")
    
    # Create and pay for order - use standard_price instead of original_price
    store_data = create_random_store_data()
    store_response = vendor_client.post("/api/v1/stores/", json=store_data)
    store_id = store_response.json()["id"]
    
    food_data = create_random_food_item_data(store_id)
    food_response = vendor_client.post("/api/v1/food-items/", json=food_data)
    food_id = food_response.json()["id"]
    
    order_data = {
        "items": [{"food_item_id": food_id, "quantity": 1}],
        "delivery_address": "123 Test Street"
    }
    order_response = customer_client.post("/api/v1/orders/", json=order_data)
    order_id = order_response.json()["id"]
    
    transaction_data = {
        "order_id": order_id,
        "payment_method": "credit_card",
        "amount": food_data["standard_price"]
    }
    customer_client.post("/api/v1/transactions", json=transaction_data)
    
    # Get user transactions
    response = customer_client.get("/api/v1/transactions/me")
    
    assert_status_code(response, 200)
    response_data = response.json()
    assert_pagination_response(response_data)
    assert len(response_data["data"]) >= 1
    
    # Verify transaction details
    transaction = response_data["data"][0]
    assert transaction["order_id"] == order_id
    assert transaction["amount"] == food_data["standard_price"]


@pytest.mark.integration
def test_get_transaction_by_id(client: TestClient):
    """Test getting specific transaction by ID."""
    vendor_client, _, _ = create_authenticated_client(client, "vendor")
    customer_client, _, _ = create_authenticated_client(client, "customer")
    
    # Create transaction - use standard_price instead of original_price
    store_data = create_random_store_data()
    store_response = vendor_client.post("/api/v1/stores/", json=store_data)
    store_id = store_response.json()["id"]
    
    food_data = create_random_food_item_data(store_id)
    food_response = vendor_client.post("/api/v1/food-items/", json=food_data)
    food_id = food_response.json()["id"]
    
    order_data = {
        "items": [{"food_item_id": food_id, "quantity": 1}],
        "delivery_address": "123 Test Street"
    }
    order_response = customer_client.post("/api/v1/orders/", json=order_data)
    order_id = order_response.json()["id"]
    
    transaction_data = {
        "order_id": order_id,
        "payment_method": "credit_card",
        "amount": food_data["standard_price"]
    }
    transaction_response = customer_client.post("/api/v1/transactions", json=transaction_data)
    transaction_id = transaction_response.json()["id"]
    
    # Get transaction by ID
    response = customer_client.get(f"/api/v1/transactions/{transaction_id}")
    
    assert_status_code(response, 200)
    response_data = response.json()
    assert response_data["id"] == transaction_id
    assert response_data["order_id"] == order_id


@pytest.mark.integration
def test_get_transaction_unauthorized(client: TestClient):
    """Test that users cannot access transactions they don't own."""
    vendor_client, _, _ = create_authenticated_client(client, "vendor")
    customer1_client, _, _ = create_authenticated_client(client, "customer")
    customer2_client, _, _ = create_authenticated_client(client, "customer")
    
    # Create transaction with customer1 - use standard_price instead of original_price
    store_data = create_random_store_data()
    store_response = vendor_client.post("/api/v1/stores/", json=store_data)
    store_id = store_response.json()["id"]
    
    food_data = create_random_food_item_data(store_id)
    food_response = vendor_client.post("/api/v1/food-items/", json=food_data)
    food_id = food_response.json()["id"]
    
    order_data = {
        "items": [{"food_item_id": food_id, "quantity": 1}],
        "delivery_address": "123 Test Street"
    }
    order_response = customer1_client.post("/api/v1/orders/", json=order_data)
    order_id = order_response.json()["id"]
    
    transaction_data = {
        "order_id": order_id,
        "payment_method": "credit_card",
        "amount": food_data["standard_price"]
    }
    transaction_response = customer1_client.post("/api/v1/transactions", json=transaction_data)
    transaction_id = transaction_response.json()["id"]
    
    # Customer2 tries to access customer1's transaction
    response = customer2_client.get(f"/api/v1/transactions/{transaction_id}")
    assert_status_code(response, 403)


@pytest.mark.integration
def test_refund_transaction_success(client: TestClient):
    """Test successful refund creation."""
    vendor_client, _, _ = create_authenticated_client(client, "vendor")
    customer_client, _, _ = create_authenticated_client(client, "customer")
    
    # Create and pay for order - use standard_price instead of original_price
    store_data = create_random_store_data()
    store_response = vendor_client.post("/api/v1/stores/", json=store_data)
    store_id = store_response.json()["id"]
    
    food_data = create_random_food_item_data(store_id)
    food_response = vendor_client.post("/api/v1/food-items/", json=food_data)
    food_id = food_response.json()["id"]
    
    order_data = {
        "items": [{"food_item_id": food_id, "quantity": 1}],
        "delivery_address": "123 Test Street"
    }
    order_response = customer_client.post("/api/v1/orders/", json=order_data)
    order_id = order_response.json()["id"]
    
    transaction_data = {
        "order_id": order_id,
        "payment_method": "credit_card",
        "amount": food_data["standard_price"]
    }
    transaction_response = customer_client.post("/api/v1/transactions", json=transaction_data)
    transaction_id = transaction_response.json()["id"]
    
    # Cancel order (which should trigger refund eligibility)
    customer_client.post(f"/api/v1/orders/{order_id}/cancel")
    
    # Create refund
    refund_data = {
        "transaction_id": transaction_id,
        "reason": "Order cancelled by customer",
        "amount": food_data["standard_price"]
    }
    
    response = customer_client.post("/api/v1/transactions/refund", json=refund_data)
    
    assert_status_code(response, 201)
    response_data = response.json()
    assert_response_contains_fields(response_data, [
        "id", "original_transaction_id", "amount", "status", 
        "reason", "transaction_type"
    ])
    assert response_data["original_transaction_id"] == transaction_id
    assert response_data["transaction_type"] == "refund"
    assert response_data["status"] == "completed"


@pytest.mark.integration
def test_filter_transactions_by_type(client: TestClient):
    """Test filtering transactions by type."""
    vendor_client, _, _ = create_authenticated_client(client, "vendor")
    customer_client, _, _ = create_authenticated_client(client, "customer")
    
    # Create payment and refund transactions - use standard_price instead of original_price
    store_data = create_random_store_data()
    store_response = vendor_client.post("/api/v1/stores/", json=store_data)
    store_id = store_response.json()["id"]
    
    food_data = create_random_food_item_data(store_id)
    food_data["total_quantity"] = 20  # Ensure enough quantity for multiple orders
    food_response = vendor_client.post("/api/v1/food-items/", json=food_data)
    food_id = food_response.json()["id"]
    
    # Create two orders and transactions
    for i in range(2):
        order_data = {
            "items": [{"food_item_id": food_id, "quantity": 1}],
            "delivery_address": "123 Test Street"
        }
        order_response = customer_client.post("/api/v1/orders/", json=order_data)
        order_id = order_response.json()["id"]
        
        transaction_data = {
            "order_id": order_id,
            "payment_method": "credit_card",
            "amount": food_data["standard_price"]
        }
        customer_client.post("/api/v1/transactions", json=transaction_data)
    
    # Filter by payment type
    response = customer_client.get("/api/v1/transactions/me?transaction_type=payment")
    
    assert_status_code(response, 200)
    response_data = response.json()
    
    # All returned transactions should be payments
    for transaction in response_data["data"]:
        assert transaction["transaction_type"] == "payment"


@pytest.mark.integration
def test_filter_transactions_by_date_range(client: TestClient):
    """Test filtering transactions by date range."""
    customer_client, _, _ = create_authenticated_client(client, "customer")
    
    from datetime import datetime, timedelta
    
    # Get transactions from last 7 days
    end_date = datetime.now()
    start_date = end_date - timedelta(days=7)
    
    response = customer_client.get(
        f"/api/v1/transactions/me?start_date={start_date.isoformat()}&end_date={end_date.isoformat()}"
    )
    
    assert_status_code(response, 200)
    response_data = response.json()
    assert_pagination_response(response_data)


@pytest.mark.integration
def test_get_vendor_transaction_summary(client: TestClient):
    """Test getting vendor transaction summary."""
    vendor_client, _, _ = create_authenticated_client(client, "vendor")
    customer_client, _, _ = create_authenticated_client(client, "customer")
    
    # Create store and receive payments - use standard_price instead of original_price
    store_data = create_random_store_data()
    store_response = vendor_client.post("/api/v1/stores/", json=store_data)
    store_id = store_response.json()["id"]
    
    food_data = create_random_food_item_data(store_id)
    food_response = vendor_client.post("/api/v1/food-items/", json=food_data)
    food_id = food_response.json()["id"]
    
    # Create order and payment
    order_data = {
        "items": [{"food_item_id": food_id, "quantity": 1}],
        "delivery_address": "123 Test Street"
    }
    order_response = customer_client.post("/api/v1/orders/", json=order_data)
    order_id = order_response.json()["id"]
    
    transaction_data = {
        "order_id": order_id,
        "payment_method": "credit_card",
        "amount": food_data["standard_price"]
    }
    customer_client.post("/api/v1/transactions", json=transaction_data)
    
    # Get vendor transaction summary
    response = vendor_client.get(f"/api/v1/transactions/vendor/summary?store_id={store_id}")
    
    assert_status_code(response, 200)
    response_data = response.json()
    assert_response_contains_fields(response_data, [
        "total_revenue", "total_transactions", "pending_payouts"
    ])
    assert response_data["total_revenue"] >= food_data["standard_price"]
    assert response_data["total_transactions"] >= 1
