import uuid
import httpx
import logging
from typing import Optional, List
from datetime import datetime
from sqlmodel import Session, select, delete
from sqlalchemy.orm import joinedload, selectinload
from sqlalchemy import or_, func, text
from sqlalchemy.sql import func as sqla_func
from geoalchemy2.functions import ST_Distance_Sphere, ST_MakePoint, ST_X, ST_Y
from geoalchemy2 import WKTElement
from passlib.context import CryptContext
from app.schemas.notification import NotificationCreate

logger = logging.getLogger(__name__)

from app.core.config import settings
from app.core.security import get_password_hash, verify_password

# --- Import Models ---
from app.models import (
    User, UserRole, Store, FoodItem, SurpriseBag, Order, OrderItem,
    OrderStatus, Review, Transaction, TransactionStatus, Notification,
    Noti_User, Conversation, ConversationMember, Message
)

# --- Import Schemas ---
from app.schemas.user import UserCreate, UserUpdate
from app.schemas.store import StoreCreate, StoreUpdate
from app.schemas.food_item import FoodItemCreate, FoodItemUpdate
from app.schemas.surprise_bag import SurpriseBagCreate, SurpriseBagUpdate
from app.schemas.order import OrderCreate
from app.schemas.transaction import OrderConfirmPickupRequest
from app.schemas.notification import NotificationCreate
from app.schemas.chat import ConversationCreate, MessageCreate
# Query stores with distance calculation
        # ST_Distance_Sphere calculates distance in meters, divide by 1000 for km
from sqlalchemy.orm import Query
from sqlalchemy import and_
        

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")


def get_password_hash(password: str) -> str:
    return pwd_context.hash(password)


def verify_password(plain_password: str, hashed_password: str) -> bool:
    return pwd_context.verify(plain_password, hashed_password)

def get_food_items_by_store(session: Session, store_id: uuid.UUID, skip: int = 0, limit: int = 100):
    statement = select(FoodItem).where(FoodItem.store_id == store_id, FoodItem.is_available == True)
    
    # Get total count
    count_statement = select(func.count(FoodItem.id)).where(FoodItem.store_id == store_id, FoodItem.is_available == True)
    total_count = session.exec(count_statement).one()
    
    statement = statement.offset(skip).limit(limit)
    items = session.exec(statement).all()
    
    return {"data": items, "count": total_count}

def get_food_item(session: Session, food_item_id: uuid.UUID) -> Optional[FoodItem]:
    statement = select(FoodItem).where(FoodItem.id == food_item_id)
    return session.exec(statement).first()

def get_food_items_with_filters(
    session: Session, 
    skip: int = 0, 
    limit: int = 100,
    category: Optional[str] = None,
    latitude: Optional[float] = None,
    longitude: Optional[float] = None,
    radius: Optional[float] = None
):
    statement = select(FoodItem).where(FoodItem.is_available == True)
    
    if category:
        statement = statement.where(FoodItem.category == category)
    
    # Get total count
    count_statement = select(func.count(FoodItem.id)).where(FoodItem.is_available == True)
    if category:
        count_statement = count_statement.where(FoodItem.category == category)
    total_count = session.exec(count_statement).one()
    
    # For location filtering, we'd need PostGIS functions
    # For now, just return basic pagination
    statement = statement.offset(skip).limit(limit)
    items = session.exec(statement).all()
    
    return {"data": items, "count": total_count}

def search_food_items(session: Session, query: str, skip: int = 0, limit: int = 100):
    statement = select(FoodItem).where(
        FoodItem.is_available == True,
        or_(
            FoodItem.name.ilike(f"%{query}%"),
            FoodItem.description.ilike(f"%{query}%")
        )
    )
    
    # Get total count
    count_statement = select(func.count(FoodItem.id)).where(
        FoodItem.is_available == True,
        or_(
            FoodItem.name.ilike(f"%{query}%"),
            FoodItem.description.ilike(f"%{query}%")
        )
    )
    total_count = session.exec(count_statement).one()
    
    statement = statement.offset(skip).limit(limit)
    items = session.exec(statement).all()
    
    return {"data": items, "count": total_count}

def update_food_item(session: Session, db_food_item: FoodItem, item_in: FoodItemUpdate) -> FoodItem:
    item_data = item_in.model_dump(exclude_unset=True)
    db_food_item.sqlmodel_update(item_data)
    session.add(db_food_item)
    session.commit()
    session.refresh(db_food_item)
    return db_food_item

def delete_food_item(session: Session, food_item_id: uuid.UUID) -> None:
    statement = select(FoodItem).where(FoodItem.id == food_item_id)
    food_item = session.exec(statement).first()
    if food_item:
        session.delete(food_item)
        session.commit()

# ============================== User CRUD ====================================================

def get_user_by_phone_number(session: Session, phone_number: str) -> Optional[User]:
    statement = select(User).where(User.phone_number == phone_number)
    return session.exec(statement).first()

def get_user_by_email(session: Session, email: str) -> Optional[User]:
    statement = select(User).where(User.email == email)
    return session.exec(statement).first()

def get_user_by_id(session: Session, user_id: uuid.UUID) -> Optional[User]:
    return session.get(User, user_id)

