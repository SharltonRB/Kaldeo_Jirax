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
    echo "Code Language Checker for Personal Issue Tracker"
    echo ""
    echo "Usage: $0 [check|fix|report|help]"
    echo ""
    echo "Commands:"
    echo "  check    Check for Spanish text in code files"
    echo "  fix      Apply automatic fixes for common Spanish terms"
    echo "  report   Generate detailed report of language issues"
    echo "  help     Show this help message"
    echo ""
    echo "This script ensures all code (variables, classes, methods, comments) is in English"
    echo "while documentation can be bilingual."
}

# Función para verificar código en español
check_spanish_in_code() {
    log "🔍 Checking for Spanish text in code files..."
    
    local issues=0
    
    # Patrones de palabras en español comunes en código
    local spanish_patterns=(
        "usuario|Usuario"
        "contraseña|Contraseña"
        "configuración|Configuracion"
        "aplicación|Aplicacion"
        "descripción|Descripcion"
        "autenticación|Autenticacion"
        "autorización|Autorizacion"
        "proyecto|Proyecto"
        "servicio|Servicio"
        "repositorio|Repositorio"
        "controlador|Controlador"
        "entidad|Entidad"
        "excepción|Excepcion"
        "validación|Validacion"
        "configurar|Configurar"
        "obtener|Obtener"
        "crear|Crear"
        "actualizar|Actualizar"
        "eliminar|Eliminar"
        "buscar|Buscar"
        "guardar|Guardar"
        "cargar|Cargar"
        "procesar|Procesar"
        "generar|Generar"
        "validar|Validar"
        "verificar|Verificar"
        "autenticar|Autenticar"
        "autorizar|Autorizar"
    )
    
    # Archivos de código a verificar
    local code_extensions=("*.java" "*.ts" "*.tsx" "*.js" "*.jsx" "*.yml" "*.yaml" "*.json")
    
    log "Scanning code files for Spanish text..."
    
    for ext in "${code_extensions[@]}"; do
        while IFS= read -r -d '' file; do
            # Excluir archivos de documentación y node_modules
            if [[ "$file" == *"/node_modules/"* ]] || [[ "$file" == *"README"* ]] || [[ "$file" == *".md"* ]]; then
                continue
            fi
            
            for pattern in "${spanish_patterns[@]}"; do
                local matches=$(grep -n -i -E "$pattern" "$file" 2>/dev/null || true)
                if [ -n "$matches" ]; then
                    log_warning "Found Spanish text in: $file"
                    echo "$matches" | head -3
                    echo ""
                    ((issues++))
                fi
            done
        done < <(find . -name "$ext" -type f -print0 2>/dev/null)
    done
    
    # Verificar nombres de archivos en español
    log "Checking for Spanish file names..."
    local spanish_files=$(find . -type f \( -name "*usuario*" -o -name "*contraseña*" -o -name "*configuracion*" -o -name "*aplicacion*" -o -name "*proyecto*" \) -not -path "*/node_modules/*" -not -path "*/.git/*" 2>/dev/null || true)
    
    if [ -n "$spanish_files" ]; then
        log_warning "Found files with Spanish names:"
        echo "$spanish_files"
        ((issues++))
    fi
    
    # Verificar nombres de clases y métodos en español
    log "Checking for Spanish class and method names..."
    local java_spanish=$(grep -r -n -E "class.*[A-Z][a-z]*Usuario|class.*[A-Z][a-z]*Proyecto|class.*[A-Z][a-z]*Servicio" --include="*.java" . 2>/dev/null || true)
    
    if [ -n "$java_spanish" ]; then
        log_warning "Found Java classes with Spanish names:"
        echo "$java_spanish" | head -5
        ((issues++))
    fi
    
    return $issues
}

