#!/bin/bash
# Script to generate BCrypt hash for password123

# Use htpasswd if available (comes with Apache)
if command -v htpasswd &> /dev/null; then
    echo "Using htpasswd to generate BCrypt hash..."
    htpasswd -bnBC 10 "" password123 | tr -d ':\n' | sed 's/^//'
    exit 0
fi

# Alternative: Use online BCrypt or manual generation
echo "Please use one of these methods to generate BCrypt hash for 'password123':"
echo "1. Online: https://bcrypt-generator.com/ (use rounds=10)"
echo "2. Or run the Java class from within the Spring Boot application"
echo ""
echo "Expected hash format: \$2a\$10\$..."
