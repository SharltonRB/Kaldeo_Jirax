#!/usr/bin/env python3
"""Verify that the new hashes work correctly"""

import bcrypt

# New hashes from V3 migration
hashes = [
    "$2b$10$xOH3XVhVbYSyVrTP9SleZe/NDT/0OSEnwcWyekoBlUDndYTQjSipW",  # john.doe
    "$2b$10$GeXMs6fsY2MGk3AtHqmgxOiUQgee.fFX/Gyt7IquO0bclrhfX7KuO",  # jane.smith
    "$2b$10$.s7B5t4D5zj9VJojA/sLcuV3UpzYkxgpVpenfUcMjyfMPj.pHQvwy",  # admin
]

users = ["john.doe@example.com", "jane.smith@example.com", "admin@example.com"]
password = "password123"

print("Verifying password hashes for all users...")
print(f"Password: {password}")
print()

all_valid = True
for user, hash_str in zip(users, hashes):
    result = bcrypt.checkpw(password.encode('utf-8'), hash_str.encode('utf-8'))
    status = "✅ VALID" if result else "❌ INVALID"
    print(f"{status} - {user}")
    if not result:
        all_valid = False

print()
if all_valid:
    print("✅ All hashes are valid! Authentication will work correctly.")
else:
    print("❌ Some hashes are invalid! There's a problem.")
