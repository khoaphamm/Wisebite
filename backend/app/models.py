import uuid
from typing import Optional, List, Any
from datetime import datetime
from enum import Enum
from sqlmodel import SQLModel, Field, Relationship
from sqlalchemy.sql import func
from geoalchemy2 import Geometry
from sqlalchemy import Column

# --- 1. User and Role Management (ADAPTED) ---

class UserRole(str, Enum):
    """ADAPTED: Roles are now Customer, Vendor, Admin"""
    ADMIN = "admin"
    CUSTOMER = "customer"
    VENDOR = "vendor"

class User(SQLModel, table=True):
    """ADAPTED: User model now links to a Store (for vendors) and Orders (for customers)"""
    id: uuid.UUID = Field(default_factory=uuid.uuid4, primary_key=True)
    full_name: str = Field(max_length=100)
    phone_number: str = Field(unique=True, index=True, max_length=15)
    hashed_password: str = Field(nullable=False)
    email: str = Field(unique=True, max_length=100, index=True)
    role: UserRole = Field(default=UserRole.CUSTOMER)
    avt_url: Optional[str] = Field(default=None)
    is_google_user: bool = Field(default=False)  # Track if user has Google account linked
    created_at: datetime = Field(default_factory=datetime.now)
    updated_at: datetime = Field(default_factory=datetime.now, sa_column_kwargs={"onupdate": func.now()})

    # --- Relationships ---
    store: Optional["Store"] = Relationship(back_populates="owner")
    orders: List["Order"] = Relationship(back_populates="customer")
    reviews: List["Review"] = Relationship(back_populates="user")
    notifications: list["Noti_User"] = Relationship(back_populates="recipient", cascade_delete=True)
    conversations: List["ConversationMember"] = Relationship(back_populates="user", cascade_delete=True)


# --- Password Reset Model (NEW) ---

class PasswordReset(SQLModel, table=True):
    """Store password reset codes with expiration"""
    id: uuid.UUID = Field(default_factory=uuid.uuid4, primary_key=True)
    email: str = Field(max_length=100, index=True)
    reset_code: str = Field(max_length=6)
    expires_at: datetime
    used: bool = Field(default=False)
    created_at: datetime = Field(default_factory=datetime.now)


# --- 2. Vendor-Specific Information (NEW) ---

class Store(SQLModel, table=True):
    """NEW: Represents the profile of a vendor's store."""
    id: uuid.UUID = Field(default_factory=uuid.uuid4, primary_key=True)
    name: str = Field(index=True, max_length=150)
    address: str = Field(max_length=255)
    description: Optional[str] = Field(default=None, max_length=500)
    logo_url: Optional[str] = Field(default=None)
    location: Optional[Any] = Field(sa_column=Column(Geometry(geometry_type="POINT", srid=4326), nullable=True), default=None)
    
    owner_id: uuid.UUID = Field(foreign_key="user.id", unique=True)
    owner: User = Relationship(back_populates="store")

    food_items: List["FoodItem"] = Relationship(back_populates="store")
    surprise_bags: List["SurpriseBag"] = Relationship(back_populates="store")


# --- 3. Product Category System (NEW) ---

class Category(SQLModel, table=True):
    """Hierarchical category system for food items"""
    id: uuid.UUID = Field(default_factory=uuid.uuid4, primary_key=True)
    name: str = Field(max_length=255, index=True)
    parent_category_id: Optional[uuid.UUID] = Field(foreign_key="category.id", default=None)
    description: Optional[str] = Field(default=None, max_length=500)
    is_active: bool = Field(default=True)
    created_at: datetime = Field(default_factory=datetime.now)
    
    # Self-referential relationship for hierarchy
    parent_category: Optional["Category"] = Relationship(
        back_populates="subcategories",
        sa_relationship_kwargs={"remote_side": "Category.id"}
    )
    subcategories: List["Category"] = Relationship(back_populates="parent_category")
    
    # Products in this category
    food_items: List["FoodItem"] = Relationship(back_populates="category")

# --- 4. Enhanced Product Models ---

