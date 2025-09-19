from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager
from app.core.config import settings
from app.api.router import api_router

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup
    print("🚀 Starting WiseBite API...")
    try:
        # Initialize database tables
        from sqlmodel import SQLModel, create_engine
        import app.models  # Import models module
        
        print("🔄 Connecting to database...")
        engine = create_engine(str(settings.POSTGRES_URL))
        print("🔄 Creating tables...")
        SQLModel.metadata.create_all(engine)
        print("✅ Database tables created successfully!")
        
        # Initialize default admin user
        try:
            from app.core.db import init_db
            from sqlmodel import Session
            with Session(engine) as session:
                init_db(session)
        except Exception as e:
            print(f"⚠️  Could not initialize default data: {e}")
            
    except Exception as e:
        print(f"❌ Database initialization failed: {e}")
        print("📋 API will run without database connection")
    
    yield
    # Shutdown
    print("🛑 Shutting down WiseBite API...")

app = FastAPI(
    title=settings.PROJECT_NAME,
    description="""
    ## WiseBite API
    
    A comprehensive food marketplace API that connects customers with local vendors selling surplus food at discounted prices.
    
    ### Key Features:
    - **Authentication**: User registration and login with JWT tokens
    - **User Management**: Customer and vendor profiles
    - **Store Management**: Vendor store registration and management
    - **Food Items**: CRUD operations for food items
    - **Surprise Bags**: Discounted mystery food packages
    - **Orders**: Complete order management system
    - **Notifications**: Real-time notifications
    - **Chat**: Messaging between customers and vendors
    - **Transactions**: Payment processing and history
    
    ### User Roles:
    - **Customer**: Browse and purchase food items
    - **Vendor**: Manage stores, food items, and orders
    - **Admin**: System administration
    
    ### API Documentation:
    - **Swagger UI**: Available at `/docs` (this page)
    - **ReDoc**: Available at `/redoc`
    - **OpenAPI Schema**: Available at `/openapi.json`
    """,
    version="1.0.0",
    contact={
        "name": "WiseBite Team",
        "email": "admin@wisebite.com",
    },
    license_info={
        "name": "MIT",
    },
    lifespan=lifespan
)

# Add CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(api_router, prefix=settings.API_STR)

