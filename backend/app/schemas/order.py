import uuid
from typing import Optional, List
from datetime import datetime
from sqlmodel import SQLModel, Field
from pydantic import field_validator, computed_field
from geoalchemy2.elements import WKBElement
from geoalchemy2.shape import to_shape
from shapely.geometry import mapping
from app.models import OrderStatus
from .user import UserPublic
from .surprise_bag import SurpriseBagPublic
from .food_item import FoodItemResponse
from .store import StorePublic

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
    food_item: Optional[FoodItemResponse] = None # Nested food item details

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
    
    @computed_field
    @property
    def store(self) -> Optional[StorePublic]:
        """
        Derive the primary store from the first order item.
        Since orders in WiseBite typically come from a single store,
        we use the store from the first item as the primary store.
        """
        if not self.items:
            return None
            
        first_item = self.items[0]
        
        # Check surprise bag store first
        if first_item.surprise_bag and first_item.surprise_bag.store:
            return StorePublic(
                id=first_item.surprise_bag.store.id,
                name=first_item.surprise_bag.store.name,
                address=first_item.surprise_bag.store.address,
                description=first_item.surprise_bag.store.description,
                logo_url=first_item.surprise_bag.store.logo_url,
                owner_id=first_item.surprise_bag.store.owner_id,
                latitude=first_item.surprise_bag.store.latitude,
                longitude=first_item.surprise_bag.store.longitude
            )
        
        # Check food item store as fallback
        # Note: FoodItemResponse only has store_id, not nested store object
        # Store information would need to be fetched separately if needed
        if first_item.food_item and hasattr(first_item.food_item, 'store') and first_item.food_item.store:
            return StorePublic(
                id=first_item.food_item.store.id,
                name=first_item.food_item.store.name,
                address=first_item.food_item.store.address,
                description=first_item.food_item.store.description,
                logo_url=first_item.food_item.store.logo_url,
                owner_id=first_item.food_item.store.owner_id,
                latitude=first_item.food_item.store.latitude,
                longitude=first_item.food_item.store.longitude
            )
        
        return None

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