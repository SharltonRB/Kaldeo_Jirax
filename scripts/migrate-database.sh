#!/bin/bash

# Database Migration Script for Personal Issue Tracker
# This script handles Flyway database migrations for production

set -e

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
ENV_FILE="${ENV_FILE:-$PROJECT_ROOT/.env.prod}"
LOG_FILE="${LOG_FILE:-$PROJECT_ROOT/logs/migration.log}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging function
log() {
    echo -e "$1" | tee -a "$LOG_FILE"
}

error_exit() {
    log "${RED}❌ Error: $1${NC}"
    exit 1
}

success() {
    log "${GREEN}✅ $1${NC}"
}

warning() {
    log "${YELLOW}⚠️  $1${NC}"
}

info() {
    log "${BLUE}ℹ️  $1${NC}"
}

# Load environment variables
load_environment() {
    if [[ ! -f "$ENV_FILE" ]]; then
        error_exit "Environment file not found: $ENV_FILE"
    fi
    
    set -a
    source "$ENV_FILE"
    set +a
    
    info "Environment loaded from: $ENV_FILE"
}

# Check database connectivity
check_database() {
    info "Checking database connectivity..."
    
    if docker ps | grep -q "issue-tracker-postgres-prod"; then
        if docker exec issue-tracker-postgres-prod pg_isready -U "$POSTGRES_USER" -d "$POSTGRES_DB" > /dev/null; then
            success "Database is accessible"
            return 0
        fi
    fi
    
    error_exit "Database is not accessible"
}

# Get current migration status
get_migration_status() {
    info "Getting current migration status..."
    
    cd "$PROJECT_ROOT/backend"
    
    ./mvnw flyway:info \
        -Dflyway.url="$DATABASE_URL" \
        -Dflyway.user="$DB_USERNAME" \
        -Dflyway.password="$DB_PASSWORD" \
        -Dflyway.locations="filesystem:src/main/resources/db/migration"
}

# Validate pending migrations
validate_migrations() {
    info "Validating pending migrations..."
    
    cd "$PROJECT_ROOT/backend"
    
    ./mvnw flyway:validate \
        -Dflyway.url="$DATABASE_URL" \
        -Dflyway.user="$DB_USERNAME" \
        -Dflyway.password="$DB_PASSWORD" \
        -Dflyway.locations="filesystem:src/main/resources/db/migration"
    
    success "Migration validation passed"
}

# Create backup before migration
create_backup() {
    info "Creating pre-migration backup..."
    
    local timestamp=$(date +"%Y%m%d_%H%M%S")
    local backup_dir="$PROJECT_ROOT/backups"
    local backup_file="$backup_dir/pre_migration_${timestamp}_database.sql"
    
    mkdir -p "$backup_dir"
    
    docker exec issue-tracker-postgres-prod pg_dump -U "$POSTGRES_USER" "$POSTGRES_DB" > "$backup_file"
    
    success "Backup created: $backup_file"
    echo "$backup_file"
}

# Run migrations
run_migrations() {
    info "Running database migrations..."
    
    cd "$PROJECT_ROOT/backend"
    
    ./mvnw flyway:migrate \
        -Dflyway.url="$DATABASE_URL" \
        -Dflyway.user="$DB_USERNAME" \
        -Dflyway.password="$DB_PASSWORD" \
        -Dflyway.locations="filesystem:src/main/resources/db/migration" \
        -Dflyway.outOfOrder=false \
        -Dflyway.validateOnMigrate=true
    
    success "Migrations completed successfully"
}

# Repair migration history (if needed)
repair_migrations() {
    warning "Repairing migration history..."
    
    cd "$PROJECT_ROOT/backend"
    
    ./mvnw flyway:repair \
        -Dflyway.url="$DATABASE_URL" \
        -Dflyway.user="$DB_USERNAME" \
        -Dflyway.password="$DB_PASSWORD" \
        -Dflyway.locations="filesystem:src/main/resources/db/migration"
    
    success "Migration history repaired"
}

