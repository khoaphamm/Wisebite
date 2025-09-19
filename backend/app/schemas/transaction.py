import uuid
from typing import List, Optional, Dict, Any
from datetime import datetime
from sqlmodel import SQLModel, Field
from app.models import TransactionMethod, TransactionStatus

# --- Minimal User Schema for embedding (UNCHANGED) ---
class UserReadMinimal(SQLModel):
    id: uuid.UUID
    full_name: Optional[str] = None
    phone_number: str

# --- INPUT SCHEMAS (ENHANCED) ---

class TransactionCreate(SQLModel):
    """Create a new payment transaction."""
    order_id: uuid.UUID
    amount: float = Field(gt=0)
    payment_method: str
    payment_details: Optional[Dict[str, Any]] = None

class RefundCreate(SQLModel):
    """Create a refund transaction."""
    transaction_id: uuid.UUID
    amount: float = Field(gt=0)
    reason: str

class OrderConfirmPickupRequest(SQLModel):
    """
    ADAPTED: Replaces OrderCompletionRequest.
    Used by a Vendor in the Merchant App to confirm a customer has picked up their order.
    This action would trigger the completion of the transaction.
    """
    order_id: uuid.UUID
    payment_method: TransactionMethod = Field(description="The payment method used (e.g., 'cash').")

# --- OUTPUT SCHEMAS (ENHANCED) ---

class TransactionPublic(SQLModel):
    """Public transaction response."""
    id: uuid.UUID
    order_id: Optional[uuid.UUID] = None
    customer_id: Optional[uuid.UUID] = None
    vendor_id: Optional[uuid.UUID] = None
    amount: float
    payment_method: str
    status: str
    transaction_type: str  # payment, refund, payout
    created_at: datetime
    payment_details: Optional[Dict[str, Any]] = None
    original_transaction_id: Optional[uuid.UUID] = None  # For refunds
    reason: Optional[str] = None  # For refunds

class VendorTransactionSummary(SQLModel):
    """Vendor financial summary."""
    total_revenue: float
    total_transactions: int
    pending_payouts: float
    completed_transactions: int
    refunded_amount: float
    date_range: Dict[str, str]

class TransactionReadResponse(SQLModel):
    """Legacy transaction response."""
    id: uuid.UUID
    order_id: uuid.UUID
    amount: float
    method: TransactionMethod
    status: TransactionStatus
    transaction_date: datetime
    payer: UserReadMinimal
    payee: UserReadMinimal