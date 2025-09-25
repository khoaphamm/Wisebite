# WiseBite API Documentation

## Overview

WiseBite is a food delivery and ordering platform API built with FastAPI. This documentation covers all available endpoints, authentication requirements, request/response schemas, and testing procedures.

## Base URL

- **Production**: TBD
- **Development**: `http://localhost:8000`
- **Testing**: Uses TestClient with mock database

## Authentication

All protected endpoints require JWT token authentication. Include the token in the Authorization header:

```
Authorization: Bearer <your_jwt_token>
```

### Authentication Endpoints

#### POST `/api/v1/auth/signup`
Register a new user account.

**Request Body:**
```json
{
  "full_name": "John Doe",
  "email": "john@example.com",
  "phone_number": "+1234567890",
  "password": "securepassword123",
  "user_type": "customer",  // or "vendor"
  "address": "123 Main St, City",
  "coordinates": [10.123456, 106.789012]  // [latitude, longitude]
}
```

**Response (201):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "full_name": "John Doe",
  "email": "john@example.com",
  "phone_number": "+1234567890",
  "user_type": "customer",
  "address": "123 Main St, City",
  "coordinates": [10.123456, 106.789012],
  "created_at": "2025-09-19T10:30:00Z"
}
```

#### POST `/api/v1/auth/login`
Authenticate user and receive JWT token.

**Request Body:**
```json
{
  "phone_number": "+1234567890",
  "password": "securepassword123"
}
```

**Response (200):**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "bearer",
  "expires_in": 86400,
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "full_name": "John Doe",
    "email": "john@example.com",
    "user_type": "customer"
  }
}
```

## Transaction Endpoints

### POST `/api/v1/transactions/`
Create a payment transaction for an order.

**Authentication:** Required (Customer who owns the order)

**Request Body:**
```json
{
  "order_id": "550e8400-e29b-41d4-a716-446655440000",
  "amount": 25.99,
  "payment_method": "credit_card",
  "payment_details": {
    "card_last_four": "1234",
    "payment_gateway": "stripe"
  }
}
```

**Response (201):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "order_id": "550e8400-e29b-41d4-a716-446655440000",
  "customer_id": "550e8400-e29b-41d4-a716-446655440002",
  "vendor_id": "550e8400-e29b-41d4-a716-446655440003",
  "amount": 25.99,
  "payment_method": "credit_card",
  "status": "completed",
  "transaction_type": "payment",
  "created_at": "2025-09-19T10:30:00Z",
  "payment_details": {
    "card_last_four": "1234",
    "payment_gateway": "stripe"
  },
  "original_transaction_id": null,
  "reason": null
}
```

**Error Responses:**
- `404`: Order not found
- `403`: Not authorized to pay for this order
- `400`: Order already paid or amount mismatch

### POST `/api/v1/transactions/refund`
Create a refund transaction.

**Authentication:** Required (Customer who made the original payment)

**Request Body:**
```json
{
  "transaction_id": "550e8400-e29b-41d4-a716-446655440001",
  "amount": 25.99,
  "reason": "Order was cancelled by vendor"
}
```

**Response (201):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440004",
  "order_id": "550e8400-e29b-41d4-a716-446655440000",
  "customer_id": "550e8400-e29b-41d4-a716-446655440002",
  "vendor_id": "550e8400-e29b-41d4-a716-446655440003",
  "amount": 25.99,
  "payment_method": "credit_card",
  "status": "completed",
  "transaction_type": "refund",
  "created_at": "2025-09-19T10:45:00Z",
  "payment_details": null,
  "original_transaction_id": "550e8400-e29b-41d4-a716-446655440001",
  "reason": "Order was cancelled by vendor"
}
```

### GET `/api/v1/transactions/me`
Get current user's transaction history with pagination and filtering.

**Authentication:** Required

**Query Parameters:**
- `skip` (int, default: 0): Number of records to skip
- `limit` (int, default: 20, max: 100): Number of records to return
- `transaction_type` (string, optional): Filter by type ("payment", "refund")
- `start_date` (datetime, optional): Filter transactions after this date
- `end_date` (datetime, optional): Filter transactions before this date

**Response (200):**
```json
{
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440001",
      "order_id": "550e8400-e29b-41d4-a716-446655440000",
      "customer_id": "550e8400-e29b-41d4-a716-446655440002",
      "vendor_id": "550e8400-e29b-41d4-a716-446655440003",
      "amount": 25.99,
      "payment_method": "credit_card",
      "status": "completed",
      "transaction_type": "payment",
      "created_at": "2025-09-19T10:30:00Z",
      "payment_details": null,
      "original_transaction_id": null,
      "reason": null
    }
  ],
  "count": 1,
  "skip": 0,
  "limit": 20
}
```

