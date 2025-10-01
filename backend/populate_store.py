#!/usr/bin/env python3
"""
WiseBite Store Population Script
Creates a store for hekinglois@gmail.com and populates it with realistic Vietnamese food items
"""

import os
import uuid
from datetime import datetime, timedelta
from sqlmodel import create_engine, Session, select

# Override the host for local connection
os.environ['POSTGRES_HOST'] = 'localhost'

from app.models import User, Store, FoodItem, Category, InventoryLog

# Database connection
engine = create_engine('postgresql://postgres:postgres@localhost:5432/wisebite_db')

def create_store_for_user(session: Session, user_id: uuid.UUID) -> Store:
    """Create a store for the specified user"""
    store = Store(
        name="Tạp Hóa Hương Lan",
        address="123 Đường Lê Lợi, Phường 3, Quận 1, TP.HCM",
        description="Tạp hóa gia đình chuyên bán thực phẩm tươi sống, đồ khô và nhu yếu phẩm hàng ngày. Cam kết chất lượng tươi ngon, giá cả hợp lý.",
        owner_id=user_id
    )
    session.add(store)
    session.commit()
    session.refresh(store)
    print(f"✅ Created store: {store.name} (ID: {store.id})")
    return store

def get_categories(session: Session) -> dict:
    """Get all categories and return as a lookup dictionary"""
    categories = {}
    
    # Get all categories
    stmt = select(Category)
    all_categories = session.exec(stmt).all()
    
    for category in all_categories:
        categories[category.name] = category.id
    
    return categories

