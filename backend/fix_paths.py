#!/usr/bin/env python3

import re

# Read the file
with open(r'E:\DIN\note-taking-2025\side-project\Wisebite\backend\tests\api\endpoints\test_order_comprehensive.py', 'r', encoding='utf-8') as f:
    content = f.read()

# Fix API paths - be more specific
content = content.replace('"/api/v1/store"', '"/api/v1/stores/"')
content = content.replace('"/api/v1/food-item"', '"/api/v1/food-items/"') 
content = content.replace('"/api/v1/order"', '"/api/v1/orders/"')

# Write the file back
with open(r'E:\DIN\note-taking-2025\side-project\Wisebite\backend\tests\api\endpoints\test_order_comprehensive.py', 'w', encoding='utf-8') as f:
    f.write(content)

print("Fixed remaining API paths in test_order_comprehensive.py")