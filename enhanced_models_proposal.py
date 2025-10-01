# Enhanced Data Models for Client Requirements

import uuid
from typing import Optional, List, Any
from datetime import datetime, time
from enum import Enum
from sqlmodel import SQLModel, Field, Relationship
from sqlalchemy.sql import func
from geoalchemy2 import Geometry
from sqlalchemy import Column

# --- Enhanced Store and Product Categories ---

class StoreType(str, Enum):
    """Store types based on client requirements"""
    MINIMART = "minimart"  # Backhoaxanh, co.op food, winmart, satra food, king food, aeon citimart
    CONVENIENCE_STORE = "convenience_store"

class ProductMainCategory(str, Enum):
    """Main product categories"""
    FRESH_GROCERY = "fresh_grocery"  # thịt, cá, rau củ, trái cây, bánh mì tươi
    PACKAGED_GOODS = "packaged_goods"  # bánh, kẹo, sữa

class FreshGrocerySubCategory(str, Enum):
    """Fresh grocery subcategories"""
    MEAT_FISH = "meat_fish"  # Thịt/cá
    VEGETABLES = "vegetables"  # Rau/củ
    FRUITS = "fruits"  # Trái cây
    FRESH_BREAD = "fresh_bread"  # Bánh mì

class PackagedGoodsSubCategory(str, Enum):
    """Packaged goods subcategories"""
    BAKERY_SNACKS = "bakery_snacks"  # Bánh/Snack
    CANDY = "candy"  # Kẹo
    BEVERAGES_DAIRY = "beverages_dairy"  # Nước/Sữa

class SurpriseBagType(str, Enum):
    """Surprise bag types"""
    FRESH_GROCERY_COMBO = "fresh_grocery_combo"
    MEAT_FISH_ONLY = "meat_fish_only"
    VEGETABLES_ONLY = "vegetables_only"
    FRUITS_ONLY = "fruits_only"
    FRESH_BREAD_ONLY = "fresh_bread_only"
    PACKAGED_GOODS_COMBO = "packaged_goods_combo"
    BAKERY_SNACKS_ONLY = "bakery_snacks_only"
    CANDY_ONLY = "candy_only"
    BEVERAGES_DAIRY_ONLY = "beverages_dairy_only"

# --- Enhanced Store Model ---

class Store(SQLModel, table=True):
    """Enhanced store model with client requirements"""
    id: uuid.UUID = Field(default_factory=uuid.uuid4, primary_key=True)
    name: str = Field(index=True, max_length=150)
    address: str = Field(max_length=255)
    description: Optional[str] = Field(default=None, max_length=500)
    logo_url: Optional[str] = Field(default=None)
    location: Optional[Any] = Field(sa_column=Column(Geometry(geometry_type="POINT", srid=4326), nullable=True), default=None)
    
    # New fields for client requirements
    store_type: StoreType = Field(default=StoreType.MINIMART)
    is_active: bool = Field(default=False)  # Store must activate to start selling
    
    # Operating hours for surplus product input and sales
    surplus_input_start: time = Field(default=time(10, 0))  # 10:00 AM
    surplus_input_end: time = Field(default=time(12, 0))    # 12:00 PM
    order_start: time = Field(default=time(14, 0))          # 2:00 PM  
    order_end: time = Field(default=time(18, 0))            # 6:00 PM
    pickup_start: time = Field(default=time(14, 0))         # 2:00 PM
    pickup_end: time = Field(default=time(20, 0))           # 8:00 PM
    
    # Commission settings
    platform_commission_rate: float = Field(default=0.05)  # 5% platform commission
    minimum_discount_rate: float = Field(default=0.45)     # Minimum 45% discount
    
    owner_id: uuid.UUID = Field(foreign_key="user.id", unique=True)
    owner: "User" = Relationship(back_populates="store")
    
    food_items: List["FoodItem"] = Relationship(back_populates="store")
    surprise_bags: List["SurpriseBag"] = Relationship(back_populates="store")
    daily_surplus_reports: List["DailySurplusReport"] = Relationship(back_populates="store")

