import uuid
from typing import List, Optional
from fastapi import APIRouter, HTTPException, status, Query
from app.api.deps import SessionDep, CurrentVendor, CurrentUser
from app import crud
from app.schemas.store import StorePublic, StoreUpdate, StoreCreate, StoreWithDistance

router = APIRouter()

@router.post("/", response_model=StorePublic, status_code=status.HTTP_201_CREATED)
def create_store(session: SessionDep, current_vendor: CurrentVendor, store_in: StoreCreate):
    """ Vendor creates a new store. """
    # Check if vendor already has a store
    existing_store = crud.get_store_by_owner_id(session=session, owner_id=current_vendor.id)
    if existing_store:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST, 
            detail="Vendor already has a store"
        )
    return crud.create_store(session=session, store_create=store_in, owner_id=current_vendor.id)

@router.get("/", response_model=List[StorePublic])
def list_stores(
    session: SessionDep, 
    skip: int = Query(0, ge=0),
    limit: int = Query(100, ge=1, le=100)
):
    """ Get a list of all public stores. """
    return crud.get_all_stores(session=session, skip=skip, limit=limit)

@router.get("/nearby", response_model=List[StoreWithDistance])
def list_nearby_stores(
    session: SessionDep,
    latitude: float = Query(..., description="Latitude of the search location"),
    longitude: float = Query(..., description="Longitude of the search location"),
    radius: float = Query(10.0, ge=0.1, le=100.0, description="Search radius in kilometers"),
    skip: int = Query(0, ge=0),
    limit: int = Query(100, ge=1, le=100)
):
    """ Get stores within a specified radius from a location, sorted by distance. """
    stores = crud.get_stores_within_radius(
        session=session,
        latitude=latitude,
        longitude=longitude,
        radius_km=radius,
        skip=skip,
        limit=limit
    )
    return stores

@router.get("/me", response_model=StorePublic)
def get_my_store(session: SessionDep, current_vendor: CurrentVendor):
    """ Vendor gets their own store profile. """
    store = crud.get_store_by_owner_id(session=session, owner_id=current_vendor.id)
    if not store:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Store not found"
        )
    return store

@router.patch("/me", response_model=StorePublic)
def update_my_store(session: SessionDep, current_vendor: CurrentVendor, store_in: StoreUpdate):
    """ Vendor updates their own store profile. """
    db_store = crud.get_store_by_owner_id(session=session, owner_id=current_vendor.id)
    if not db_store:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Store not found"
        )
    return crud.update_store(session=session, db_store=db_store, store_in=store_in)

@router.delete("/me", status_code=status.HTTP_204_NO_CONTENT)
def delete_my_store(session: SessionDep, current_vendor: CurrentVendor):
    """ Vendor deletes their own store. """
    db_store = crud.get_store_by_owner_id(session=session, owner_id=current_vendor.id)
    if not db_store:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Store not found"
        )
    crud.delete_store(session=session, store_id=db_store.id)

@router.get("/{store_id}", response_model=StorePublic)
def get_store_by_id(session: SessionDep, store_id: uuid.UUID):
    """ Get public details of any store by its ID. """
    store = crud.get_store_by_id(session=session, store_id=store_id)
    if not store:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Store not found"
        )
    return store

@router.put("/{store_id}", response_model=StorePublic)
def update_store_by_id(
    session: SessionDep, 
    current_vendor: CurrentVendor,
    store_id: uuid.UUID, 
    store_in: StoreUpdate
):
    """ Vendor updates store by ID (only if they own it). """
    db_store = crud.get_store_by_id(session=session, store_id=store_id)
    if not db_store:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Store not found"
        )
    if db_store.owner_id != current_vendor.id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not authorized to update this store"
        )
    return crud.update_store(session=session, db_store=db_store, store_in=store_in)

@router.delete("/{store_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_store_by_id(session: SessionDep, current_vendor: CurrentVendor, store_id: uuid.UUID):
    """ Vendor deletes store by ID (only if they own it). """
    db_store = crud.get_store_by_id(session=session, store_id=store_id)
    if not db_store:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Store not found"
        )
    if db_store.owner_id != current_vendor.id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not authorized to delete this store"
        )
    crud.delete_store(session=session, store_id=store_id)