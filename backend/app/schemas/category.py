"""
Schemas for product categories
"""

import uuid
from typing import Optional, List
from datetime import datetime
from pydantic import BaseModel

class CategoryBase(BaseModel):
    """Base category schema"""
    name: str
    description: Optional[str] = None
    parent_category_id: Optional[uuid.UUID] = None

class CategoryCreate(CategoryBase):
    """Schema for creating a category"""
    pass

class CategoryUpdate(BaseModel):
    """Schema for updating a category"""
    name: Optional[str] = None
    description: Optional[str] = None
    parent_category_id: Optional[uuid.UUID] = None
    is_active: Optional[bool] = None

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