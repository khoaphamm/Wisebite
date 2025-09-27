from datetime import timedelta
from fastapi import APIRouter, HTTPException, Depends, status
from fastapi.security import OAuth2PasswordRequestForm
from typing import Annotated
import requests
import json

from app.schemas.auth import Token, GoogleSignInRequest, ForgotPasswordRequest, ResetPasswordRequest, Message, LinkPhoneNumberRequest
from app.schemas.user import UserLogin, UserPublic, UserCreate, UserUpdate
from app.api.deps import SessionDep, CurrentUser
from app.models import User
from app.core.security import create_access_token
from app.core.config import settings
from app import crud

router = APIRouter(prefix="/auth", tags=["Auth"])

@router.post("/login", response_model=Token)
def login(session: SessionDep, form_data: Annotated[OAuth2PasswordRequestForm, Depends()]):
    """ Flexible login: accepts phone number OR email as username """
    user = crud.authenticate_flexible(session=session, identifier=form_data.username, password=form_data.password)
    if not user:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Incorrect credentials")
    
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

@router.post("/google-signin", response_model=Token)
async def google_signin(session: SessionDep, google_request: GoogleSignInRequest):
    """
    Sign in with Google ID token
    """
    try:
        # Verify Google ID token
        google_user_info = await verify_google_token(google_request.id_token)
        
        if not google_user_info:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED, 
                detail="Invalid Google ID token"
            )
        
        email = google_user_info.get('email')
        name = google_user_info.get('name', '')
        picture = google_user_info.get('picture', '')
        
        # Check if user exists
        user = crud.get_user_by_email(session=session, email=email)
        
        if not user:
            # Create new user with Google info
            # Generate a unique placeholder phone number to avoid constraint violations
            import random
            placeholder_phone = f"google_user_{random.randint(100000, 999999)}"
            
            user_create = UserCreate(
                email=email,
                full_name=name,
                phone_number=placeholder_phone,  # Unique placeholder instead of empty string
                password="google_oauth",  # Placeholder password
                role="customer",
                avt_url=picture,  # Use correct field name
                is_google_user=True
            )
            try:
                user = crud.create_user(session=session, user_create=user_create)
            except Exception as create_error:
                # If creation fails due to constraint violation, try to find existing user by email again
                print(f"User creation failed: {create_error}")
                user = crud.get_user_by_email(session=session, email=email)
                if not user:
                    raise HTTPException(
                        status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                        detail=f"Failed to create or find user: {str(create_error)}"
                    )
        else:
            # User exists - this is account linking!
            print(f"Account linking: Google sign-in for existing user {email}")
            update_needed = False
            update_data = {}
            
            # Mark user as Google-enabled for future reference
            if not hasattr(user, 'is_google_user') or not user.is_google_user:
                update_data['is_google_user'] = True
                update_needed = True
            
            # Update avatar if user doesn't have one or it's the default
            if not user.avt_url or user.avt_url == settings.DEFAULT_AVATAR_URL:
                if picture:
                    update_data['avt_url'] = picture
                    update_needed = True
            
            # Update name if user's name is empty and Google provides one
            if not user.full_name.strip() and name:
                update_data['full_name'] = name
                update_needed = True
            
            # Update user if needed
            if update_needed:
                try:
                    from app.schemas.user import UserUpdate
                    user_update = UserUpdate(**update_data)
                    user = crud.update_user(session=session, db_user=user, user_in=user_update)
                    print(f"Successfully linked Google account to existing user")
                except Exception as e:
                    print(f"Failed to update user profile with Google info: {e}")
                    # Continue with login even if update fails
        
        # Create access token
        access_token_expires = timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES)
        return Token(access_token=create_access_token(user.id, expires_delta=access_token_expires))
        
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=f"Google sign-in failed: {str(e)}"
        )

@router.post("/forgot-password", response_model=Message)
async def forgot_password(session: SessionDep, request: ForgotPasswordRequest):
    """
    Send password reset code to user's email
    """
    user = crud.get_user_by_email(session=session, email=request.email)
    if not user:
        # Don't reveal if email exists for security
        return Message(message="Nếu email tồn tại, mã reset đã được gửi.")
    
    # Generate 6-digit reset code
    import random
    reset_code = f"{random.randint(100000, 999999):06d}"
    
    # Set expiration time (15 minutes from now)
    from datetime import datetime, timedelta
    expires_at = datetime.now() + timedelta(minutes=15)
    
    try:
        # Store reset code in database
        crud.store_reset_code(session=session, email=request.email, reset_code=reset_code, expires_at=expires_at)
        
        # Send email using existing service
        from app.services.email import send_email
        subject = f"{settings.PROJECT_NAME} - Reset Password"
        html_content = f"""
        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
            <h2 style="color: #4CAF50;">🔐 Reset Your Password</h2>
            <p>Hello,</p>
            <p>You requested to reset your password for your WiseBite account.</p>
            <div style="background-color: #f8f9fa; padding: 20px; border-radius: 8px; text-align: center; margin: 20px 0;">
                <h3 style="color: #333; margin: 0;">Your Reset Code:</h3>
                <div style="font-size: 32px; font-weight: bold; color: #4CAF50; letter-spacing: 4px; margin: 10px 0;">
                    {reset_code}
                </div>
                <p style="color: #666; font-size: 14px; margin: 0;">This code expires in 15 minutes</p>
            </div>
            <p>Enter this code in the app to reset your password.</p>
            <p style="color: #666; font-size: 12px;">If you didn't request this, please ignore this email.</p>
            <hr style="border: 1px solid #eee; margin: 20px 0;">
            <p style="color: #999; font-size: 12px;">© 2025 WiseBite - Reduce Food Waste</p>
        </div>
        """
        
        send_email(email_to=request.email, subject=subject, html_content=html_content)
        
        return Message(message="Mã reset password đã được gửi đến email của bạn. Vui lòng kiểm tra hộp thư.")
        
    except Exception as e:
        print(f"Failed to send reset email: {e}")
        # Don't reveal the error to the user for security
        return Message(message="Đã có lỗi xảy ra. Vui lòng thử lại sau.")

