"""
Test configuration for WiseBite Backend - Auth tests only.
"""
import os
import uuid
from typing import Generator

import pytest
from fastapi.testclient import TestClient
from sqlmodel import Session, SQLModel, create_engine
from sqlmodel.pool import StaticPool

from app.main import app
from app.api.deps import get_db, get_current_user
from app.models import User, UserRole
from app.core.security import get_password_hash, create_access_token
from app.core.config import settings
from datetime import datetime, timedelta


# Use in-memory SQLite for simple tests
engine = create_engine(
    "sqlite:///:memory:",
    connect_args={"check_same_thread": False},
    poolclass=StaticPool,
)


@pytest.fixture(scope="function")
def session() -> Generator[Session, None, None]:
    """Create a test database session."""
    # Create tables without PostGIS extensions
    try:
        SQLModel.metadata.create_all(engine)
    except Exception as e:
        # Skip PostGIS errors for SQLite testing
        if "RecoverGeometryColumn" in str(e) or "CheckSpatialIndex" in str(e):
            pass
        else:
            raise
    
    with Session(engine) as session:
        yield session
    
    try:
        SQLModel.metadata.drop_all(engine)
    except Exception:
        pass  # Ignore cleanup errors


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
    unique_id = str(uuid.uuid4())[:8]
    user = User(
        id=uuid.uuid4(),
        full_name="Test Customer",
        phone_number=f"01234{unique_id[:5]}",
        email=f"customer{unique_id}@test.com",
        hashed_password=get_password_hash("testpassword"),
        role=UserRole.CUSTOMER
    )
    session.add(user)
    session.commit()
    session.refresh(user)
    return user


@pytest.fixture
def customer_token(test_customer: User) -> str:
    """Create an access token for test customer."""
    access_token_expires = timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES)
    return create_access_token(
        subject=str(test_customer.id),
        expires_delta=access_token_expires
    )
