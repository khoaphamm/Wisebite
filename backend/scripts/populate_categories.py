"""
Script to populate initial product categories for WiseBite merchants
Run this after applying the database migration
"""

import asyncio
import uuid
from datetime import datetime
from sqlmodel import Session, select
from app.core.db import engine
from app.models import Category

async def populate_initial_categories():
    """Populate the initial category hierarchy"""
    
    categories_data = [
        # Top-level categories
        {
            "name": "Fresh Grocery",
            "description": "Fresh grocery items including meat, fish, vegetables, fruits, and fresh bread",
            "parent": None,
            "subcategories": [
                {"name": "Thịt", "description": "Fresh meat products"},
                {"name": "Cá", "description": "Fresh fish and seafood"},
                {"name": "Rau Củ", "description": "Fresh vegetables and roots"},
                {"name": "Trái Cây", "description": "Fresh fruits"},
                {"name": "Bánh Mì Tươi", "description": "Fresh bread and bakery items"},
            ]
        },
        {
            "name": "Packaged Goods", 
            "description": "Packaged food items including snacks, candy, and beverages",
            "parent": None,
            "subcategories": [
                {"name": "Bánh/Snack", "description": "Packaged cakes, cookies, and snacks"},
                {"name": "Kẹo", "description": "Candy and confectionery"},
                {"name": "Nước/Sữa", "description": "Beverages and dairy products"},
            ]
        }
    ]
    
    with Session(engine) as session:
        try:
            # Check if categories already exist
            existing_categories = session.exec(select(Category)).all()
            if existing_categories:
                print("Categories already exist. Skipping population.")
                return
            
            created_categories = {}
            
            # Create top-level categories first
            for category_data in categories_data:
                parent_category = Category(
                    id=uuid.uuid4(),
                    name=category_data["name"],
                    description=category_data["description"],
                    parent_category_id=None,
                    is_active=True,
                    created_at=datetime.now()
                )
                session.add(parent_category)
                session.flush()  # Flush to get the ID
                created_categories[category_data["name"]] = parent_category.id
                print(f"Created parent category: {category_data['name']}")
                
                # Create subcategories
                for sub_data in category_data["subcategories"]:
                    subcategory = Category(
                        id=uuid.uuid4(),
                        name=sub_data["name"],
                        description=sub_data["description"],
                        parent_category_id=parent_category.id,
                        is_active=True,
                        created_at=datetime.now()
                    )
                    session.add(subcategory)
                    created_categories[sub_data["name"]] = subcategory.id
                    print(f"  Created subcategory: {sub_data['name']}")
            
            session.commit()
            print(f"\n✅ Successfully created {len(created_categories)} categories!")
            
            # Print category hierarchy
            print("\n📋 Category Hierarchy:")
            print("=" * 50)
            for category_data in categories_data:
                print(f"📁 {category_data['name']}")
                for sub_data in category_data["subcategories"]:
                    print(f"  └── 📄 {sub_data['name']}")
            
        except Exception as e:
            session.rollback()
            print(f"❌ Error creating categories: {e}")
            raise

def get_category_mapping():
    """Get mapping of category names to IDs for easy reference"""
    with Session(engine) as session:
        categories = session.exec(select(Category)).all()
        return {cat.name: cat.id for cat in categories}

if __name__ == "__main__":
    print("🚀 Populating initial product categories...")
    asyncio.run(populate_initial_categories())
    
    print("\n📊 Category mapping:")
    mapping = get_category_mapping()
    for name, cat_id in mapping.items():
        print(f"  {name}: {cat_id}")