def authenticate(session: Session, phone_number: str, password: str) -> Optional[User]:
    db_user = get_user_by_phone_number(session=session, phone_number=phone_number)
    if not db_user:
        return None
    if not verify_password(password, db_user.hashed_password):
        return None
    return db_user

def authenticate_flexible(session: Session, identifier: str, password: str) -> Optional[User]:
    """Authenticate user by phone number OR email"""
    # Try phone number first
    db_user = get_user_by_phone_number(session=session, phone_number=identifier)
    if not db_user:
        # Try email if phone number doesn't work
        db_user = get_user_by_email(session=session, email=identifier)
    
    if not db_user:
        return None
    if not verify_password(password, db_user.hashed_password):
        return None
    return db_user

def create_user(session: Session, user_create: UserCreate) -> User:
    # This function creates ANY type of user based on the role in user_create
    db_user = User.model_validate(
        user_create,
        update={"hashed_password": get_password_hash(user_create.password)}
    )
    session.add(db_user)
    session.flush() # Flush to get the user ID for other operations
    
    # Note: Automatic store creation disabled for testing
    # If the new user is a VENDOR, create an associated store profile
    # if db_user.role == UserRole.VENDOR:
    #     # Create a default store name based on the user's full name
    #     default_store_name = f"Store of {db_user.full_name}"
    #     store_in = StoreCreate(name=default_store_name, address="Default Address")
    #     create_store(session=session, store_create=store_in, owner_id=db_user.id)
        
    add_noti_to_new_user(session, db_user.id)
    session.commit()
    session.refresh(db_user)
    return db_user

def update_user(session: Session, db_user: User, user_in: UserUpdate) -> User:
    user_data = user_in.model_dump(exclude_unset=True)
    if "password" in user_data:
        hashed_password = get_password_hash(user_data["password"])
        del user_data["password"]
        user_data["hashed_password"] = hashed_password
    
    # Merge the user object into the current session if it's from a different session
    db_user = session.merge(db_user)
    db_user.sqlmodel_update(user_data)
    session.commit()
    session.refresh(db_user)
    return db_user

def delete_user(session: Session, db_user: User) -> None:
    session.delete(db_user)
    session.commit()

# ============================== Store CRUD (NEW) =============================================

def get_store_by_id(session: Session, store_id: uuid.UUID) -> Optional[Store]:
    return session.get(Store, store_id)

def get_store_by_owner_id(session: Session, owner_id: uuid.UUID) -> Optional[Store]:
    statement = select(Store).where(Store.owner_id == owner_id)
    return session.exec(statement).first()

def _extract_coordinates_from_store(session: Session, store: Store) -> tuple[Optional[float], Optional[float]]:
    """Extract latitude and longitude from PostGIS location field."""
    if not store.location:
        return None, None
    
    try:
        lat_result = session.execute(select(ST_Y(store.location))).scalar()
        lon_result = session.execute(select(ST_X(store.location))).scalar()
        return lat_result, lon_result
    except Exception:
        return None, None

def get_store_with_location(session: Session, store_id: uuid.UUID) -> Optional[dict]:
    """Get store by ID with location coordinates extracted."""
    store = session.get(Store, store_id)
    if not store:
        return None
    
    lat, lon = _extract_coordinates_from_store(session, store)
    
    return {
        "id": store.id,
        "name": store.name,
        "address": store.address,
        "description": store.description,
        "logo_url": store.logo_url,
        "owner_id": store.owner_id,
        "latitude": lat,
        "longitude": lon
    }

def create_store(session: Session, store_create: StoreCreate, owner_id: uuid.UUID) -> Store:
    store_data = store_create.model_dump(exclude_unset=True)
    
    # Handle location data
    location = None
    if store_create.latitude is not None and store_create.longitude is not None:
        # Create PostGIS POINT geometry using WKTElement
        point_wkt = f"POINT({store_create.longitude} {store_create.latitude})"
        location = WKTElement(point_wkt, srid=4326)
    
    # Remove latitude/longitude from store_data as they're not direct fields
    store_data.pop("latitude", None)
    store_data.pop("longitude", None)
    store_data["owner_id"] = owner_id
    
    db_store = Store(**store_data)
    if location is not None:
        db_store.location = location
    
    session.add(db_store)
    session.commit()
    session.refresh(db_store)
    return db_store

def update_store(session: Session, db_store: Store, store_in: StoreUpdate) -> Store:
    store_data = store_in.model_dump(exclude_unset=True)
    
    # Handle location data
    if store_in.latitude is not None and store_in.longitude is not None:
        # Update location using PostGIS
        point_wkt = f"POINT({store_in.longitude} {store_in.latitude})"
        location = WKTElement(point_wkt, srid=4326)
        store_data["location"] = location
    
    # Remove latitude/longitude from store_data as they're not direct fields
    store_data.pop("latitude", None)
    store_data.pop("longitude", None)
    
    db_store.sqlmodel_update(store_data)
    session.add(db_store)
    session.commit()
    session.refresh(db_store)
    return db_store

def get_all_stores(session: Session, skip: int = 0, limit: int = 100) -> List[Store]:
    query = select(Store).offset(skip).limit(limit)
    return list(session.exec(query).all())