class FoodItem(SQLModel, table=True):
    """Enhanced food item with better inventory management"""
    id: uuid.UUID = Field(default_factory=uuid.uuid4, primary_key=True)
    name: str = Field(max_length=255, index=True)
    description: Optional[str] = Field(default=None, max_length=500)
    sku: Optional[str] = Field(default=None, max_length=100, index=True)  # Stock Keeping Unit
    image_url: Optional[str] = Field(default=None)
    
    # Pricing
    standard_price: float = Field(ge=0)  # Regular selling price
    cost_price: Optional[float] = Field(default=None, ge=0)  # Cost to store (for profit calculation)
    
    # Product characteristics
    is_fresh: bool = Field(default=True)  # TRUE for fresh groceries, FALSE for packaged goods
    expires_at: Optional[datetime] = Field(default=None)
    
    # Inventory management
    total_quantity: int = Field(ge=0, default=0)  # Total stock
    surplus_quantity: int = Field(ge=0, default=0)  # Available for surprise bags
    reserved_quantity: int = Field(ge=0, default=0)  # Reserved for orders
    available_quantity: int = Field(ge=0, default=0)  # Available for regular sale
    
    # Surplus management
    is_marked_for_surplus: bool = Field(default=False)  # Merchant can tick this
    surplus_discount_percentage: Optional[float] = Field(default=None, ge=0, le=1)
    surplus_price: Optional[float] = Field(default=None, ge=0)
    marked_surplus_at: Optional[datetime] = Field(default=None)
    
    # Additional info
    ingredients: Optional[str] = Field(default=None, max_length=500)
    allergens: Optional[str] = Field(default=None, max_length=200)
    weight: Optional[float] = Field(default=None, ge=0)  # Weight in grams
    unit: Optional[str] = Field(default="piece", max_length=20)  # kg, piece, pack, etc.
    
    # Status
    is_available: bool = Field(default=True)
    is_active: bool = Field(default=True)
    
    # Timestamps
    created_at: datetime = Field(default_factory=datetime.now)
    updated_at: datetime = Field(default_factory=datetime.now, sa_column_kwargs={"onupdate": func.now()})
    last_inventory_update: Optional[datetime] = Field(default=None)

    # Relationships
    store_id: uuid.UUID = Field(foreign_key="store.id", index=True)
    category_id: Optional[uuid.UUID] = Field(foreign_key="category.id", default=None)
    
    store: "Store" = Relationship(back_populates="food_items")
    category: Optional["Category"] = Relationship(back_populates="food_items")
    order_items: List["OrderItem"] = Relationship(back_populates="food_item")
    surprise_bag_items: List["SurpriseBagItem"] = Relationship(back_populates="food_item")
    inventory_logs: List["InventoryLog"] = Relationship(back_populates="food_item")

# --- 5. Inventory Management (NEW) ---

class InventoryLog(SQLModel, table=True):
    """Track inventory changes for better management"""
    id: uuid.UUID = Field(default_factory=uuid.uuid4, primary_key=True)
    food_item_id: uuid.UUID = Field(foreign_key="fooditem.id", index=True)
    
    # Change details
    change_type: str = Field(max_length=50)  # "restock", "sale", "surplus_marked", "expired", etc.
    quantity_change: int  # Positive for additions, negative for reductions
    previous_quantity: int = Field(ge=0)
    new_quantity: int = Field(ge=0)
    
    # Additional context
    reason: Optional[str] = Field(default=None, max_length=255)
    reference_id: Optional[uuid.UUID] = Field(default=None)  # Order ID, surprise bag ID, etc.
    
    # Timestamps
    created_at: datetime = Field(default_factory=datetime.now)
    
    # Relationships
    food_item: "FoodItem" = Relationship(back_populates="inventory_logs")

# --- 6. Enhanced Surprise Bag System ---

class SurpriseBag(SQLModel, table=True):
    """Enhanced surprise bag with better item management"""
    id: uuid.UUID = Field(default_factory=uuid.uuid4, primary_key=True)
    name: str = Field(default="Túi Bất Ngờ", max_length=255)
    description: Optional[str] = Field(default=None, max_length=500)
    
    # Bag type
    bag_type: str = Field(default="combo", max_length=50)  # "single_item", "combo"
    
    # Pricing
    original_value: float = Field(ge=0)  # Total value of items inside
    discounted_price: float = Field(ge=0)  # Price customer pays
    discount_percentage: float = Field(ge=0, le=1)  # Actual discount applied
    
    # Availability
    quantity_available: int = Field(ge=0)
    max_per_customer: int = Field(default=1)
    
    # Time management
    available_from: datetime  # When customers can start ordering
    available_until: datetime  # Last order time
    pickup_start_time: datetime
    pickup_end_time: datetime
    
    # Status
    is_active: bool = Field(default=True)
    is_auto_generated: bool = Field(default=False)  # Generated from surplus items
    
    # Timestamps
    created_at: datetime = Field(default_factory=datetime.now)
    updated_at: datetime = Field(default_factory=datetime.now, sa_column_kwargs={"onupdate": func.now()})

    # Relationships
    store_id: uuid.UUID = Field(foreign_key="store.id", index=True)
    store: "Store" = Relationship(back_populates="surprise_bags")
    
    bag_items: List["SurpriseBagItem"] = Relationship(back_populates="surprise_bag")
    order_items: List["OrderItem"] = Relationship(back_populates="surprise_bag")

