import uuid
from typing import List
from fastapi import APIRouter, HTTPException, status
from app.api.deps import SessionDep, CurrentUser, CurrentVendor
from app import crud
from app.schemas.order import OrderCreate, OrderPublic
from app.schemas.transaction import OrderConfirmPickupRequest
from app.schemas.review import ReviewCreate, ReviewPublic
from app.models import Review, OrderStatus

router = APIRouter()

@router.post("/", response_model=OrderPublic, status_code=status.HTTP_201_CREATED)
def create_order(session: SessionDep, current_user: CurrentUser, order_in: OrderCreate):
    """ CUSTOMER: Creates a new order from a list of surprise bags in their cart. """
    try:
        return crud.create_order(session=session, order_create=order_in, customer_id=current_user.id)
    except ValueError as e:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=str(e))

@router.get("/me", response_model=List[OrderPublic])
def get_my_orders(session: SessionDep, current_user: CurrentUser):
    """ CUSTOMER: Get their own order history. """
    return crud.get_orders_by_customer(session=session, customer_id=current_user.id)

@router.get("/vendor/me", response_model=List[OrderPublic])
def get_orders_for_my_store(session: SessionDep, current_vendor: CurrentVendor):
    """ VENDOR: Get all incoming orders for their store. """
    # Note: A new crud function 'get_orders_by_vendor' will be needed.
    # For now, this is a placeholder.
    raise HTTPException(status_code=status.HTTP_501_NOT_IMPLEMENTED, detail="CRUD function not yet implemented")

@router.post("/{order_id}/confirm-pickup")
def confirm_pickup(session: SessionDep, current_vendor: CurrentVendor, order_id: uuid.UUID, pickup_in: OrderConfirmPickupRequest):
    """ VENDOR: Confirms a customer has picked up the order. This completes the order and transaction. """
    return crud.confirm_order_pickup_and_pay(session=session, order_id=order_id, vendor=current_vendor, pickup_data=pickup_in)

@router.post("/{order_id}/review", response_model=ReviewPublic)
def review_order(session: SessionDep, current_user: CurrentUser, order_id: uuid.UUID, review_in: ReviewCreate):
    """ CUSTOMER: Reviews a completed order. """
    order = crud.get_order_by_id(session=session, order_id=order_id)
    if not order or order.customer_id != current_user.id:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Order not found or you are not the owner.")
    if order.status != OrderStatus.COMPLETED:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Can only review completed orders.")
    if order.review:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Order has already been reviewed.")
        
    db_review = Review.model_validate(review_in, update={"user_id": current_user.id, "order_id": order_id})
    session.add(db_review)
    session.commit()
    session.refresh(db_review)
    return db_review