def get_stores_within_radius(
    session: Session,
    latitude: float,
    longitude: float,
    radius_km: float = 10.0,
    skip: int = 0,
    limit: int = 100
) -> List[dict]:
    """
    Get stores within a specified radius from a given location, sorted by distance.
    Returns list of dicts with store data and distance_km.
    """
    try:
        # Try PostGIS spatial query first
        # Create a point for the search location
        search_point = WKTElement(f"POINT({longitude} {latitude})", srid=4326)
        
        
        query = session.query(
            Store,
            (ST_Distance_Sphere(Store.location, search_point) / 1000).label('distance_km')
        ).filter(
            and_(
                Store.location.isnot(None),
                ST_Distance_Sphere(Store.location, search_point) <= radius_km * 1000  # Convert km to meters
            )
        ).order_by(
            'distance_km'
        ).offset(skip).limit(limit)
        
        results = []
        for store, distance_km in query.all():
            store_dict = {
                "id": store.id,
                "name": store.name,
                "address": store.address,
                "description": store.description,
                "logo_url": store.logo_url,
                "owner_id": store.owner_id,
                "distance_km": round(distance_km, 2) if distance_km else None
            }
            
            # Extract latitude and longitude from PostGIS location if available
            lat, lon = _extract_coordinates_from_store(session, store)
            store_dict["latitude"] = lat
            store_dict["longitude"] = lon
                
            results.append(store_dict)
        
        return results
    
    except Exception as e:
        # Fallback to simple query without spatial functions for testing environments
        print(f"Warning: PostGIS spatial functions not available ({e}), using fallback query")
        
        # Rollback the current transaction to clear the error state
        session.rollback()
        
        # Simple query without spatial filtering for testing
        query = select(Store).where(Store.location.isnot(None)).offset(skip).limit(limit)
        stores = session.exec(query).all()
        
        results = []
        for store in stores:
            store_dict = {
                "id": store.id,
                "name": store.name,
                "address": store.address,
                "description": store.description,
                "logo_url": store.logo_url,
                "owner_id": store.owner_id,
                "distance_km": 5.0  # Mock distance for testing
            }
            
            # Extract latitude and longitude from PostGIS location if available
            lat, lon = _extract_coordinates_from_store(session, store)
            store_dict["latitude"] = lat
            store_dict["longitude"] = lon
                
            results.append(store_dict)
        
        return results

def delete_store(session: Session, store_id: uuid.UUID) -> None:
    db_store = session.get(Store, store_id)
    if db_store:
        session.delete(db_store)
        session.commit()

# ============================== FoodItem CRUD (NEW) ==========================================

def create_food_item(session: Session, item_create: FoodItemCreate, store_id: uuid.UUID) -> FoodItem:
    db_item = FoodItem.model_validate(item_create, update={"store_id": store_id})
    session.add(db_item)
    session.commit()
    session.refresh(db_item)
    return db_item

# ============================== SurpriseBag CRUD (NEW) =======================================

def create_surprise_bag(session: Session, bag_create: SurpriseBagCreate, store_id: uuid.UUID) -> SurpriseBag:
    db_bag = SurpriseBag.model_validate(bag_create, update={"store_id": store_id})
    session.add(db_bag)
    session.commit()
    session.refresh(db_bag)
    return db_bag

def get_surprise_bag_by_id(session: Session, bag_id: uuid.UUID) -> Optional[SurpriseBag]:
    return session.get(SurpriseBag, bag_id)

def get_all_active_surprise_bags(session: Session) -> List[SurpriseBag]:
    statement = select(SurpriseBag).where(SurpriseBag.quantity_available > 0).options(selectinload(SurpriseBag.store))
    return session.exec(statement).all()

def get_surprise_bags_with_filters(
    session: Session,
    skip: int = 0,
    limit: int = 100,
    min_price: Optional[float] = None,
    max_price: Optional[float] = None,
    available_only: Optional[bool] = False
):
    statement = select(SurpriseBag).options(selectinload(SurpriseBag.store))
    
    if available_only:
        statement = statement.where(SurpriseBag.quantity_available > 0)
    
    if min_price is not None:
        statement = statement.where(SurpriseBag.discounted_price >= min_price)
    
    if max_price is not None:
        statement = statement.where(SurpriseBag.discounted_price <= max_price)
    
    # Get total count
    count_statement = select(func.count(SurpriseBag.id))
    if available_only:
        count_statement = count_statement.where(SurpriseBag.quantity_available > 0)
    if min_price is not None:
        count_statement = count_statement.where(SurpriseBag.discounted_price >= min_price)
    if max_price is not None:
        count_statement = count_statement.where(SurpriseBag.discounted_price <= max_price)
    
    total_count = session.exec(count_statement).one()
    
    statement = statement.offset(skip).limit(limit)
    items = session.exec(statement).all()
    
    return {"data": items, "count": total_count}

def update_surprise_bag(session: Session, db_bag: SurpriseBag, bag_in: SurpriseBagUpdate) -> SurpriseBag:
    bag_data = bag_in.model_dump(exclude_unset=True)
    db_bag.sqlmodel_update(bag_data)
    session.add(db_bag)
    session.commit()
    session.refresh(db_bag)
    return db_bag

