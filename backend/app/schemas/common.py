from typing import List, TypeVar, Generic
from sqlmodel import SQLModel

T = TypeVar('T')

class PaginationResponse(SQLModel, Generic[T]):
    """Generic pagination response schema"""
    data: List[T]
    count: int
    skip: int = 0
    limit: int = 100