def create_food_items(session: Session, store_id: uuid.UUID, categories: dict):
    """Create comprehensive food items for a Vietnamese convenience store"""
    
    food_items_data = [
        # Fresh Vegetables (Rau Củ)
        {
            "name": "Rau cải ngọt",
            "description": "Rau cải ngọt tươi, thu hoạch sáng nay từ Đà Lạt",
            "sku": "RAU001",
            "standard_price": 15000.0,
            "cost_price": 10000.0,
            "is_fresh": True,
            "expires_at": datetime.now() + timedelta(days=3),
            "total_quantity": 25,
            "weight": 500.0,
            "unit": "bó",
            "category_name": "Rau Củ",
            "allergens": "Không có",
            "ingredients": "Rau cải ngọt tươi 100%"
        },
        {
            "name": "Cà chua bi",
            "description": "Cà chua bi ngọt, tươi ngon, thích hợp làm salad",
            "sku": "RAU002", 
            "standard_price": 35000.0,
            "cost_price": 25000.0,
            "is_fresh": True,
            "expires_at": datetime.now() + timedelta(days=5),
            "total_quantity": 20,
            "weight": 500.0,
            "unit": "hộp",
            "category_name": "Rau Củ",
            "allergens": "Không có",
            "ingredients": "Cà chua bi tươi 100%"
        },
        {
            "name": "Hành lá",
            "description": "Hành lá tươi, thơm ngon, dùng trang trí món ăn",
            "sku": "RAU003",
            "standard_price": 8000.0,
            "cost_price": 5000.0,
            "is_fresh": True,
            "expires_at": datetime.now() + timedelta(days=4),
            "total_quantity": 30,
            "weight": 100.0,
            "unit": "bó",
            "category_name": "Rau Củ",
            "allergens": "Không có",
            "ingredients": "Hành lá tươi 100%"
        },
        
        # Fresh Fruits (Trái Cây)
        {
            "name": "Táo Envy",
            "description": "Táo Envy nhập khẩu New Zealand, giòn ngọt, thơm mát",
            "sku": "TRAI001",
            "standard_price": 120000.0,
            "cost_price": 90000.0,
            "is_fresh": True,
            "expires_at": datetime.now() + timedelta(days=10),
            "total_quantity": 15,
            "weight": 1000.0,
            "unit": "kg",
            "category_name": "Trái Cây",
            "allergens": "Không có",
            "ingredients": "Táo Envy tươi 100%"
        },
        {
            "name": "Chuối sứ",
            "description": "Chuối sứ Việt Nam, chín tự nhiên, ngọt thơm",
            "sku": "TRAI002",
            "standard_price": 25000.0,
            "cost_price": 18000.0,
            "is_fresh": True,
            "expires_at": datetime.now() + timedelta(days=3),
            "total_quantity": 40,
            "weight": 1000.0,
            "unit": "nải",
            "category_name": "Trái Cây",
            "allergens": "Không có",
            "ingredients": "Chuối sứ tươi 100%"
        },
        
        # Fresh Meat (Thịt)
        {
            "name": "Thịt heo ba chỉ",
            "description": "Thịt heo ba chỉ tươi ngon, thích hợp nướng hoặc kho",
            "sku": "THIT001",
            "standard_price": 140000.0,
            "cost_price": 120000.0,
            "is_fresh": True,
            "expires_at": datetime.now() + timedelta(days=2),
            "total_quantity": 8,
            "weight": 500.0,
            "unit": "kg",
            "category_name": "Thịt",
            "allergens": "Không có",
            "ingredients": "Thịt heo tươi 100%"
        },
        {
            "name": "Thịt gà ta",
            "description": "Thịt gà ta thả vườn, thịt chắc ngọt tự nhiên",
            "sku": "THIT002",
            "standard_price": 160000.0,
            "cost_price": 135000.0,
            "is_fresh": True,
            "expires_at": datetime.now() + timedelta(days=2),
            "total_quantity": 5,
            "weight": 1200.0,
            "unit": "con",
            "category_name": "Thịt",
            "allergens": "Không có",
            "ingredients": "Thịt gà ta tươi 100%"
        },
        
        # Fresh Fish (Cá)
        {
            "name": "Cá điêu hồng",
            "description": "Cá điêu hồng tươi sống, thịt ngọt thích hợp nướng",
            "sku": "CA001",
            "standard_price": 180000.0,
            "cost_price": 150000.0,
            "is_fresh": True,
            "expires_at": datetime.now() + timedelta(days=1),
            "total_quantity": 6,
            "weight": 800.0,
            "unit": "con",
            "category_name": "Cá",
            "allergens": "Hải sản",
            "ingredients": "Cá điêu hồng tươi 100%"
        },
        {
            "name": "Cá thu",
            "description": "Cá thu tươi ngon, thịt chắc ngọt thơm",
            "sku": "CA002",
            "standard_price": 220000.0,
            "cost_price": 190000.0,
            "is_fresh": True,
            "expires_at": datetime.now() + timedelta(days=1),
            "total_quantity": 4,
            "weight": 1000.0,
            "unit": "kg",
            "category_name": "Cá",
            "allergens": "Hải sản",
            "ingredients": "Cá thu tươi 100%"
        },
        
        # Fresh Bread (Bánh Mì Tươi)
        {
            "name": "Bánh mì que",
            "description": "Bánh mì que giòn tan, nướng tươi mỗi ngày",
            "sku": "BANH001",
            "standard_price": 3000.0,
            "cost_price": 2000.0,
            "is_fresh": True,
            "expires_at": datetime.now() + timedelta(days=1),
            "total_quantity": 50,
            "weight": 80.0,
            "unit": "cái",
            "category_name": "Bánh Mì Tươi",
            "allergens": "Gluten",
            "ingredients": "Bột mì, nước, muối, men nướng"
        },
        
        # Packaged Snacks (Bánh/Snack)
        {
            "name": "Bánh quy Cosy Marie",
            "description": "Bánh quy bơ thơm ngon, giòn tan hảo hạng",
            "sku": "SNACK001",
            "standard_price": 25000.0,
            "cost_price": 18000.0,
            "is_fresh": False,
            "expires_at": datetime.now() + timedelta(days=365),
            "total_quantity": 24,
            "weight": 300.0,
            "unit": "hộp",
            "category_name": "Bánh/Snack",
            "allergens": "Gluten, Sữa",
            "ingredients": "Bột mì, bơ, đường, trứng, vani"
        },
        {
            "name": "Snack khoai tây Lay's",
            "description": "Snack khoai tây vị tự nhiên, giòn ngon hấp dẫn",
            "sku": "SNACK002",
            "standard_price": 18000.0,
            "cost_price": 13000.0,
            "is_fresh": False,
            "expires_at": datetime.now() + timedelta(days=180),
            "total_quantity": 36,
            "weight": 95.0,
            "unit": "gói",
            "category_name": "Bánh/Snack",
            "allergens": "Có thể chứa Gluten",
            "ingredients": "Khoai tây, dầu thực vật, muối"
        },
        
        # Candy (Kẹo)
        {
            "name": "Kẹo Chupa Chups",
            "description": "Kẹo mút vị trái cây, thương hiệu nổi tiếng thế giới",
            "sku": "KEO001",
            "standard_price": 5000.0,
            "cost_price": 3500.0,
            "is_fresh": False,
            "expires_at": datetime.now() + timedelta(days=730),
            "total_quantity": 100,
            "weight": 12.0,
            "unit": "cái",
            "category_name": "Kẹo",
            "allergens": "Có thể chứa Sữa",
            "ingredients": "Đường, xi-rô glucose, hương liệu tự nhiên"
        },
        {
            "name": "Kẹo dẻo Haribo",
            "description": "Kẹo dẻo hình gấu nhỏ, nhiều vị trái cây",
            "sku": "KEO002",
            "standard_price": 35000.0,
            "cost_price": 25000.0,
            "is_fresh": False,
            "expires_at": datetime.now() + timedelta(days=545),
            "total_quantity": 20,
            "weight": 200.0,
            "unit": "gói",
            "category_name": "Kẹo",
            "allergens": "Có thể chứa Gluten",
            "ingredients": "Đường, xi-rô glucose, gelatin, acid citric"
        },
        
        # Beverages/Dairy (Nước/Sữa)
        {
            "name": "Nước ngọt Coca Cola",
            "description": "Nước ngọt có gas Coca Cola classic, chai thủy tinh",
            "sku": "NUOC001",
            "standard_price": 12000.0,
            "cost_price": 8500.0,
            "is_fresh": False,
            "expires_at": datetime.now() + timedelta(days=365),
            "total_quantity": 48,
            "weight": 390.0,
            "unit": "chai",
            "category_name": "Nước/Sữa",
            "allergens": "Không có",
            "ingredients": "Nước, đường, CO2, hương liệu tự nhiên"
        },
        {
            "name": "Sữa tươi Vinamilk",
            "description": "Sữa tươi nguyên chất không đường, bổ dưỡng",
            "sku": "SUA001",
            "standard_price": 28000.0,
            "cost_price": 22000.0,
            "is_fresh": True,
            "expires_at": datetime.now() + timedelta(days=7),
            "total_quantity": 30,
            "weight": 1000.0,
            "unit": "hộp",
            "category_name": "Nước/Sữa",
            "allergens": "Sữa",
            "ingredients": "Sữa bò tươi nguyên chất 100%"
        },
        {
            "name": "Nước suối Lavie",
            "description": "Nước suối tinh khiết từ nguồn nước thiên nhiên",
            "sku": "NUOC002", 
            "standard_price": 8000.0,
            "cost_price": 6000.0,
            "is_fresh": False,
            "expires_at": datetime.now() + timedelta(days=730),
            "total_quantity": 60,
            "weight": 500.0,
            "unit": "chai",
            "category_name": "Nước/Sữa",
            "allergens": "Không có",
            "ingredients": "Nước suối tinh khiết"
        }
    ]
    
    created_items = []
    
    for item_data in food_items_data:
        # Get category ID if specified
        category_id = None
        if item_data.get("category_name") and item_data["category_name"] in categories:
            category_id = categories[item_data["category_name"]]
        
        # Calculate available quantity (same as total for new items)
        available_quantity = item_data["total_quantity"]
        
        food_item = FoodItem(
            name=item_data["name"],
            description=item_data["description"],
            sku=item_data["sku"],
            standard_price=item_data["standard_price"],
            cost_price=item_data["cost_price"],
            is_fresh=item_data["is_fresh"],
            expires_at=item_data["expires_at"],
            total_quantity=item_data["total_quantity"],
            surplus_quantity=0,
            reserved_quantity=0,
            available_quantity=available_quantity,
            is_marked_for_surplus=False,
            ingredients=item_data["ingredients"],
            allergens=item_data["allergens"],
            weight=item_data["weight"],
            unit=item_data["unit"],
            is_available=True,
            is_active=True,
            store_id=store_id,
            category_id=category_id,
            created_at=datetime.now(),
            updated_at=datetime.now(),
            last_inventory_update=datetime.now()
        )
        
        session.add(food_item)
        session.commit()
        session.refresh(food_item)
        
        # Create initial inventory log
        inventory_log = InventoryLog(
            food_item_id=food_item.id,
            change_type="initial_stock",
            quantity_change=item_data["total_quantity"],
            previous_quantity=0,
            new_quantity=item_data["total_quantity"],
            reason="Initial stock setup for new store",
            created_at=datetime.now()
        )
        session.add(inventory_log)
        
        created_items.append(food_item)
        print(f"✅ Created: {food_item.name} - {food_item.total_quantity} {food_item.unit} @ {food_item.standard_price:,.0f} VND")
    
    session.commit()
    return created_items

