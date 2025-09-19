import uuid
from typing import Optional
from sqlmodel import SQLModel

class FoodItemBase(SQLModel):
    name: str
    description: Optional[str] = None
    image_url: Optional[str] = None
    original_price: float
    is_available: bool = True

class FoodItemCreate(FoodItemBase):
    pass

class FoodItemUpdate(SQLModel):
    name: Optional[str] = None
    original_price: Optional[float] = None
    is_available: Optional[bool] = None

class FoodItemPublic(FoodItemBase):
    id: uuid.UUID
    store_id: uuid.UUID