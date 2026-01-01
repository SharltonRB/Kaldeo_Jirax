#!/bin/bash

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Función para logging
log() {
    echo -e "${BLUE}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')] ✓${NC} $1"
}

log_error() {
    echo -e "${RED}[$(date +'%Y-%m-%d %H:%M:%S')] ✗${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[$(date +'%Y-%m-%d %H:%M:%S')] ⚠${NC} $1"
}

# Función para mostrar ayuda
show_help() {
    echo "Personal Issue Tracker - Structure Migration Script"
    echo ""
    echo "This script documents the migration from the old flat structure"
    echo "to the new organized structure with backend/, frontend/, docs/, etc."
    echo ""
    echo "Migration completed on: $(date)"
    echo ""
    echo "Changes made:"
    echo "  ✓ Moved pom.xml, src/, target/ → backend/"
    echo "  ✓ Moved docker/ → infrastructure/docker/"
    echo "  ✓ Moved documentation files → docs/"
    echo "  ✓ Moved scripts → scripts/"
    echo "  ✓ Created module-specific READMEs"
    echo "  ✓ Updated docker-compose.yml paths"
    echo "  ✓ Created Dockerfiles for production"
    echo "  ✓ Created automation scripts"
    echo ""
    echo "New structure:"
    echo "  backend/          - Spring Boot application"
    echo "  frontend/         - React TypeScript application"
    echo "  infrastructure/   - Docker, K8s, deployment configs"
    echo "  docs/            - All documentation organized by category"
    echo "  scripts/         - Build, test, and setup automation"
    echo "  logs/            - Application logs"
    echo ""
    echo "Quick start after migration:"
    echo "  ./scripts/setup/setup-backend.sh"
    echo "  ./scripts/setup/setup-frontend.sh"
    echo "  ./scripts/build.sh all"
}

# Función para verificar la estructura actual
verify_structure() {
    log "Verifying current project structure..."
    
    local errors=0
    
    # Verificar directorios principales
    for dir in "backend" "frontend" "infrastructure" "docs" "scripts"; do
        if [ ! -d "$dir" ]; then
            log_error "Missing directory: $dir"
            ((errors++))
        else
            log_success "Found directory: $dir"
        fi
    done
    
    # Verificar archivos clave
    if [ ! -f "backend/pom.xml" ]; then
        log_error "Missing backend/pom.xml"
        ((errors++))
    else
        log_success "Found backend/pom.xml"
    fi
    
    if [ ! -f "frontend/package.json" ]; then
        log_error "Missing frontend/package.json"
        ((errors++))
    else
        log_success "Found frontend/package.json"
    fi
    
    if [ ! -f "infrastructure/docker/init-db.sql" ]; then
        log_error "Missing infrastructure/docker/init-db.sql"
        ((errors++))
    else
        log_success "Found infrastructure/docker/init-db.sql"
    fi
    
    if [ $errors -eq 0 ]; then
        log_success "Project structure verification completed successfully"
        return 0
    else
        log_error "Project structure verification failed with $errors errors"
        return 1
    fi
}

# Función para mostrar comandos de desarrollo actualizados
show_updated_commands() {
    log "Updated development commands after migration:"
    echo ""
    echo "🏗️  Setup:"
    echo "  ./scripts/setup/setup-backend.sh     # Setup backend environment"
    echo "  ./scripts/setup/setup-frontend.sh    # Setup frontend environment"
    echo ""
    echo "🚀 Build:"
    echo "  ./scripts/build.sh all               # Build both backend and frontend"
    echo "  ./scripts/build.sh backend           # Build backend only"
    echo "  ./scripts/build.sh frontend          # Build frontend only"
    echo ""
    echo "🧪 Test:"
    echo "  ./scripts/test-scripts.sh fast       # Fast tests for development"
    echo "  ./scripts/test-scripts.sh ci         # Complete test suite"
    echo ""
    echo "🐳 Docker:"
    echo "  docker-compose up -d                 # Start development services"
    echo "  docker build -f infrastructure/docker/Dockerfile.backend -t backend ."
    echo "  docker build -f infrastructure/docker/Dockerfile.frontend -t frontend ."
    echo ""
    echo "💻 Development:"
    echo "  cd backend && mvn spring-boot:run    # Start backend"
    echo "  cd frontend && npm run dev           # Start frontend"
    echo ""
    echo "📚 Documentation:"
    echo "  docs/                                # All documentation"
    echo "  backend/README.md                    # Backend specific docs"
    echo "  frontend/README.md                   # Frontend specific docs"
}

# Función principal
main() {
    case "${1:-help}" in
        "verify")
            verify_structure
            ;;
        "commands")
            show_updated_commands
            ;;
        "help"|*)
            show_help
            ;;
    esac
}

# Ejecutar función principal
main "$@"