def mark_some_items_surplus(session: Session, food_items: list):
    """Mark some items as surplus to demonstrate the feature"""
    surplus_items = [
        {"sku": "RAU001", "surplus_qty": 8, "discount": 30.0},  # Rau cải ngọt
        {"sku": "TRAI002", "surplus_qty": 15, "discount": 25.0},  # Chuối sứ  
        {"sku": "BANH001", "surplus_qty": 20, "discount": 40.0},  # Bánh mì que
        {"sku": "SUA001", "surplus_qty": 5, "discount": 20.0}   # Sữa tươi
    ]
    
    print("\n🏷️ Marking surplus items...")
    
    for surplus_data in surplus_items:
        # Find the food item
        food_item = None
        for item in food_items:
            if item.sku == surplus_data["sku"]:
                food_item = item
                break
        
        if food_item and food_item.available_quantity >= surplus_data["surplus_qty"]:
            # Calculate surplus price
            discount_percent = surplus_data["discount"]
            surplus_price = food_item.standard_price * (1 - discount_percent / 100)
            
            # Update the item
            food_item.surplus_quantity = surplus_data["surplus_qty"]
            food_item.available_quantity -= surplus_data["surplus_qty"]
            food_item.is_marked_for_surplus = True
            food_item.surplus_discount_percentage = discount_percent
            food_item.surplus_price = surplus_price
            food_item.marked_surplus_at = datetime.now()
            
            # Create inventory log
            inventory_log = InventoryLog(
                food_item_id=food_item.id,
                change_type="surplus_marked",
                quantity_change=-surplus_data["surplus_qty"],
                previous_quantity=food_item.available_quantity + surplus_data["surplus_qty"],
                new_quantity=food_item.available_quantity,
                reason=f"Marked {surplus_data['surplus_qty']} {food_item.unit} as surplus with {discount_percent}% discount",
                created_at=datetime.now()
            )
            session.add(inventory_log)
            
            print(f"  📉 {food_item.name}: {surplus_data['surplus_qty']} {food_item.unit} surplus @ {surplus_price:,.0f} VND ({discount_percent}% off)")
    
    session.commit()

