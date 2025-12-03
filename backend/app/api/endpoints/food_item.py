from typing import List, Optional
from fastapi import APIRouter, HTTPException, status, Query, Depends
from app.api.deps import SessionDep, CurrentVendor, CurrentUser
from app import crud
from app.schemas.food_item import FoodItemCreate, FoodItemResponse, FoodItemUpdate
import uuid

router = APIRouter()

@router.post("/", response_model=FoodItemResponse, status_code=status.HTTP_201_CREATED)
def create_food_item(session: SessionDep, current_vendor: CurrentVendor, item_in: FoodItemCreate):
    """ Vendor creates a new regular food item for their store. """
    store = crud.get_store_by_owner_id(session=session, owner_id=current_vendor.id)
    if not store:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Vendor must have a store to create food items"
        )
    return crud.create_food_item(session=session, item_create=item_in, store_id=store.id)

@router.get("/")
def list_food_items(
    session: SessionDep,
    skip: int = Query(0, ge=0),
    limit: int = Query(100, ge=1, le=1000),
    category: Optional[str] = Query(None),
    latitude: Optional[float] = Query(None),
    longitude: Optional[float] = Query(None),
    radius: Optional[float] = Query(None, ge=0)
):
    """ Get all available food items with optional filtering. """
    return crud.get_food_items_with_filters(
        session=session, 
        skip=skip, 
        limit=limit,
        category=category,
        latitude=latitude,
        longitude=longitude,
        radius=radius
    )

@router.get("/search")
def search_food_items(
    session: SessionDep,
    query: str = Query(..., min_length=1),
    skip: int = Query(0, ge=0),
    limit: int = Query(100, ge=1, le=1000)
):
    """ Search food items by name or description. """
    return crud.search_food_items(session=session, query=query, skip=skip, limit=limit)

@router.get("/me")
def get_my_food_items(
    session: SessionDep, 
    current_vendor: CurrentVendor,
    skip: int = Query(0, ge=0),
    limit: int = Query(100, ge=1, le=1000)
):
    """ Vendor lists all regular food items for their store. """
    store = crud.get_store_by_owner_id(session=session, owner_id=current_vendor.id)
    if not store:
        return {"data": [], "count": 0}
    return crud.get_food_items_by_store(session=session, store_id=store.id, skip=skip, limit=limit)

@router.get("/{food_item_id}", response_model=FoodItemResponse)
def get_food_item(session: SessionDep, food_item_id: uuid.UUID):
    """ Get a specific food item by ID. """
    food_item = crud.get_food_item(session=session, food_item_id=food_item_id)
    if not food_item:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Food item not found"
        )
    return food_item

@router.put("/{food_item_id}", response_model=FoodItemResponse)
def update_food_item(
    session: SessionDep, 
    current_vendor: CurrentVendor,
    food_item_id: uuid.UUID,
    item_in: FoodItemUpdate
):
    """ Update a food item (vendor must own the store). """
    food_item = crud.get_food_item(session=session, food_item_id=food_item_id)
    if not food_item:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Food item not found"
        )
    
    # Check if vendor owns the store that owns this food item
    if food_item.store.owner_id != current_vendor.id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not authorized to update this food item"
        )
    
    return crud.update_food_item(session=session, db_food_item=food_item, item_in=item_in)

@router.delete("/{food_item_id}")
def delete_food_item(
    session: SessionDep,
    current_vendor: CurrentVendor,
    food_item_id: uuid.UUID
):
    """ Delete a food item (vendor must own the store). """
    food_item = crud.get_food_item(session=session, food_item_id=food_item_id)
    if not food_item:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Food item not found"
        )
    
    # Check if vendor owns the store that owns this food item
    if food_item.store.owner_id != current_vendor.id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not authorized to delete this food item"
        )
    
    crud.delete_food_item(session=session, food_item_id=food_item_id)
    return {"message": "Food item deleted successfully"}

# Keep the old endpoint for backward compatibility
@router.get("/my-items")
def get_my_food_items_legacy(
    session: SessionDep, 
    current_vendor: CurrentVendor,
    skip: int = Query(0, ge=0),
    limit: int = Query(100, ge=1, le=1000)
):
    """ Vendor lists all regular food items for their store (legacy endpoint). """
    store = crud.get_store_by_owner_id(session=session, owner_id=current_vendor.id)
    if not store:
        return {"data": [], "count": 0}
    return crud.get_food_items_by_store(session=session, store_id=store.id, skip=skip, limit=limit)