# Rollback to specific version
rollback_migration() {
    local target_version="$1"
    
    if [[ -z "$target_version" ]]; then
        error_exit "Target version not specified"
    fi
    
    warning "Rolling back to version: $target_version"
    
    # Flyway doesn't support automatic rollback
    # We need to restore from backup
    error_exit "Automatic rollback not supported. Please use rollback.sh to restore from backup."
}

# Baseline existing database
baseline_database() {
    info "Baselining database..."
    
    cd "$PROJECT_ROOT/backend"
    
    ./mvnw flyway:baseline \
        -Dflyway.url="$DATABASE_URL" \
        -Dflyway.user="$DB_USERNAME" \
        -Dflyway.password="$DB_PASSWORD" \
        -Dflyway.locations="filesystem:src/main/resources/db/migration" \
        -Dflyway.baselineVersion=1 \
        -Dflyway.baselineDescription="Initial baseline"
    
    success "Database baselined"
}

# Clean database (DANGEROUS - only for development)
clean_database() {
    if [[ "${SPRING_PROFILES_ACTIVE:-prod}" == "prod" ]]; then
        error_exit "Cannot clean production database. This operation is only allowed in development."
    fi
    
    warning "This will delete all data in the database!"
    read -p "Are you sure? Type 'DELETE ALL DATA' to confirm: " confirmation
    
    if [[ "$confirmation" != "DELETE ALL DATA" ]]; then
        info "Clean operation cancelled"
        return 0
    fi
    
    cd "$PROJECT_ROOT/backend"
    
    ./mvnw flyway:clean \
        -Dflyway.url="$DATABASE_URL" \
        -Dflyway.user="$DB_USERNAME" \
        -Dflyway.password="$DB_PASSWORD" \
        -Dflyway.locations="filesystem:src/main/resources/db/migration"
    
    warning "Database cleaned"
}

# Main migration function
main() {
    log "${BLUE}🗄️  Database Migration Tool${NC}"
    log "=================================="
    
    # Create log directory
    mkdir -p "$(dirname "$LOG_FILE")"
    
    load_environment
    check_database
    
    case "${1:-migrate}" in
        "migrate")
            get_migration_status
            validate_migrations
            
            # Ask for confirmation
            echo
            read -p "Proceed with migration? (yes/no): " confirmation
            if [[ "$confirmation" != "yes" ]]; then
                info "Migration cancelled"
                exit 0
            fi
            
            BACKUP_FILE=$(create_backup)
            run_migrations
            get_migration_status
            
            success "Migration completed successfully!"
            info "Backup saved to: $BACKUP_FILE"
            ;;
        
        "status"|"info")
            get_migration_status
            ;;
        
        "validate")
            validate_migrations
            ;;
        
        "repair")
            repair_migrations
            ;;
        
        "baseline")
            baseline_database
            ;;
        
        "clean")
            clean_database
            ;;
        
        "rollback")
            rollback_migration "$2"
            ;;
        
        "help")
            echo "Usage: $0 [command] [options]"
            echo ""
            echo "Commands:"
            echo "  migrate   - Run pending migrations (default)"
            echo "  status    - Show migration status"
            echo "  validate  - Validate migration scripts"
            echo "  repair    - Repair migration history"
            echo "  baseline  - Baseline existing database"
            echo "  clean     - Clean database (development only)"
            echo "  rollback  - Rollback to version (use rollback.sh instead)"
            echo "  help      - Show this help message"
            echo ""
            echo "Examples:"
            echo "  $0 migrate              # Run migrations"
            echo "  $0 status               # Check migration status"
            echo "  $0 validate             # Validate migrations"
            ;;
        
        *)
            error_exit "Unknown command: $1. Use 'help' for usage information."
            ;;
    esac
}

main "$@"
