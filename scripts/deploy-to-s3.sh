#!/bin/bash

# S3/CDN Deployment Script for Personal Issue Tracker Frontend
# This script deploys frontend static assets to AWS S3 and CloudFront

set -e

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
FRONTEND_DIR="$PROJECT_ROOT/frontend"
DIST_DIR="$FRONTEND_DIR/dist"

# AWS Configuration (set via environment variables)
S3_BUCKET="${S3_BUCKET:-your-frontend-bucket}"
CLOUDFRONT_DISTRIBUTION_ID="${CLOUDFRONT_DISTRIBUTION_ID:-}"
AWS_REGION="${AWS_REGION:-us-east-1}"
AWS_PROFILE="${AWS_PROFILE:-default}"

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
    
    # Check if AWS CLI is installed
    if ! command -v aws &> /dev/null; then
        error_exit "AWS CLI is not installed. Please install AWS CLI first."
    fi
    
    # Check AWS credentials
    if ! aws sts get-caller-identity --profile "$AWS_PROFILE" &> /dev/null; then
        error_exit "AWS credentials not configured for profile: $AWS_PROFILE"
    fi
    
    # Check if build directory exists
    if [[ ! -d "$DIST_DIR" ]]; then
        error_exit "Build directory not found: $DIST_DIR. Please run the build first."
    fi
    
    # Check required environment variables
    if [[ "$S3_BUCKET" == "your-frontend-bucket" ]]; then
        error_exit "Please set S3_BUCKET environment variable"
    fi
    
    success "Prerequisites check passed"
}

# Validate S3 bucket
validate_s3_bucket() {
    info "Validating S3 bucket..."
    
    # Check if bucket exists
    if ! aws s3 ls "s3://$S3_BUCKET" --profile "$AWS_PROFILE" &> /dev/null; then
        error_exit "S3 bucket does not exist or is not accessible: $S3_BUCKET"
    fi
    
    success "S3 bucket validated"
}

# Deploy static assets to S3
deploy_to_s3() {
    info "Deploying static assets to S3..."
    
    cd "$DIST_DIR"
    
    # Sync files with appropriate cache headers
    info "Uploading HTML files (no cache)..."
    aws s3 sync . "s3://$S3_BUCKET" \
        --profile "$AWS_PROFILE" \
        --region "$AWS_REGION" \
        --exclude "*" \
        --include "*.html" \
        --cache-control "no-cache, no-store, must-revalidate" \
        --metadata-directive REPLACE \
        --delete
    
    info "Uploading CSS and JS files (1 year cache)..."
    aws s3 sync . "s3://$S3_BUCKET" \
        --profile "$AWS_PROFILE" \
        --region "$AWS_REGION" \
        --exclude "*" \
        --include "*.css" \
        --include "*.js" \
        --cache-control "public, max-age=31536000, immutable" \
        --metadata-directive REPLACE
    
    info "Uploading image files (1 year cache)..."
    aws s3 sync . "s3://$S3_BUCKET" \
        --profile "$AWS_PROFILE" \
        --region "$AWS_REGION" \
        --exclude "*" \
        --include "*.png" \
        --include "*.jpg" \
        --include "*.jpeg" \
        --include "*.gif" \
        --include "*.svg" \
        --include "*.ico" \
        --cache-control "public, max-age=31536000, immutable" \
        --metadata-directive REPLACE
    
    info "Uploading font files (1 year cache)..."
    aws s3 sync . "s3://$S3_BUCKET" \
        --profile "$AWS_PROFILE" \
        --region "$AWS_REGION" \
        --exclude "*" \
        --include "*.woff" \
        --include "*.woff2" \
        --include "*.ttf" \
        --include "*.eot" \
        --cache-control "public, max-age=31536000, immutable" \
        --metadata-directive REPLACE
    
    info "Uploading remaining files..."
    aws s3 sync . "s3://$S3_BUCKET" \
        --profile "$AWS_PROFILE" \
        --region "$AWS_REGION" \
        --cache-control "public, max-age=86400" \
        --metadata-directive REPLACE
    
    success "Static assets deployed to S3"
}

