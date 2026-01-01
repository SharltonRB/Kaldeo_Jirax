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
    echo "Internationalization Manager for Personal Issue Tracker"
    echo ""
    echo "Usage: $0 [check|sync|create|help]"
    echo ""
    echo "Commands:"
    echo "  check    Check for missing translations and inconsistencies"
    echo "  sync     Synchronize content between language versions"
    echo "  create   Create missing language versions"
    echo "  help     Show this help message"
    echo ""
    echo "Language Convention:"
    echo "  - Spanish (default): filename.md"
    echo "  - English: filename.en.md"
    echo ""
    echo "Examples:"
    echo "  $0 check    # Check translation status"
    echo "  $0 create   # Create missing English versions"
    echo "  $0 sync     # Sync language versions"
}

# Función para verificar estado de traducciones
check_translations() {
    log "🔍 Checking translation status..."
    
    local missing_en=0
    local missing_es=0
    
    # Buscar archivos .md (español por defecto)
    log "Checking for missing English versions..."
    while IFS= read -r -d '' spanish_file; do
        # Obtener el archivo inglés correspondiente
        local dir=$(dirname "$spanish_file")
        local basename=$(basename "$spanish_file" .md)
        local english_file="$dir/$basename.en.md"
        
        # Excluir archivos que ya son versiones en inglés
        if [[ "$spanish_file" == *.en.md ]]; then
            continue
        fi
        
        if [ ! -f "$english_file" ]; then
            log_warning "Missing English version: $english_file"
            ((missing_en++))
        else
            log_success "✓ $spanish_file has English version"
        fi
    done < <(find . -name "*.md" -not -path "*/node_modules/*" -not -path "*/.git/*" -print0)
    
    # Buscar archivos .en.md (inglés)
    log "Checking for missing Spanish versions..."
    while IFS= read -r -d '' english_file; do
        # Obtener el archivo español correspondiente
        local dir=$(dirname "$english_file")
        local basename=$(basename "$english_file" .en.md)
        local spanish_file="$dir/$basename.md"
        
        if [ ! -f "$spanish_file" ]; then
            log_warning "Missing Spanish version: $spanish_file"
            ((missing_es++))
        else
            log_success "✓ $english_file has Spanish version"
        fi
    done < <(find . -name "*.en.md" -not -path "*/node_modules/*" -not -path "*/.git/*" -print0)
    
    echo ""
    log "Translation Status Summary:"
    echo "  Missing English versions: $missing_en"
    echo "  Missing Spanish versions: $missing_es"
    
    if [ $missing_en -eq 0 ] && [ $missing_es -eq 0 ]; then
        log_success "All documentation is properly translated!"
        return 0
    else
        log_error "Found missing translations"
        return 1
    fi
}

# Función para crear versiones faltantes
create_missing_versions() {
    log "📝 Creating missing language versions..."
    
    # Crear versiones en inglés faltantes
    log "Creating missing English versions..."
    while IFS= read -r -d '' spanish_file; do
        # Obtener el archivo inglés correspondiente
        local dir=$(dirname "$spanish_file")
        local basename=$(basename "$spanish_file" .md)
        local english_file="$dir/$basename.en.md"
        
        # Excluir archivos que ya son versiones en inglés
        if [[ "$spanish_file" == *.en.md ]]; then
            continue
        fi
        
        if [ ! -f "$english_file" ]; then
            log "Creating English version: $english_file"
            
            # Crear encabezado con nota de traducción
            cat > "$english_file" << EOF
# [TRANSLATION NEEDED] $(head -n 1 "$spanish_file" | sed 's/^# //')

> **Note**: This is an auto-generated template. Please translate the content from the Spanish version.
> **Nota**: Esta es una plantilla auto-generada. Por favor traduce el contenido de la versión en español.

## Language Versions / Versiones de Idioma

- **English**: [$(basename "$english_file")]($(basename "$english_file"))
- **Español**: [$(basename "$spanish_file")]($(basename "$spanish_file"))

---

## Content to Translate / Contenido a Traducir

Please translate the content from: [\`$(basename "$spanish_file")\`]($(basename "$spanish_file"))

EOF
            
            log_success "Created template: $english_file"
        fi
    done < <(find . -name "*.md" -not -path "*/node_modules/*" -not -path "*/.git/*" -print0)
    
    log_success "Missing language versions created"
}

# Función para sincronizar versiones
sync_versions() {
    log "🔄 Synchronizing language versions..."
    
    # Verificar que ambas versiones tengan enlaces de idioma
    log "Adding language version links..."
    
    while IFS= read -r -d '' spanish_file; do
        local dir=$(dirname "$spanish_file")
        local basename=$(basename "$spanish_file" .md)
        local english_file="$dir/$basename.en.md"
        
        # Excluir archivos que ya son versiones en inglés
        if [[ "$spanish_file" == *.en.md ]]; then
            continue
        fi
        
        if [ -f "$english_file" ]; then
            # Verificar si el archivo español tiene enlaces de idioma
            if ! grep -q "Versiones de Idioma\|Language Versions" "$spanish_file"; then
                log "Adding language links to: $spanish_file"
                echo "" >> "$spanish_file"
                echo "## Versiones de Idioma" >> "$spanish_file"
                echo "" >> "$spanish_file"
                echo "- **English**: [$(basename "$english_file")]($(basename "$english_file"))" >> "$spanish_file"
                echo "- **Español**: [$(basename "$spanish_file")]($(basename "$spanish_file"))" >> "$spanish_file"
            fi
            
            # Verificar si el archivo inglés tiene enlaces de idioma
            if ! grep -q "Language Versions\|Versiones de Idioma" "$english_file"; then
                log "Adding language links to: $english_file"
                echo "" >> "$english_file"
                echo "## Language Versions" >> "$english_file"
                echo "" >> "$english_file"
                echo "- **English**: [$(basename "$english_file")]($(basename "$english_file"))" >> "$english_file"
                echo "- **Español**: [$(basename "$spanish_file")]($(basename "$spanish_file"))" >> "$english_file"
            fi
        fi
    done < <(find . -name "*.md" -not -path "*/node_modules/*" -not -path "*/.git/*" -print0)
    
    log_success "Language versions synchronized"
}

# Función para mostrar estadísticas
show_stats() {
    log "📊 Documentation Statistics:"
    
    local total_md=$(find . -name "*.md" -not -path "*/node_modules/*" -not -path "*/.git/*" | wc -l)
    local spanish_files=$(find . -name "*.md" -not -name "*.en.md" -not -path "*/node_modules/*" -not -path "*/.git/*" | wc -l)
    local english_files=$(find . -name "*.en.md" -not -path "*/node_modules/*" -not -path "*/.git/*" | wc -l)
    
    echo "  Total markdown files: $total_md"
    echo "  Spanish files (.md): $spanish_files"
    echo "  English files (.en.md): $english_files"
    echo "  Translation coverage: $(( english_files * 100 / spanish_files ))%"
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
            check_translations
            show_stats
            ;;
        "create")
            create_missing_versions
            ;;
        "sync")
            sync_versions
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