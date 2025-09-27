from datetime import timedelta
from sqlmodel import SQLModel, Field
from pydantic import validator
from typing import Optional

class TokenPayLoad(SQLModel):
    sub: str = Field(min_length=1)
    exp: int = Field(gt=0)


class Token(SQLModel):
    access_token: str = Field(min_length=10)
    token_type: str = Field(default="bearer", regex="^bearer$")


class Message(SQLModel):
    message: str = Field(min_length=1, max_length=1000)


class GoogleSignInRequest(SQLModel):
    id_token: str = Field(min_length=1, description="Google ID token")
    

class ForgotPasswordRequest(SQLModel):
    email: str = Field(min_length=1, description="User email address")
    

class ResetPasswordRequest(SQLModel):
    email: str = Field(min_length=1, description="User email address")
    reset_code: str = Field(min_length=6, max_length=6, description="6-digit reset code")
    new_password: str = Field(min_length=8, description="New password")


class LinkPhoneNumberRequest(SQLModel):
    phone_number: str = Field(min_length=10, max_length=15, description="Phone number to link to Google account")
    password: str = Field(min_length=8, description="Password for traditional login")
