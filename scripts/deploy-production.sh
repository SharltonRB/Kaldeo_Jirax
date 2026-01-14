#!/bin/bash

# Production Deployment Script for Personal Issue Tracker
# This script handles the complete production deployment process

set -e

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
ENV_FILE="${ENV_FILE:-$PROJECT_ROOT/.env.prod}"
BACKUP_DIR="${BACKUP_DIR:-$PROJECT_ROOT/backups}"
LOG_FILE="${LOG_FILE:-$PROJECT_ROOT/logs/deployment.log}"

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

# Error handling
error_exit() {
    log "${RED}❌ Error: $1${NC}"
    exit 1
}

# Success message
success() {
    log "${GREEN}✅ $1${NC}"
}

# Warning message
warning() {
    log "${YELLOW}⚠️  $1${NC}"
}

# Info message
info() {
    log "${BLUE}ℹ️  $1${NC}"
}

# Check prerequisites
check_prerequisites() {
    info "Checking prerequisites..."
    
    # Check if Docker is installed and running
    if ! command -v docker &> /dev/null; then
        error_exit "Docker is not installed. Please install Docker first."
    fi
    
    if ! docker info &> /dev/null; then
        error_exit "Docker is not running. Please start Docker first."
    fi
    
    # Check if Docker Compose is available
    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        error_exit "Docker Compose is not available. Please install Docker Compose."
    fi
    
    # Check if environment file exists
    if [[ ! -f "$ENV_FILE" ]]; then
        error_exit "Environment file not found: $ENV_FILE. Please create it from .env.prod.template"
    fi
    
    success "Prerequisites check passed"
}

# Validate environment configuration
validate_environment() {
    info "Validating environment configuration..."
    
    # Source environment file
    set -a
    source "$ENV_FILE"
    set +a
    
    # Check critical environment variables
    local required_vars=(
        "DATABASE_URL"
        "DB_USERNAME"
        "DB_PASSWORD"
        "JWT_SECRET"
        "POSTGRES_PASSWORD"
    )
    
    for var in "${required_vars[@]}"; do
        if [[ -z "${!var}" ]]; then
            error_exit "Required environment variable $var is not set in $ENV_FILE"
        fi
    done
    
    # Check JWT secret strength
    if [[ ${#JWT_SECRET} -lt 32 ]]; then
        error_exit "JWT_SECRET must be at least 32 characters long for security"
    fi
    
    # Check if default passwords are being used
    if [[ "$DB_PASSWORD" == "CHANGE_ME_SECURE_PASSWORD_HERE" ]] || 
       [[ "$POSTGRES_PASSWORD" == "CHANGE_ME_POSTGRES_PASSWORD_HERE" ]] ||
       [[ "$JWT_SECRET" == "CHANGE_ME_GENERATE_SECURE_JWT_SECRET_BASE64_ENCODED_HERE" ]]; then
        error_exit "Please update default passwords and secrets in $ENV_FILE"
    fi
    
    success "Environment configuration validated"
}

# Create backup
create_backup() {
    info "Creating backup..."
    
    local timestamp=$(date +"%Y%m%d_%H%M%S")
    local backup_name="backup_$timestamp"
    
    mkdir -p "$BACKUP_DIR"
    
    # Backup database if running
    if docker ps | grep -q "issue-tracker-postgres-prod"; then
        info "Backing up database..."
        docker exec issue-tracker-postgres-prod pg_dump -U "$POSTGRES_USER" "$POSTGRES_DB" > "$BACKUP_DIR/${backup_name}_database.sql"
        success "Database backup created: $BACKUP_DIR/${backup_name}_database.sql"
    fi
    
    # Backup volumes
    if docker volume ls | grep -q "personal-issue-tracker_postgres_prod_data"; then
        info "Backing up database volume..."
        docker run --rm -v personal-issue-tracker_postgres_prod_data:/data -v "$BACKUP_DIR":/backup alpine tar czf "/backup/${backup_name}_postgres_volume.tar.gz" -C /data .
        success "Database volume backup created: $BACKUP_DIR/${backup_name}_postgres_volume.tar.gz"
    fi
    
    success "Backup completed"
}

# Build application
build_application() {
    info "Building application..."
    
    cd "$PROJECT_ROOT"
    
    # Build backend
    info "Building backend..."
    cd backend
    ./mvnw clean package -DskipTests -Pprod
    cd ..
    
    # Build Docker images
    info "Building Docker images..."
    docker-compose -f docker-compose.prod.yml build --no-cache
    
    success "Application built successfully"
}

# Deploy application
deploy_application() {
    info "Deploying application..."
    
    cd "$PROJECT_ROOT"
    
    # Stop existing containers
    info "Stopping existing containers..."
    docker-compose -f docker-compose.prod.yml down || true
    
    # Start new containers
    info "Starting new containers..."
    docker-compose -f docker-compose.prod.yml --env-file "$ENV_FILE" up -d
    
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
    
    success "Application deployed successfully"
}

# Run health checks
run_health_checks() {
    info "Running health checks..."
    
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
    
    # Check Redis connectivity
    if docker exec issue-tracker-redis-prod redis-cli ping | grep -q "PONG"; then
        success "Redis connectivity check passed"
    else
        warning "Redis connectivity check failed (non-critical)"
    fi
    
    success "Health checks completed"
}

# Show deployment status
show_status() {
    info "Deployment Status:"
    echo
    docker-compose -f docker-compose.prod.yml ps
    echo
    info "Application URLs:"
    echo "Backend API: http://localhost:${BACKEND_PORT:-8080}/api"
    echo "Health Check: http://localhost:${BACKEND_PORT:-8080}/actuator/health"
    echo "Metrics: http://localhost:${BACKEND_PORT:-8080}/actuator/metrics"
    if [[ "${SSL_ENABLED:-false}" == "true" ]]; then
        echo "HTTPS Backend: https://localhost:${BACKEND_PORT:-8080}/api"
    fi
    echo
    info "Logs:"
    echo "View logs: docker-compose -f docker-compose.prod.yml logs -f"
    echo "Backend logs: docker-compose -f docker-compose.prod.yml logs -f backend"
    echo "Database logs: docker-compose -f docker-compose.prod.yml logs -f postgres"
}

# Main deployment function
main() {
    log "${GREEN}🚀 Starting Production Deployment${NC}"
    log "=================================="
    
    # Create log directory
    mkdir -p "$(dirname "$LOG_FILE")"
    
    # Run deployment steps
    check_prerequisites
    validate_environment
    create_backup
    build_application
    deploy_application
    run_health_checks
    show_status
    
    success "🎉 Production deployment completed successfully!"
    info "Deployment log saved to: $LOG_FILE"
}

# Handle script arguments
case "${1:-deploy}" in
    "deploy")
        main
        ;;
    "backup")
        create_backup
        ;;
    "health")
        run_health_checks
        ;;
    "status")
        show_status
        ;;
    "help")
        echo "Usage: $0 [deploy|backup|health|status|help]"
        echo "  deploy  - Full production deployment (default)"
        echo "  backup  - Create backup only"
        echo "  health  - Run health checks only"
        echo "  status  - Show deployment status"
        echo "  help    - Show this help message"
        ;;
    *)
        error_exit "Unknown command: $1. Use 'help' for usage information."
        ;;
esac