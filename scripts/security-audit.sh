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

log_critical() {
    echo -e "${RED}[$(date +'%Y-%m-%d %H:%M:%S')] 🚨 CRITICAL${NC} $1"
}

# Función para mostrar ayuda
show_help() {
    echo "Security Audit Script for Personal Issue Tracker"
    echo ""
    echo "Usage: $0 [scan|fix|report|help]"
    echo ""
    echo "Commands:"
    echo "  scan     Scan for security vulnerabilities and sensitive data"
    echo "  fix      Apply automatic security fixes"
    echo "  report   Generate security report"
    echo "  help     Show this help message"
    echo ""
    echo "This script checks for:"
    echo "  - Hardcoded passwords, API keys, and secrets"
    echo "  - Exposed credentials in configuration files"
    echo "  - Personal information in committed files"
    echo "  - Insecure default configurations"
    echo "  - Files that should be in .gitignore"
}

# Función para escanear información sensible
scan_sensitive_data() {
    log "🔍 Scanning for sensitive data..."
    
    local issues=0
    
    # Patrones de información sensible
    local sensitive_patterns=(
        "password\s*=\s*['\"][^'\"]{1,}"
        "secret\s*=\s*['\"][^'\"]{1,}"
        "api[_-]?key\s*=\s*['\"][^'\"]{1,}"
        "token\s*=\s*['\"][^'\"]{1,}"
        "private[_-]?key"
        "BEGIN\s+(RSA\s+)?PRIVATE\s+KEY"
        "jdbc:.*://.*:[0-9]+.*password="
        "mongodb://.*:.*@"
        "redis://.*:.*@"
        "mysql://.*:.*@"
    )
    
    log "Checking for hardcoded credentials..."
    for pattern in "${sensitive_patterns[@]}"; do
        local matches=$(grep -r -i -E "$pattern" . \
            --exclude-dir=node_modules \
            --exclude-dir=target \
            --exclude-dir=.git \
            --exclude-dir=logs \
            --exclude="*.log" \
            --exclude="security-audit.sh" \
            2>/dev/null || true)
        
        if [ -n "$matches" ]; then
            log_critical "Found potential credentials matching pattern: $pattern"
            echo "$matches" | head -5
            echo ""
            ((issues++))
        fi
    done
    
    # Verificar archivos específicos
    log "Checking specific configuration files..."
    
    # JWT secrets
    if grep -q "secret.*mySecretKey\|secret.*dev-secret\|secret.*test" backend/src/main/resources/*.yml 2>/dev/null; then
        log_warning "Found default JWT secrets in configuration files"
        ((issues++))
    fi
    
    # Database passwords en texto plano
    if grep -q "password:\s*postgres\|password:\s*admin\|password:\s*root" backend/src/main/resources/*.yml 2>/dev/null; then
        log_warning "Found default database passwords in configuration files"
        ((issues++))
    fi
    
    # Archivos .env con credenciales
    if find . -name ".env" -not -path "*/node_modules/*" -exec grep -l "password\|secret\|key" {} \; 2>/dev/null | grep -v ".env.example"; then
        log_warning "Found .env files with potential credentials"
        ((issues++))
    fi
    
    return $issues
}

# Función para verificar archivos que deberían estar en .gitignore
check_gitignore_compliance() {
    log "🔒 Checking .gitignore compliance..."
    
    local issues=0
    
    # Archivos que nunca deberían estar en git
    local forbidden_files=(
        ".env"
        "*.key"
        "*.pem"
        "*.p12"
        "*.jks"
        "application-local.yml"
        "application-secret.yml"
        "credentials.json"
        "keystore.jks"
        "truststore.jks"
    )
    
    log "Checking for files that should be ignored..."
    for pattern in "${forbidden_files[@]}"; do
        local found_files=$(find . -name "$pattern" -not -path "*/node_modules/*" -not -path "*/.git/*" 2>/dev/null || true)
        if [ -n "$found_files" ]; then
            log_error "Found files that should be in .gitignore: $pattern"
            echo "$found_files"
            ((issues++))
        fi
    done
    
    # Verificar si archivos sensibles están siendo rastreados por git
    log "Checking if sensitive files are tracked by git..."
    local tracked_sensitive=$(git ls-files | grep -E "\\.env$|\\.key$|\\.pem$|secret|credential" || true)
    if [ -n "$tracked_sensitive" ]; then
        log_critical "Found sensitive files tracked by git:"
        echo "$tracked_sensitive"
        ((issues++))
    fi
    
    return $issues
}

# Función para verificar configuraciones inseguras
check_insecure_configs() {
    log "⚙️ Checking for insecure configurations..."
    
    local issues=0
    
    # Verificar configuraciones de desarrollo en producción
    if grep -q "show-sql: true\|DEBUG" backend/src/main/resources/application-prod.yml 2>/dev/null; then
        log_warning "Found debug configurations in production profile"
        ((issues++))
    fi
    
    # Verificar CORS permisivo
    if grep -q "allowedOrigins.*\*\|allowCredentials.*true" backend/src/main/java/**/*.java 2>/dev/null; then
        log_warning "Found potentially insecure CORS configuration"
        ((issues++))
    fi
    
    # Verificar configuraciones de seguridad
    if grep -q "csrf().disable()\|authorizeRequests().anyRequest().permitAll()" backend/src/main/java/**/*.java 2>/dev/null; then
        log_warning "Found potentially insecure security configuration"
        ((issues++))
    fi
    
    return $issues
}

