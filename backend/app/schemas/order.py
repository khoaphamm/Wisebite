import uuid
from typing import Optional, List
from datetime import datetime
from sqlmodel import SQLModel, Field
from pydantic import field_validator
from geoalchemy2.elements import WKBElement
from geoalchemy2.shape import to_shape
from shapely.geometry import mapping
from app.models import OrderStatus
from .user import UserPublic
from .surprise_bag import SurpriseBagPublic
from .food_item import FoodItemPublic

# --- CREATION SCHEMAS (REWRITTEN for WiseBite) ---

class OrderItemCreate(SQLModel):
    surprise_bag_id: Optional[uuid.UUID] = None
    food_item_id: Optional[uuid.UUID] = None
    quantity: int = Field(gt=0)
    
    @field_validator('surprise_bag_id')
    @classmethod
    def validate_item_ids(cls, v, info):
        # Ensure exactly one of surprise_bag_id or food_item_id is provided
        food_item_id = info.data.get('food_item_id')
        if v is None and food_item_id is None:
            raise ValueError('Either surprise_bag_id or food_item_id must be provided')
        if v is not None and food_item_id is not None:
            raise ValueError('Only one of surprise_bag_id or food_item_id can be provided')
        return v

class OrderCreate(SQLModel):
    items: List[OrderItemCreate]
    delivery_address: Optional[str] = None
    notes: Optional[str] = None
    preferred_pickup_time: Optional[datetime] = None  # Customer's preferred pickup time

# --- PUBLIC RESPONSE SCHEMAS (REWRITTEN for WiseBite) ---

class OrderItemPublic(SQLModel):
    quantity: int
    price_per_item: float
    surprise_bag: Optional[SurpriseBagPublic] = None # Nested surprise bag details
    food_item: Optional[FoodItemPublic] = None # Nested food item details

class OrderPublic(SQLModel):
    id: uuid.UUID
    customer_id: uuid.UUID  # Add this field for API compatibility
    status: OrderStatus
    total_amount: float
    created_at: datetime
    delivery_address: Optional[str] = None  # Delivery address
    notes: Optional[str] = None  # Order notes
    preferred_pickup_time: Optional[datetime] = None  # Customer's preferred pickup time
    customer: UserPublic # Nested customer details
    items: List[OrderItemPublic]

# --- UPDATE & STATUS CHANGE SCHEMAS (ADAPTED) ---

class OrderUpdate(SQLModel):
    # For a customer to cancel or for an admin to change status
    status: OrderStatus

class OrderStatusUpdate(SQLModel):
    # For updating order status specifically
    status: OrderStatus

# --- GEO & NEARBY SCHEMAS (KEPT) ---
# These are still useful if you want to show nearby stores/orders.
# The internal logic would adapt to querying stores, not individual orders.

class NearbyOrderPublic(OrderPublic):
    # This schema can be repurposed to show nearby available bags
    distance_km: float
    travel_time_seconds: Optional[float] = None 
    travel_distance_meters: Optional[float] = None