def main():
    """Main function to populate the store"""
    print("🚀 Starting WiseBite Store Population...")
    
    with Session(engine) as session:
        # Find the user
        stmt = select(User).where(User.email == 'hekinglois@gmail.com')
        user = session.exec(stmt).first()
        
        if not user:
            print("❌ User hekinglois@gmail.com not found!")
            return
        
        print(f"👤 Found user: {user.full_name} ({user.email})")
        
        # Check if store already exists
        store_stmt = select(Store).where(Store.owner_id == user.id)
        existing_store = session.exec(store_stmt).first()
        
        if existing_store:
            print(f"🏪 Store already exists: {existing_store.name}")
            store = existing_store
        else:
            # Create the store
            store = create_store_for_user(session, user.id)
        
        # Get categories
        categories = get_categories(session)
        print(f"📂 Found {len(categories)} categories")
        
        # Check if store already has items
        existing_items_stmt = select(FoodItem).where(FoodItem.store_id == store.id)
        existing_items = session.exec(existing_items_stmt).all()
        
        if existing_items:
            print(f"📦 Store already has {len(existing_items)} items")
            food_items = existing_items
        else:
            # Create food items
            print("\n📦 Creating food items...")
            food_items = create_food_items(session, store.id, categories)
        
        # Mark some items as surplus
        mark_some_items_surplus(session, food_items)
        
        print(f"\n✨ Store population completed!")
        print(f"🏪 Store: {store.name}")
        print(f"📦 Total items: {len(food_items)}")
        
        # Summary stats
        total_value = sum(item.standard_price * item.total_quantity for item in food_items)
        fresh_items = sum(1 for item in food_items if item.is_fresh)
        surplus_items = sum(1 for item in food_items if item.is_marked_for_surplus)
        
        print(f"💰 Total inventory value: {total_value:,.0f} VND")
        print(f"🥬 Fresh items: {fresh_items}")
        print(f"🏷️ Items with surplus: {surplus_items}")
        
        print("\n🎉 Ready for mobile app testing!")

if __name__ == "__main__":
    main()