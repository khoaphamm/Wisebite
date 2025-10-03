import uuid
import logging
from typing import List, Optional
from fastapi import APIRouter, HTTPException, status
from app.api.deps import SessionDep, CurrentUser, CurrentVendor
from app import crud
from app.schemas.order import OrderCreate, OrderPublic, OrderStatusUpdate
from app.schemas.common import PaginationResponse
from app.schemas.transaction import OrderConfirmPickupRequest
from app.schemas.review import ReviewCreate, ReviewPublic
from app.models import Review, OrderStatus

logger = logging.getLogger(__name__)

router = APIRouter()

@router.post("/", response_model=OrderPublic, status_code=status.HTTP_201_CREATED)
def create_order(session: SessionDep, current_user: CurrentUser, order_in: OrderCreate):
    """ CUSTOMER: Creates a new order from a list of surprise bags in their cart. """
    try:
        return crud.create_order(session=session, order_create=order_in, customer_id=current_user.id)
    except ValueError as e:
        error_msg = str(e)
        if "not found" in error_msg:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=error_msg)
        elif "not enough stock" in error_msg or "insufficient" in error_msg.lower():
            raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=error_msg)
        else:
            raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=error_msg)

@router.get("/me", response_model=PaginationResponse[OrderPublic])
def get_my_orders(session: SessionDep, current_user: CurrentUser):
    """ CUSTOMER: Get their own order history. """
    orders = crud.get_orders_by_customer(session=session, customer_id=current_user.id)
    return PaginationResponse[OrderPublic](data=orders, count=len(orders))

@router.get("/my-orders", response_model=PaginationResponse[OrderPublic])
def get_my_orders_legacy(session: SessionDep, current_user: CurrentUser):
    """ CUSTOMER: Get their own order history (legacy endpoint). """
    orders = crud.get_orders_by_customer(session=session, customer_id=current_user.id)
    return PaginationResponse[OrderPublic](data=orders, count=len(orders))

@router.get("/vendor/me", response_model=PaginationResponse[OrderPublic])
def get_orders_for_my_store(session: SessionDep, current_vendor: CurrentVendor):
    """ VENDOR: Get all incoming orders for their store. """
    logger.info(f"Getting orders for vendor: {current_vendor.id}")
    
    store = crud.get_store_by_owner_id(session=session, owner_id=current_vendor.id)
    if not store:
        logger.warning(f"No store found for vendor: {current_vendor.id}")
        return PaginationResponse[OrderPublic](data=[], count=0)
    
    logger.info(f"Found store: {store.id} - {store.name}")
    orders = crud.get_orders_by_store(session=session, store_id=store.id)
    logger.info(f"Found {len(orders)} orders for store {store.id}")
    
    # Convert to OrderPublic explicitly
    order_publics = []
    for i, order in enumerate(orders):
        logger.info(f"Order {i+1}: ID={order.id}")
        logger.info(f"  Customer: {order.customer}")
        logger.info(f"  Customer ID: {order.customer_id}")
        logger.info(f"  Items count: {len(order.items) if order.items else 0}")
        
        # Convert Order to OrderPublic manually
        try:
            order_public = OrderPublic(
                id=order.id,
                customer_id=order.customer_id,
                status=order.status,
                total_amount=order.total_amount,
                created_at=order.created_at,
                delivery_address=order.delivery_address,
                notes=order.notes,
                preferred_pickup_time=order.preferred_pickup_time,
                customer=order.customer,  # This should include the full customer object
                items=order.items  # This should include the full items with relationships
            )
            order_publics.append(order_public)
            logger.info(f"  Successfully converted order {order.id}")
            logger.info(f"  OrderPublic customer: {order_public.customer}")
            logger.info(f"  OrderPublic items: {order_public.items}")
        except Exception as e:
            logger.error(f"  Failed to convert order {order.id}: {e}")
            import traceback
            logger.error(traceback.format_exc())
    
    return PaginationResponse[OrderPublic](data=order_publics, count=len(order_publics))

@router.get("/store/{store_id}", response_model=PaginationResponse[OrderPublic])
def get_orders_by_store(session: SessionDep, current_vendor: CurrentVendor, store_id: uuid.UUID):
    """ VENDOR: Get all orders for a specific store (if they own it). """
    store = crud.get_store_by_owner_id(session=session, owner_id=current_vendor.id)
    if not store or store.id != store_id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not authorized to view orders for this store"
        )
    orders = crud.get_orders_by_store(session=session, store_id=store_id)
    return PaginationResponse[OrderPublic](data=orders, count=len(orders))

@router.get("/{order_id}", response_model=OrderPublic)
def get_order(session: SessionDep, current_user: CurrentUser, order_id: uuid.UUID):
    """ Get a specific order by ID. """
    order = crud.get_order_by_id(session=session, order_id=order_id)
    if not order:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Order not found"
        )
    
    # Check if user has access to this order
    if order.customer_id != current_user.id:
        # Check if user is vendor of the store that has items in this order
        if hasattr(current_user, 'role') and current_user.role == "vendor":
            store = crud.get_store_by_owner_id(session=session, owner_id=current_user.id)
            if not store or not crud.order_belongs_to_store(session=session, order_id=order_id, store_id=store.id):
                raise HTTPException(
                    status_code=status.HTTP_403_FORBIDDEN,
                    detail="Not authorized to view this order"
                )
        else:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Not authorized to view this order"
            )
    
    return order

@router.patch("/{order_id}/status", response_model=OrderPublic)
def update_order_status(
    session: SessionDep,
    current_vendor: CurrentVendor,
    order_id: uuid.UUID,
    status_update: OrderStatusUpdate
):
    """ VENDOR: Update order status. """
    order = crud.get_order_by_id(session=session, order_id=order_id)
    if not order:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Order not found"
        )
    
    # Check if vendor owns the store for this order
    store = crud.get_store_by_owner_id(session=session, owner_id=current_vendor.id)
    if not store or not crud.order_belongs_to_store(session=session, order_id=order_id, store_id=store.id):
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not authorized to update this order"
        )
    
    return crud.update_order_status(session=session, order_id=order_id, new_status=status_update.status)

@router.patch("/{order_id}/cancel", response_model=OrderPublic)
@router.post("/{order_id}/cancel", response_model=OrderPublic)
def cancel_order(session: SessionDep, current_user: CurrentUser, order_id: uuid.UUID):
    """ CUSTOMER: Cancel their order. """
    order = crud.get_order_by_id(session=session, order_id=order_id)
    if not order:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Order not found"
        )
    
    # Check if user owns the order
    if order.customer_id != current_user.id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not authorized to cancel this order"
        )
    
    # Check if order can be cancelled
    if order.status in [OrderStatus.COMPLETED, OrderStatus.CANCELLED]:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Order cannot be cancelled"
        )
    
    return crud.cancel_order(session=session, order_id=order_id)

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