class SurpriseBagItem(SQLModel, table=True):
    """Items that can be included in surprise bags"""
    id: uuid.UUID = Field(default_factory=uuid.uuid4, primary_key=True)
    
    surprise_bag_id: uuid.UUID = Field(foreign_key="surprisebag.id")
    food_item_id: uuid.UUID = Field(foreign_key="fooditem.id")
    
    # Quantity range for this item in the bag
    min_quantity: int = Field(gt=0, default=1)
    max_quantity: int = Field(gt=0, default=1)
    
    # Value contribution
    estimated_value_per_unit: float = Field(ge=0)
    weight_in_selection: float = Field(default=1.0, ge=0)  # Priority in selection algorithm
    
    # Relationships
    surprise_bag: "SurpriseBag" = Relationship(back_populates="bag_items")
    food_item: "FoodItem" = Relationship(back_populates="surprise_bag_items")

# --- 7. Previous Product-Related Models (KEEP FOR COMPATIBILITY) ---


# --- 4. Customer Order Management (HEAVILY ADAPTED) ---

class OrderStatus(str, Enum):
    """ADAPTED: Renamed for clarity."""
    PENDING = "pending"  # Add for test compatibility
    PENDING_PAYMENT = "pending_payment"
    CONFIRMED = "confirmed"
    AWAITING_PICKUP = "awaiting_pickup"
    COMPLETED = "completed"
    CANCELLED = "cancelled"

class Order(SQLModel, table=True):
    """ADAPTED: Now linked to a Customer (User) instead of Owner/Collector."""
    id: uuid.UUID = Field(default_factory=uuid.uuid4, primary_key=True)
    customer_id: uuid.UUID = Field(foreign_key="user.id", index=True)
    status: OrderStatus = Field(default=OrderStatus.PENDING)
    total_amount: float | None = Field(default=None)
    delivery_address: Optional[str] = Field(default=None, max_length=500)
    notes: Optional[str] = Field(default=None, max_length=500)
    created_at: datetime = Field(default_factory=datetime.now)
    updated_at: datetime = Field(default_factory=datetime.now, sa_column_kwargs={"onupdate": func.now()})

    customer: "User" = Relationship(back_populates="orders")
    items: List["OrderItem"] = Relationship(back_populates="order")
    review: Optional["Review"] = Relationship(back_populates="order")
    transaction: Optional["Transaction"] = Relationship(back_populates="order")

class OrderItem(SQLModel, table=True):
    """ADAPTED: This connects an Order to either a SurpriseBag or FoodItem."""
    id: uuid.UUID = Field(default_factory=uuid.uuid4, primary_key=True)
    order_id: uuid.UUID = Field(foreign_key="order.id")
    surprise_bag_id: Optional[uuid.UUID] = Field(foreign_key="surprisebag.id", default=None)
    food_item_id: Optional[uuid.UUID] = Field(foreign_key="fooditem.id", default=None)
    
    quantity: int = Field(gt=0)
    price_per_item: float = Field(ge=0)

    order: "Order" = Relationship(back_populates="items")
    surprise_bag: Optional["SurpriseBag"] = Relationship(back_populates="order_items")
    food_item: Optional["FoodItem"] = Relationship(back_populates="order_items")


# --- 5. Ancillary Features (KEPT AND INTEGRATED) ---

class Review(SQLModel, table=True):
    """KEPT: Linked to User and Order, which still exist."""
    id: uuid.UUID = Field(default_factory=uuid.uuid4, primary_key=True)
    user_id: uuid.UUID = Field(foreign_key="user.id", index=True)
    order_id: uuid.UUID = Field(foreign_key="order.id", index=True, unique=True)
    rating: int = Field(ge=1, le=5)  
    comment: str | None = Field(default=None, max_length=500, nullable=True)
    created_at: datetime = Field(default_factory=datetime.now)

    user: "User" = Relationship(back_populates="reviews")
    order: "Order" = Relationship(back_populates="review")

class TransactionMethod(str, Enum):
    CASH = "cash"
    WALLET = "wallet"
    CREDIT_CARD = "credit_card"

