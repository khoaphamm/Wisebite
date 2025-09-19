import os
import uuid
from typing import Generator

import pytest
from fastapi.testclient import TestClient
from sqlmodel import Session, SQLModel, create_engine
from sqlmodel.pool import StaticPool

from app.main import app
from app.api.deps import get_db, get_current_user
from app.models import User, UserRole, Store, FoodItem, SurpriseBag
from app.core.security import get_password_hash, create_access_token
from app.core.config import settings
from datetime import datetime, timedelta


# Get test database URL from environment
TEST_DATABASE_URL = os.getenv("TEST_DATABASE_URL")

if not TEST_DATABASE_URL:
    raise ValueError("TEST_DATABASE_URL environment variable is not set")

# Create test engine
engine = create_engine(
    TEST_DATABASE_URL,
    poolclass=StaticPool,
    connect_args={"check_same_thread": False} if "sqlite" in TEST_DATABASE_URL else {}
)


@pytest.fixture(scope="function")
def session() -> Generator[Session, None, None]:
    """Create a test database session."""
    SQLModel.metadata.create_all(engine)
    with Session(engine) as session:
        yield session
    SQLModel.metadata.drop_all(engine)


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
    user = User(
        id=uuid.uuid4(),
        full_name="Test Customer",
        phone_number="0123456789",
        email="customer@test.com",
        hashed_password=get_password_hash("testpassword"),
        role=UserRole.CUSTOMER
    )
    session.add(user)
    session.commit()
    session.refresh(user)
    return user


@pytest.fixture
def test_vendor(session: Session) -> User:
    """Create a test vendor user."""
    user = User(
        id=uuid.uuid4(),
        full_name="Test Vendor",
        phone_number="0987654321",
        email="vendor@test.com",
        hashed_password=get_password_hash("testpassword"),
        role=UserRole.VENDOR
    )
    session.add(user)
    session.commit()
    session.refresh(user)
    return user


@pytest.fixture
def test_admin(session: Session) -> User:
    """Create a test admin user."""
    user = User(
        id=uuid.uuid4(),
        full_name="Test Admin",
        phone_number="0111222333",
        email="admin@test.com",
        hashed_password=get_password_hash("testpassword"),
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
    """Create a test food item."""
    food_item = FoodItem(
        id=uuid.uuid4(),
        name="Test Banh Mi",
        description="A delicious test banh mi",
        original_price=25000.0,
        store_id=test_store.id
    )
    session.add(food_item)
    session.commit()
    session.refresh(food_item)
    return food_item


@pytest.fixture
def test_surprise_bag(session: Session, test_store: Store) -> SurpriseBag:
    """Create a test surprise bag."""
    now = datetime.now()
    surprise_bag = SurpriseBag(
        id=uuid.uuid4(),
        name="Test Surprise Bag",
        original_value=50000.0,
        discounted_price=25000.0,
        quantity_available=5,
        pickup_start_time=now + timedelta(hours=1),
        pickup_end_time=now + timedelta(hours=3),
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
def authenticated_customer_client(client: TestClient, test_customer: User, customer_token: str) -> TestClient:
    """Create an authenticated test client for customer."""
    def get_current_user_override():
        return test_customer

    app.dependency_overrides[get_current_user] = get_current_user_override
    client.headers.update({"Authorization": f"Bearer {customer_token}"})
    
    return client


@pytest.fixture
def authenticated_vendor_client(client: TestClient, test_vendor: User, vendor_token: str) -> TestClient:
    """Create an authenticated test client for vendor."""
    def get_current_user_override():
        return test_vendor

    app.dependency_overrides[get_current_user] = get_current_user_override
    client.headers.update({"Authorization": f"Bearer {vendor_token}"})
    
    return client


@pytest.fixture
def authenticated_admin_client(client: TestClient, test_admin: User, admin_token: str) -> TestClient:
    """Create an authenticated test client for admin."""
    def get_current_user_override():
        return test_admin

    app.dependency_overrides[get_current_user] = get_current_user_override
    client.headers.update({"Authorization": f"Bearer {admin_token}"})
    
    return client
