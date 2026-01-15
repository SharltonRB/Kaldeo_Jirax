#!/bin/bash
# Script to generate correct BCrypt hashes using the Spring Boot application

set -e

echo "Generating BCrypt hashes for development users..."
echo ""

# Compile and run the hash generator
cd "$(dirname "$0")"

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed or not in PATH"
    exit 1
fi

# Generate hash using Spring Boot's BCrypt
mvn -q exec:java -Dexec.mainClass="GenerateHash" -Dexec.classpathScope=compile

echo ""
echo "Use these hashes in your SQL migration files"
