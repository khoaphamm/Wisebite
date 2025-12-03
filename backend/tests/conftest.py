import os
import uuid
from typing import Generator

import pytest
from fastapi.testclient import TestClient
from sqlmodel import Session, SQLModel, create_engine
from sqlmodel.pool import StaticPool
from sqlalchemy import text

from app.main import app
from app.api.deps import get_db, get_current_user, get_current_admin
from app.models import User, UserRole, Store, FoodItem, SurpriseBag
from app.core.security import get_password_hash, create_access_token
from app.core.config import settings
from datetime import datetime, timedelta


# Get test database URL from environment
TEST_DATABASE_URL = os.getenv("TEST_DATABASE_URL")

if not TEST_DATABASE_URL:
    raise ValueError("TEST_DATABASE_URL environment variable is not set")

# Create test engine - removed StaticPool for PostgreSQL
engine = create_engine(
    TEST_DATABASE_URL,
    echo=False,  # Set to True for SQL debugging
    pool_pre_ping=True,  # Verify connections before using
)

# Cache hashed password to avoid slow bcrypt on every test
_CACHED_PASSWORD_HASH = None

def get_cached_password_hash(password: str = "testpassword") -> str:
    """Cache password hash to avoid slow bcrypt computation for each test."""
    global _CACHED_PASSWORD_HASH
    if _CACHED_PASSWORD_HASH is None:
        _CACHED_PASSWORD_HASH = get_password_hash(password)
    return _CACHED_PASSWORD_HASH


# Session-scoped fixture to set up database ONCE
@pytest.fixture(scope="session", autouse=True)
def setup_database():
    """Set up database once for entire test session."""
    # Enable PostGIS extension
    if "postgresql" in TEST_DATABASE_URL:
        try:
            with engine.connect() as conn:
                conn.execute(text("CREATE EXTENSION IF NOT EXISTS postgis;"))
                conn.execute(text("CREATE EXTENSION IF NOT EXISTS postgis_topology;"))
                conn.commit()
        except Exception as e:
            print(f"Warning: Could not enable PostGIS extensions: {e}")
    
    # Drop all tables using CASCADE to handle circular dependencies
    try:
        with engine.connect() as conn:
            # Drop tables with circular dependencies first
            conn.execute(text("DROP TABLE IF EXISTS message CASCADE;"))
            conn.execute(text("DROP TABLE IF EXISTS conversation CASCADE;"))
            conn.execute(text("DROP TABLE IF EXISTS conversationmember CASCADE;"))
            conn.commit()
        SQLModel.metadata.drop_all(engine)
    except Exception as e:
        print(f"Warning dropping tables: {e}")
    
    # Create all tables once
    SQLModel.metadata.create_all(engine)
    
    yield
    
    # Cleanup after all tests - use CASCADE for circular deps
    try:
        with engine.connect() as conn:
            conn.execute(text("DROP TABLE IF EXISTS message CASCADE;"))
            conn.execute(text("DROP TABLE IF EXISTS conversation CASCADE;"))
            conn.execute(text("DROP TABLE IF EXISTS conversationmember CASCADE;"))
            conn.commit()
        SQLModel.metadata.drop_all(engine)
    except Exception:
        pass


@pytest.fixture(scope="function")
def session(setup_database) -> Generator[Session, None, None]:
    """Create a test database session with transaction rollback for isolation."""
    connection = engine.connect()
    transaction = connection.begin()
    session = Session(bind=connection)
    
    # Use nested transaction for test isolation
    nested = connection.begin_nested()
    
    @pytest.fixture(autouse=True)
    def handle_savepoint():
        nonlocal nested
        if not nested.is_active:
            nested = connection.begin_nested()
    
    yield session
    
    # Rollback everything - this is FAST
    session.close()
    transaction.rollback()
    connection.close()


@pytest.fixture(scope="function")
def client(session: Session) -> Generator[TestClient, None, None]:
    """Create a test client."""
    def get_session_override():
        return session

    app.dependency_overrides[get_db] = get_session_override
    
    with TestClient(app) as c:
        yield c
    
    app.dependency_overrides.clear()


@pytest.fixture
def test_customer(session: Session) -> User:
    """Create a test customer user."""
    unique_id = str(uuid.uuid4())[:8]  # Use first 8 chars of UUID for uniqueness
    user = User(
        id=uuid.uuid4(),
        full_name="Test Customer",
        phone_number=f"01234{unique_id[:5]}",  # Unique phone number
        email=f"customer{unique_id}@test.com",  # Unique email
        hashed_password=get_cached_password_hash(),  # Use cached hash - FAST!
        role=UserRole.CUSTOMER
    )
    session.add(user)
    session.commit()
    session.refresh(user)
    return user


@pytest.fixture
def test_vendor(session: Session) -> User:
    """Create a test vendor user."""
    unique_id = str(uuid.uuid4())[:8]  # Use first 8 chars of UUID for uniqueness
    user = User(
        id=uuid.uuid4(),
        full_name="Test Vendor",
        phone_number=f"09876{unique_id[:5]}",  # Unique phone number  
        email=f"vendor{unique_id}@test.com",  # Unique email
        hashed_password=get_cached_password_hash(),  # Use cached hash - FAST!
        role=UserRole.VENDOR
    )
    session.add(user)
    session.commit()
    session.refresh(user)
    return user


