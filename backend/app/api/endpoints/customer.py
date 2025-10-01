from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, Query
from sqlmodel import Session, select, and_
from datetime import datetime, timedelta

from app.api.deps import SessionDep
from app.models import Store, SurpriseBag, User
from app.schemas.store import StorePublic
from app.schemas.surprise_bag import SurpriseBagPublic

router = APIRouter()

@router.get("/stores", response_model=List[StorePublic])
def get_available_stores(
    session: SessionDep,
    city: Optional[str] = Query(None, description="Filter by city")
):
    """
    Get all stores that have active surprise bags available for customers.
    """
    # Get stores that have at least one active surprise bag
    query = select(Store).join(SurpriseBag).where(
        and_(
            SurpriseBag.is_active == True,
            SurpriseBag.quantity_available > 0,
            SurpriseBag.available_until > datetime.utcnow()
        )
    ).distinct()
    
    if city:
        query = query.where(Store.address.ilike(f"%{city}%"))
    
    stores = session.exec(query).all()
    return stores

@router.get("/stores/{store_id}/surprise-bags", response_model=List[SurpriseBagPublic])
def get_store_surprise_bags(
    store_id: str,
    session: SessionDep,
    category: Optional[str] = Query(None, description="Filter by bag category")
):
    """
    Get all available surprise bags for a specific store.
    """
    query = select(SurpriseBag).where(
        and_(
            SurpriseBag.store_id == store_id,
            SurpriseBag.is_active == True,
            SurpriseBag.quantity_available > 0,
            SurpriseBag.available_until > datetime.utcnow()
        )
    )
    
    if category:
        query = query.where(SurpriseBag.bag_type == category)
    
    surprise_bags = session.exec(query).all()
    return surprise_bags

@router.get("/surprise-bags", response_model=List[SurpriseBagPublic])
def get_all_surprise_bags(
    session: SessionDep,
    category: Optional[str] = Query(None, description="Filter by bag category"),
    city: Optional[str] = Query(None, description="Filter by city"),
    available_from: Optional[datetime] = Query(None, description="Available from time"),
    available_until: Optional[datetime] = Query(None, description="Available until time"),
    max_price: Optional[float] = Query(None, description="Maximum price filter")
):
    """
    Get all available surprise bags with optional filters.
    Time window: Order 2-6h chiều (14:00-18:00)
    """
    now = datetime.utcnow()
    
    # Base query for active surprise bags
    query = select(SurpriseBag).join(Store).where(
        and_(
            SurpriseBag.is_active == True,
            SurpriseBag.quantity_available > 0,
            SurpriseBag.available_until > now,
            # Ensure pickup time is at least 5 minutes from now
            SurpriseBag.pickup_start_time > now + timedelta(minutes=5)
        )
    )
    
    # Apply filters
    if category:
        query = query.where(SurpriseBag.bag_type == category)
    
    if city:
        query = query.where(Store.address.ilike(f"%{city}%"))
    
    if available_from:
        query = query.where(SurpriseBag.available_from >= available_from)
    
    if available_until:
        query = query.where(SurpriseBag.available_until <= available_until)
    
    if max_price:
        query = query.where(SurpriseBag.discounted_price <= max_price)
    
    # Order by pickup time to show soonest first
    query = query.order_by(SurpriseBag.pickup_start_time)
    
    surprise_bags = session.exec(query).all()
    return surprise_bags

@router.get("/surprise-bags/{bag_id}", response_model=SurpriseBagPublic)
def get_surprise_bag_details(
    bag_id: str,
    session: SessionDep
):
    """
    Get detailed information about a specific surprise bag.
    """
    surprise_bag = session.get(SurpriseBag, bag_id)
    if not surprise_bag:
        raise HTTPException(status_code=404, detail="Surprise bag not found")
    
    # Check if still available
    now = datetime.utcnow()
    if (not surprise_bag.is_active or 
        surprise_bag.quantity_available <= 0 or 
        surprise_bag.available_until <= now or
        surprise_bag.pickup_start_time <= now + timedelta(minutes=5)):
        raise HTTPException(status_code=400, detail="Surprise bag is no longer available")
    
    return surprise_bag

@router.get("/categories", response_model=List[str])
def get_available_categories(
    session: SessionDep
):
    """
    Get all available surprise bag categories.
    """
    # Vietnamese categories as defined in Phase 1
    categories = [
        "Combo",
        "Thịt/Cá", 
        "Rau/Củ",
        "Trái cây",
        "Bánh mì"
    ]
    return categories