#!/bin/bash

echo "🚀 Starting Personal Issue Tracker..."

# Check if PostgreSQL is running
if ! pg_isready -h localhost -p 5432 >/dev/null 2>&1; then
    echo "❌ PostgreSQL is not running. Please start it first."
    exit 1
fi

echo "✅ PostgreSQL is running"

# Start backend in background
echo "🔧 Starting backend..."
cd backend
JWT_SECRET=ZGV2c2VjcmV0a2V5Y2hhbmdlaW5wcm9kdWN0aW9uMTIzNDVzdXBlcnNlY3VyZWtleQ== DB_USERNAME=postgres DB_PASSWORD=postgres mvn spring-boot:run -Dspring-boot.run.profiles=dev &
BACKEND_PID=$!

# Wait a bit for backend to start
sleep 10

# Start frontend
echo "🎨 Starting frontend..."
cd ../frontend
npm run dev &
FRONTEND_PID=$!

echo "✅ Backend running on http://localhost:8080"
echo "✅ Frontend running on http://localhost:3000"
echo ""
echo "🎯 Ready to test! Open http://localhost:3000"
echo ""
echo "Press Ctrl+C to stop all services"

# Wait for user to stop
wait $BACKEND_PID $FRONTEND_PID