# Función para verificar información personal
check_personal_info() {
    log "👤 Checking for personal information..."
    
    local issues=0
    
    # Patrones de información personal
    local personal_patterns=(
        "/Users/[^/]+/"
        "/home/[^/]+/"
        "C:\\\\Users\\\\[^\\\\]+"
        "@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
        "[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}"
    )
    
    for pattern in "${personal_patterns[@]}"; do
        local matches=$(grep -r -E "$pattern" . \
            --exclude-dir=node_modules \
            --exclude-dir=target \
            --exclude-dir=.git \
            --exclude-dir=logs \
            --exclude="*.log" \
            --exclude="security-audit.sh" \
            2>/dev/null || true)
        
        if [ -n "$matches" ]; then
            log_warning "Found potential personal information matching pattern: $pattern"
            echo "$matches" | head -3
            echo ""
            ((issues++))
        fi
    done
    
    return $issues
}

# Función para aplicar correcciones automáticas
apply_fixes() {
    log "🔧 Applying automatic security fixes..."
    
    # Crear .env.example si no existe
    if [ ! -f "frontend/.env.example" ]; then
        log "Creating frontend/.env.example..."
        cat > frontend/.env.example << 'EOF'
# API Configuration
VITE_API_BASE_URL=http://localhost:8080

# Environment
VITE_NODE_ENV=development
EOF
    fi
    
    # Crear archivo de configuración de seguridad
    if [ ! -f "docs/security/security-guidelines.md" ]; then
        mkdir -p docs/security
        log "Creating security guidelines..."
        cat > docs/security/security-guidelines.md << 'EOF'
# Security Guidelines

## Environment Variables
- Never commit `.env` files with real credentials
- Always use `.env.example` for templates
- Use environment variables for all sensitive configuration

## JWT Configuration
- Never use default JWT secrets
- Use strong, randomly generated secrets in production
- Rotate secrets regularly

## Database Security
- Never commit database passwords
- Use environment variables for database configuration
- Use connection pooling and proper timeouts

## API Security
- Implement proper authentication and authorization
- Use HTTPS in production
- Implement rate limiting
- Validate all inputs

## File Security
- Keep sensitive files out of version control
- Use proper file permissions
- Encrypt sensitive data at rest
EOF
    fi
    
    log_success "Automatic fixes applied"
}

# Función para generar reporte
generate_report() {
    log "📊 Generating security report..."
    
    local report_file="security-report-$(date +%Y%m%d-%H%M%S).md"
    
    cat > "$report_file" << EOF
# Security Audit Report

**Generated:** $(date)
**Project:** Personal Issue Tracker

## Summary

This report contains the results of the security audit performed on the project.

## Scan Results

EOF
    
    # Ejecutar escaneos y agregar al reporte
    {
        echo "### Sensitive Data Scan"
        scan_sensitive_data
        echo ""
        
        echo "### .gitignore Compliance"
        check_gitignore_compliance
        echo ""
        
        echo "### Insecure Configurations"
        check_insecure_configs
        echo ""
        
        echo "### Personal Information"
        check_personal_info
        echo ""
    } >> "$report_file" 2>&1
    
    log_success "Security report generated: $report_file"
}

# Función principal
main() {
    # Verificar que estamos en el directorio raíz del proyecto
    if [ ! -f "docker-compose.yml" ]; then
        log_error "Please run this script from the project root directory"
        exit 1
    fi
    
    case "${1:-scan}" in
        "scan")
            log "🔍 Starting security scan..."
            local total_issues=0
            
            scan_sensitive_data
            total_issues=$((total_issues + $?))
            
            check_gitignore_compliance
            total_issues=$((total_issues + $?))
            
            check_insecure_configs
            total_issues=$((total_issues + $?))
            
            check_personal_info
            total_issues=$((total_issues + $?))
            
            if [ $total_issues -eq 0 ]; then
                log_success "Security scan completed with no issues found!"
            else
                log_error "Security scan completed with $total_issues issues found"
                echo ""
                log "Run './scripts/security-audit.sh fix' to apply automatic fixes"
                log "Run './scripts/security-audit.sh report' to generate detailed report"
            fi
            ;;
        "fix")
            apply_fixes
            ;;
        "report")
            generate_report
            ;;
        "help"|*)
            show_help
            ;;
    esac
}

# Ejecutar función principal
main "$@"