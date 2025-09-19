import uuid
from fastapi import APIRouter
from app.api.deps import SessionDep, CurrentVendor, CurrentUser
from app import crud
from app.schemas.store import StorePublic, StoreUpdate

router = APIRouter()

@router.get("/me", response_model=StorePublic)
def get_my_store(session: SessionDep, current_vendor: CurrentVendor):
    """ Vendor gets their own store profile. """
    return crud.get_store_by_owner_id(session=session, owner_id=current_vendor.id)

@router.patch("/me", response_model=StorePublic)
def update_my_store(session: SessionDep, current_vendor: CurrentVendor, store_in: StoreUpdate):
    """ Vendor updates their own store profile. """
    db_store = crud.get_store_by_owner_id(session=session, owner_id=current_vendor.id)
    return crud.update_store(session=session, db_store=db_store, store_in=store_in)

@router.get("/{store_id}", response_model=StorePublic)
def get_store_by_id(session: SessionDep, store_id: uuid.UUID):
    """ Get public details of any store by its ID. """
    return crud.get_store_by_id(session=session, store_id=store_id)