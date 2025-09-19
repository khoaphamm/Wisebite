import uuid
from typing import List, Optional
from datetime import datetime
from sqlmodel import SQLModel, Field
from app.models import TransactionMethod, TransactionStatus

# --- Minimal User Schema for embedding (UNCHANGED) ---
class UserReadMinimal(SQLModel):
    id: uuid.UUID
    full_name: Optional[str] = None
    phone_number: str

# --- INPUT SCHEMAS (ADAPTED) ---

class OrderConfirmPickupRequest(SQLModel):
    """
    ADAPTED: Replaces OrderCompletionRequest.
    Used by a Vendor in the Merchant App to confirm a customer has picked up their order.
    This action would trigger the completion of the transaction.
    """
    order_id: uuid.UUID
    payment_method: TransactionMethod = Field(description="The payment method used (e.g., 'cash').")

# --- OUTPUT SCHEMAS (UNCHANGED) ---

class TransactionReadResponse(SQLModel):
    id: uuid.UUID
    order_id: uuid.UUID
    amount: float
    method: TransactionMethod
    status: TransactionStatus
    transaction_date: datetime
    payer: UserReadMinimal
    payee: UserReadMinimal