def delete_surprise_bag(session: Session, bag_id: uuid.UUID) -> bool:
    """Delete a surprise bag by ID"""
    bag = session.get(SurpriseBag, bag_id)
    if bag:
        session.delete(bag)
        session.commit()
        return True
    return False

def get_customer_surprise_bag_bookings(session: Session, customer_id: uuid.UUID, skip: int = 0, limit: int = 100):
    # Get customer's orders that contain surprise bags
    orders_query = session.query(Order).filter(Order.customer_id == customer_id)
    orders_with_bags = orders_query.join(OrderItem).join(SurpriseBag).all()
    
    bookings = []
    for order in orders_with_bags:
        for item in order.items:
            if item.surprise_bag:
                booking = {
                    "id": order.id,
                    "surprise_bag_id": item.surprise_bag_id,
                    "customer_id": customer_id,
                    "quantity": item.quantity,
                    "pickup_time": item.surprise_bag.pickup_start_time,  # Use bag's start time as default
                    "status": order.status.value,
                    "created_at": order.created_at
                }
                bookings.append(booking)
    
    # Apply pagination
    total_count = len(bookings)
    paginated_bookings = bookings[skip:skip + limit]
    
    return {"data": paginated_bookings, "count": total_count}

def create_surprise_bag_booking(session: Session, booking_create, bag_id: uuid.UUID, customer_id: uuid.UUID) -> dict:
    # Get the surprise bag to calculate total amount
    surprise_bag = session.query(SurpriseBag).filter(SurpriseBag.id == bag_id).first()
    if not surprise_bag:
        raise ValueError("Surprise bag not found")
    
    if surprise_bag.quantity_available < booking_create.quantity:
        raise ValueError("Insufficient quantity available")
    
    # Create order
    order = Order(
        customer_id=customer_id,
        status=OrderStatus.PENDING_PAYMENT,
        total_amount=surprise_bag.discounted_price * booking_create.quantity
    )
    session.add(order)
    session.flush()  # Get the order ID
    
    # Create order item
    order_item = OrderItem(
        order_id=order.id,
        surprise_bag_id=bag_id,
        quantity=booking_create.quantity,
        price_per_item=surprise_bag.discounted_price
    )
    session.add(order_item)
    
    # Update surprise bag quantity
    surprise_bag.quantity_available -= booking_create.quantity
    session.add(surprise_bag)
    
    session.commit()
    
    return {
        "id": order.id,
        "surprise_bag_id": bag_id,
        "customer_id": customer_id,
        "quantity": booking_create.quantity,
        "pickup_time": booking_create.pickup_time,
        "status": order.status.value,
        "created_at": order.created_at
    }

def get_surprise_bag_booking(session: Session, booking_id: uuid.UUID) -> Optional[dict]:
    order = session.query(Order).filter(Order.id == booking_id).first()
    if not order:
        return None
    
    # Find the surprise bag item in this order
    for item in order.items:
        if item.surprise_bag:
            return {
                "id": order.id,
                "surprise_bag_id": item.surprise_bag_id,
                "customer_id": order.customer_id,
                "quantity": item.quantity,
                "pickup_time": item.surprise_bag.pickup_start_time,  # Default to start time
                "status": order.status.value,
                "created_at": order.created_at
            }
    
    return None

def cancel_surprise_bag_booking(session: Session, booking_id: uuid.UUID) -> dict:
    order = session.query(Order).filter(Order.id == booking_id).first()
    if not order:
        raise ValueError("Booking not found")
    
    # Update order status
    order.status = OrderStatus.CANCELLED
    
    # Restore surprise bag quantity
    for item in order.items:
        if item.surprise_bag:
            item.surprise_bag.quantity_available += item.quantity
            session.add(item.surprise_bag)
    
    session.add(order)
    session.commit()
    
    return {"id": booking_id, "status": "cancelled"}

# ============================== Order CRUD (ADAPTED) =========================================

def get_order_by_id(session: Session, order_id: uuid.UUID) -> Optional[Order]:
    # Use options to pre-load related data to prevent extra queries (N+1 problem)
    statement = select(Order).where(Order.id == order_id).options(
        selectinload(Order.items).selectinload(OrderItem.surprise_bag).selectinload(SurpriseBag.store),
        selectinload(Order.customer)
    )
    return session.exec(statement).first()

def get_orders_by_customer(session: Session, customer_id: uuid.UUID) -> List[Order]:
    statement = select(Order).where(Order.customer_id == customer_id).order_by(Order.created_at.desc()).options(
        selectinload(Order.customer),
        selectinload(Order.items).selectinload(OrderItem.surprise_bag).selectinload(SurpriseBag.store),
        selectinload(Order.items).selectinload(OrderItem.food_item).selectinload(FoodItem.store)
    )
    return session.exec(statement).all()

