#!/usr/bin/env python3
"""Test script to debug order serialization"""

import uuid
from app.core.db import get_session
from app.crud import get_orders_by_store
from app.models import Store, Order
from app.schemas.order import OrderPublic
from sqlmodel import select
import json

def test_order_serialization():
    with get_session() as session:
        # Get a store
        store = session.exec(select(Store).limit(1)).first()
        if not store:
            print("No stores found")
            return
            
        print(f"Store found: {store.name}")
        
        # Get orders using the same function as the API
        orders = get_orders_by_store(session, store.id)
        print(f"Found {len(orders)} orders")
        
        if orders:
            order = orders[0]
            print(f"\n--- Raw Order Object ---")
            print(f"Order ID: {order.id}")
            print(f"Customer: {order.customer}")
            print(f"Customer ID: {order.customer_id}")
            print(f"Items: {order.items}")
            
            if order.customer:
                print(f"Customer name: {order.customer.full_name}")
            else:
                print("Customer is None!")
                
            if order.items:
                print(f"Items count: {len(order.items)}")
                for item in order.items:
                    print(f"  Item ID: {item.id}")
                    print(f"  Surprise bag: {item.surprise_bag}")
                    print(f"  Food item: {item.food_item}")
            else:
                print("Items is None!")
            
            # Try to convert to OrderPublic
            try:
                print(f"\n--- Converting to OrderPublic ---")
                order_public = OrderPublic.model_validate(order)
                print("Conversion successful!")
                print(f"OrderPublic customer: {order_public.customer}")
                print(f"OrderPublic items: {order_public.items}")
                
                # Convert to JSON to see the final output
                order_dict = order_public.model_dump()
                print(f"\n--- Final JSON ---")
                print(json.dumps(order_dict, default=str, indent=2))
                
            except Exception as e:
                print(f"Conversion failed: {e}")
                import traceback
                traceback.print_exc()

if __name__ == "__main__":
    test_order_serialization()