import uuid
from typing import Optional
from sqlmodel import SQLModel, Field
from pydantic import EmailStr
from app.models import UserRole
from app.core.config import settings
from .store import StorePublic # NEW: Import for nesting

# --- BASE & PUBLIC SCHEMAS ---

class UserBase(SQLModel):
    email: EmailStr
    phone_number: str
    full_name: str
    gender: str | None = Field(default=None)
    birth_date: str | None = Field(default=None)

class UserPublic(UserBase):
    id: uuid.UUID
    role: UserRole
    avt_url: str
    store: Optional[StorePublic] = None # NEW: Add store info for vendors

class UsersPublic(SQLModel):
    data: list[UserPublic]
    count: int

# --- CREATION & AUTHENTICATION SCHEMAS (KEPT & ADAPTED) ---

class UserCreate(UserBase):
    # ADAPTED: Role is now required during creation for clarity
    role: UserRole 
    password: str
    avt_url: str = Field(default=settings.DEFAULT_AVATAR_URL)

# KEPT: The following classes from the original file are essential and are preserved
class UserRegister(SQLModel):
    email: str
    phone_number: str
    password: str
    full_name: str
    register_token: str

class UserLogin(SQLModel):
    phone_number: str
    password: str

# --- UPDATE SCHEMAS (KEPT) ---

class UserUpdate(UserBase):
    email: EmailStr | None = Field(default=None)
    phone_number: str | None = Field(default=None)
    full_name: str | None = Field(default=None)
    role: UserRole | None = Field(default=None)
    avt_url: str | None = Field(default=None)

class UserUpdateMe(UserBase):
    email: EmailStr | None = Field(default=None)
    phone_number: str | None = Field(default=None)
    full_name: str | None = Field(default=None)

class UpdatePassword(SQLModel):
    old_password: str = Field(max_length=100)
    new_password: str = Field(max_length=100)

# --- SPECIALIZED PUBLIC SCHEMAS (KEPT & RENAMED) ---

class VendorPublic(UserPublic):
    # RENAMED: CollectorPublic is now VendorPublic
    average_rating: float | None = None