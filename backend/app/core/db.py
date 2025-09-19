# /app/core/db.py

from sqlmodel import create_engine, Session

# --- Core Imports ---
# Import the settings object which holds our configuration (like the database URL)
from app.core.config import settings

# --- WiseBite Specific Imports ---
# Import the UserRole enum from our new models.py
from app.models import UserRole
# Import the schema for creating a user (we will create this in a later step)
from app.schemas.user import UserCreate
# Import the main crud object (we will build this out later)
from app import crud

# --- Database Engine Creation ---
# This is the central object that connects our application to the database.
# The configuration is loaded from the settings object, which is very flexible.
# This code does not need to be changed.
engine = create_engine(
    str(settings.POSTGRES_URL), 
    echo=False, 
    echo_pool=True,
    pool_pre_ping=True,
    pool_size=20,
    max_overflow=10,
    pool_timeout=30,
    pool_recycle=1800,
)

# --- Initial Database Population ---
def init_db(session: Session) -> None:
    """
    This function is called during application startup to ensure
    the database has essential data. For WiseBite, this means
    creating a default Super Admin account if one doesn't exist.
    """
    
    # Check if the admin user already exists using their phone number.
    # Note: We will later need to create the 'crud.user.get_by_phone_number' function.
    # We also assume the admin's phone number is stored in our settings file.
    user = crud.get_user_by_phone_number(session=session, phone_number=settings.FIRST_SUPERUSER_PHONE)

    # If the user is not found in the database, create them.
    if not user:
        # Create a UserCreate schema object with the admin's details.
        # It's best practice to load these values from your settings file.
        admin_in = UserCreate(
            full_name=settings.FIRST_SUPERUSER_NAME,
            phone_number=settings.FIRST_SUPERUSER_PHONE,
            password=settings.FIRST_SUPERUSER_PASSWORD,
            email=settings.FIRST_SUPERUSER_EMAIL,
            role=UserRole.ADMIN  # We use our adapted UserRole enum
        )   
        # This calls the function to create the user in the database.
        # Note: We will need to create the 'crud.user.create' function later.
        crud.create_user(session=session, user_create=admin_in)
        print("Default Super Admin user has been created.")

    # The logic to create a default "ScrapCategory" has been removed
    # as it is not needed for the WiseBite application. Vendors will
    # be responsible for creating their own FoodItems and SurpriseBags.