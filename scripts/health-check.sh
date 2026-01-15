#!/bin/bash

# Health Check Script for Personal Issue Tracker
# This script performs comprehensive health checks on the application

set -e

# Configuration
BACKEND_URL="${BACKEND_URL:-http://localhost:8080}"
TIMEOUT="${TIMEOUT:-10}"
ALERT_EMAIL="${ALERT_EMAIL:-}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Counters
CHECKS_PASSED=0
CHECKS_FAILED=0
CHECKS_WARNING=0

# Logging function
log() {
    echo -e "$1"
}

success() {
    log "${GREEN}✅ $1${NC}"
    ((CHECKS_PASSED++))
}

failure() {
    log "${RED}❌ $1${NC}"
    ((CHECKS_FAILED++))
}

warning() {
    log "${YELLOW}⚠️  $1${NC}"
    ((CHECKS_WARNING++))
}

info() {
    log "${BLUE}ℹ️  $1${NC}"
}

# Check if service is reachable
check_service_reachable() {
    info "Checking if backend service is reachable..."
    
    if curl -f -s --max-time "$TIMEOUT" "$BACKEND_URL/actuator/health" > /dev/null; then
        success "Backend service is reachable"
        return 0
    else
        failure "Backend service is not reachable"
        return 1
    fi
}

# Check overall health
check_overall_health() {
    info "Checking overall application health..."
    
    local response=$(curl -f -s --max-time "$TIMEOUT" "$BACKEND_URL/actuator/health" 2>/dev/null)
    
    if [[ -z "$response" ]]; then
        failure "Failed to get health status"
        return 1
    fi
    
    local status=$(echo "$response" | grep -o '"status":"[^"]*"' | head -1 | cut -d'"' -f4)
    
    if [[ "$status" == "UP" ]]; then
        success "Application health: UP"
        return 0
    else
        failure "Application health: $status"
        return 1
    fi
}

# Check database health
check_database_health() {
    info "Checking database health..."
    
    local response=$(curl -f -s --max-time "$TIMEOUT" "$BACKEND_URL/actuator/health" 2>/dev/null)
    
    if [[ -z "$response" ]]; then
        failure "Failed to get database health"
        return 1
    fi
    
    # Check if database component exists and is UP
    if echo "$response" | grep -q '"database".*"status":"UP"'; then
        local response_time=$(echo "$response" | grep -o '"responseTime":"[^"]*"' | cut -d'"' -f4)
        success "Database health: UP (Response time: $response_time)"
        return 0
    else
        failure "Database health: DOWN"
        return 1
    fi
}

# Check memory usage
check_memory_usage() {
    info "Checking memory usage..."
    
    local response=$(curl -f -s --max-time "$TIMEOUT" "$BACKEND_URL/monitoring/metrics" 2>/dev/null)
    
    if [[ -z "$response" ]]; then
        warning "Failed to get memory metrics"
        return 1
    fi
    
    local heap_usage=$(echo "$response" | grep -o '"heapUsagePercent":"[^"]*"' | cut -d'"' -f4 | sed 's/%//')
    
    if [[ -n "$heap_usage" ]]; then
        local heap_usage_int=$(echo "$heap_usage" | cut -d'.' -f1)
        
        if [[ $heap_usage_int -lt 80 ]]; then
            success "Memory usage: ${heap_usage}% (healthy)"
        elif [[ $heap_usage_int -lt 90 ]]; then
            warning "Memory usage: ${heap_usage}% (elevated)"
        else
            failure "Memory usage: ${heap_usage}% (critical)"
            return 1
        fi
    else
        warning "Could not determine memory usage"
    fi
    
    return 0
}

# Check response time
check_response_time() {
    info "Checking response time..."
    
    local start_time=$(date +%s%N)
    curl -f -s --max-time "$TIMEOUT" "$BACKEND_URL/actuator/health" > /dev/null 2>&1
    local end_time=$(date +%s%N)
    
    local response_time=$(( (end_time - start_time) / 1000000 ))
    
    if [[ $response_time -lt 500 ]]; then
        success "Response time: ${response_time}ms (excellent)"
    elif [[ $response_time -lt 1000 ]]; then
        success "Response time: ${response_time}ms (good)"
    elif [[ $response_time -lt 2000 ]]; then
        warning "Response time: ${response_time}ms (slow)"
    else
        failure "Response time: ${response_time}ms (critical)"
        return 1
    fi
    
    return 0
}

