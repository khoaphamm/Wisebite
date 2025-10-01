import uuid
from typing import Optional, List
from datetime import datetime
from sqlmodel import SQLModel, Field
from pydantic import BaseModel, validator

# Enhanced schemas for the new food item management system

class CategoryResponse(BaseModel):
    """Category response schema"""
    id: uuid.UUID
    name: str
    parent_category_id: Optional[uuid.UUID] = None
    description: Optional[str] = None
    is_active: bool
    created_at: datetime
    subcategories: Optional[List['CategoryResponse']] = None
    
    class Config:
        from_attributes = True

# Forward reference for self-referencing model
CategoryResponse.model_rebuild()

class FoodItemCreate(BaseModel):
    """Schema for creating a new food item"""
    name: str = Field(..., max_length=255, description="Product name")
    description: Optional[str] = Field(None, max_length=500, description="Product description")
    sku: Optional[str] = Field(None, max_length=100, description="Stock Keeping Unit")
    image_url: Optional[str] = None
    
    # Pricing
    standard_price: float = Field(..., ge=0, description="Regular selling price")
    cost_price: Optional[float] = Field(None, ge=0, description="Cost price for profit calculation")
    
    # Product characteristics
    is_fresh: bool = Field(True, description="True for fresh groceries, False for packaged goods")
    expires_at: Optional[datetime] = None
    
    # Inventory
    total_quantity: int = Field(..., ge=0, description="Total stock quantity")
    
    # Categories
    category_id: Optional[uuid.UUID] = None
    
    # Additional info
    ingredients: Optional[str] = Field(None, max_length=500)
    allergens: Optional[str] = Field(None, max_length=200)
    weight: Optional[float] = Field(None, ge=0, description="Weight in grams")
    unit: Optional[str] = Field("piece", max_length=20, description="Unit of measurement")

class FoodItemUpdate(BaseModel):
    """Schema for updating a food item"""
    name: Optional[str] = Field(None, max_length=255)
    description: Optional[str] = Field(None, max_length=500)
    sku: Optional[str] = Field(None, max_length=100)
    image_url: Optional[str] = None
    standard_price: Optional[float] = Field(None, ge=0)
    cost_price: Optional[float] = Field(None, ge=0)
    is_fresh: Optional[bool] = None
    expires_at: Optional[datetime] = None
    category_id: Optional[uuid.UUID] = None
    ingredients: Optional[str] = Field(None, max_length=500)
    allergens: Optional[str] = Field(None, max_length=200)
    weight: Optional[float] = Field(None, ge=0)
    unit: Optional[str] = Field(None, max_length=20)
    is_available: Optional[bool] = None
    is_active: Optional[bool] = None

class FoodItemResponse(BaseModel):
    """Schema for food item response"""
    id: uuid.UUID
    name: str
    description: Optional[str] = None
    sku: Optional[str] = None
    image_url: Optional[str] = None
    
    # Pricing
    standard_price: float
    cost_price: Optional[float] = None
    
    # Product characteristics
    is_fresh: bool
    expires_at: Optional[datetime] = None
    
    # Inventory
    total_quantity: int
    surplus_quantity: int
    reserved_quantity: int
    available_quantity: int
    
    # Surplus management
    is_marked_for_surplus: bool
    surplus_discount_percentage: Optional[float] = None
    surplus_price: Optional[float] = None
    marked_surplus_at: Optional[datetime] = None
    
    # Additional info
    ingredients: Optional[str] = None
    allergens: Optional[str] = None
    weight: Optional[float] = None
    unit: Optional[str] = None
    
    # Status
    is_available: bool
    is_active: bool
    
    # Timestamps
    created_at: datetime
    updated_at: datetime
    last_inventory_update: Optional[datetime] = None
    
    # Relationships
    store_id: uuid.UUID
    category_id: Optional[uuid.UUID] = None
    
    class Config:
        from_attributes = True

class FoodItemWithCategory(FoodItemResponse):
    """Food item response with category information"""
    category: Optional[CategoryResponse] = None

class InventoryUpdateRequest(BaseModel):
    """Schema for updating inventory"""
    new_total_quantity: int = Field(..., ge=0, description="New total quantity")
    change_type: str = Field(..., description="Type of change: restock, adjustment, etc.")
    reason: Optional[str] = Field(None, max_length=255, description="Reason for the change")

class SurplusMarkingRequest(BaseModel):
    """Schema for marking items as surplus"""
    surplus_quantity: int = Field(..., ge=0, description="Quantity to mark as surplus")
    discount_percentage: float = Field(..., ge=0, le=1, description="Discount percentage (0-1)")
    surplus_price: Optional[float] = Field(None, ge=0, description="Calculated surplus price")

class InventoryLogResponse(BaseModel):
    """Schema for inventory log response"""
    id: uuid.UUID
    change_type: str
    quantity_change: int
    previous_quantity: int
    new_quantity: int
    reason: Optional[str] = None
    reference_id: Optional[uuid.UUID] = None
    created_at: datetime
    
    class Config:
        from_attributes = True

# Legacy schemas for backward compatibility
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

class FoodItemPublic(FoodItemBase):
    id: uuid.UUID
    store_id: uuid.UUID