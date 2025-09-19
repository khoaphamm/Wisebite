from typing import List
from fastapi import APIRouter
from app.api.deps import SessionDep, CurrentVendor
from app import crud
from app.schemas.food_item import FoodItemCreate, FoodItemPublic

router = APIRouter()

@router.post("/", response_model=FoodItemPublic)
def create_food_item(session: SessionDep, current_vendor: CurrentVendor, item_in: FoodItemCreate):
    """ Vendor creates a new regular food item for their store. """
    store = crud.get_store_by_owner_id(session=session, owner_id=current_vendor.id)
    return crud.create_food_item(session=session, item_create=item_in, store_id=store.id)

@router.get("/my-items", response_model=List[FoodItemPublic])
def get_my_food_items(session: SessionDep, current_vendor: CurrentVendor):
    """ Vendor lists all regular food items for their store. """
    store = crud.get_store_by_owner_id(session=session, owner_id=current_vendor.id)
    return crud.get_food_items_by_store(session=session, store_id=store.id)