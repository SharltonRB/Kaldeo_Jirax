#!/bin/bash
# Script to reset development database and ensure clean state

set -e

echo "🔄 Resetting development database..."
echo ""

# Stop all containers
echo "📦 Stopping containers..."
docker-compose down -v

echo ""
echo "✅ Database reset complete!"
echo ""
echo "🚀 Starting fresh database..."
docker-compose up -d postgres

echo ""
echo "⏳ Waiting for PostgreSQL to be ready..."
sleep 5

# Check if PostgreSQL is ready
until docker-compose exec -T postgres pg_isready -U postgres > /dev/null 2>&1; do
    echo "   Waiting for PostgreSQL..."
    sleep 2
done

echo ""
echo "✅ PostgreSQL is ready!"
echo ""
echo "📝 Database will be initialized automatically when you start the backend."
echo ""
echo "To start the full application, run:"
echo "   ./scripts/start-dev.sh"
echo ""
echo "Development credentials:"
echo "   Email: john.doe@example.com"
echo "   Password: password123"
echo ""
echo "See backend/DEVELOPMENT_CREDENTIALS.md for all test users."