@router.post("/reset-password", response_model=Message)
async def reset_password(session: SessionDep, request: ResetPasswordRequest):
    """
    Reset password using email and reset code
    """
    # Verify reset code and get user
    user = crud.verify_reset_code(session=session, email=request.email, reset_code=request.reset_code)
    if not user:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST, 
            detail="Mã reset không hợp lệ hoặc đã hết hạn."
        )
    
    try:
        # Update user password
        crud.update_user_password(session=session, user=user, new_password=request.new_password)
        
        # Send confirmation email
        from app.services.email import send_email
        subject = f"{settings.PROJECT_NAME} - Password Reset Successful"
        html_content = f"""
        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
            <h2 style="color: #4CAF50;">✅ Password Reset Successful</h2>
            <p>Hello {user.full_name},</p>
            <p>Your password has been successfully reset for your WiseBite account.</p>
            <div style="background-color: #e8f5e8; padding: 15px; border-radius: 8px; margin: 20px 0;">
                <p style="margin: 0; color: #2e7d32;"><strong>✓ Your password is now updated</strong></p>
                <p style="margin: 5px 0 0 0; color: #666;">You can now login with your new password</p>
            </div>
            <p>If you didn't make this change, please contact our support immediately.</p>
            <hr style="border: 1px solid #eee; margin: 20px 0;">
            <p style="color: #999; font-size: 12px;">© 2025 WiseBite - Reduce Food Waste</p>
        </div>
        """
        
        try:
            send_email(email_to=request.email, subject=subject, html_content=html_content)
        except Exception as email_error:
            # Don't fail the password reset if email fails
            print(f"Failed to send confirmation email: {email_error}")
        
        return Message(message="Mật khẩu đã được đặt lại thành công. Bạn có thể đăng nhập với mật khẩu mới.")
        
    except Exception as e:
        print(f"Failed to reset password: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Đã có lỗi xảy ra khi đặt lại mật khẩu. Vui lòng thử lại."
        )

@router.post("/link-phone-number", response_model=Message)
async def link_phone_number(session: SessionDep, request: LinkPhoneNumberRequest, current_user: CurrentUser):
    """
    Link a phone number and password to a Google account for traditional login
    """
    # Check if phone number is already taken by another user
    existing_user = crud.get_user_by_phone_number(session=session, phone_number=request.phone_number)
    if existing_user and existing_user.id != current_user.id:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST, 
            detail="Phone number already exists"
        )
    
    # Update user with phone number and password
    from app.core.security import get_password_hash
    update_data = UserUpdate(
        phone_number=request.phone_number
    )
    
    # Also update the hashed password directly
    current_user.phone_number = request.phone_number
    current_user.hashed_password = get_password_hash(request.password)
    
    try:
        session.add(current_user)
        session.commit()
        session.refresh(current_user)
        return Message(message="Phone number and password linked successfully. You can now login with phone number.")
    except Exception as e:
        session.rollback()
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to link phone number: {str(e)}"
        )

async def verify_google_token(id_token: str) -> dict:
    """
    Verify Google ID token and return user info
    """
    try:
        # Google's token verification endpoint
        url = f"https://oauth2.googleapis.com/tokeninfo?id_token={id_token}"
        response = requests.get(url)
        
        print(f"Google token verification response: {response.status_code}")
        print(f"Google Client ID from settings: {settings.GOOGLE_CLIENT_ID}")
        
        if response.status_code == 200:
            user_info = response.json()
            print(f"Google user info: {user_info}")
            
            # Verify the token is for our app
            token_aud = user_info.get('aud')
            print(f"Token audience: {token_aud}")
            print(f"Expected audience: {settings.GOOGLE_CLIENT_ID}")
            
            if token_aud == settings.GOOGLE_CLIENT_ID:
                return user_info
            else:
                print(f"Audience mismatch: expected {settings.GOOGLE_CLIENT_ID}, got {token_aud}")
        else:
            print(f"Google token verification failed: {response.text}")
        
        return None
    except Exception as e:
        print(f"Error verifying Google token: {e}")
        return None