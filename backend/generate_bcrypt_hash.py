#!/usr/bin/env python3
"""
Generate BCrypt hashes for development passwords.
This uses the same BCrypt algorithm as Spring Security.
"""

import sys

try:
    import bcrypt
except ImportError:
    print("Error: bcrypt module not installed")
    print("Install it with: pip3 install bcrypt")
    sys.exit(1)

def generate_hash(password, rounds=10):
    """Generate BCrypt hash with specified rounds (default 10 to match Spring Security)"""
    salt = bcrypt.gensalt(rounds=rounds)
    hash_bytes = bcrypt.hashpw(password.encode('utf-8'), salt)
    return hash_bytes.decode('utf-8')

def verify_hash(password, hash_str):
    """Verify that a password matches a hash"""
    return bcrypt.checkpw(password.encode('utf-8'), hash_str.encode('utf-8'))

if __name__ == "__main__":
    password = "password123"
    
    print(f"Generating BCrypt hash for: {password}")
    print(f"Using 10 rounds (matches Spring Security default)")
    print()
    
    # Generate 3 different hashes (BCrypt generates unique hashes each time)
    for i in range(3):
        hash_str = generate_hash(password)
        verified = verify_hash(password, hash_str)
        print(f"Hash {i+1}: {hash_str}")
        print(f"Verified: {verified}")
        print()
    
    print("Note: Each hash is unique but all will validate the same password")
    print("Use any of these hashes in your SQL migration file")
