#!/usr/bin/env python3
"""
Database initialization script for WiseBite
Creates tables and initial data
"""
import asyncio
from sqlmodel import SQLModel, create_engine
from app.core.config import settings
from app.models import *  # Import all models

def init_db():
    """Initialize the database with tables"""
    try:
        print("🔄 Connecting to database...")
        engine = create_engine(str(settings.POSTGRES_URL))
        
        print("🔄 Creating tables...")
        SQLModel.metadata.create_all(engine)
        
        print("✅ Database initialized successfully!")
        return True
    except Exception as e:
        print(f"❌ Database initialization failed: {e}")
        return False

if __name__ == "__main__":
    init_db()