class TransactionStatus(str, Enum):
    SUCCESSFUL = "successful"
    FAILED = "failed"
    PENDING = "pending"

class Transaction(SQLModel, table=True):
    """KEPT: Linked to our new Order and User models."""
    id: uuid.UUID = Field(default_factory=uuid.uuid4, primary_key=True)
    order_id: Optional[uuid.UUID] = Field(foreign_key="order.id", index=True, default=None) 
    payer_id: uuid.UUID = Field(foreign_key="user.id", index=True) # This will be the customer
    payee_id: uuid.UUID = Field(foreign_key="user.id", index=True) # This will be the vendor
    amount: float = Field(ge=0)
    method: TransactionMethod
    status: TransactionStatus
    transaction_date: datetime = Field(default_factory=datetime.now)

    order: Optional["Order"] = Relationship(back_populates="transaction")
    payer: "User" = Relationship(sa_relationship_kwargs={"foreign_keys": "Transaction.payer_id"})
    payee: "User" = Relationship(sa_relationship_kwargs={"foreign_keys": "Transaction.payee_id"})

class Notification(SQLModel, table=True):
    """KEPT: No changes needed."""
    id: uuid.UUID = Field(default_factory=uuid.uuid4, primary_key=True, index=True)
    title: str
    message: str
    is_important: bool = Field(default=False)
    created_at: datetime = Field(default_factory=datetime.now)
    updated_at: datetime = Field(default_factory=datetime.now, sa_column_kwargs={"onupdate": func.now()})

    recipients: List["Noti_User"] = Relationship(back_populates="notification", cascade_delete=True)

class Noti_User(SQLModel, table=True):
    """KEPT: No changes needed."""
    notification_id: uuid.UUID = Field(foreign_key="notification.id", ondelete="CASCADE", primary_key=True)
    user_id: uuid.UUID = Field(foreign_key="user.id", primary_key=True, ondelete="CASCADE")
    is_read: bool = Field(default=False)
    created_at: datetime = Field(default_factory=datetime.now)

    notification: "Notification" = Relationship(back_populates="recipients")
    recipient: "User" = Relationship(back_populates="notifications")

class ConversationType(str, Enum):
    PRIVATE = "private"
    GROUP = "group"

class Conversation(SQLModel, table=True):
    """KEPT: No changes needed."""
    id: uuid.UUID = Field(default_factory=uuid.uuid4, primary_key=True, index=True)
    name: str | None = Field(default=None, max_length=100, nullable=True)
    type: ConversationType = Field(default=ConversationType.PRIVATE, max_length=20)
    last_message_id: uuid.UUID | None = Field(default=None, foreign_key="message.id", index=True)
    created_at: datetime = Field(default_factory=datetime.now)
    updated_at: datetime = Field(default_factory=datetime.now, sa_column_kwargs={"onupdate": func.now()}, index=True)

    messages: List["Message"] = Relationship(back_populates="conversation", cascade_delete=True, sa_relationship_kwargs={"foreign_keys": "Message.conversation_id"})
    members: List["ConversationMember"] = Relationship(back_populates="conversation", cascade_delete=True)
    last_message: "Message" = Relationship(sa_relationship_kwargs={"foreign_keys": "Conversation.last_message_id"})

class ConversationMember(SQLModel, table=True):
    """KEPT: No changes needed."""
    conversation_id: uuid.UUID = Field(foreign_key="conversation.id", primary_key=True)
    user_id: uuid.UUID = Field(foreign_key="user.id", primary_key=True)
    unread_count: int = Field(default=0)

    conversation: "Conversation" = Relationship(back_populates="members", sa_relationship_kwargs={"foreign_keys": "ConversationMember.conversation_id"})
    user: "User" = Relationship(back_populates="conversations", sa_relationship_kwargs={"foreign_keys": "ConversationMember.user_id"})

class Message(SQLModel, table=True):
    """KEPT: No changes needed."""
    id: uuid.UUID = Field(default_factory=uuid.uuid4, primary_key=True, index=True)
    conversation_id: uuid.UUID = Field(foreign_key="conversation.id", index=True)
    sender_id: uuid.UUID = Field(foreign_key="user.id", index=True)
    content: str
    created_at: datetime = Field(default_factory=datetime.now, index=True)
    is_deleted: bool = Field(default=False)
    deleted_at: datetime | None = Field(default=None)

    conversation: "Conversation" = Relationship(back_populates="messages", sa_relationship_kwargs={"foreign_keys": "Message.conversation_id"})
    sender: "User" = Relationship(sa_relationship_kwargs={"foreign_keys": "Message.sender_id"})