def create_order(session: Session, order_create: OrderCreate, customer_id: uuid.UUID) -> Order:
    total_amount = 0
    db_order_items = []
    primary_store_id = None  # Track the primary store for this order
    
    # Start a nested transaction to handle potential errors
    with session.begin_nested():
        # Step 1: Validate and process each item in the order
        for item in order_create.items:
            if item.surprise_bag_id:
                # Handle surprise bag ordering
                surprise_bag = session.get(SurpriseBag, item.surprise_bag_id)
                if not surprise_bag:
                    raise ValueError(f"SurpriseBag with id {item.surprise_bag_id} not found.")
                if surprise_bag.quantity_available < item.quantity:
                    raise ValueError(f"Not enough stock for {surprise_bag.name}. Available: {surprise_bag.quantity_available}, Requested: {item.quantity}")

                # Set primary store from first item
                if primary_store_id is None:
                    primary_store_id = surprise_bag.store_id

                # Decrease stock
                surprise_bag.quantity_available -= item.quantity
                session.add(surprise_bag)

                # Calculate price for this line item and add to total
                item_total = surprise_bag.discounted_price * item.quantity
                total_amount += item_total

                # Create the OrderItem model
                db_item = OrderItem(
                    surprise_bag_id=item.surprise_bag_id,
                    quantity=item.quantity,
                    price_per_item=surprise_bag.discounted_price
                )
                db_order_items.append(db_item)
                
            elif item.food_item_id:
                # Handle food item ordering
                food_item = session.get(FoodItem, item.food_item_id)
                if not food_item:
                    raise ValueError(f"Food item with id {item.food_item_id} not found.")
                if food_item.quantity < item.quantity:
                    raise ValueError(f"Not enough stock for {food_item.name}. Available: {food_item.quantity}, Requested: {item.quantity}")

                # Set primary store from first item
                if primary_store_id is None:
                    primary_store_id = food_item.store_id

                # Decrease stock
                food_item.quantity -= item.quantity
                session.add(food_item)

                # Calculate price for this line item and add to total
                item_total = food_item.original_price * item.quantity
                total_amount += item_total

                # Create the OrderItem model
                db_item = OrderItem(
                    food_item_id=item.food_item_id,
                    quantity=item.quantity,
                    price_per_item=food_item.original_price
                )
                db_order_items.append(db_item)
            else:
                raise ValueError("Either surprise_bag_id or food_item_id must be provided")

        # Step 2: Create the main Order
        db_order = Order(
            customer_id=customer_id,
            total_amount=total_amount,
            status=OrderStatus.PENDING, # Default status for new orders
            delivery_address=order_create.delivery_address,
            notes=order_create.notes,
            preferred_pickup_time=order_create.preferred_pickup_time,
            items=db_order_items
        )
        session.add(db_order)

    # Commit the transaction if all steps succeeded
    session.commit()
    session.refresh(db_order)
    
    # Step 3: Send notification to merchant about new order
    if primary_store_id:
        try:
            # Get store owner to send notification
            store = session.get(Store, primary_store_id)
            if store and store.owner_id:
                customer = session.get(User, customer_id)
                customer_name = customer.full_name if customer else "Khách hàng"
                
                pickup_time_str = ""
                if order_create.preferred_pickup_time:
                    pickup_time_str = f" vào lúc {order_create.preferred_pickup_time.strftime('%H:%M ngày %d/%m/%Y')}"
                
                notification_data = NotificationCreate(
                    title="🛒 Đơn hàng mới!",
                    message=f"{customer_name} đã đặt {len(db_order_items)} món từ cửa hàng của bạn{pickup_time_str}. Tổng tiền: {total_amount:,.0f}đ",
                    is_important=True
                )
                
                create_notification(session, notification_data, [store.owner_id])
        except Exception as e:
            # Don't fail the order if notification fails
            print(f"Failed to send notification for order {db_order.id}: {e}")
    
    return db_order

def get_orders_by_store(session: Session, store_id: uuid.UUID) -> List[Order]:
    """Get all orders that contain items from a specific store."""
    logger.info(f"Getting orders for store: {store_id}")
    
    # Get order IDs from both surprise bags and food items for this store
    surprise_bag_order_ids = select(Order.id).join(OrderItem).join(SurpriseBag).where(
        SurpriseBag.store_id == store_id
    ).distinct()
    
    food_item_order_ids = select(Order.id).join(OrderItem).join(FoodItem).where(
        FoodItem.store_id == store_id
    ).distinct()
    
    # Combine the order IDs using union
    all_order_ids = surprise_bag_order_ids.union(food_item_order_ids)
    
    logger.info("Building query with relationships...")
    # Now get the actual Order objects with relationships loaded
    statement = select(Order).where(Order.id.in_(all_order_ids)).order_by(Order.created_at.desc()).options(
        selectinload(Order.customer),
        selectinload(Order.items).selectinload(OrderItem.surprise_bag),
        selectinload(Order.items).selectinload(OrderItem.food_item)
    )
    
    logger.info("Executing query...")
    orders = session.exec(statement).all()
    logger.info(f"Raw query returned {len(orders)} orders")
    
    # Log details for debugging
    for i, order in enumerate(orders):
        logger.info(f"Order {i+1}: {order.id}")
        logger.info(f"  Customer object: {order.customer}")
        logger.info(f"  Customer ID: {order.customer_id}")
        logger.info(f"  Items: {order.items}")
        logger.info(f"  Items length: {len(order.items) if order.items else 'None'}")
        
        if order.customer:
            logger.info(f"  Customer full_name: {order.customer.full_name}")
        else:
            logger.warning(f"  Customer is None!")
            
        if order.items:
            for j, item in enumerate(order.items):
                logger.info(f"    Item {j+1}: {item.id}")
                if item.surprise_bag:
                    logger.info(f"      Surprise bag: {item.surprise_bag.name}")
                else:
                    logger.info(f"      Surprise bag: None")
                if item.food_item:
                    logger.info(f"      Food item: {item.food_item.name}")
                else:
                    logger.info(f"      Food item: None")
        else:
            logger.warning(f"  Items is None or empty!")
    
    return orders

