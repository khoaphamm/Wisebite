from typing import List
import uuid
from fastapi import APIRouter
from app.api.deps import SessionDep, CurrentVendor
from app import crud
from app.schemas.surprise_bag import SurpriseBagCreate, SurpriseBagPublic

router = APIRouter()

@router.post("/", response_model=SurpriseBagPublic)
def create_surprise_bag(session: SessionDep, current_vendor: CurrentVendor, bag_in: SurpriseBagCreate):
    """ Vendor creates a new Surprise Bag for their store. """
    store = crud.get_store_by_owner_id(session=session, owner_id=current_vendor.id)
    return crud.create_surprise_bag(session=session, bag_create=bag_in, store_id=store.id)

@router.get("/", response_model=List[SurpriseBagPublic])
def get_all_active_bags(session: SessionDep):
    """ CUSTOMER: Get a list of all available surprise bags from all stores. """
    return crud.get_all_active_surprise_bags(session=session)

@router.get("/{bag_id}", response_model=SurpriseBagPublic)
def get_bag_by_id(session: SessionDep, bag_id: uuid.UUID):
    """ CUSTOMER: Get details of a specific surprise bag. """
    return crud.get_surprise_bag_by_id(session=session, bag_id=bag_id)