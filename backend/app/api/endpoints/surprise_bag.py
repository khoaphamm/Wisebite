from typing import List, Optional
from fastapi import APIRouter, HTTPException, status, Query, Depends
from datetime import datetime
import uuid
from app.api.deps import SessionDep, CurrentVendor, CurrentUser
from app import crud
from app.schemas.surprise_bag import (
    SurpriseBagCreate, 
    SurpriseBagPublic, 
    SurpriseBagUpdate,
    SurpriseBagBookingCreate,
    SurpriseBagBookingPublic
)

router = APIRouter()

@router.post("/", response_model=SurpriseBagPublic, status_code=status.HTTP_201_CREATED)
def create_surprise_bag(session: SessionDep, current_vendor: CurrentVendor, bag_in: SurpriseBagCreate):
    """ Vendor creates a new Surprise Bag for their store. """
    store = crud.get_store_by_owner_id(session=session, owner_id=current_vendor.id)
    if not store:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Vendor must have a store to create surprise bags"
        )
    return crud.create_surprise_bag(session=session, bag_create=bag_in, store_id=store.id)

@router.get("/")
def get_all_active_bags(
    session: SessionDep,
    skip: int = Query(0, ge=0),
    limit: int = Query(100, ge=1, le=1000),
    min_price: Optional[float] = Query(None, ge=0),
    max_price: Optional[float] = Query(None, ge=0),
    available_only: Optional[bool] = Query(False)
):
    """ Get a list of all available surprise bags from all stores with optional filtering. """
    return crud.get_surprise_bags_with_filters(
        session=session,
        skip=skip,
        limit=limit,
        min_price=min_price,
        max_price=max_price,
        available_only=available_only
    )

@router.get("/my-bookings")
def get_my_bookings(
    session: SessionDep, 
    current_user: CurrentUser,
    skip: int = Query(0, ge=0),
    limit: int = Query(100, ge=1, le=1000)
):
    """ Customer gets their surprise bag bookings. """
    return crud.get_customer_surprise_bag_bookings(session=session, customer_id=current_user.id, skip=skip, limit=limit)

@router.get("/{bag_id}", response_model=SurpriseBagPublic)
def get_bag_by_id(session: SessionDep, bag_id: uuid.UUID):
    """ Get details of a specific surprise bag. """
    bag = crud.get_surprise_bag_by_id(session=session, bag_id=bag_id)
    if not bag:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Surprise bag not found"
        )
    return bag

@router.put("/{bag_id}", response_model=SurpriseBagPublic)
def update_surprise_bag(
    session: SessionDep,
    current_vendor: CurrentVendor,
    bag_id: uuid.UUID,
    bag_in: SurpriseBagUpdate
):
    """ Update a surprise bag (vendor must own the store). """
    bag = crud.get_surprise_bag_by_id(session=session, bag_id=bag_id)
    if not bag:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Surprise bag not found"
        )
    
    # Check if vendor owns the store that owns this surprise bag
    if bag.store.owner_id != current_vendor.id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not authorized to update this surprise bag"
        )
    
    return crud.update_surprise_bag(session=session, db_bag=bag, bag_in=bag_in)

@router.post("/{bag_id}/book", response_model=SurpriseBagBookingPublic, status_code=status.HTTP_201_CREATED)
def book_surprise_bag(
    session: SessionDep,
    current_user: CurrentUser,
    bag_id: uuid.UUID,
    booking_in: SurpriseBagBookingCreate
):
    """ Customer books a surprise bag. """
    # Get the surprise bag
    bag = crud.get_surprise_bag_by_id(session=session, bag_id=bag_id)
    if not bag:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Surprise bag not found"
        )
    
    # Check availability
    if bag.quantity_available < booking_in.quantity:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Insufficient quantity available"
        )
    
    # Check time window
    now = datetime.now()
    if now < bag.pickup_start_time or now > bag.pickup_end_time:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Booking window has closed"
        )
    
    return crud.create_surprise_bag_booking(
        session=session,
        booking_create=booking_in,
        bag_id=bag_id,
        customer_id=current_user.id
    )

@router.delete("/{bag_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_surprise_bag(
    session: SessionDep,
    current_vendor: CurrentVendor,
    bag_id: uuid.UUID
):
    """ Delete a surprise bag (vendor must own the store). """
    bag = crud.get_surprise_bag_by_id(session=session, bag_id=bag_id)
    if not bag:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Surprise bag not found"
        )
    
    # Check if vendor owns the store that owns this surprise bag
    if bag.store.owner_id != current_vendor.id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not authorized to delete this surprise bag"
        )
    
    crud.delete_surprise_bag(session=session, bag_id=bag_id)
    return None

@router.post("/booking/{booking_id}/cancel")
def cancel_booking(
    session: SessionDep,
    current_user: CurrentUser,
    booking_id: uuid.UUID
):
    """ Customer cancels their surprise bag booking. """
    booking = crud.get_surprise_bag_booking(session=session, booking_id=booking_id)
    if not booking:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Booking not found"
        )
    
    # Check if user owns the booking
    if booking["customer_id"] != current_user.id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not authorized to cancel this booking"
        )
    
    return crud.cancel_surprise_bag_booking(session=session, booking_id=booking_id)