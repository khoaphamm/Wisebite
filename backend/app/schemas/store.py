import uuid
from typing import Optional
from sqlmodel import SQLModel

class StoreBase(SQLModel):
    name: str
    address: str
    description: Optional[str] = None
    logo_url: Optional[str] = None

class StoreCreate(StoreBase):
    latitude: Optional[float] = None
    longitude: Optional[float] = None

class StoreUpdate(SQLModel):
    name: Optional[str] = None
    address: Optional[str] = None
    description: Optional[str] = None
    logo_url: Optional[str] = None
    latitude: Optional[float] = None
    longitude: Optional[float] = None

class StorePublic(StoreBase):
    id: uuid.UUID
    owner_id: uuid.UUID
    latitude: Optional[float] = None
    longitude: Optional[float] = None

class StoreWithDistance(StorePublic):
    distance_km: Optional[float] = None