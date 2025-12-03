import uuid
from typing import List, Optional
from fastapi import APIRouter, HTTPException, status, Query
from app.api.deps import SessionDep, CurrentVendor, CurrentUser
from app import crud
from app.schemas.store import StorePublic, StoreUpdate, StoreCreate, StoreWithDistance, StoreWithTravelInfo
from app.services.mapbox import get_travel_info_multi_profile

router = APIRouter()

@router.post("/", response_model=StorePublic, status_code=status.HTTP_201_CREATED)
def create_store(session: SessionDep, current_vendor: CurrentVendor, store_in: StoreCreate):
    """ Vendor creates a new store. """
    import logging
    logger = logging.getLogger(__name__)
    
    logger.info(f"Creating store for vendor {current_vendor.id}")
    logger.info(f"Store request data: {store_in}")
    logger.info(f"  name: {store_in.name}")
    logger.info(f"  description: {store_in.description}")
    logger.info(f"  address: {store_in.address}")
    logger.info(f"  logo_url: {store_in.logo_url}")
    logger.info(f"  latitude: {store_in.latitude}")
    logger.info(f"  longitude: {store_in.longitude}")
    
    # Check if vendor already has a store
    existing_store = crud.get_store_by_owner_id(session=session, owner_id=current_vendor.id)
    if existing_store:
        logger.warning(f"Vendor {current_vendor.id} already has a store {existing_store.id}")
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST, 
            detail="Vendor already has a store"
        )
    
    logger.info(f"No existing store found, creating new store for vendor {current_vendor.id}")
    
    try:
        result = crud.create_store(session=session, store_create=store_in, owner_id=current_vendor.id)
        logger.info(f"Successfully created store {result.id} for vendor {current_vendor.id}")
        return result
    except Exception as e:
        logger.error(f"Failed to create store for vendor {current_vendor.id}: {e}")
        logger.error(f"Exception type: {type(e)}")
        raise

@router.get("/", response_model=List[StorePublic])
def list_stores(
    session: SessionDep, 
    skip: int = Query(0, ge=0),
    limit: int = Query(100, ge=1, le=100)
):
    """ Get a list of all public stores. """
    return crud.get_all_stores(session=session, skip=skip, limit=limit)

@router.get("/with-travel-info", response_model=List[StoreWithTravelInfo])
async def list_stores_with_travel_info(
    session: SessionDep,
    current_vendor: CurrentVendor,
    vendor_latitude: float = Query(..., description="Vendor's current latitude"),
    vendor_longitude: float = Query(..., description="Vendor's current longitude"),
    radius: float = Query(50.0, ge=0.1, le=100.0, description="Search radius in kilometers"),
    skip: int = Query(0, ge=0),
    limit: int = Query(100, ge=1, le=100),
    travel_methods: Optional[str] = Query("driving,walking", description="Comma-separated travel methods (driving, walking)")
):
    """
    Get stores with distance and travel time information for vendors.
    This allows vendors to see how far and how long it takes to reach each store.
    """
    # Get nearby stores first
    stores = crud.get_stores_within_radius(
        session=session,
        latitude=vendor_latitude,
        longitude=vendor_longitude,
        radius_km=radius,
        skip=skip,
        limit=limit
    )
    
    if not stores:
        return []
    
    # Prepare coordinates for Mapbox API
    vendor_origin = (vendor_longitude, vendor_latitude)  # Mapbox expects (lng, lat)
    destinations = []
    store_coords_map = {}
    
    for i, store in enumerate(stores):
        if store.latitude is not None and store.longitude is not None:
            destinations.append((store.longitude, store.latitude))
            store_coords_map[i] = store.id
    
    if not destinations:
        # Return stores without travel info if no coordinates available
        return [
            StoreWithTravelInfo(**store.model_dump())
            for store in stores
        ]
    
    # Get travel information from Mapbox
    travel_info = await get_travel_info_multi_profile(vendor_origin, destinations)
    
    # Combine store data with travel information
    result = []
    dest_index = 0
    
    for store in stores:
        store_dict = store.model_dump()
        
        if store.latitude is not None and store.longitude is not None and travel_info:
            # Add travel information if available
            if travel_info.get("driving") and dest_index < len(travel_info["driving"]):
                driving_info = travel_info["driving"][dest_index]
                if driving_info:
                    store_dict["travel_time_driving_seconds"] = driving_info.get("duration")
                    store_dict["travel_distance_driving_meters"] = driving_info.get("distance")
            
            if travel_info.get("walking") and dest_index < len(travel_info["walking"]):
                walking_info = travel_info["walking"][dest_index]
                if walking_info:
                    store_dict["travel_time_walking_seconds"] = walking_info.get("duration")
                    store_dict["travel_distance_walking_meters"] = walking_info.get("distance")
            
            dest_index += 1
        
        result.append(StoreWithTravelInfo(**store_dict))
    
    return result


@router.get("/{store_id}/with-travel-info", response_model=StoreWithTravelInfo)
async def get_store_with_travel_info(
    store_id: uuid.UUID,
    session: SessionDep,
    current_user: CurrentVendor,  # Allow both vendors and customers
    user_latitude: float = Query(..., description="User's current latitude"),
    user_longitude: float = Query(..., description="User's current longitude"),
    travel_methods: Optional[str] = Query("driving,walking", description="Comma-separated travel methods (driving, walking)")
):
    """
    Get travel information for a specific store.
    This allows users to see distance and travel time to a particular store.
    """
    # Get the specific store
    store = crud.get_store_by_id(session=session, store_id=store_id)
    if not store:
        raise HTTPException(status_code=404, detail="Store not found")
    
    # Check if store has coordinates
    if store.latitude is None or store.longitude is None:
        # Return store without travel info if no coordinates available
        return StoreWithTravelInfo(**store.model_dump())
    
    # Prepare coordinates for Mapbox API
    user_origin = (user_longitude, user_latitude)  # Mapbox expects (lng, lat)
    destination = (store.longitude, store.latitude)
    
    # Get travel information from Mapbox for single destination
    travel_info = await get_travel_info_multi_profile(user_origin, [destination])
    
    # Combine store data with travel information
    store_dict = store.model_dump()
    
    if travel_info:
        # Add travel information if available
        if travel_info.get("driving") and len(travel_info["driving"]) > 0:
            driving_info = travel_info["driving"][0]
            if driving_info:
                store_dict["travel_time_driving_seconds"] = driving_info.get("duration")
                store_dict["travel_distance_driving_meters"] = driving_info.get("distance")
        
        if travel_info.get("walking") and len(travel_info["walking"]) > 0:
            walking_info = travel_info["walking"][0]
            if walking_info:
                store_dict["travel_time_walking_seconds"] = walking_info.get("duration")
                store_dict["travel_distance_walking_meters"] = walking_info.get("distance")
    
    return StoreWithTravelInfo(**store_dict)

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