# Función para aplicar correcciones automáticas
apply_fixes() {
    log "🔧 Applying automatic language fixes..."
    
    # Mapeo de términos español -> inglés
    declare -A translations=(
        ["usuario"]="user"
        ["Usuario"]="User"
        ["contraseña"]="password"
        ["Contraseña"]="Password"
        ["configuración"]="configuration"
        ["Configuración"]="Configuration"
        ["aplicación"]="application"
        ["Aplicación"]="Application"
        ["descripción"]="description"
        ["Descripción"]="Description"
        ["autenticación"]="authentication"
        ["Autenticación"]="Authentication"
        ["autorización"]="authorization"
        ["Autorización"]="Authorization"
        ["proyecto"]="project"
        ["Proyecto"]="Project"
        ["servicio"]="service"
        ["Servicio"]="Service"
        ["repositorio"]="repository"
        ["Repositorio"]="Repository"
        ["controlador"]="controller"
        ["Controlador"]="Controller"
        ["entidad"]="entity"
        ["Entidad"]="Entity"
        ["excepción"]="exception"
        ["Excepción"]="Exception"
        ["validación"]="validation"
        ["Validación"]="Validation"
    )
    
    log "Applying translations to code files..."
    
    # Aplicar traducciones a archivos de código
    for spanish in "${!translations[@]}"; do
        local english="${translations[$spanish]}"
        
        # Buscar y reemplazar en archivos Java
        find . -name "*.java" -not -path "*/node_modules/*" -exec sed -i.bak "s/\\b$spanish\\b/$english/g" {} \; 2>/dev/null || true
        
        # Buscar y reemplazar en archivos TypeScript
        find . -name "*.ts" -o -name "*.tsx" -not -path "*/node_modules/*" -exec sed -i.bak "s/\\b$spanish\\b/$english/g" {} \; 2>/dev/null || true
        
        # Buscar y reemplazar en archivos JavaScript
        find . -name "*.js" -o -name "*.jsx" -not -path "*/node_modules/*" -exec sed -i.bak "s/\\b$spanish\\b/$english/g" {} \; 2>/dev/null || true
    done
    
    # Limpiar archivos de respaldo
    find . -name "*.bak" -delete 2>/dev/null || true
    
    log_success "Automatic fixes applied"
}

# Función para generar reporte
generate_report() {
    log "📊 Generating language compliance report..."
    
    local report_file="language-report-$(date +%Y%m%d-%H%M%S).md"
    
    cat > "$report_file" << EOF
# Language Compliance Report

**Generated:** $(date)
**Project:** Personal Issue Tracker

## Summary

This report shows the language compliance status of the codebase.

## Code Language Requirements

- **Code**: All variables, classes, methods, and comments must be in English
- **Documentation**: Can be bilingual (English and Spanish)
- **Configuration**: Should use English keys and values

## Scan Results

EOF
    
    # Ejecutar verificación y agregar al reporte
    {
        echo "### Spanish Text in Code Files"
        check_spanish_in_code
        echo ""
    } >> "$report_file" 2>&1
    
    log_success "Language report generated: $report_file"
}

# Función para mostrar estadísticas
show_stats() {
    log "📈 Language Statistics:"
    
    local total_java=$(find . -name "*.java" -not -path "*/node_modules/*" | wc -l)
    local total_ts=$(find . -name "*.ts" -o -name "*.tsx" -not -path "*/node_modules/*" | wc -l)
    local total_js=$(find . -name "*.js" -o -name "*.jsx" -not -path "*/node_modules/*" | wc -l)
    local total_config=$(find . -name "*.yml" -o -name "*.yaml" -o -name "*.json" -not -path "*/node_modules/*" | wc -l)
    
    echo "  Java files: $total_java"
    echo "  TypeScript files: $total_ts"
    echo "  JavaScript files: $total_js"
    echo "  Configuration files: $total_config"
    
    # Verificar archivos con comentarios en español
    local spanish_comments=$(grep -r -l "//.*[áéíóúñü]" --include="*.java" --include="*.ts" --include="*.js" . 2>/dev/null | wc -l || echo "0")
    echo "  Files with potential Spanish comments: $spanish_comments"
}

# Función principal
main() {
    # Verificar que estamos en el directorio raíz del proyecto
    if [ ! -f "docker-compose.yml" ]; then
        log_error "Please run this script from the project root directory"
        exit 1
    fi
    
    case "${1:-check}" in
        "check")
            local issues=0
            check_spanish_in_code
            issues=$?
            show_stats
            
            if [ $issues -eq 0 ]; then
                log_success "Code language compliance check passed!"
            else
                log_error "Found $issues language compliance issues"
                echo ""
                log "Run './scripts/code-language-check.sh fix' to apply automatic fixes"
            fi
            ;;
        "fix")
            apply_fixes
            ;;
        "report")
            generate_report
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