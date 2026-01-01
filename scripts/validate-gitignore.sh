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
    echo "Usage: $0 [check|test|clean]"
    echo ""
    echo "Commands:"
    echo "  check    Check if important files/directories are properly ignored"
    echo "  test     Test .gitignore patterns with sample files"
    echo "  clean    Show what would be cleaned by git clean"
    echo "  help     Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 check    # Verify .gitignore is working correctly"
    echo "  $0 test     # Test patterns with common file types"
}

# Función para verificar patrones importantes
check_gitignore() {
    log "Checking .gitignore patterns..."
    
    local errors=0
    
    # Archivos/directorios que DEBEN ser ignorados
    local should_ignore=(
        "backend/target/"
        "frontend/node_modules/"
        "frontend/dist/"
        "logs/"
        ".env"
        "backend/application-local.yml"
        "infrastructure/terraform/.terraform/"
        "*.log"
        ".DS_Store"
        ".idea/"
        "*.class"
        "*.jar"
    )
    
    # Archivos que NO deben ser ignorados
    local should_track=(
        "README.md"
        "backend/README.md"
        "frontend/README.md"
        "backend/pom.xml"
        "frontend/package.json"
        "docker-compose.yml"
        ".gitignore"
        "frontend/.env.example"
    )
    
    log "Checking files that should be ignored..."
    for pattern in "${should_ignore[@]}"; do
        if git check-ignore "$pattern" >/dev/null 2>&1; then
            log_success "✓ $pattern is ignored"
        else
            log_error "✗ $pattern should be ignored but isn't"
            ((errors++))
        fi
    done
    
    log "Checking files that should be tracked..."
    for file in "${should_track[@]}"; do
        if [ -f "$file" ] || [ -d "$file" ]; then
            if git check-ignore "$file" >/dev/null 2>&1; then
                log_error "✗ $file is ignored but should be tracked"
                ((errors++))
            else
                log_success "✓ $file is tracked"
            fi
        else
            log_warning "⚠ $file doesn't exist (skipping)"
        fi
    done
    
    if [ $errors -eq 0 ]; then
        log_success "All .gitignore patterns are working correctly!"
        return 0
    else
        log_error ".gitignore validation failed with $errors errors"
        return 1
    fi
}

# Función para probar patrones con archivos de ejemplo
test_patterns() {
    log "Testing .gitignore patterns with sample files..."
    
    # Crear archivos temporales para probar
    local test_files=(
        "backend/target/test.class"
        "frontend/node_modules/test-package/index.js"
        "frontend/dist/bundle.js"
        "logs/application.log"
        ".env"
        "backend/application-local.yml"
        "test.DS_Store"
        ".idea/workspace.xml"
        "backend/src/main/java/Test.java"
        "frontend/src/App.tsx"
    )
    
    log "Creating test files..."
    for file in "${test_files[@]}"; do
        # Crear directorio si no existe
        mkdir -p "$(dirname "$file")"
        # Crear archivo temporal
        touch "$file.test"
    done
    
    log "Testing ignore patterns..."
    for file in "${test_files[@]}"; do
        if git check-ignore "$file.test" >/dev/null 2>&1; then
            log_success "✓ $file.test would be ignored"
        else
            log_warning "⚠ $file.test would be tracked"
        fi
    done
    
    # Limpiar archivos de prueba
    log "Cleaning up test files..."
    for file in "${test_files[@]}"; do
        rm -f "$file.test"
    done
    
    # Limpiar directorios vacíos
    find . -type d -empty -delete 2>/dev/null || true
    
    log_success "Pattern testing completed"
}

# Función para mostrar qué sería limpiado
show_clean() {
    log "Files that would be cleaned by 'git clean':"
    
    if git clean -n -d; then
        log_success "Git clean preview completed"
    else
        log_warning "No files to clean or git clean failed"
    fi
    
    echo ""
    log "Files currently ignored:"
    git status --ignored --porcelain | head -20
}

# Función para mostrar estadísticas del .gitignore
show_stats() {
    log "Gitignore statistics:"
    
    local total_lines=$(wc -l < .gitignore)
    local comment_lines=$(grep -c "^#" .gitignore || echo "0")
    local empty_lines=$(grep -c "^$" .gitignore || echo "0")
    local pattern_lines=$((total_lines - comment_lines - empty_lines))
    
    echo "  Total lines: $total_lines"
    echo "  Comment lines: $comment_lines"
    echo "  Empty lines: $empty_lines"
    echo "  Pattern lines: $pattern_lines"
    
    echo ""
    log "Sections in .gitignore:"
    grep "^# ====" .gitignore | sed 's/^# =*//g' | sed 's/=*$//g' | sed 's/^ *//g' | sed 's/ *$//g' | grep -v "^$" | while read -r section; do
        echo "  - $section"
    done
}

# Función principal
main() {
    # Verificar que estamos en el directorio raíz del proyecto
    if [ ! -f ".gitignore" ]; then
        log_error "No .gitignore found. Please run this script from the project root."
        exit 1
    fi
    
    case "${1:-check}" in
        "check")
            check_gitignore
            ;;
        "test")
            test_patterns
            ;;
        "clean")
            show_clean
            ;;
        "stats")
            show_stats
            ;;
        "help"|*)
            show_help
            ;;
    esac
}

# Ejecutar función principal
main "$@"