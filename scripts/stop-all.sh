#!/bin/bash

echo "🛑 Stopping Personal Issue Tracker..."

# Stop backend (Java processes)
echo "🔧 Stopping backend..."
pkill -f "PersonalIssueTrackerApplication"

# Stop frontend (Node/Vite processes)
echo "🎨 Stopping frontend..."
pkill -f "vite"

# Alternative: Kill by port
echo "🔍 Cleaning up ports..."
lsof -ti:8080 | xargs kill -9 2>/dev/null || true
lsof -ti:3000 | xargs kill -9 2>/dev/null || true

echo "✅ All services stopped!"
echo "💡 PostgreSQL is still running (as it should be)"