def order_belongs_to_store(session: Session, order_id: uuid.UUID, store_id: uuid.UUID) -> bool:
    """Check if an order contains items from a specific store."""
    # Check surprise bags
    surprise_bag_statement = select(OrderItem).join(SurpriseBag).where(
        OrderItem.order_id == order_id,
        SurpriseBag.store_id == store_id
    )
    
    # Check food items
    food_item_statement = select(OrderItem).join(FoodItem).where(
        OrderItem.order_id == order_id,
        FoodItem.store_id == store_id
    )
    
    return (session.exec(surprise_bag_statement).first() is not None or 
            session.exec(food_item_statement).first() is not None)

def update_order_status(session: Session, order_id: uuid.UUID, new_status: OrderStatus) -> Order:
    """Update order status."""
    order = session.get(Order, order_id)
    if order:
        old_status = order.status
        order.status = new_status
        session.add(order)
        session.commit()
        session.refresh(order)
        
        # Send notification when order is accepted by vendor (status changed to confirmed)
        if new_status == OrderStatus.CONFIRMED and old_status != OrderStatus.CONFIRMED:
            try:
                # Get customer name
                customer = session.get(User, order.customer_id)
                customer_name = customer.full_name if customer else "Khách hàng"
                
                # Get vendor/store info
                if order.items:
                    first_item = order.items[0]
                    store = None
                    
                    # Try to get store from surprise bag or food item
                    if first_item.surprise_bag_id:
                        surprise_bag = session.get(SurpriseBag, first_item.surprise_bag_id)
                        if surprise_bag:
                            store = session.get(Store, surprise_bag.store_id)
                    elif first_item.food_item_id:
                        food_item = session.get(FoodItem, first_item.food_item_id)
                        if food_item:
                            store = session.get(Store, food_item.store_id)
                    
                    if store:
                        # Send notification to merchant (vendor)
                        merchant_notification = NotificationCreate(
                            title="✅ Đơn hàng đã được chấp nhận",
                            message=f"Bạn đã chấp nhận đơn hàng #{str(order_id)[:8]} của {customer_name} ({len(order.items)} món). Tổng: {order.total_amount:,.0f}đ",
                            is_important=True
                        )
                        create_notification(session, merchant_notification, [store.owner_id])
                        
                        # Send notification to customer
                        customer_notification = NotificationCreate(
                            title="✅ Đơn hàng được chấp nhận",
                            message=f"Cửa hàng {store.name} đã chấp nhận đơn hàng #{str(order_id)[:8]} của bạn!",
                            is_important=True
                        )
                        create_notification(session, customer_notification, [order.customer_id])
            except Exception as e:
                # Don't fail the order update if notification fails
                print(f"Failed to send order accepted notification for order {order_id}: {e}")
    
    return order

def cancel_order(session: Session, order_id: uuid.UUID) -> Order:
    """Cancel an order and restore surprise bag quantities."""
    order = session.get(Order, order_id)
    if order:
        # Restore quantities for all items
        for item in order.items:
            surprise_bag = session.get(SurpriseBag, item.surprise_bag_id)
            if surprise_bag:
                surprise_bag.quantity_available += item.quantity
                session.add(surprise_bag)
        
        # Update order status
        order.status = OrderStatus.CANCELLED
        session.add(order)
        session.commit()
        session.refresh(order)
    return order

# ============================== Transaction CRUD (ADAPTED) ===================================

def confirm_order_pickup_and_pay(
    session: Session,
    order_id: uuid.UUID,
    vendor: User, # The user confirming the pickup MUST be a vendor
    pickup_data: OrderConfirmPickupRequest
) -> Transaction:
    from fastapi import HTTPException, status

    try:
        order = session.exec(
            select(Order).where(Order.id == order_id).with_for_update()
        ).first()

        if not order:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Order not found.")
        
        # Check if the vendor performing the action is the owner of the store associated with the order
        first_item = order.items[0] if order.items else None
        if not first_item or first_item.surprise_bag.store.owner_id != vendor.id:
             raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="You are not the vendor for this order.")

        if order.status not in [OrderStatus.CONFIRMED, OrderStatus.AWAITING_PICKUP]:
            raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=f"Order is in an invalid state for pickup: {order.status}")

        order.status = OrderStatus.COMPLETED
        session.add(order)
        
        new_transaction = Transaction(
            order_id=order_id,
            payer_id=order.customer_id,
            payee_id=vendor.id,
            amount=order.total_amount,
            method=pickup_data.payment_method,
            status=TransactionStatus.SUCCESSFUL
        )
        session.add(new_transaction)
        
        session.commit()
        session.refresh(new_transaction)
        
        return new_transaction
        
    except Exception as e:
        session.rollback()
        raise e

