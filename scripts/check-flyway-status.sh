#!/bin/bash
# Script to check Flyway migration status

set -e

echo "🔍 Checking Flyway migration status..."
echo ""

# Check if PostgreSQL is running
if ! docker ps | grep -q "issue-tracker-postgres.*Up"; then
    echo "❌ PostgreSQL is not running"
    echo "   Start it with: docker-compose up -d postgres"
    exit 1
fi

echo "✅ PostgreSQL is running"
echo ""

# Check if flyway_schema_history table exists
HISTORY_EXISTS=$(docker exec issue-tracker-postgres psql -U postgres -d issue_tracker_dev -tAc "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'flyway_schema_history');" 2>/dev/null || echo "false")

if [ "$HISTORY_EXISTS" = "t" ]; then
    echo "📊 Current migration status:"
    echo ""
    docker exec issue-tracker-postgres psql -U postgres -d issue_tracker_dev -c "SELECT version, description, type, installed_on, success FROM flyway_schema_history ORDER BY installed_rank;" 2>/dev/null || echo "Could not read migration history"
    echo ""
    
    # Check for pending migrations
    echo "📁 Available migration files:"
    ls -1 backend/src/main/resources/db/migration/ | grep "^V" | sort
    echo ""
    
    # Get last applied version
    LAST_VERSION=$(docker exec issue-tracker-postgres psql -U postgres -d issue_tracker_dev -tAc "SELECT version FROM flyway_schema_history WHERE success = true ORDER BY installed_rank DESC LIMIT 1;" 2>/dev/null | tr -d ' ')
    
    if [ -n "$LAST_VERSION" ]; then
        echo "✅ Last applied migration: V${LAST_VERSION}"
    fi
else
    echo "ℹ️  No migration history found (fresh database)"
    echo "   Migrations will be applied on next application start"
fi

echo ""
echo "To reset the database completely:"
echo "   ./scripts/reset-dev-database.sh"
