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

# --- CREATION SCHEMAS (REWRITTEN for WiseBite) ---

class OrderItemCreate(SQLModel):
    surprise_bag_id: uuid.UUID
    quantity: int = Field(gt=0)

class OrderCreate(SQLModel):
    items: List[OrderItemCreate]

# --- PUBLIC RESPONSE SCHEMAS (REWRITTEN for WiseBite) ---

class OrderItemPublic(SQLModel):
    quantity: int
    price_per_item: float
    surprise_bag: SurpriseBagPublic # Nested surprise bag details

class OrderPublic(SQLModel):
    id: uuid.UUID
    status: OrderStatus
    total_amount: float
    created_at: datetime
    customer: UserPublic # Linked to a customer
    items: List[OrderItemPublic]

# --- UPDATE & STATUS CHANGE SCHEMAS (ADAPTED) ---

class OrderUpdate(SQLModel):
    # For a customer to cancel or for an admin to change status
    status: OrderStatus

# --- GEO & NEARBY SCHEMAS (KEPT) ---
# These are still useful if you want to show nearby stores/orders.
# The internal logic would adapt to querying stores, not individual orders.

class NearbyOrderPublic(OrderPublic):
    # This schema can be repurposed to show nearby available bags
    distance_km: float
    travel_time_seconds: Optional[float] = None 
    travel_distance_meters: Optional[float] = None