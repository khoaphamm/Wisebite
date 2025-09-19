import uuid
from typing import Optional
from datetime import datetime
from sqlmodel import SQLModel
from .store import StorePublic

class SurpriseBagBase(SQLModel):
    name: str = "Túi Bất Ngờ"
    original_value: float
    discounted_price: float
    quantity_available: int
    pickup_start_time: datetime
    pickup_end_time: datetime

class SurpriseBagCreate(SurpriseBagBase):
    pass

class SurpriseBagUpdate(SQLModel):
    # Allow updating only specific fields
    quantity_available: Optional[int] = None
    is_active: Optional[bool] = None # To easily activate/deactivate

class SurpriseBagPublic(SurpriseBagBase):
    id: uuid.UUID
    store_id: uuid.UUID
    store: Optional[StorePublic] = None