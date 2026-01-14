#!/bin/bash

# Test script para probar la activación de sprint con fechas

echo "Testing sprint activation with date updates..."

# Primero, hacer login para obtener un token
echo "1. Getting authentication token..."
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "sharlton.romero@gmail.com",
    "password": "password"
  }')

echo "Login response: $LOGIN_RESPONSE"

# Extraer el token (asumiendo que viene en formato JSON)
TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo "❌ Failed to get authentication token"
    exit 1
fi

echo "✅ Got token: ${TOKEN:0:20}..."

# Ahora probar la activación del sprint
echo "2. Activating sprint with new dates..."
ACTIVATION_RESPONSE=$(curl -s -X POST http://localhost:8080/api/sprints/16/activate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "newStartDate": "2026-01-06",
    "newEndDate": "2026-01-20"
  }')

echo "Activation response: $ACTIVATION_RESPONSE"

echo "3. Checking sprint dates in database..."
psql -h localhost -p 5432 -U postgres -d issue_tracker_dev -c "SELECT id, name, start_date, end_date, status FROM sprints WHERE id = 16;"