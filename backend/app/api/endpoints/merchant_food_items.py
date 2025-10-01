"""
API endpoints for merchant food item management
"""

from fastapi import APIRouter, HTTPException, Depends, status, Query
from typing import List, Optional
from datetime import datetime
import uuid

from app.api.deps import SessionDep, CurrentUser
from app.models import FoodItem, Category, Store, InventoryLog, User, UserRole
from app.schemas.food_item import (
    FoodItemCreate, 
    FoodItemUpdate, 
    FoodItemResponse, 
    FoodItemWithCategory,
    InventoryUpdateRequest,
    SurplusMarkingRequest
)
from app.schemas.category import CategoryResponse
from sqlmodel import select, and_

router = APIRouter(prefix="/merchant/food-items", tags=["Merchant Food Items"])

@router.get("/categories", response_model=List[CategoryResponse])
def get_categories(session: SessionDep):
    """Get all available product categories"""
    categories = session.exec(select(Category).where(Category.is_active == True)).all()
    return categories

@router.get("/categories/hierarchy", response_model=List[CategoryResponse])
def get_category_hierarchy(session: SessionDep):
    """Get categories in hierarchical structure"""
    # Get top-level categories (no parent)
    top_categories = session.exec(
        select(Category).where(
            and_(Category.parent_category_id.is_(None), Category.is_active == True)
        )
    ).all()
    
    result = []
    for top_cat in top_categories:
        # Get subcategories
        subcategories = session.exec(
            select(Category).where(
                and_(Category.parent_category_id == top_cat.id, Category.is_active == True)
            )
        ).all()
        
        category_data = CategoryResponse.model_validate(top_cat)
        category_data.subcategories = [CategoryResponse.model_validate(sub) for sub in subcategories]
        result.append(category_data)
    
    return result

@router.post("/", response_model=FoodItemResponse, status_code=status.HTTP_201_CREATED)
def create_food_item(
    session: SessionDep, 
    current_user: CurrentUser,
    food_item_data: FoodItemCreate
):
    """Create a new food item for the merchant's store"""
    
    # Verify user is a vendor
    if current_user.role != UserRole.VENDOR:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Only vendors can create food items"
        )
    
    # Get merchant's store
    store = session.exec(select(Store).where(Store.owner_id == current_user.id)).first()
    if not store:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Store not found. Please create a store first."
        )
    
    # Verify category exists if provided
    if food_item_data.category_id:
        category = session.get(Category, food_item_data.category_id)
        if not category or not category.is_active:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Category not found or inactive"
            )
    
    # Create food item
    food_item = FoodItem(
        **food_item_data.model_dump(),
        store_id=store.id,
        available_quantity=food_item_data.total_quantity,  # Initially all quantity is available
        created_at=datetime.now(),
        updated_at=datetime.now()
    )
    
    session.add(food_item)
    session.flush()
    
    # Log initial inventory
    inventory_log = InventoryLog(
        food_item_id=food_item.id,
        change_type="initial_stock",
        quantity_change=food_item_data.total_quantity,
        previous_quantity=0,
        new_quantity=food_item_data.total_quantity,
        reason="Initial product creation",
        created_at=datetime.now()
    )
    session.add(inventory_log)
    
    session.commit()
    session.refresh(food_item)
    
    return food_item

@router.get("/", response_model=List[FoodItemWithCategory])
def get_my_food_items(
    session: SessionDep,
    current_user: CurrentUser,
    category_id: Optional[uuid.UUID] = Query(None, description="Filter by category"),
    is_surplus_available: Optional[bool] = Query(None, description="Filter by surplus availability"),
    is_active: Optional[bool] = Query(True, description="Filter by active status")
):
    """Get all food items for the current merchant's store"""
    
    # Verify user is a vendor
    if current_user.role != UserRole.VENDOR:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Only vendors can view food items"
        )
    
    # Get merchant's store
    store = session.exec(select(Store).where(Store.owner_id == current_user.id)).first()
    if not store:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Store not found"
        )
    
    # Build query
    query = select(FoodItem).where(FoodItem.store_id == store.id)
    
    if category_id:
        query = query.where(FoodItem.category_id == category_id)
    if is_surplus_available is not None:
        query = query.where(FoodItem.is_marked_for_surplus == is_surplus_available)
    if is_active is not None:
        query = query.where(FoodItem.is_active == is_active)
    
    food_items = session.exec(query).all()
    
    # Add category information
    result = []
    for item in food_items:
        item_data = FoodItemWithCategory.model_validate(item)
        if item.category_id:
            category = session.get(Category, item.category_id)
            if category:
                item_data.category = CategoryResponse.model_validate(category)
        result.append(item_data)
    
    return result

@router.put("/{item_id}", response_model=FoodItemResponse)
def update_food_item(
    session: SessionDep,
    current_user: CurrentUser,
    item_id: uuid.UUID,
    update_data: FoodItemUpdate
):
    """Update a food item"""
    
    # Get food item and verify ownership
    food_item = session.get(FoodItem, item_id)
    if not food_item:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Food item not found")
    
    # Verify store ownership
    store = session.get(Store, food_item.store_id)
    if not store or store.owner_id != current_user.id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="You can only update your own food items"
        )
    
    # Update fields
    update_dict = update_data.model_dump(exclude_unset=True)
    for field, value in update_dict.items():
        setattr(food_item, field, value)
    
    food_item.updated_at = datetime.now()
    
    session.add(food_item)
    session.commit()
    session.refresh(food_item)
    
    return food_item

