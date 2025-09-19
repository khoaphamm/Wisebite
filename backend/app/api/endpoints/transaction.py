from fastapi import APIRouter, Depends, Query, HTTPException, status
from sqlmodel import Session, select
from app.api.deps import SessionDep, CurrentUser, CurrentVendor
from app.models import Transaction, Order, TransactionStatus
from app.schemas.transaction import (
    TransactionCreate,
    TransactionPublic,
    TransactionReadResponse,
    RefundCreate,
    VendorTransactionSummary
)
from app.schemas.common import PaginationResponse
from app import crud
from typing import List, Optional
from datetime import datetime
import uuid

router = APIRouter()

@router.post("/", response_model=TransactionPublic, status_code=status.HTTP_201_CREATED)
def create_payment_transaction(
    session: SessionDep,
    current_user: CurrentUser,
    transaction_in: TransactionCreate
):
    """Create a payment transaction for an order."""
    # Get the order
    order = crud.get_order_by_id(session=session, order_id=transaction_in.order_id)
    if not order:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Order not found"
        )
    
    # Check if user owns the order
    if order.customer_id != current_user.id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not authorized to pay for this order"
        )
    
    # Check if order is already paid
    existing_payment = crud.get_payment_transaction_by_order(session=session, order_id=order.id)
    if existing_payment:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Order is already paid"
        )
    
    # Validate amount matches order total
    if abs(transaction_in.amount - order.total_amount) > 0.01:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Amount mismatch with order total"
        )
    
    transaction = crud.create_payment_transaction(
        session=session,
        transaction_create=transaction_in,
        customer_id=current_user.id,
        order_id=order.id
    )
    
    # Convert to TransactionPublic response
    return TransactionPublic(
        id=transaction.id,
        order_id=transaction.order_id,
        customer_id=transaction.payer_id,
        vendor_id=transaction.payee_id,
        amount=transaction.amount,
        payment_method=transaction.method.value,
        status="completed" if transaction.status == TransactionStatus.SUCCESSFUL else transaction.status.value,
        transaction_type="payment",
        created_at=transaction.transaction_date,
        payment_details=None,
        original_transaction_id=None,
        reason=None
    )

@router.post("/refund", response_model=TransactionPublic, status_code=status.HTTP_201_CREATED)
def create_refund_transaction(
    session: SessionDep,
    current_user: CurrentUser,
    refund_in: RefundCreate
):
    """Create a refund transaction."""
    # Get the original transaction
    original_transaction = crud.get_transaction(session=session, transaction_id=refund_in.transaction_id)
    if not original_transaction:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Original transaction not found"
        )
    
    # Check if user owns the transaction
    if original_transaction.payer_id != current_user.id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not authorized to refund this transaction"
        )
    
    # Check if refund is allowed
    if original_transaction.status != TransactionStatus.SUCCESSFUL:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Can only refund completed transactions"
        )
    
    refund_transaction = crud.create_refund_transaction(
        session=session,
        refund_create=refund_in,
        original_transaction=original_transaction
    )
    
    # Convert to TransactionPublic response
    return TransactionPublic(
        id=refund_transaction.id,
        order_id=refund_transaction.order_id,
        customer_id=refund_transaction.payee_id,  # For refunds, customer receives money
        vendor_id=refund_transaction.payer_id,   # For refunds, vendor pays money
        amount=refund_transaction.amount,
        payment_method=refund_transaction.method.value,
        status="completed" if refund_transaction.status == TransactionStatus.SUCCESSFUL else refund_transaction.status.value,
        transaction_type="refund",
        created_at=refund_transaction.transaction_date,
        payment_details=None,
        original_transaction_id=original_transaction.id,
        reason=refund_in.reason
    )