# --- Enhanced Product Models ---

class FoodItem(SQLModel, table=True):
    """Enhanced food item with detailed categorization"""
    id: uuid.UUID = Field(default_factory=uuid.uuid4, primary_key=True)
    name: str = Field(max_length=100)
    product_code: Optional[str] = Field(default=None, max_length=50)  # Store's internal product code
    description: Optional[str] = Field(default=None, max_length=500)
    image_url: Optional[str] = Field(default=None)
    
    # Pricing
    original_price: float = Field(ge=0)
    current_surplus_price: Optional[float] = Field(default=None, ge=0)
    discount_percentage: Optional[float] = Field(default=None, ge=0, le=1)
    
    # Inventory
    total_quantity: int = Field(ge=0, default=0)
    surplus_quantity: int = Field(ge=0, default=0)  # Daily surplus quantity
    
    # Categories
    main_category: ProductMainCategory
    fresh_subcategory: Optional[FreshGrocerySubCategory] = Field(default=None)
    packaged_subcategory: Optional[PackagedGoodsSubCategory] = Field(default=None)
    
    # Time management
    expires_at: Optional[datetime] = Field(default=None)
    surplus_added_at: Optional[datetime] = Field(default=None)  # When surplus was added
    
    # Additional info
    ingredients: Optional[str] = Field(default=None, max_length=500)
    allergens: Optional[str] = Field(default=None, max_length=200)
    is_available: bool = Field(default=True)
    is_surplus_available: bool = Field(default=False)

    store_id: uuid.UUID = Field(foreign_key="store.id")
    store: "Store" = Relationship(back_populates="food_items")
    order_items: List["OrderItem"] = Relationship(back_populates="food_item")
    surplus_bag_items: List["SurpluseBagItem"] = Relationship(back_populates="food_item")

# --- Enhanced Surprise Bag Models ---

class SurpriseBag(SQLModel, table=True):
    """Enhanced surprise bag with category-specific structure"""
    id: uuid.UUID = Field(default_factory=uuid.uuid4, primary_key=True)
    name: str = Field(max_length=100)
    description: Optional[str] = Field(default=None, max_length=500)
    
    # Bag type and category
    bag_type: SurpriseBagType
    main_category: ProductMainCategory
    
    # Pricing
    original_value: float = Field(ge=0)
    discounted_price: float = Field(ge=0)
    discount_percentage: float = Field(ge=0, le=1)
    
    # Availability
    quantity_available: int = Field(ge=0)
    max_orders_per_customer: int = Field(default=1)
    
    # Time windows
    available_from: datetime  # When customers can start ordering
    available_until: datetime  # Last order time
    pickup_start_time: datetime
    pickup_end_time: datetime
    
    # Auto-generation settings
    is_auto_generated: bool = Field(default=False)  # Generated from surplus
    generation_rules: Optional[str] = Field(default=None)  # JSON rules for auto-generation
    
    store_id: uuid.UUID = Field(foreign_key="store.id")
    store: "Store" = Relationship(back_populates="surprise_bags")
    
    bag_items: List["SurpriseBagItem"] = Relationship(back_populates="surprise_bag")
    order_items: List["OrderItem"] = Relationship(back_populates="surprise_bag")

# --- New: Surprise Bag Contents ---

class SurpriseBagItem(SQLModel, table=True):
    """Items included in a surprise bag"""
    id: uuid.UUID = Field(default_factory=uuid.uuid4, primary_key=True)
    
    surprise_bag_id: uuid.UUID = Field(foreign_key="surprisebag.id")
    food_item_id: uuid.UUID = Field(foreign_key="fooditem.id")
    
    quantity_per_bag: int = Field(gt=0)
    estimated_value: float = Field(ge=0)  # Estimated value of this item in the bag
    
    surprise_bag: "SurpriseBag" = Relationship(back_populates="bag_items")
    food_item: "FoodItem" = Relationship(back_populates="surplus_bag_items")

# --- New: Daily Surplus Management ---