### GET `/api/v1/transactions/vendor/summary`
Get vendor's transaction summary and financial overview.

**Authentication:** Required (Vendor only)

**Query Parameters:**
- `store_id` (UUID, optional): Specific store ID (defaults to vendor's store)
- `start_date` (datetime, optional): Summary start date
- `end_date` (datetime, optional): Summary end date

**Response (200):**
```json
{
  "total_revenue": 1250.75,
  "total_transactions": 48,
  "pending_payouts": 125.50,
  "completed_transactions": 45,
  "refunded_amount": 75.25,
  "date_range": {
    "start": "2025-09-01T00:00:00Z",
    "end": "2025-09-19T23:59:59Z"
  }
}
```

### GET `/api/v1/transactions/{transaction_id}`
Get a specific transaction by ID.

**Authentication:** Required (Must be payer or payee)

**Response (200):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "order_id": "550e8400-e29b-41d4-a716-446655440000",
  "customer_id": "550e8400-e29b-41d4-a716-446655440002",
  "vendor_id": "550e8400-e29b-41d4-a716-446655440003",
  "amount": 25.99,
  "payment_method": "credit_card",
  "status": "completed",
  "transaction_type": "payment",
  "created_at": "2025-09-19T10:30:00Z",
  "payment_details": null,
  "original_transaction_id": null,
  "reason": null
}
```

**Error Responses:**
- `404`: Transaction not found
- `403`: Not authorized to view this transaction

## Order Endpoints

### POST `/api/v1/orders/`
Create a new order.

**Authentication:** Required (Customer)

**Request Body:**
```json
{
  "items": [
    {
      "food_item_id": "550e8400-e29b-41d4-a716-446655440010",
      "quantity": 2
    },
    {
      "food_item_id": "550e8400-e29b-41d4-a716-446655440011",
      "quantity": 1
    }
  ],
  "delivery_address": "123 Test Street, Ho Chi Minh City",
  "notes": "Please handle with care"
}
```

**Response (201):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "customer_id": "550e8400-e29b-41d4-a716-446655440002",
  "items": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440020",
      "food_item_id": "550e8400-e29b-41d4-a716-446655440010",
      "quantity": 2,
      "unit_price": 12.99,
      "food_item": {
        "id": "550e8400-e29b-41d4-a716-446655440010",
        "name": "Delicious Sandwich",
        "price": 12.99
      }
    }
  ],
  "total_amount": 25.99,
  "status": "pending",
  "created_at": "2025-09-19T10:00:00Z",
  "delivery_address": "123 Test Street, Ho Chi Minh City"
}
```

### GET `/api/v1/orders/me`
Get current user's orders.

**Authentication:** Required

**Query Parameters:**
- `skip` (int, default: 0): Number of records to skip
- `limit` (int, default: 20): Number of records to return

**Response (200):**
```json
{
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "customer_id": "550e8400-e29b-41d4-a716-446655440002",
      "total_amount": 25.99,
      "status": "pending",
      "created_at": "2025-09-19T10:00:00Z",
      "delivery_address": "123 Test Street, Ho Chi Minh City"
    }
  ],
  "count": 1,
  "skip": 0,
  "limit": 20
}
```

## Store Endpoints

### POST `/api/v1/stores/`
Create a new store.

**Authentication:** Required (Vendor only)

**Request Body:**
```json
{
  "name": "Joe's Pizza Palace",
  "description": "Best pizza in town",
  "address": "456 Food Street, Ho Chi Minh City",
  "coordinates": [10.762622, 106.660172],
  "phone_number": "+84901234567",
  "opening_hours": {
    "monday": "09:00-22:00",
    "tuesday": "09:00-22:00"
  }
}
```

