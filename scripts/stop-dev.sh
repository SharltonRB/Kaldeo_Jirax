#!/bin/bash

echo "🛑 Stopping Personal Issue Tracker..."

# Stop backend (Java processes)
echo "🔧 Stopping backend..."
pkill -f "PersonalIssueTrackerApplication"
if [ $? -eq 0 ]; then
    echo "   ✅ Backend stopped"
else
    echo "   ℹ️  No backend process found"
fi

# Stop frontend (Node/Vite processes)
echo "🎨 Stopping frontend..."
pkill -f "vite"
if [ $? -eq 0 ]; then
    echo "   ✅ Frontend stopped"
else
    echo "   ℹ️  No frontend process found"
fi

# Alternative: Kill by port (more aggressive)
echo "🔍 Cleaning up ports..."
lsof -ti:8080 | xargs kill -9 2>/dev/null && echo "   ✅ Port 8080 cleaned" || echo "   ℹ️  Port 8080 already free"
lsof -ti:3000 | xargs kill -9 2>/dev/null && echo "   ✅ Port 3000 cleaned" || echo "   ℹ️  Port 3000 already free"

echo ""
echo "✅ All services stopped!"
echo "💡 PostgreSQL is still running (as it should be)"
echo ""
echo "To restart everything, run: ./start-dev.sh"