@router.post("/{item_id}/update-inventory", response_model=FoodItemResponse)
def update_inventory(
    session: SessionDep,
    current_user: CurrentUser,
    item_id: uuid.UUID,
    inventory_data: InventoryUpdateRequest
):
    """Update inventory quantities for a food item"""
    
    # Get food item and verify ownership
    food_item = session.get(FoodItem, item_id)
    if not food_item:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Food item not found")
    
    # Verify store ownership
    store = session.get(Store, food_item.store_id)
    if not store or store.owner_id != current_user.id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="You can only update your own food items"
        )
    
    # Calculate changes
    previous_total = food_item.total_quantity
    quantity_change = inventory_data.new_total_quantity - previous_total
    
    # Update quantities
    food_item.total_quantity = inventory_data.new_total_quantity
    food_item.available_quantity = max(0, inventory_data.new_total_quantity - food_item.reserved_quantity - food_item.surplus_quantity)
    food_item.last_inventory_update = datetime.now()
    food_item.updated_at = datetime.now()
    
    # Log inventory change
    inventory_log = InventoryLog(
        food_item_id=food_item.id,
        change_type=inventory_data.change_type,
        quantity_change=quantity_change,
        previous_quantity=previous_total,
        new_quantity=inventory_data.new_total_quantity,
        reason=inventory_data.reason,
        created_at=datetime.now()
    )
    
    session.add(food_item)
    session.add(inventory_log)
    session.commit()
    session.refresh(food_item)
    
    return food_item

@router.post("/{item_id}/mark-surplus", response_model=FoodItemResponse)
def mark_for_surplus(
    session: SessionDep,
    current_user: CurrentUser,
    item_id: uuid.UUID,
    surplus_data: SurplusMarkingRequest
):
    """Mark a food item as available for surplus bags"""
    
    # Get food item and verify ownership
    food_item = session.get(FoodItem, item_id)
    if not food_item:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Food item not found")
    
    # Verify store ownership
    store = session.get(Store, food_item.store_id)
    if not store or store.owner_id != current_user.id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="You can only update your own food items"
        )
    
    # Validate surplus quantity
    max_available = food_item.available_quantity
    if surplus_data.surplus_quantity > max_available:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Surplus quantity cannot exceed available quantity ({max_available})"
        )
    
    # Update surplus information
    previous_surplus = food_item.surplus_quantity
    food_item.surplus_quantity = surplus_data.surplus_quantity
    food_item.surplus_discount_percentage = surplus_data.discount_percentage
    food_item.surplus_price = surplus_data.surplus_price
    food_item.is_marked_for_surplus = surplus_data.surplus_quantity > 0
    food_item.marked_surplus_at = datetime.now() if surplus_data.surplus_quantity > 0 else None
    
    # Update available quantity
    food_item.available_quantity = food_item.total_quantity - food_item.reserved_quantity - food_item.surplus_quantity
    food_item.updated_at = datetime.now()
    
    # Log surplus marking
    surplus_change = surplus_data.surplus_quantity - previous_surplus
    inventory_log = InventoryLog(
        food_item_id=food_item.id,
        change_type="surplus_marked" if surplus_change > 0 else "surplus_removed",
        quantity_change=surplus_change,
        previous_quantity=previous_surplus,
        new_quantity=surplus_data.surplus_quantity,
        reason=f"Marked for surplus with {surplus_data.discount_percentage*100:.1f}% discount",
        created_at=datetime.now()
    )
    
    session.add(food_item)
    session.add(inventory_log)
    session.commit()
    session.refresh(food_item)
    
    return food_item

@router.get("/{item_id}/inventory-history")
def get_inventory_history(
    session: SessionDep,
    current_user: CurrentUser,
    item_id: uuid.UUID
):
    """Get inventory change history for a food item"""
    
    # Get food item and verify ownership
    food_item = session.get(FoodItem, item_id)
    if not food_item:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Food item not found")
    
    # Verify store ownership
    store = session.get(Store, food_item.store_id)
    if not store or store.owner_id != current_user.id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="You can only view your own food items"
        )
    
    # Get inventory logs
    logs = session.exec(
        select(InventoryLog)
        .where(InventoryLog.food_item_id == item_id)
        .order_by(InventoryLog.created_at.desc())
    ).all()
    
    return logs

@router.delete("/{item_id}")
def delete_food_item(
    session: SessionDep,
    current_user: CurrentUser,
    item_id: uuid.UUID
):
    """Soft delete a food item (mark as inactive)"""
    
    # Get food item and verify ownership
    food_item = session.get(FoodItem, item_id)
    if not food_item:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Food item not found")
    
    # Verify store ownership
    store = session.get(Store, food_item.store_id)
    if not store or store.owner_id != current_user.id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="You can only delete your own food items"
        )
    
    # Soft delete
    food_item.is_active = False
    food_item.is_available = False
    food_item.updated_at = datetime.now()
    
    session.add(food_item)
    session.commit()
    
    return {"message": "Food item deleted successfully"}