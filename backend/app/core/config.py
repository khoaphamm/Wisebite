from pydantic_settings import SettingsConfigDict, BaseSettings
from pydantic import EmailStr, PostgresDsn, field_validator
from typing import Optional, Any

class Settings(BaseSettings):
    # This tells Pydantic to load settings from a file named '.env'
    # It's a standard and secure way to manage configuration.
    model_config = SettingsConfigDict(
        env_file="./.env",
        env_ignore_empty=True,
        extra="ignore"
    )

    # --- 1. Superuser Admin Settings (NEW) ---
    # These values are used by 'init_db' to create the first admin account.
    FIRST_SUPERUSER_NAME: str = "Admin"
    FIRST_SUPERUSER_EMAIL: EmailStr = "admin@wisebite.com"
    FIRST_SUPERUSER_PHONE: str = "0123456789"
    FIRST_SUPERUSER_PASSWORD: str = "changeme"

    # --- 2. Database Settings (ADAPTED) ---
    POSTGRES_HOST: str = "localhost"
    POSTGRES_PORT: int = 5432
    POSTGRES_USER: str = "postgres"
    POSTGRES_PASSWORD: str = "postgres"
    POSTGRES_DB: str = "wisebite_db" # Renamed for clarity
    
    # This is a more robust way to build the database URL using Pydantic's validation
    POSTGRES_URL: Optional[PostgresDsn] = None

    @field_validator("POSTGRES_URL", mode="before")
    def assemble_db_connection(cls, v: Optional[str], values) -> Any:
        if isinstance(v, str):
            return v
        return PostgresDsn.build(
            scheme="postgresql",
            username=values.data.get("POSTGRES_USER"),
            password=values.data.get("POSTGRES_PASSWORD"),
            host=values.data.get("POSTGRES_HOST"),
            port=values.data.get("POSTGRES_PORT"),
            path=f"{values.data.get('POSTGRES_DB') or ''}",
        )

    # --- 3. Project and API Settings (ADAPTED) ---
    PROJECT_NAME: str = "WiseBite API"
    API_STR: str = "/api/v1"

    # --- 4. Security Settings (UNCHANGED) ---
    # Used for JWT token creation (user login)
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 60 * 24 * 7 # 7 days
    SECRET_KEY: str = "a_very_secret_key_that_you_should_change"

    # --- 5. Third-Party API Keys (ADAPTED/SIMPLIFIED) ---
    # For uploading images like logos and food pictures
    CLOUDINARY_CLOUD_NAME: str = "your_cloud_name"
    CLOUDINARY_API_KEY: str = "your_api_key"
    CLOUDINARY_API_SECRET: str = "your_api_secret"
    
    # For displaying maps and store locations
    MAPBOX_ACCESS_TOKEN: str = "your_mapbox_access_token"
    
    DEFAULT_AVATAR_URL: str = "https://i.ibb.co/5xt2NvW0/453178253-471506465671661-2781666950760530985-n.png"

    # --- 6. Redis Settings (UNCHANGED, for future use) ---
    REDIS_HOST: str = "redis"
    REDIS_PORT: int = 6379
    REDIS_DB: int = 0

    # --- 7. Email Settings (UNCHANGED, for future use) ---
    # For sending password resets, etc.
    SMTP_HOST: str | None = None
    SMTP_PORT: int = 587
    SMTP_USER: str | None = None
    SMTP_PASSWORD: str | None = None
    EMAILS_FROM_EMAIL: EmailStr | None = None

settings = Settings()