#!/bin/bash

# Frontend Build Script for Personal Issue Tracker
# This script handles optimized frontend builds for different environments

set -e

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
FRONTEND_DIR="$PROJECT_ROOT/frontend"
BUILD_MODE="${BUILD_MODE:-production}"
ANALYZE="${ANALYZE:-false}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging function
log() {
    echo -e "$1"
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
    
    # Check if Node.js is installed
    if ! command -v node &> /dev/null; then
        error_exit "Node.js is not installed. Please install Node.js first."
    fi
    
    # Check if npm is installed
    if ! command -v npm &> /dev/null; then
        error_exit "npm is not installed. Please install npm first."
    fi
    
    # Check if frontend directory exists
    if [[ ! -d "$FRONTEND_DIR" ]]; then
        error_exit "Frontend directory not found: $FRONTEND_DIR"
    fi
    
    success "Prerequisites check passed"
}

# Install dependencies
install_dependencies() {
    info "Installing dependencies..."
    
    cd "$FRONTEND_DIR"
    
    # Clean install for production builds
    if [[ "$BUILD_MODE" == "production" ]]; then
        info "Performing clean install for production..."
        rm -rf node_modules package-lock.json
        npm install
    else
        npm ci
    fi
    
    success "Dependencies installed"
}

# Run linting and type checking
run_quality_checks() {
    info "Running quality checks..."
    
    cd "$FRONTEND_DIR"
    
    # Type checking
    info "Running TypeScript type checking..."
    npm run typecheck
    
    # Linting
    info "Running ESLint..."
    npm run lint
    
    # Format checking
    info "Checking code formatting..."
    npm run format:check
    
    success "Quality checks passed"
}

# Build application
build_application() {
    info "Building application for $BUILD_MODE environment..."
    
    cd "$FRONTEND_DIR"
    
    # Set build timestamp and version
    export BUILD_TIME=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
    export VERSION=$(node -p "require('./package.json').version")
    
    # Build based on mode
    case "$BUILD_MODE" in
        "production")
            npm run build:prod
            ;;
        "staging")
            npm run build:staging
            ;;
        "development")
            npm run build
            ;;
        *)
            error_exit "Unknown build mode: $BUILD_MODE"
            ;;
    esac
    
    success "Application built successfully"
}

# Analyze bundle (optional)
analyze_bundle() {
    if [[ "$ANALYZE" == "true" ]]; then
        info "Analyzing bundle size..."
        
        cd "$FRONTEND_DIR"
        
        # Generate bundle analysis
        npm run build:analyze
        
        info "Bundle analysis completed. Check dist/stats.html for details."
    fi
}

# Optimize build output
optimize_build() {
    info "Optimizing build output..."
    
    cd "$FRONTEND_DIR"
    
    # Check if dist directory exists
    if [[ ! -d "dist" ]]; then
        error_exit "Build output directory not found: dist"
    fi
    
    # Calculate build size
    local build_size=$(du -sh dist | cut -f1)
    info "Build size: $build_size"
    
    # Count files
    local file_count=$(find dist -type f | wc -l)
    info "Total files: $file_count"
    
    # Check for large files
    info "Checking for large files (>1MB)..."
    find dist -type f -size +1M -exec ls -lh {} \; | awk '{print $9 ": " $5}' || true
    
    success "Build optimization completed"
}

# Validate build
validate_build() {
    info "Validating build..."
    
    cd "$FRONTEND_DIR"
    
    # Check if index.html exists
    if [[ ! -f "dist/index.html" ]]; then
        error_exit "index.html not found in build output"
    fi
    
    # Check if main JS and CSS files exist
    if ! find dist -name "*.js" -type f | grep -q .; then
        error_exit "No JavaScript files found in build output"
    fi
    
    if ! find dist -name "*.css" -type f | grep -q .; then
        warning "No CSS files found in build output (this might be expected)"
    fi
    
    # Validate HTML structure
    if ! grep -q "<div id=\"root\">" dist/index.html; then
        error_exit "React root element not found in index.html"
    fi
    
    success "Build validation passed"
}

# Show build summary
show_summary() {
    info "Build Summary:"
    echo "=============="
    echo "Mode: $BUILD_MODE"
    echo "Build Time: $(date)"
    echo "Output Directory: $FRONTEND_DIR/dist"
    
    cd "$FRONTEND_DIR"
    if [[ -d "dist" ]]; then
        echo "Build Size: $(du -sh dist | cut -f1)"
        echo "Total Files: $(find dist -type f | wc -l)"
        echo
        info "Build completed successfully! 🎉"
        echo
        info "Next Steps:"
        echo "1. Test the build locally: npm run preview:prod"
        echo "2. Deploy using Docker: docker build -f infrastructure/docker/Dockerfile.frontend ."
        echo "3. Or serve the dist/ directory with any static file server"
    fi
}

# Main build function
main() {
    log "${GREEN}🏗️  Starting Frontend Build${NC}"
    log "================================"
    
    check_prerequisites
    install_dependencies
    run_quality_checks
    build_application
    analyze_bundle
    optimize_build
    validate_build
    show_summary
}

# Handle script arguments
case "${1:-build}" in
    "build")
        main
        ;;
    "clean")
        info "Cleaning build artifacts..."
        cd "$FRONTEND_DIR"
        rm -rf dist node_modules/.vite
        success "Clean completed"
        ;;
    "deps")
        check_prerequisites
        install_dependencies
        ;;
    "check")
        check_prerequisites
        cd "$FRONTEND_DIR"
        run_quality_checks
        ;;
    "help")
        echo "Usage: $0 [build|clean|deps|check|help]"
        echo "  build  - Full frontend build (default)"
        echo "  clean  - Clean build artifacts"
        echo "  deps   - Install dependencies only"
        echo "  check  - Run quality checks only"
        echo "  help   - Show this help message"
        echo
        echo "Environment Variables:"
        echo "  BUILD_MODE - Build mode (production, staging, development)"
        echo "  ANALYZE    - Enable bundle analysis (true/false)"
        ;;
    *)
        error_exit "Unknown command: $1. Use 'help' for usage information."
        ;;
esac