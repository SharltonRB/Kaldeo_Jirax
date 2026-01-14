#!/bin/bash

echo "🚀 Starting Personal Issue Tracker (Development)..."

# Check if PostgreSQL is running
if ! pg_isready -h localhost -p 5432 >/dev/null 2>&1; then
    echo "❌ PostgreSQL is not running. Please start it first:"
    echo "   brew services start postgresql"
    echo "   or: docker-compose up -d postgres"
    exit 1
fi

echo "✅ PostgreSQL is running"

# Start backend in background
echo "🔧 Starting backend..."
cd backend
JWT_SECRET=ZGV2c2VjcmV0a2V5Y2hhbmdlaW5wcm9kdWN0aW9uMTIzNDVzdXBlcnNlY3VyZWtleQ== DB_USERNAME=postgres DB_PASSWORD=postgres mvn spring-boot:run -Dspring-boot.run.profiles=dev &
BACKEND_PID=$!

# Wait a bit for backend to start
echo "⏳ Waiting for backend to start..."
sleep 15

# Check if backend started successfully
if ! curl -s http://localhost:8080/api/actuator/health >/dev/null 2>&1; then
    echo "⚠️  Backend might still be starting... continuing with frontend"
fi

# Start frontend
echo "🎨 Starting frontend..."
cd ../frontend
npm run dev &
FRONTEND_PID=$!

echo ""
echo "✅ Backend starting on http://localhost:8080"
echo "✅ Frontend starting on http://localhost:3000"
echo ""
echo "🎯 Ready to test! Open http://localhost:3000"
echo ""
echo "📋 Test credentials:"
echo "   Email: john.doe@example.com"
echo "   Password: password123"
echo ""
echo "Press Ctrl+C to stop all services"

# Function to cleanup on exit
cleanup() {
    echo ""
    echo "🛑 Stopping services..."
    kill $BACKEND_PID 2>/dev/null
    kill $FRONTEND_PID 2>/dev/null
    pkill -f "PersonalIssueTrackerApplication" 2>/dev/null
    pkill -f "vite" 2>/dev/null
    echo "✅ All services stopped"
    exit 0
}

# Trap Ctrl+C
trap cleanup SIGINT

# Wait for user to stop
wait $BACKEND_PID $FRONTEND_PID