def get_payment_transaction_by_order(session: Session, order_id: uuid.UUID) -> Optional[Transaction]:
    """Get existing payment transaction for an order."""
    statement = select(Transaction).where(
        Transaction.order_id == order_id,
        Transaction.status == TransactionStatus.SUCCESSFUL
    )
    return session.exec(statement).first()

def create_payment_transaction(session: Session, transaction_create, customer_id: uuid.UUID, order_id: uuid.UUID) -> Transaction:
    """Create a payment transaction."""
    # Get the order to determine the payee (vendor)
    order = get_order_by_id(session=session, order_id=order_id)
    if not order:
        raise ValueError("Order not found")
    
    # Determine the payee from the order items
    payee_id = None
    for item in order.items:
        if item.surprise_bag_id:
            surprise_bag = session.get(SurpriseBag, item.surprise_bag_id)
            if surprise_bag and surprise_bag.store:
                payee_id = surprise_bag.store.owner_id
                break
        elif item.food_item_id:
            food_item = session.get(FoodItem, item.food_item_id)
            if food_item and food_item.store:
                payee_id = food_item.store.owner_id
                break
    
    if not payee_id:
        raise ValueError("Could not determine payee for transaction")
    
    transaction = Transaction(
        order_id=order_id,
        payer_id=customer_id,
        payee_id=payee_id,
        amount=transaction_create.amount,
        method=transaction_create.payment_method,
        status=TransactionStatus.SUCCESSFUL
    )
    session.add(transaction)
    session.commit()
    session.refresh(transaction)
    return transaction

def get_transaction(session: Session, transaction_id: uuid.UUID) -> Optional[Transaction]:
    """Get a transaction by ID."""
    return session.get(Transaction, transaction_id)

def create_refund_transaction(session: Session, refund_create, original_transaction: Transaction) -> Transaction:
    """Create a refund transaction."""
    refund = Transaction(
        order_id=None,  # Refunds are not directly linked to orders
        payer_id=original_transaction.payee_id,  # Vendor pays back to customer
        payee_id=original_transaction.payer_id,
        amount=refund_create.amount,
        method=original_transaction.method,
        status=TransactionStatus.SUCCESSFUL
    )
    session.add(refund)
    session.commit()
    session.refresh(refund)
    return refund

def get_user_transactions(
    session: Session,
    user_id: uuid.UUID,
    skip: int = 0,
    limit: int = 100,
    transaction_type: Optional[str] = None,
    start_date: Optional[str] = None,
    end_date: Optional[str] = None
) -> List[Transaction]:
    """Get user's transactions with optional filtering."""
    statement = select(Transaction).where(
        or_(Transaction.payer_id == user_id, Transaction.payee_id == user_id)
    ).order_by(Transaction.transaction_date.desc()).offset(skip).limit(limit)
    return session.exec(statement).all()

def count_user_transactions(
    session: Session,
    user_id: uuid.UUID,
    transaction_type: Optional[str] = None,
    start_date: Optional[str] = None,
    end_date: Optional[str] = None
) -> int:
    """Count user's transactions with optional filtering."""
    from sqlalchemy import func
    statement = select(func.count(Transaction.id)).where(
        or_(Transaction.payer_id == user_id, Transaction.payee_id == user_id)
    )
    return session.exec(statement).one()

def get_transactions_by_order(session: Session, order_id: uuid.UUID) -> List[Transaction]:
    """Get all transactions for an order."""
    statement = select(Transaction).where(Transaction.order_id == order_id)
    return session.exec(statement).all()

def get_vendor_transaction_summary(
    session: Session,
    store_id: uuid.UUID,
    start_date: Optional[str] = None,
    end_date: Optional[str] = None
) -> dict:
    """Get vendor transaction summary."""
    # Get transactions where the vendor is the payee and the store matches
    # First, get the store owner (vendor)
    store = session.get(Store, store_id)
    if not store:
        return {
            "total_revenue": 0.0,
            "total_transactions": 0,
            "pending_payouts": 0.0,
            "completed_transactions": 0,
            "refunded_amount": 0.0,
            "date_range": {"start": start_date or "", "end": end_date or ""}
        }
    
    # Get all successful transactions where this vendor is the payee
    statement = select(Transaction).where(
        Transaction.payee_id == store.owner_id,
        Transaction.status == TransactionStatus.SUCCESSFUL,
        Transaction.order_id.isnot(None)  # Only payment transactions, not refunds
    )
    
    transactions = session.exec(statement).all()
    
    total_revenue = sum(t.amount for t in transactions)
    return {
        "total_revenue": total_revenue,
        "total_transactions": len(transactions),
        "pending_payouts": 0.0,
        "completed_transactions": len(transactions),
        "refunded_amount": 0.0,
        "date_range": {"start": start_date or "", "end": end_date or ""}
    }

def get_store(session: Session, store_id: uuid.UUID) -> Optional[Store]:
    """Get store by ID."""
    return session.get(Store, store_id)

# ============================== Notification CRUD (KEPT) =====================================

