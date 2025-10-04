#!/usr/bin/env python3

import requests
import json
import sys

# Test the customer orders API to check store information
BASE_URL = "http://localhost:8000/api/v1"

def test_orders_api():
    """Test the orders API to see store information"""
    
    # First, let's get a customer token (you'll need to replace this with actual auth)
    # For now, let's just test the endpoint structure
    
    try:
        response = requests.get(f"{BASE_URL}/orders/me", timeout=10)
        print(f"Status Code: {response.status_code}")
        print(f"Response Headers: {dict(response.headers)}")
        
        if response.status_code == 401:
            print("❌ Unauthorized - need valid auth token")
            print("This is expected without proper authentication")
            return
            
        if response.status_code == 200:
            data = response.json()
            print("✅ API Response:")
            print(json.dumps(data, indent=2, default=str))
            
            # Check if orders have store information
            if 'data' in data and data['data']:
                first_order = data['data'][0]
                if 'store' in first_order:
                    print("✅ Store field found in order!")
                    print(f"Store: {first_order['store']}")
                else:
                    print("❌ No store field in order")
                    print("Available fields:", list(first_order.keys()))
            else:
                print("📝 No orders found")
        else:
            print(f"❌ API Error: {response.status_code}")
            print(response.text)
            
    except requests.exceptions.RequestException as e:
        print(f"❌ Request failed: {e}")

if __name__ == "__main__":
    print("🔍 Testing Customer Orders API for Store Information")
    print("=" * 50)
    test_orders_api()