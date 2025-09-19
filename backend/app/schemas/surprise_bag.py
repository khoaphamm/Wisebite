import uuid
from typing import Optional
from datetime import datetime
from sqlmodel import SQLModel, Field
from pydantic import field_validator, model_validator
from .store import StorePublic

class SurpriseBagBase(SQLModel):
    name: str = Field(default="Túi Bất Ngờ", max_length=100)
    description: Optional[str] = Field(default=None, max_length=500)
    original_value: float = Field(ge=0)
    discounted_price: float = Field(ge=0)
    quantity_available: int = Field(ge=0)
    pickup_start_time: datetime
    pickup_end_time: datetime

class SurpriseBagCreate(SurpriseBagBase):
    @model_validator(mode='after')
    def validate_times_and_pricing(self):
        # Validate pickup times
        if self.pickup_end_time <= self.pickup_start_time:
            raise ValueError('pickup_end_time must be after pickup_start_time')
        
        # Validate pricing
        if self.discounted_price >= self.original_value:
            raise ValueError('discounted_price must be less than original_value')
        
        return self

class SurpriseBagUpdate(SQLModel):
    # Allow updating only specific fields
    name: Optional[str] = Field(default=None, max_length=100)
    description: Optional[str] = Field(default=None, max_length=500)
    original_value: Optional[float] = Field(default=None, ge=0)
    discounted_price: Optional[float] = Field(default=None, ge=0)
    quantity_available: Optional[int] = Field(default=None, ge=0)
    pickup_start_time: Optional[datetime] = None
    pickup_end_time: Optional[datetime] = None
    is_active: Optional[bool] = None # To easily activate/deactivate

class SurpriseBagPublic(SurpriseBagBase):
    id: uuid.UUID
    store_id: uuid.UUID
    store: Optional[StorePublic] = None

# Booking-related schemas
class SurpriseBagBookingBase(SQLModel):
    quantity: int
    pickup_time: datetime

class SurpriseBagBookingCreate(SurpriseBagBookingBase):
    pass

class SurpriseBagBookingPublic(SurpriseBagBookingBase):
    id: uuid.UUID
    surprise_bag_id: uuid.UUID
    customer_id: uuid.UUID
    status: str  # pending, confirmed, completed, cancelled
    created_at: datetime