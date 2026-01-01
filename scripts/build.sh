#!/bin/bash

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Función para mostrar ayuda
show_help() {
    echo "Usage: $0 [backend|frontend|all] [options]"
    echo ""
    echo "Options:"
    echo "  -h, --help     Show this help message"
    echo "  -c, --clean    Clean before building"
    echo "  -t, --test     Run tests after building"
    echo ""
    echo "Examples:"
    echo "  $0 all                 # Build both backend and frontend"
    echo "  $0 backend --clean     # Clean and build backend"
    echo "  $0 frontend --test     # Build frontend and run tests"
}

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

# Función para construir backend
build_backend() {
    log "Building backend..."
    cd backend
    
    if [ "$CLEAN" = true ]; then
        log "Cleaning backend..."
        mvn clean
    fi
    
    mvn package -DskipTests
    
    if [ "$RUN_TESTS" = true ]; then
        log "Running backend tests..."
        mvn test -Pfast-tests
    fi
    
    cd ..
    log_success "Backend build completed"
}

# Función para construir frontend
build_frontend() {
    log "Building frontend..."
    cd frontend
    
    if [ "$CLEAN" = true ]; then
        log "Cleaning frontend..."
        rm -rf dist node_modules/.cache
    fi
    
    # Instalar dependencias si no existen
    if [ ! -d "node_modules" ]; then
        log "Installing frontend dependencies..."
        npm ci
    fi
    
    npm run build
    
    if [ "$RUN_TESTS" = true ]; then
        log "Running frontend tests..."
        npm run test:run
    fi
    
    cd ..
    log_success "Frontend build completed"
}

# Parsear argumentos
TARGET=""
CLEAN=false
RUN_TESTS=false

while [[ $# -gt 0 ]]; do
    case $1 in
        backend|frontend|all)
            TARGET="$1"
            shift
            ;;
        -c|--clean)
            CLEAN=true
            shift
            ;;
        -t|--test)
            RUN_TESTS=true
            shift
            ;;
        -h|--help)
            show_help
            exit 0
            ;;
        *)
            log_error "Unknown option: $1"
            show_help
            exit 1
            ;;
    esac
done

# Validar target
if [ -z "$TARGET" ]; then
    log_error "Please specify a target: backend, frontend, or all"
    show_help
    exit 1
fi

# Verificar que estamos en el directorio raíz del proyecto
if [ ! -f "docker-compose.yml" ]; then
    log_error "Please run this script from the project root directory"
    exit 1
fi

log "Starting build process for: $TARGET"
if [ "$CLEAN" = true ]; then
    log_warning "Clean build enabled"
fi
if [ "$RUN_TESTS" = true ]; then
    log_warning "Tests will be executed after build"
fi

# Ejecutar build según el target
case $TARGET in
    backend)
        build_backend
        ;;
    frontend)
        build_frontend
        ;;
    all)
        build_backend
        build_frontend
        ;;
esac

log_success "Build process completed successfully!"