**Response (201):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440030",
  "name": "Joe's Pizza Palace",
  "description": "Best pizza in town",
  "address": "456 Food Street, Ho Chi Minh City",
  "coordinates": [10.762622, 106.660172],
  "phone_number": "+84901234567",
  "owner_id": "550e8400-e29b-41d4-a716-446655440003",
  "created_at": "2025-09-19T10:00:00Z",
  "opening_hours": {
    "monday": "09:00-22:00",
    "tuesday": "09:00-22:00"
  }
}
```

### GET `/api/v1/stores/`
List all stores with pagination.

**Authentication:** Not required

**Query Parameters:**
- `skip` (int, default: 0): Number of records to skip
- `limit` (int, default: 20): Number of records to return

### GET `/api/v1/stores/with-travel-info`
Get stores with distance and travel time information for vendors.

**Authentication:** Required (Vendor only)

**Description:** Allows vendors to see the distance and estimated travel time (both driving and walking) from their current location to each store.

**Query Parameters:**
- `vendor_latitude` (float, required): Vendor's current latitude
- `vendor_longitude` (float, required): Vendor's current longitude  
- `radius` (float, default: 50.0): Search radius in kilometers (0.1-100.0)
- `skip` (int, default: 0): Number of records to skip
- `limit` (int, default: 100): Number of records to return (1-100)
- `travel_methods` (string, default: "driving,walking"): Comma-separated travel methods

**Response (200):**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440030",
    "name": "Joe's Pizza Palace",
    "description": "Best pizza in town",
    "address": "456 Food Street, Ho Chi Minh City",
    "latitude": 10.762622,
    "longitude": 106.660172,
    "owner_id": "550e8400-e29b-41d4-a716-446655440003",
    "distance_km": 5.2,
    "travel_time_driving_seconds": 900,
    "travel_time_walking_seconds": 3600,
    "travel_distance_driving_meters": 5000,
    "travel_distance_walking_meters": 4500
  }
]
```

**Response Fields:**
- `distance_km`: Straight-line distance in kilometers
- `travel_time_driving_seconds`: Estimated driving time in seconds
- `travel_time_walking_seconds`: Estimated walking time in seconds  
- `travel_distance_driving_meters`: Actual driving distance in meters
- `travel_distance_walking_meters`: Actual walking distance in meters

**Error Responses:**
- `403`: Customer trying to access vendor-only endpoint
- `422`: Invalid query parameters (missing coords, invalid radius)

### GET `/api/v1/stores/nearby`
Get stores within a specified radius from a location.

**Authentication:** Not required

**Query Parameters:**
- `latitude` (float, required): Search center latitude
- `longitude` (float, required): Search center longitude
- `radius` (float, default: 10.0): Search radius in kilometers (0.1-100.0)
- `skip` (int, default: 0): Number of records to skip
- `limit` (int, default: 100): Number of records to return

**Response (200):**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440030",
    "name": "Joe's Pizza Palace", 
    "address": "456 Food Street, Ho Chi Minh City",
    "latitude": 10.762622,
    "longitude": 106.660172,
    "owner_id": "550e8400-e29b-41d4-a716-446655440003",
    "distance_km": 5.2
  }
]
```

## User Endpoints

### GET `/api/v1/users/me`
Get current user profile.

**Authentication:** Required

**Response (200):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440002",
  "full_name": "John Doe",
  "email": "john@example.com",
  "phone_number": "+1234567890",
  "user_type": "customer",
  "address": "123 Main St, City",
  "coordinates": [10.123456, 106.789012],
  "created_at": "2025-09-19T09:00:00Z"
}
```

### PUT `/api/v1/users/me`
Update current user profile.

**Authentication:** Required

**Request Body:**
```json
{
  "full_name": "John Updated Doe",
  "address": "456 New Street, Updated City",
  "coordinates": [10.987654, 106.123456]
}
```

## Common Response Schemas

### Error Response
```json
{
  "detail": "Error message describing what went wrong"
}
```

### Pagination Response
```json
{
  "data": [...],  // Array of items
  "count": 100,   // Total number of items
  "skip": 0,      // Number of items skipped
  "limit": 20     // Number of items requested
}
```

## Status Codes

- `200`: OK - Request successful
- `201`: Created - Resource created successfully
- `400`: Bad Request - Invalid request data
- `401`: Unauthorized - Authentication required
- `403`: Forbidden - Access denied
- `404`: Not Found - Resource not found
- `422`: Unprocessable Entity - Validation error
- `500`: Internal Server Error - Server error

## Data Types

### Transaction Status
- `pending`: Transaction is being processed
- `completed`: Transaction completed successfully
- `failed`: Transaction failed
- `cancelled`: Transaction was cancelled

### Payment Methods
- `credit_card`: Credit card payment
- `debit_card`: Debit card payment
- `cash`: Cash payment
- `e_wallet`: Digital wallet payment

### User Types
- `customer`: Regular customer
- `vendor`: Store owner/vendor
- `admin`: System administrator

### Order Status
- `pending`: Order placed, awaiting confirmation
- `confirmed`: Order confirmed by vendor
- `preparing`: Order is being prepared
- `ready`: Order ready for pickup/delivery
- `completed`: Order completed
- `cancelled`: Order cancelled

## Rate Limiting

API endpoints are rate-limited to prevent abuse:
- Authentication endpoints: 5 requests per minute
- Other endpoints: 100 requests per minute

## Testing

See [TESTING_GUIDE.md](./TESTING_GUIDE.md) for comprehensive testing instructions.