# Configure S3 bucket for static website hosting
configure_s3_website() {
    info "Configuring S3 bucket for static website hosting..."
    
    # Create website configuration
    cat > /tmp/website-config.json << EOF
{
    "IndexDocument": {
        "Suffix": "index.html"
    },
    "ErrorDocument": {
        "Key": "index.html"
    }
}
EOF
    
    # Apply website configuration
    aws s3api put-bucket-website \
        --bucket "$S3_BUCKET" \
        --website-configuration file:///tmp/website-config.json \
        --profile "$AWS_PROFILE"
    
    # Clean up temp file
    rm -f /tmp/website-config.json
    
    success "S3 website configuration applied"
}

# Invalidate CloudFront cache
invalidate_cloudfront() {
    if [[ -n "$CLOUDFRONT_DISTRIBUTION_ID" ]]; then
        info "Invalidating CloudFront cache..."
        
        local invalidation_id=$(aws cloudfront create-invalidation \
            --distribution-id "$CLOUDFRONT_DISTRIBUTION_ID" \
            --paths "/*" \
            --profile "$AWS_PROFILE" \
            --query 'Invalidation.Id' \
            --output text)
        
        info "CloudFront invalidation created: $invalidation_id"
        info "Waiting for invalidation to complete..."
        
        aws cloudfront wait invalidation-completed \
            --distribution-id "$CLOUDFRONT_DISTRIBUTION_ID" \
            --id "$invalidation_id" \
            --profile "$AWS_PROFILE"
        
        success "CloudFront cache invalidated"
    else
        warning "CloudFront distribution ID not provided, skipping cache invalidation"
    fi
}

# Show deployment summary
show_summary() {
    info "Deployment Summary:"
    echo "=================="
    echo "S3 Bucket: $S3_BUCKET"
    echo "AWS Region: $AWS_REGION"
    echo "AWS Profile: $AWS_PROFILE"
    
    if [[ -n "$CLOUDFRONT_DISTRIBUTION_ID" ]]; then
        echo "CloudFront Distribution: $CLOUDFRONT_DISTRIBUTION_ID"
    fi
    
    echo "Deployment Time: $(date)"
    echo
    
    # Calculate deployed size
    cd "$DIST_DIR"
    local deployed_size=$(du -sh . | cut -f1)
    echo "Deployed Size: $deployed_size"
    echo "Total Files: $(find . -type f | wc -l)"
    echo
    
    success "🎉 Deployment completed successfully!"
    echo
    info "Website URLs:"
    echo "S3 Website: http://$S3_BUCKET.s3-website-$AWS_REGION.amazonaws.com"
    
    if [[ -n "$CLOUDFRONT_DISTRIBUTION_ID" ]]; then
        local cloudfront_domain=$(aws cloudfront get-distribution \
            --id "$CLOUDFRONT_DISTRIBUTION_ID" \
            --profile "$AWS_PROFILE" \
            --query 'Distribution.DomainName' \
            --output text)
        echo "CloudFront: https://$cloudfront_domain"
    fi
}

# Main deployment function
main() {
    log "${GREEN}🚀 Starting S3/CDN Deployment${NC}"
    log "================================"
    
    check_prerequisites
    validate_s3_bucket
    deploy_to_s3
    configure_s3_website
    invalidate_cloudfront
    show_summary
}

# Handle script arguments
case "${1:-deploy}" in
    "deploy")
        main
        ;;
    "check")
        check_prerequisites
        validate_s3_bucket
        ;;
    "invalidate")
        if [[ -n "$CLOUDFRONT_DISTRIBUTION_ID" ]]; then
            invalidate_cloudfront
        else
            error_exit "CloudFront distribution ID not provided"
        fi
        ;;
    "help")
        echo "Usage: $0 [deploy|check|invalidate|help]"
        echo "  deploy     - Full deployment to S3 and CloudFront (default)"
        echo "  check      - Check prerequisites and S3 bucket"
        echo "  invalidate - Invalidate CloudFront cache only"
        echo "  help       - Show this help message"
        echo
        echo "Required Environment Variables:"
        echo "  S3_BUCKET                    - S3 bucket name for static assets"
        echo "  CLOUDFRONT_DISTRIBUTION_ID   - CloudFront distribution ID (optional)"
        echo "  AWS_REGION                   - AWS region (default: us-east-1)"
        echo "  AWS_PROFILE                  - AWS profile (default: default)"
        ;;
    *)
        error_exit "Unknown command: $1. Use 'help' for usage information."
        ;;
esac