# Check error rate
check_error_rate() {
    info "Checking error rate..."
    
    local response=$(curl -f -s --max-time "$TIMEOUT" "$BACKEND_URL/monitoring/errors" 2>/dev/null)
    
    if [[ -z "$response" ]]; then
        warning "Failed to get error statistics"
        return 0
    fi
    
    # Check if there are any recent errors
    local recent_errors=$(echo "$response" | grep -o '"recentCount":[0-9]*' | cut -d':' -f2 | awk '{s+=$1} END {print s}')
    
    if [[ -z "$recent_errors" ]] || [[ "$recent_errors" == "0" ]]; then
        success "Error rate: No recent errors"
    elif [[ $recent_errors -lt 10 ]]; then
        warning "Error rate: $recent_errors errors in last minute"
    else
        failure "Error rate: $recent_errors errors in last minute (critical)"
        return 1
    fi
    
    return 0
}

# Check Docker containers (if running in Docker)
check_docker_containers() {
    if ! command -v docker &> /dev/null; then
        info "Docker not available, skipping container checks"
        return 0
    fi
    
    info "Checking Docker containers..."
    
    local containers=("issue-tracker-backend-prod" "issue-tracker-postgres-prod" "issue-tracker-redis-prod")
    local all_running=true
    
    for container in "${containers[@]}"; do
        if docker ps --format '{{.Names}}' | grep -q "^${container}$"; then
            local status=$(docker inspect --format='{{.State.Health.Status}}' "$container" 2>/dev/null || echo "unknown")
            
            if [[ "$status" == "healthy" ]] || [[ "$status" == "unknown" ]]; then
                success "Container $container: running"
            else
                warning "Container $container: $status"
                all_running=false
            fi
        else
            warning "Container $container: not running"
            all_running=false
        fi
    done
    
    if [[ "$all_running" == false ]]; then
        return 1
    fi
    
    return 0
}

# Send alert email
send_alert() {
    if [[ -z "$ALERT_EMAIL" ]]; then
        return 0
    fi
    
    local subject="Health Check Alert - Personal Issue Tracker"
    local body="Health check failed with $CHECKS_FAILED failures and $CHECKS_WARNING warnings.\n\nPlease check the application immediately."
    
    echo -e "$body" | mail -s "$subject" "$ALERT_EMAIL"
    info "Alert email sent to $ALERT_EMAIL"
}

# Main health check function
main() {
    log "${BLUE}🏥 Personal Issue Tracker - Health Check${NC}"
    log "=========================================="
    echo
    
    # Run all checks
    check_service_reachable || true
    check_overall_health || true
    check_database_health || true
    check_memory_usage || true
    check_response_time || true
    check_error_rate || true
    check_docker_containers || true
    
    # Summary
    echo
    log "=========================================="
    log "${BLUE}Health Check Summary${NC}"
    log "=========================================="
    success "Checks passed: $CHECKS_PASSED"
    
    if [[ $CHECKS_WARNING -gt 0 ]]; then
        warning "Checks with warnings: $CHECKS_WARNING"
    fi
    
    if [[ $CHECKS_FAILED -gt 0 ]]; then
        failure "Checks failed: $CHECKS_FAILED"
        send_alert
        exit 1
    else
        success "All health checks passed!"
        exit 0
    fi
}

# Handle script arguments
case "${1:-check}" in
    "check")
        main
        ;;
    "help")
        echo "Usage: $0 [check|help]"
        echo "  check - Run health checks (default)"
        echo "  help  - Show this help message"
        echo ""
        echo "Environment variables:"
        echo "  BACKEND_URL   - Backend URL (default: http://localhost:8080)"
        echo "  TIMEOUT       - Request timeout in seconds (default: 10)"
        echo "  ALERT_EMAIL   - Email address for alerts (optional)"
        ;;
    *)
        log "${RED}Unknown command: $1${NC}"
        echo "Use 'help' for usage information."
        exit 1
        ;;
esac
