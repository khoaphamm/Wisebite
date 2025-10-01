import uuid
from typing import Optional
from datetime import datetime
from sqlmodel import SQLModel, Field
from pydantic import field_validator, model_validator
from .store import StorePublic

class SurpriseBagBase(SQLModel):
    name: str = Field(default="Túi Bất Ngờ", max_length=255)
    description: Optional[str] = Field(default=None, max_length=500)
    bag_type: str = Field(default="combo", max_length=50)
    original_value: float = Field(ge=0)
    discounted_price: float = Field(ge=0)
    discount_percentage: float = Field(ge=0, le=1)
    quantity_available: int = Field(ge=0)
    max_per_customer: int = Field(default=1)
    available_from: datetime
    available_until: datetime
    pickup_start_time: datetime
    pickup_end_time: datetime
    is_active: bool = Field(default=True)
    is_auto_generated: bool = Field(default=False)

class SurpriseBagCreate(SurpriseBagBase):
    @model_validator(mode='after')
    def validate_times_and_pricing(self):
        # Validate pickup times
        if self.pickup_end_time <= self.pickup_start_time:
            raise ValueError('pickup_end_time must be after pickup_start_time')
        
        # Validate available times
        if self.available_until <= self.available_from:
            raise ValueError('available_until must be after available_from')
        
        # Validate pricing
        if self.discounted_price >= self.original_value:
            raise ValueError('discounted_price must be less than original_value')
        
        # Validate discount percentage matches the prices
        calculated_discount = (self.original_value - self.discounted_price) / self.original_value
        if abs(calculated_discount - self.discount_percentage) > 0.01:  # Allow small rounding differences
            raise ValueError('discount_percentage must match the actual discount from prices')
        
        return self

class SurpriseBagUpdate(SQLModel):
    # Allow updating only specific fields
    name: Optional[str] = Field(default=None, max_length=255)
    description: Optional[str] = Field(default=None, max_length=500)
    bag_type: Optional[str] = Field(default=None, max_length=50)
    original_value: Optional[float] = Field(default=None, ge=0)
    discounted_price: Optional[float] = Field(default=None, ge=0)
    discount_percentage: Optional[float] = Field(default=None, ge=0, le=1)
    quantity_available: Optional[int] = Field(default=None, ge=0)
    max_per_customer: Optional[int] = Field(default=None, ge=1)
    available_from: Optional[datetime] = None
    available_until: Optional[datetime] = None
    pickup_start_time: Optional[datetime] = None
    pickup_end_time: Optional[datetime] = None
    is_active: Optional[bool] = None
    is_auto_generated: Optional[bool] = None

class SurpriseBagPublic(SurpriseBagBase):
    id: uuid.UUID
    store_id: uuid.UUID
    created_at: datetime
    updated_at: datetime
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