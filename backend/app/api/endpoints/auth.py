from datetime import timedelta
from fastapi import APIRouter, HTTPException, Depends, status
from fastapi.security import OAuth2PasswordRequestForm
from typing import Annotated

from app.schemas.auth import Token
from app.schemas.user import UserLogin, UserPublic, UserCreate
from app.api.deps import SessionDep
from app.core.security import create_access_token
from app.core.config import settings
from app import crud

router = APIRouter(prefix="/auth", tags=["Auth"])

@router.post("/login", response_model=Token)
def login(session: SessionDep, form_data: Annotated[OAuth2PasswordRequestForm, Depends()]):
    """ Standard OAuth2 login. The 'username' is the user's phone number. """
    user = crud.authenticate(session=session, phone_number=form_data.username, password=form_data.password)
    if not user:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Incorrect phone number or password")
    
    access_token_expires = timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES)
    return Token(access_token=create_access_token(user.id, expires_delta=access_token_expires))

@router.post("/signup", response_model=UserPublic, status_code=status.HTTP_201_CREATED)
def signup(session: SessionDep, user_in: UserCreate):
    """
    ADAPTED: Single endpoint to sign up a CUSTOMER or a VENDOR.
    The 'role' field in the request body determines the user type.
    """
    if crud.get_user_by_phone_number(session=session, phone_number=user_in.phone_number):
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Phone number already exists")
    if crud.get_user_by_email(session=session, email=user_in.email):
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Email already exists")
        
    user = crud.create_user(session=session, user_create=user_in)
    return user