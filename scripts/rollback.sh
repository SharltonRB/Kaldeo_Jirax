#!/bin/bash

# Rollback Script for Personal Issue Tracker
# This script handles rollback to previous deployment state

set -e

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
BACKUP_DIR="${BACKUP_DIR:-$PROJECT_ROOT/backups}"
LOG_FILE="${LOG_FILE:-$PROJECT_ROOT/logs/rollback.log}"

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

# List available backups
list_backups() {
    info "Available backups:"
    echo
    
    if [[ ! -d "$BACKUP_DIR" ]] || [[ -z "$(ls -A "$BACKUP_DIR" 2>/dev/null)" ]]; then
        warning "No backups found in $BACKUP_DIR"
        return 1
    fi
    
    # List database backups
    local backups=($(ls -t "$BACKUP_DIR"/backup_*_database.sql 2>/dev/null))
    
    if [[ ${#backups[@]} -eq 0 ]]; then
        warning "No database backups found"
        return 1
    fi
    
    local i=1
    for backup in "${backups[@]}"; do
        local filename=$(basename "$backup")
        local timestamp=$(echo "$filename" | sed 's/backup_\(.*\)_database.sql/\1/')
        local size=$(du -h "$backup" | cut -f1)
        echo "$i) $timestamp ($size)"
        ((i++))
    done
    
    return 0
}

# Select backup
select_backup() {
    list_backups || error_exit "No backups available for rollback"
    
    echo
    read -p "Select backup number to restore (or 'q' to quit): " selection
    
    if [[ "$selection" == "q" ]]; then
        info "Rollback cancelled"
        exit 0
    fi
    
    local backups=($(ls -t "$BACKUP_DIR"/backup_*_database.sql 2>/dev/null))
    local index=$((selection - 1))
    
    if [[ $index -lt 0 ]] || [[ $index -ge ${#backups[@]} ]]; then
        error_exit "Invalid selection"
    fi
    
    SELECTED_BACKUP="${backups[$index]}"
    BACKUP_TIMESTAMP=$(basename "$SELECTED_BACKUP" | sed 's/backup_\(.*\)_database.sql/\1/')
    
    info "Selected backup: $BACKUP_TIMESTAMP"
}

# Confirm rollback
confirm_rollback() {
    warning "This will rollback the application to backup: $BACKUP_TIMESTAMP"
    warning "Current data will be backed up before rollback"
    echo
    read -p "Are you sure you want to proceed? (yes/no): " confirmation
    
    if [[ "$confirmation" != "yes" ]]; then
        info "Rollback cancelled"
        exit 0
    fi
}

# Create pre-rollback backup
create_pre_rollback_backup() {
    info "Creating pre-rollback backup..."
    
    local timestamp=$(date +"%Y%m%d_%H%M%S")
    local backup_name="pre_rollback_$timestamp"
    
    # Backup current database
    if docker ps | grep -q "issue-tracker-postgres-prod"; then
        docker exec issue-tracker-postgres-prod pg_dump -U "$POSTGRES_USER" "$POSTGRES_DB" > "$BACKUP_DIR/${backup_name}_database.sql"
        success "Pre-rollback backup created: $BACKUP_DIR/${backup_name}_database.sql"
    else
        warning "Database container not running - skipping pre-rollback backup"
    fi
}

# Stop application
stop_application() {
    info "Stopping application..."
    
    cd "$PROJECT_ROOT"
    docker-compose -f docker-compose.prod.yml down
    
    success "Application stopped"
}

# Restore database
restore_database() {
    info "Restoring database from backup..."
    
    cd "$PROJECT_ROOT"
    
    # Start only database container
    docker-compose -f docker-compose.prod.yml up -d postgres
    
    # Wait for database to be ready
    info "Waiting for database to be ready..."
    local max_attempts=30
    local attempt=1
    
    while [[ $attempt -le $max_attempts ]]; do
        if docker exec issue-tracker-postgres-prod pg_isready -U "$POSTGRES_USER" > /dev/null 2>&1; then
            success "Database is ready"
            break
        fi
        
        if [[ $attempt -eq $max_attempts ]]; then
            error_exit "Database failed to start within timeout"
        fi
        
        sleep 2
        ((attempt++))
    done
    
    # Drop and recreate database
    info "Recreating database..."
    docker exec issue-tracker-postgres-prod psql -U "$POSTGRES_USER" -c "DROP DATABASE IF EXISTS $POSTGRES_DB;"
    docker exec issue-tracker-postgres-prod psql -U "$POSTGRES_USER" -c "CREATE DATABASE $POSTGRES_DB;"
    
    # Restore from backup
    info "Restoring data..."
    cat "$SELECTED_BACKUP" | docker exec -i issue-tracker-postgres-prod psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"
    
    success "Database restored successfully"
}

# Restore Docker images
restore_docker_images() {
    info "Restoring Docker images..."
    
    # Get image tags from backup timestamp
    local image_tag="backup-$BACKUP_TIMESTAMP"
    
    # Check if backup images exist
    if docker images | grep -q "$image_tag"; then
        info "Found backup images with tag: $image_tag"
        
        # Tag backup images as latest
        docker tag "issue-tracker-backend:$image_tag" "issue-tracker-backend:latest"
        docker tag "issue-tracker-frontend:$image_tag" "issue-tracker-frontend:latest"
        
        success "Docker images restored"
    else
        warning "No backup images found - using current images"
    fi
}

# Start application
start_application() {
    info "Starting application..."
    
    cd "$PROJECT_ROOT"
    docker-compose -f docker-compose.prod.yml up -d
    
    # Wait for services to be healthy
    info "Waiting for services to be healthy..."
    local max_attempts=30
    local attempt=1
    
    while [[ $attempt -le $max_attempts ]]; do
        if docker-compose -f docker-compose.prod.yml ps | grep -q "Up (healthy)"; then
            success "Services are healthy"
            break
        fi
        
        if [[ $attempt -eq $max_attempts ]]; then
            error_exit "Services failed to become healthy within timeout"
        fi
        
        info "Attempt $attempt/$max_attempts - waiting for services..."
        sleep 10
        ((attempt++))
    done
    
    success "Application started successfully"
}

# Verify rollback
verify_rollback() {
    info "Verifying rollback..."
    
    # Check backend health
    local backend_url="http://localhost:${BACKEND_PORT:-8080}/actuator/health"
    if curl -f -s "$backend_url" > /dev/null; then
        success "Backend health check passed"
    else
        error_exit "Backend health check failed"
    fi
    
    # Check database connectivity
    if docker exec issue-tracker-postgres-prod pg_isready -U "$POSTGRES_USER" -d "$POSTGRES_DB" > /dev/null; then
        success "Database connectivity check passed"
    else
        error_exit "Database connectivity check failed"
    fi
    
    success "Rollback verification completed"
}

# Main rollback function
main() {
    log "${YELLOW}🔄 Starting Rollback Process${NC}"
    log "=================================="
    
    # Create log directory
    mkdir -p "$(dirname "$LOG_FILE")"
    
    # Load environment variables
    if [[ -f "$PROJECT_ROOT/.env.prod" ]]; then
        set -a
        source "$PROJECT_ROOT/.env.prod"
        set +a
    fi
    
    # Run rollback steps
    select_backup
    confirm_rollback
    create_pre_rollback_backup
    stop_application
    restore_database
    restore_docker_images
    start_application
    verify_rollback
    
    success "🎉 Rollback completed successfully!"
    info "Rollback log saved to: $LOG_FILE"
    info "Application restored to backup: $BACKUP_TIMESTAMP"
}

# Handle script arguments
case "${1:-rollback}" in
    "rollback")
        main
        ;;
    "list")
        list_backups
        ;;
    "help")
        echo "Usage: $0 [rollback|list|help]"
        echo "  rollback - Perform rollback to previous backup (default)"
        echo "  list     - List available backups"
        echo "  help     - Show this help message"
        ;;
    *)
        error_exit "Unknown command: $1. Use 'help' for usage information."
        ;;
esac
