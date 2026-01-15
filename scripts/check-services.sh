#!/bin/bash

echo "🔍 Checking Personal Issue Tracker Services Status..."
echo ""

# Check Docker
echo "🐳 Docker:"
if docker info >/dev/null 2>&1; then
    echo "   ✅ Docker is running"
else
    echo "   ❌ Docker is NOT running"
fi

# Check PostgreSQL container
echo ""
echo "🗄️  PostgreSQL Container:"
if docker ps | grep -q issue-tracker-postgres; then
    echo "   ✅ PostgreSQL container is running"
    if docker exec issue-tracker-postgres pg_isready -U postgres >/dev/null 2>&1; then
        echo "   ✅ PostgreSQL is accepting connections"
    else
        echo "   ⚠️  PostgreSQL container is running but not ready"
    fi
else
    echo "   ❌ PostgreSQL container is NOT running"
fi

# Check Redis container
echo ""
echo "📦 Redis Container:"
if docker ps | grep -q issue-tracker-redis; then
    echo "   ✅ Redis container is running"
else
    echo "   ❌ Redis container is NOT running"
fi

# Check Backend
echo ""
echo "🔧 Backend (Spring Boot):"
if pgrep -f "PersonalIssueTrackerApplication" >/dev/null; then
    echo "   ✅ Backend process is running"
    if curl -s http://localhost:8080/actuator/health >/dev/null 2>&1; then
        echo "   ✅ Backend is responding on http://localhost:8080"
    else
        echo "   ⚠️  Backend process is running but not responding yet"
    fi
else
    echo "   ❌ Backend is NOT running"
fi

# Check Frontend
echo ""
echo "🎨 Frontend (Vite):"
if pgrep -f "vite" >/dev/null; then
    echo "   ✅ Frontend process is running"
    if curl -s http://localhost:3000 >/dev/null 2>&1; then
        echo "   ✅ Frontend is responding on http://localhost:3000"
    else
        echo "   ⚠️  Frontend process is running but not responding yet"
    fi
else
    echo "   ❌ Frontend is NOT running"
fi

# Check ports
echo ""
echo "🔌 Port Status:"
if lsof -ti:5432 >/dev/null 2>&1; then
    echo "   ✅ Port 5432 (PostgreSQL) is in use"
else
    echo "   ❌ Port 5432 (PostgreSQL) is free"
fi

if lsof -ti:6379 >/dev/null 2>&1; then
    echo "   ✅ Port 6379 (Redis) is in use"
else
    echo "   ❌ Port 6379 (Redis) is free"
fi

if lsof -ti:8080 >/dev/null 2>&1; then
    echo "   ✅ Port 8080 (Backend) is in use"
else
    echo "   ❌ Port 8080 (Backend) is free"
fi

if lsof -ti:3000 >/dev/null 2>&1; then
    echo "   ✅ Port 3000 (Frontend) is in use"
else
    echo "   ❌ Port 3000 (Frontend) is free"
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "💡 Quick Actions:"
echo "   Start all services:  ./scripts/start-dev.sh"
echo "   Stop all services:   ./scripts/stop-dev.sh"
echo "   View backend logs:   docker-compose logs -f postgres"
echo "   View all containers: docker-compose ps"
echo ""
