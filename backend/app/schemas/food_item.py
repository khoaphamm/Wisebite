import uuid
from typing import Optional
from datetime import datetime
from sqlmodel import SQLModel, Field

class FoodItemBase(SQLModel):
    name: str = Field(min_length=1, max_length=100)
    description: Optional[str] = Field(default=None, max_length=500)
    image_url: Optional[str] = None
    original_price: float = Field(ge=0)
    quantity: int = Field(ge=0, default=0)
    category: Optional[str] = Field(default=None, max_length=50)
    expires_at: Optional[datetime] = None
    ingredients: Optional[str] = Field(default=None, max_length=500)
    allergens: Optional[str] = Field(default=None, max_length=200)
    is_available: bool = True

class FoodItemCreate(FoodItemBase):
    pass

class FoodItemUpdate(SQLModel):
    name: Optional[str] = Field(default=None, min_length=1, max_length=100)
    description: Optional[str] = Field(default=None, max_length=500)
    original_price: Optional[float] = Field(default=None, ge=0)
    quantity: Optional[int] = Field(default=None, ge=0)
    category: Optional[str] = Field(default=None, max_length=50)
    expires_at: Optional[datetime] = None
    ingredients: Optional[str] = Field(default=None, max_length=500)
    allergens: Optional[str] = Field(default=None, max_length=200)
    is_available: Optional[bool] = None

class FoodItemPublic(FoodItemBase):
    id: uuid.UUID
    store_id: uuid.UUID