#!/usr/bin/env python3
"""Test if the current hash in the database works with password123"""

import bcrypt

# Hash actual en la base de datos
current_hash = "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi"
password = "password123"

print(f"Testing hash: {current_hash}")
print(f"Password: {password}")

try:
    result = bcrypt.checkpw(password.encode('utf-8'), current_hash.encode('utf-8'))
    print(f"Result: {result}")
except Exception as e:
    print(f"Error: {e}")