class DailySurplusReport(SQLModel, table=True):
    """Daily surplus report for store management"""
    id: uuid.UUID = Field(default_factory=uuid.uuid4, primary_key=True)
    
    store_id: uuid.UUID = Field(foreign_key="store.id")
    report_date: datetime = Field(default_factory=datetime.now)
    
    # Summary statistics
    total_surplus_items: int = Field(default=0)
    total_surplus_value: float = Field(default=0)
    total_potential_revenue: float = Field(default=0)
    
    # Status
    input_completed: bool = Field(default=False)
    surplus_bags_generated: bool = Field(default=False)
    
    # Timestamps
    input_started_at: Optional[datetime] = Field(default=None)
    input_completed_at: Optional[datetime] = Field(default=None)
    
    store: "Store" = Relationship(back_populates="daily_surplus_reports")

# --- Enhanced Order Management ---

class OrderStatus(str, Enum):
    """Enhanced order status"""
    PENDING = "pending"
    CONFIRMED = "confirmed"
    PREPARING = "preparing"  # Store is preparing the order
    READY_FOR_PICKUP = "ready_for_pickup"
    COMPLETED = "completed"
    CANCELLED = "cancelled"
    NO_SHOW = "no_show"  # Customer didn't pick up

class Order(SQLModel, table=True):
    """Enhanced order with pickup time management"""
    id: uuid.UUID = Field(default_factory=uuid.uuid4, primary_key=True)
    order_number: str = Field(unique=True, index=True)  # Human-readable order number
    
    customer_id: uuid.UUID = Field(foreign_key="user.id", index=True)
    store_id: uuid.UUID = Field(foreign_key="store.id", index=True)  # Direct link to store
    
    status: OrderStatus = Field(default=OrderStatus.PENDING)
    
    # Pricing
    subtotal: float = Field(default=0)
    platform_fee: float = Field(default=0)
    total_amount: float = Field(default=0)
    
    # Pickup management
    selected_pickup_time: datetime  # Customer's selected pickup time
    actual_pickup_time: Optional[datetime] = Field(default=None)
    pickup_window_start: datetime  # Start of pickup window
    pickup_window_end: datetime    # End of pickup window
    
    # Additional info
    notes: Optional[str] = Field(default=None, max_length=500)
    special_instructions: Optional[str] = Field(default=None, max_length=500)
    
    # Timestamps
    created_at: datetime = Field(default_factory=datetime.now)
    updated_at: datetime = Field(default_factory=datetime.now, sa_column_kwargs={"onupdate": func.now()})
    confirmed_at: Optional[datetime] = Field(default=None)
    
    # Relationships
    customer: "User" = Relationship(back_populates="orders")
    store: "Store" = Relationship(back_populates="orders")
    items: List["OrderItem"] = Relationship(back_populates="order")
    review: Optional["Review"] = Relationship(back_populates="order")
    transaction: Optional["Transaction"] = Relationship(back_populates="order")

# --- Enhanced Order Items ---

class OrderItem(SQLModel, table=True):
    """Enhanced order item"""
    id: uuid.UUID = Field(default_factory=uuid.uuid4, primary_key=True)
    
    order_id: uuid.UUID = Field(foreign_key="order.id")
    surprise_bag_id: Optional[uuid.UUID] = Field(foreign_key="surprisebag.id", default=None)
    food_item_id: Optional[uuid.UUID] = Field(foreign_key="fooditem.id", default=None)
    
    quantity: int = Field(gt=0)
    unit_price: float = Field(ge=0)
    total_price: float = Field(ge=0)
    original_price: float = Field(ge=0)  # Original price before discount
    discount_amount: float = Field(ge=0)
    
    # Item status
    is_available: bool = Field(default=True)
    substitution_notes: Optional[str] = Field(default=None)
    
    # Relationships
    order: "Order" = Relationship(back_populates="items")
    surprise_bag: Optional["SurpriseBag"] = Relationship(back_populates="order_items")
    food_item: Optional["FoodItem"] = Relationship(back_populates="order_items")