@router.get("/me", response_model=PaginationResponse[TransactionPublic])
def get_my_transactions(
    session: SessionDep,
    current_user: CurrentUser,
    skip: int = Query(0, ge=0),
    limit: int = Query(20, ge=1, le=100),
    transaction_type: Optional[str] = Query(None),
    start_date: Optional[datetime] = Query(None),
    end_date: Optional[datetime] = Query(None)
):
    """Get current user's transaction history with optional filtering."""
    transactions = crud.get_user_transactions(
        session=session,
        user_id=current_user.id,
        skip=skip,
        limit=limit,
        transaction_type=transaction_type,
        start_date=start_date,
        end_date=end_date
    )
    
    # Convert transactions to TransactionPublic
    transaction_data = []
    for transaction in transactions:
        transaction_data.append(TransactionPublic(
            id=transaction.id,
            order_id=transaction.order_id,
            customer_id=transaction.payer_id,
            vendor_id=transaction.payee_id,
            amount=transaction.amount,
            payment_method=transaction.method.value,
            status="completed" if transaction.status == TransactionStatus.SUCCESSFUL else transaction.status.value,
            transaction_type="payment",  # For now, assume all are payment transactions
            created_at=transaction.transaction_date,
            payment_details=None,
            original_transaction_id=None,
            reason=None
        ))
    
    # Count total transactions for pagination
    total_count = crud.count_user_transactions(
        session=session,
        user_id=current_user.id,
        transaction_type=transaction_type,
        start_date=start_date,
        end_date=end_date
    )
    
    return PaginationResponse[TransactionPublic](
        data=transaction_data,
        count=total_count,
        skip=skip,
        limit=limit
    )

@router.get("/vendor/summary", response_model=VendorTransactionSummary)
def get_vendor_transaction_summary(
    session: SessionDep,
    current_vendor: CurrentVendor,
    store_id: Optional[uuid.UUID] = Query(None),
    start_date: Optional[datetime] = Query(None),
    end_date: Optional[datetime] = Query(None)
):
    """Get vendor's transaction summary and financial overview."""
    # If no store_id provided, use vendor's store
    if not store_id:
        store = crud.get_store_by_owner_id(session=session, owner_id=current_vendor.id)
        if not store:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Vendor must have a store"
            )
        store_id = store.id
    else:
        # Check if vendor owns the store
        store = crud.get_store(session=session, store_id=store_id)
        if not store or store.owner_id != current_vendor.id:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Not authorized to view this store's transactions"
            )
    
    return crud.get_vendor_transaction_summary(
        session=session,
        store_id=store_id,
        start_date=start_date,
        end_date=end_date
    )

@router.get("/{transaction_id}", response_model=TransactionPublic)
def get_transaction(session: SessionDep, current_user: CurrentUser, transaction_id: uuid.UUID):
    """Get a specific transaction by ID."""
    transaction = crud.get_transaction(session=session, transaction_id=transaction_id)
    if not transaction:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Transaction not found"
        )
    
    # Check if user has access to this transaction
    if transaction.payer_id != current_user.id and transaction.payee_id != current_user.id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not authorized to view this transaction"
        )
    
    # Convert to TransactionPublic response
    return TransactionPublic(
        id=transaction.id,
        order_id=transaction.order_id,
        customer_id=transaction.payer_id,
        vendor_id=transaction.payee_id,
        amount=transaction.amount,
        payment_method=transaction.method.value,
        status="completed" if transaction.status == TransactionStatus.SUCCESSFUL else transaction.status.value,
        transaction_type="payment",  # For now, assume all are payment transactions
        created_at=transaction.transaction_date,
        payment_details=None,
        original_transaction_id=None,
        reason=None
    )

# Legacy endpoints for backward compatibility
@router.get("/user/{user_id}", response_model=List[TransactionPublic])
def get_transactions_by_user_legacy(
    user_id: uuid.UUID,
    session: SessionDep,
    current_user: CurrentUser,
    limit: int = Query(20, ge=1, le=100),
    offset: int = Query(0, ge=0)
):
    """Legacy endpoint for getting user transactions."""
    # Only allow access to your own transactions
    if current_user.id != user_id:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Not authorized to view these transactions.")
    
    return crud.get_user_transactions(
        session=session,
        user_id=user_id,
        skip=offset,
        limit=limit
    )

@router.get("/order/{order_id}", response_model=List[TransactionPublic])
def get_transactions_by_order_legacy(
    order_id: uuid.UUID,
    session: SessionDep,
    current_user: CurrentUser
):
    """Legacy endpoint for getting transactions by order."""
    order = crud.get_order_by_id(session=session, order_id=order_id)
    if not order:
        raise HTTPException(status_code=404, detail="Order not found")
    
    # Check if user has access
    if current_user.id != order.customer_id:
        # Check if user is payer/payee in any transaction for this order
        transactions = crud.get_transactions_by_order(session=session, order_id=order_id)
        user_transactions = [
            tx for tx in transactions 
            if tx.payer_id == current_user.id or tx.payee_id == current_user.id
        ]
        if not user_transactions:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN, 
                detail="Not authorized to view transactions for this order."
            )
        return user_transactions
    
    # If owner, return all transactions for the order
    return crud.get_transactions_by_order(session=session, order_id=order_id)