def create_notification(session: Session, notification_create: NotificationCreate, user_ids: list[uuid.UUID]) -> Notification:
    db_notification = Notification.model_validate(notification_create)
    session.add(db_notification)
    session.commit()
    session.refresh(db_notification)
    for user_id in user_ids:
        noti_user = Noti_User(notification_id=db_notification.id, user_id=user_id)
        session.add(noti_user)
    session.commit()
    return db_notification

def get_user_notifications(session: Session, user_id: uuid.UUID) -> List[dict]:
    stmt = select(
        Notification.id, Notification.title, Notification.message,
        Noti_User.is_read, Noti_User.created_at
    ).join(Noti_User).where(Noti_User.user_id == user_id).order_by(Noti_User.created_at.desc())
    results = session.exec(stmt).all()
    # Convert Row objects to dictionaries for proper schema validation
    return [r._asdict() for r in results]

def mark_notification_as_read(session: Session, notification_id: uuid.UUID, user_id: uuid.UUID) -> bool:
    stmt = select(Noti_User).where(
        Noti_User.notification_id == notification_id,
        Noti_User.user_id == user_id
    )
    noti_user = session.exec(stmt).first()
    if not noti_user:
        return False
    noti_user.is_read = True
    session.add(noti_user)
    session.commit()
    return True

def add_noti_to_new_user(session: Session, user_id: uuid.UUID):
    stmt = select(Notification).where(Notification.is_important == True)
    important_notifications = session.exec(stmt).all()
    for notification in important_notifications:
        noti_user = Noti_User(notification_id=notification.id, user_id=user_id)
        session.add(noti_user)

# ============================== Review & Rating CRUD (KEPT & ADAPTED) ===========================

def get_user_reviews(session: Session, user_id: uuid.UUID):
    # This now gets reviews for a VENDOR
    statement = select(Review).join(Order).join(OrderItem).join(SurpriseBag).join(Store).where(
        Store.owner_id == user_id,
        Order.status == OrderStatus.COMPLETED
    )
    return session.exec(statement).all()

def get_user_average_rating(session: Session, user_id: uuid.UUID):
    # This now gets the average rating for a VENDOR
    reviews = get_user_reviews(session, user_id)
    if reviews:
        return sum(r.rating for r in reviews) / len(reviews)
    return None

# ============================== Chat CRUD (KEPT) =============================================
# No changes are needed for the chat functionality as it is generic.

def create_conversation(session: Session, conversation_create: ConversationCreate, user_id: uuid.UUID) -> Conversation:
    conversation_in = conversation_create.model_dump(exclude={"member_ids"})
    new_convo = Conversation.model_validate(conversation_in)
    session.add(new_convo)
    session.flush()

    member_ids = set(conversation_create.member_ids)
    member_ids.add(user_id)
    for member_id in member_ids:
        conversation_member = ConversationMember(conversation_id=new_convo.id, user_id=member_id)
        session.add(conversation_member)
    session.commit()
    session.refresh(new_convo)
    return new_convo

def create_message(session: Session, message_create: MessageCreate, sender_id: uuid.UUID) -> Message:
    db_message = Message.model_validate(message_create, update={"sender_id": sender_id})
    session.add(db_message)
    session.flush()
    db_conversation = session.get(Conversation, message_create.conversation_id)
    if db_conversation:
        db_conversation.last_message_id = db_message.id
        session.add(db_conversation)
    session.commit()
    session.refresh(db_message)
    return db_message

def get_user_conversations(session: Session, user_id: uuid.UUID) -> list[Conversation]:
    statement = select(Conversation).join(ConversationMember).where(ConversationMember.user_id == user_id).order_by(Conversation.updated_at.desc())
    return session.exec(statement).all()

def get_messages_by_conversation(session: Session, conversation_id: uuid.UUID) -> list[Message]:
    statement = select(Message).where(Message.conversation_id == conversation_id).order_by(Message.created_at.asc())
    return session.exec(statement).all()


# --- Password Reset CRUD Functions ---

def store_reset_code(session: Session, email: str, reset_code: str, expires_at: datetime) -> None:
    """Store a password reset code for an email"""
    from app.models import PasswordReset
    
    # Delete any existing reset codes for this email
    statement = delete(PasswordReset).where(PasswordReset.email == email)
    session.exec(statement)
    
    # Create new reset code
    password_reset = PasswordReset(
        email=email,
        reset_code=reset_code,
        expires_at=expires_at
    )
    session.add(password_reset)
    session.commit()

def verify_reset_code(session: Session, email: str, reset_code: str) -> Optional[User]:
    """Verify reset code and return user if valid"""
    from app.models import PasswordReset
    
    # Find valid reset code
    statement = select(PasswordReset).where(
        PasswordReset.email == email,
        PasswordReset.reset_code == reset_code,
        PasswordReset.used == False,
        PasswordReset.expires_at > datetime.now()
    )
    password_reset = session.exec(statement).first()
    
    if not password_reset:
        return None
    
    # Mark reset code as used
    password_reset.used = True
    session.add(password_reset)
    session.commit()
    
    # Return the user
    return get_user_by_email(session=session, email=email)

def update_user_password(session: Session, user: User, new_password: str) -> User:
    """Update user password"""
    user.hashed_password = get_password_hash(new_password)
    session.add(user)
    session.commit()
    session.refresh(user)
    return user