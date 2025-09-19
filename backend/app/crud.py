import uuid
import httpx
from typing import Optional, List
from sqlmodel import Session, select
from sqlalchemy.orm import selectinload
from sqlalchemy import func

from app.core.config import settings
from app.core.security import get_password_hash, verify_password

# --- Import Models ---
from app.models import (
    User, UserRole, Store, FoodItem, SurpriseBag, Order, OrderItem,
    OrderStatus, Review, Transaction, TransactionStatus, Notification,
    Noti_User, Conversation, ConversationMember, Message
)

# --- Import Schemas ---
# (We will need to create these files in the /app/crud/ directory)
from app.schemas.user import UserCreate, UserUpdate
from app.schemas.store import StoreCreate, StoreUpdate
from app.schemas.food_item import FoodItemCreate, FoodItemUpdate
from app.schemas.surprise_bag import SurpriseBagCreate, SurpriseBagUpdate
from app.schemas.order import OrderCreate
from app.schemas.transaction import OrderConfirmPickupRequest
from app.schemas.notification import NotificationCreate
from app.schemas.chat import ConversationCreate, MessageCreate

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

def create_user(session: Session, user_create: UserCreate) -> User:
    # This function creates ANY type of user based on the role in user_create
    db_user = User.model_validate(
        user_create,
        update={"hashed_password": get_password_hash(user_create.password)}
    )
    session.add(db_user)
    session.flush() # Flush to get the user ID for other operations
    
    # If the new user is a VENDOR, create an associated store profile
    if db_user.role == UserRole.VENDOR:
        # Create a default store name based on the user's full name
        default_store_name = f"Store of {db_user.full_name}"
        store_in = StoreCreate(name=default_store_name, address="Default Address")
        create_store(session=session, store_create=store_in, owner_id=db_user.id)
        
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
        
    db_user.sqlmodel_update(user_data)
    session.add(db_user)
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

def create_store(session: Session, store_create: StoreCreate, owner_id: uuid.UUID) -> Store:
    db_store = Store.model_validate(store_create, update={"owner_id": owner_id})
    session.add(db_store)
    session.commit()
    session.refresh(db_store)
    return db_store

def update_store(session: Session, db_store: Store, store_in: StoreUpdate) -> Store:
    store_data = store_in.model_dump(exclude_unset=True)
    db_store.sqlmodel_update(store_data)
    session.add(db_store)
    session.commit()
    session.refresh(db_store)
    return db_store

# ============================== FoodItem CRUD (NEW) ==========================================

def create_food_item(session: Session, item_create: FoodItemCreate, store_id: uuid.UUID) -> FoodItem:
    db_item = FoodItem.model_validate(item_create, update={"store_id": store_id})
    session.add(db_item)
    session.commit()
    session.refresh(db_item)
    return db_item

def get_food_items_by_store(session: Session, store_id: uuid.UUID) -> List[FoodItem]:
    statement = select(FoodItem).where(FoodItem.store_id == store_id)
    return session.exec(statement).all()

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

# ============================== Order CRUD (ADAPTED) =========================================

def get_order_by_id(session: Session, order_id: uuid.UUID) -> Optional[Order]:
    # Use options to pre-load related data to prevent extra queries (N+1 problem)
    statement = select(Order).where(Order.id == order_id).options(
        selectinload(Order.items).selectinload(OrderItem.surprise_bag).selectinload(SurpriseBag.store),
        selectinload(Order.customer)
    )
    return session.exec(statement).first()

def get_orders_by_customer(session: Session, customer_id: uuid.UUID) -> List[Order]:
    statement = select(Order).where(Order.customer_id == customer_id).order_by(Order.created_at.desc())
    return session.exec(statement).all()

def create_order(session: Session, order_create: OrderCreate, customer_id: uuid.UUID) -> Order:
    total_amount = 0
    db_order_items = []
    
    # Start a nested transaction to handle potential errors
    with session.begin_nested():
        # Step 1: Validate and process each item in the order
        for item in order_create.items:
            surprise_bag = session.get(SurpriseBag, item.surprise_bag_id)
            if not surprise_bag:
                raise ValueError(f"SurpriseBag with id {item.surprise_bag_id} not found.")
            if surprise_bag.quantity_available < item.quantity:
                raise ValueError(f"Not enough stock for {surprise_bag.name}. Available: {surprise_bag.quantity_available}, Requested: {item.quantity}")

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

        # Step 2: Create the main Order
        db_order = Order(
            customer_id=customer_id,
            total_amount=total_amount,
            status=OrderStatus.PENDING_PAYMENT, # Or CONFIRMED if payment is immediate
            items=db_order_items
        )
        session.add(db_order)

    # Commit the transaction if all steps succeeded
    session.commit()
    session.refresh(db_order)
    return db_order

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