@pytest.fixture
def test_admin(session: Session) -> User:
    """Create a test admin user."""
    unique_id = str(uuid.uuid4())[:8]  # Use first 8 chars of UUID for uniqueness
    user = User(
        id=uuid.uuid4(),
        full_name="Test Admin",
        phone_number=f"01112{unique_id[:5]}",  # Unique phone number
        email=f"admin{unique_id}@test.com",  # Unique email
        hashed_password=get_cached_password_hash(),  # Use cached hash - FAST!
        role=UserRole.ADMIN
    )
    session.add(user)
    session.commit()
    session.refresh(user)
    return user


@pytest.fixture
def test_store(session: Session, test_vendor: User) -> Store:
    """Create a test store for the vendor."""
    store = Store(
        id=uuid.uuid4(),
        name="Test Store",
        address="123 Test Street, Ho Chi Minh City",
        description="A test store for testing purposes",
        owner_id=test_vendor.id
    )
    session.add(store)
    session.commit()
    session.refresh(store)
    return store


@pytest.fixture
def test_food_item(session: Session, test_store: Store) -> FoodItem:
    """Create a test food item.
    
    Updated to use new FoodItem model fields:
    - standard_price instead of original_price
    - total_quantity instead of quantity
    """
    food_item = FoodItem(
        id=uuid.uuid4(),
        name="Test Banh Mi",
        description="A delicious test banh mi",
        standard_price=25000.0,
        total_quantity=10,
        available_quantity=10,
        surplus_quantity=0,
        reserved_quantity=0,
        is_fresh=True,
        is_available=True,
        is_active=True,
        store_id=test_store.id
    )
    session.add(food_item)
    session.commit()
    session.refresh(food_item)
    return food_item


@pytest.fixture
def test_surprise_bag(session: Session, test_store: Store) -> SurpriseBag:
    """Create a test surprise bag.
    
    Updated to include all required fields for new SurpriseBag model:
    - available_from, available_until
    - discount_percentage
    - bag_type, max_per_customer
    """
    now = datetime.now()
    surprise_bag = SurpriseBag(
        id=uuid.uuid4(),
        name="Test Surprise Bag",
        description="A test surprise bag with mystery items",
        bag_type="combo",
        original_value=50000.0,
        discounted_price=25000.0,
        discount_percentage=0.5,  # 50% discount
        quantity_available=5,
        max_per_customer=2,
        available_from=now + timedelta(days=6),  # Available 1 day before pickup
        available_until=now + timedelta(days=7, hours=2),
        pickup_start_time=now + timedelta(days=7),  # DEMO: 7 days from now for testing
        pickup_end_time=now + timedelta(days=7, hours=2),  # DEMO: 7 days + 2 hours from now
        is_active=True,
        is_auto_generated=False,
        store_id=test_store.id
    )
    session.add(surprise_bag)
    session.commit()
    session.refresh(surprise_bag)
    return surprise_bag


@pytest.fixture
def customer_token(test_customer: User) -> str:
    """Create an access token for test customer."""
    access_token_expires = timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES)
    return create_access_token(
        subject=str(test_customer.id),
        expires_delta=access_token_expires
    )


@pytest.fixture
def vendor_token(test_vendor: User) -> str:
    """Create an access token for test vendor."""
    access_token_expires = timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES)
    return create_access_token(
        subject=str(test_vendor.id),
        expires_delta=access_token_expires
    )


@pytest.fixture
def admin_token(test_admin: User) -> str:
    """Create an access token for test admin."""
    access_token_expires = timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES)
    return create_access_token(
        subject=str(test_admin.id),
        expires_delta=access_token_expires
    )


@pytest.fixture
def authenticated_customer_client(client: TestClient, session: Session, test_customer: User, customer_token: str) -> TestClient:
    """Create an authenticated test client for customer."""
    def get_session_override():
        return session

    def get_current_user_override():
        return test_customer

    app.dependency_overrides[get_db] = get_session_override
    app.dependency_overrides[get_current_user] = get_current_user_override
    client.headers.update({"Authorization": f"Bearer {customer_token}"})
    
    return client


@pytest.fixture
def authenticated_vendor_client(client: TestClient, session: Session, test_vendor: User, vendor_token: str) -> TestClient:
    """Create an authenticated test client for vendor."""
    def get_session_override():
        return session

    def get_current_user_override():
        return test_vendor

    app.dependency_overrides[get_db] = get_session_override
    app.dependency_overrides[get_current_user] = get_current_user_override
    client.headers.update({"Authorization": f"Bearer {vendor_token}"})
    
    return client


@pytest.fixture
def authenticated_admin_client(client: TestClient, session: Session, test_admin: User, admin_token: str) -> TestClient:
    """Create an authenticated test client for admin."""
    def get_session_override():
        return session

    def get_current_user_override():
        return test_admin

    app.dependency_overrides[get_db] = get_session_override
    app.dependency_overrides[get_current_user] = get_current_user_override
    client.headers.update({"Authorization": f"Bearer {admin_token}"})
    
    return client
