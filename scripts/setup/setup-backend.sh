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

# Verificar prerrequisitos
check_prerequisites() {
    log "Checking prerequisites..."
    
    # Verificar Java 21
    if ! command -v java &> /dev/null; then
        log_error "Java is not installed. Please install Java 21."
        exit 1
    fi
    
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -lt 21 ]; then
        log_error "Java 21 or higher is required. Current version: $JAVA_VERSION"
        exit 1
    fi
    log_success "Java $JAVA_VERSION detected"
    
    # Verificar Maven
    if ! command -v mvn &> /dev/null; then
        log_error "Maven is not installed. Please install Maven."
        exit 1
    fi
    
    MVN_VERSION=$(mvn -version | head -n 1 | cut -d' ' -f3)
    log_success "Maven $MVN_VERSION detected"
    
    # Verificar Docker
    if ! command -v docker &> /dev/null; then
        log_warning "Docker is not installed. You'll need it for running databases."
    else
        log_success "Docker detected"
    fi
    
    # Verificar Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        log_warning "Docker Compose is not installed. You'll need it for running services."
    else
        log_success "Docker Compose detected"
    fi
}

# Configurar base de datos
setup_database() {
    log "Setting up database..."
    
    if command -v docker-compose &> /dev/null; then
        log "Starting PostgreSQL and Redis with Docker Compose..."
        docker-compose up -d postgres redis
        
        # Esperar a que PostgreSQL esté listo
        log "Waiting for PostgreSQL to be ready..."
        sleep 10
        
        # Verificar conexión
        if docker-compose exec postgres pg_isready -U postgres; then
            log_success "PostgreSQL is ready"
        else
            log_error "PostgreSQL failed to start"
            exit 1
        fi
        
        log_success "Database services started"
    else
        log_warning "Docker Compose not available. Please start PostgreSQL and Redis manually."
        log_warning "PostgreSQL: localhost:5432, database: issue_tracker_dev, user: postgres, password: postgres"
        log_warning "Redis: localhost:6379"
    fi
}

# Configurar aplicación
setup_application() {
    log "Setting up backend application..."
    
    cd backend
    
    # Descargar dependencias
    log "Downloading Maven dependencies..."
    mvn dependency:go-offline
    
    # Ejecutar migraciones de base de datos
    if command -v docker-compose &> /dev/null; then
        log "Running database migrations..."
        mvn flyway:migrate
        log_success "Database migrations completed"
    else
        log_warning "Skipping database migrations. Please ensure PostgreSQL is running and run: mvn flyway:migrate"
    fi
    
    cd ..
    log_success "Backend application setup completed"
}

# Ejecutar tests
run_tests() {
    log "Running backend tests..."
    cd backend
    mvn test -Pfast-tests
    cd ..
    log_success "Backend tests completed"
}

# Función principal
main() {
    log "Starting backend setup..."
    
    # Verificar que estamos en el directorio raíz del proyecto
    if [ ! -f "docker-compose.yml" ]; then
        log_error "Please run this script from the project root directory"
        exit 1
    fi
    
    check_prerequisites
    setup_database
    setup_application
    
    if [ "$1" = "--with-tests" ]; then
        run_tests
    fi
    
    log_success "Backend setup completed successfully!"
    echo ""
    log "Next steps:"
    echo "  1. Start the backend: cd backend && mvn spring-boot:run"
    echo "  2. API will be available at: http://localhost:8080"
    echo "  3. Run tests: cd backend && mvn test"
    echo "  4. View logs: tail -f logs/application.log"
}

# Ejecutar función principal
main "$@"