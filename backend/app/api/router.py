from fastapi import APIRouter

# Import existing, unchanged routers
from app.api.endpoints import auth, user, transaction, notification, chat, otp

# Import our NEW routers
from app.api.endpoints import store, food_item, surprise_bag, order

api_router = APIRouter()

# Include existing routers
api_router.include_router(auth.router)
api_router.include_router(user.router)
api_router.include_router(notification.router)
api_router.include_router(chat.router)
api_router.include_router(otp.router)
api_router.include_router(transaction.router, prefix="/transactions", tags=["Transactions"])

# Include NEW and ADAPTED routers with logical prefixes
api_router.include_router(store.router, prefix="/stores", tags=["Stores"])
api_router.include_router(food_item.router, prefix="/food-items", tags=["Vendor Food Items"])
api_router.include_router(surprise_bag.router, prefix="/surprise-bags", tags=["Surprise Bags"])
api_router.include_router(order.router, prefix="/orders", tags=["Orders"])