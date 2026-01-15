#!/bin/bash

# Production Validation Script for Personal Issue Tracker
# This script performs comprehensive validation of the production deployment

set -e

# Configuration
BACKEND_URL="${BACKEND_URL:-http://localhost:8080}"
FRONTEND_URL="${FRONTEND_URL:-http://localhost:3000}"
TIMEOUT="${TIMEOUT:-30}"
LOAD_TEST_USERS="${LOAD_TEST_USERS:-10}"
LOAD_TEST_DURATION="${LOAD_TEST_DURATION:-60}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Counters
TESTS_PASSED=0
TESTS_FAILED=0
TESTS_WARNING=0

# Logging functions
log() {
    echo -e "$1"
}

success() {
    log "${GREEN}✅ $1${NC}"
    ((TESTS_PASSED++))
}

failure() {
    log "${RED}❌ $1${NC}"
    ((TESTS_FAILED++))
}

warning() {
    log "${YELLOW}⚠️  $1${NC}"
    ((TESTS_WARNING++))
}

info() {
    log "${BLUE}ℹ️  $1${NC}"
}

section() {
    echo
    log "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    log "${CYAN}$1${NC}"
    log "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo
}

# Test 1: Service Availability
test_service_availability() {
    section "1. Testing Service Availability"
    
    info "Testing backend availability..."
    if curl -f -s --max-time "$TIMEOUT" "$BACKEND_URL/actuator/health" > /dev/null; then
        success "Backend is available at $BACKEND_URL"
    else
        failure "Backend is not available at $BACKEND_URL"
        return 1
    fi
    
    info "Testing frontend availability..."
    if curl -f -s --max-time "$TIMEOUT" "$FRONTEND_URL" > /dev/null; then
        success "Frontend is available at $FRONTEND_URL"
    else
        failure "Frontend is not available at $FRONTEND_URL"
        return 1
    fi
}

# Test 2: Authentication Flow
test_authentication() {
    section "2. Testing Authentication Flow"
    
    info "Testing user registration..."
    local timestamp=$(date +%s)
    local test_email="test_${timestamp}@example.com"
    local test_password="TestPassword123!"
    
    local register_response=$(curl -s -X POST "$BACKEND_URL/api/auth/register" \
        -H "Content-Type: application/json" \
        -d "{\"email\":\"$test_email\",\"password\":\"$test_password\",\"name\":\"Test User\"}" \
        -w "\n%{http_code}")
    
    local register_status=$(echo "$register_response" | tail -n1)
    
    if [[ "$register_status" == "200" ]] || [[ "$register_status" == "201" ]]; then
        success "User registration successful"
        
        info "Testing user login..."
        local login_response=$(curl -s -X POST "$BACKEND_URL/api/auth/login" \
            -H "Content-Type: application/json" \
            -d "{\"email\":\"$test_email\",\"password\":\"$test_password\"}" \
            -w "\n%{http_code}")
        
        local login_status=$(echo "$login_response" | tail -n1)
        local login_body=$(echo "$login_response" | head -n-1)
        
        if [[ "$login_status" == "200" ]]; then
            success "User login successful"
            
            # Extract JWT token
            local jwt_token=$(echo "$login_body" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
            
            if [[ -n "$jwt_token" ]]; then
                success "JWT token generated successfully"
                echo "$jwt_token" > /tmp/test_jwt_token
            else
                failure "JWT token not found in response"
            fi
        else
            failure "User login failed with status $login_status"
        fi
    else
        failure "User registration failed with status $register_status"
    fi
}

# Test 3: CRUD Operations
test_crud_operations() {
    section "3. Testing CRUD Operations"
    
    if [[ ! -f /tmp/test_jwt_token ]]; then
        warning "Skipping CRUD tests - no JWT token available"
        return 0
    fi
    
    local jwt_token=$(cat /tmp/test_jwt_token)
    
    info "Testing project creation..."
    local project_response=$(curl -s -X POST "$BACKEND_URL/api/projects" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $jwt_token" \
        -d "{\"name\":\"Test Project\",\"key\":\"TEST\",\"description\":\"Test project for validation\"}" \
        -w "\n%{http_code}")
    
    local project_status=$(echo "$project_response" | tail -n1)
    local project_body=$(echo "$project_response" | head -n-1)
    
    if [[ "$project_status" == "200" ]] || [[ "$project_status" == "201" ]]; then
        success "Project creation successful"
        
        local project_id=$(echo "$project_body" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
        
        if [[ -n "$project_id" ]]; then
            info "Testing issue creation..."
            local issue_response=$(curl -s -X POST "$BACKEND_URL/api/issues" \
                -H "Content-Type: application/json" \
                -H "Authorization: Bearer $jwt_token" \
                -d "{\"title\":\"Test Issue\",\"description\":\"Test issue\",\"projectId\":$project_id,\"priority\":\"MEDIUM\",\"issueTypeId\":1}" \
                -w "\n%{http_code}")
            
            local issue_status=$(echo "$issue_response" | tail -n1)
            
            if [[ "$issue_status" == "200" ]] || [[ "$issue_status" == "201" ]]; then
                success "Issue creation successful"
            else
                failure "Issue creation failed with status $issue_status"
            fi
        fi
    else
        failure "Project creation failed with status $project_status"
    fi
}

# Test 4: Database Connectivity
test_database() {
    section "4. Testing Database Connectivity"
    
    info "Testing database health..."
    local health_response=$(curl -s "$BACKEND_URL/actuator/health")
    
    if echo "$health_response" | grep -q '"database".*"status":"UP"'; then
        success "Database is healthy"
        
        local response_time=$(echo "$health_response" | grep -o '"responseTime":"[^"]*"' | cut -d'"' -f4)
        if [[ -n "$response_time" ]]; then
            info "Database response time: $response_time"
        fi
    else
        failure "Database is not healthy"
    fi
}

# Test 5: Performance Metrics
test_performance() {
    section "5. Testing Performance Metrics"
    
    info "Testing memory usage..."
    local metrics_response=$(curl -s "$BACKEND_URL/monitoring/metrics")
    
    if [[ -n "$metrics_response" ]]; then
        local heap_usage=$(echo "$metrics_response" | grep -o '"heapUsagePercent":"[^"]*"' | cut -d'"' -f4 | sed 's/%//')
        
        if [[ -n "$heap_usage" ]]; then
            local heap_usage_int=$(echo "$heap_usage" | cut -d'.' -f1)
            
            if [[ $heap_usage_int -lt 80 ]]; then
                success "Memory usage: ${heap_usage}% (healthy)"
            elif [[ $heap_usage_int -lt 90 ]]; then
                warning "Memory usage: ${heap_usage}% (elevated)"
            else
                failure "Memory usage: ${heap_usage}% (critical)"
            fi
        fi
    else
        warning "Could not retrieve performance metrics"
    fi
    
    info "Testing response time..."
    local start_time=$(date +%s%N)
    curl -s "$BACKEND_URL/actuator/health" > /dev/null
    local end_time=$(date +%s%N)
    
    local response_time=$(( (end_time - start_time) / 1000000 ))
    
    if [[ $response_time -lt 500 ]]; then
        success "Response time: ${response_time}ms (excellent)"
    elif [[ $response_time -lt 1000 ]]; then
        success "Response time: ${response_time}ms (good)"
    elif [[ $response_time -lt 2000 ]]; then
        warning "Response time: ${response_time}ms (acceptable)"
    else
        failure "Response time: ${response_time}ms (too slow)"
    fi
}

# Test 6: Security Configuration
test_security() {
    section "6. Testing Security Configuration"
    
    info "Testing CORS configuration..."
    local cors_response=$(curl -s -I -X OPTIONS "$BACKEND_URL/api/projects" \
        -H "Origin: http://localhost:3000" \
        -H "Access-Control-Request-Method: GET")
    
    if echo "$cors_response" | grep -q "Access-Control-Allow-Origin"; then
        success "CORS is configured"
    else
        warning "CORS headers not found"
    fi
    
    info "Testing security headers..."
    local security_headers=$(curl -s -I "$BACKEND_URL/actuator/health")
    
    if echo "$security_headers" | grep -q "X-Content-Type-Options"; then
        success "Security headers are present"
    else
        warning "Some security headers may be missing"
    fi
    
    info "Testing authentication requirement..."
    local unauth_response=$(curl -s -w "\n%{http_code}" "$BACKEND_URL/api/projects")
    local unauth_status=$(echo "$unauth_response" | tail -n1)
    
    if [[ "$unauth_status" == "401" ]] || [[ "$unauth_status" == "403" ]]; then
        success "Protected endpoints require authentication"
    else
        failure "Protected endpoints may not require authentication (status: $unauth_status)"
    fi
}

# Test 7: SSL/TLS Configuration
test_ssl() {
    section "7. Testing SSL/TLS Configuration"
    
    if [[ "$BACKEND_URL" == https://* ]]; then
        info "Testing SSL certificate..."
        
        if openssl s_client -connect "${BACKEND_URL#https://}" -servername "${BACKEND_URL#https://}" < /dev/null 2>/dev/null | grep -q "Verify return code: 0"; then
            success "SSL certificate is valid"
        else
            warning "SSL certificate validation failed (may be self-signed)"
        fi
    else
        info "SSL not configured (using HTTP)"
    fi
}

# Test 8: Error Handling
test_error_handling() {
    section "8. Testing Error Handling"
    
    info "Testing 404 error handling..."
    local not_found_response=$(curl -s -w "\n%{http_code}" "$BACKEND_URL/api/nonexistent")
    local not_found_status=$(echo "$not_found_response" | tail -n1)
    
    if [[ "$not_found_status" == "404" ]]; then
        success "404 errors are handled correctly"
    else
        warning "404 error handling may not be correct (status: $not_found_status)"
    fi
    
    info "Testing validation error handling..."
    local validation_response=$(curl -s -X POST "$BACKEND_URL/api/auth/register" \
        -H "Content-Type: application/json" \
        -d "{\"email\":\"invalid\",\"password\":\"short\"}" \
        -w "\n%{http_code}")
    
    local validation_status=$(echo "$validation_response" | tail -n1)
    
    if [[ "$validation_status" == "400" ]]; then
        success "Validation errors are handled correctly"
    else
        warning "Validation error handling may not be correct (status: $validation_status)"
    fi
}

# Test 9: Load Testing (Basic)
test_load() {
    section "9. Testing Load Handling (Basic)"
    
    if ! command -v ab &> /dev/null; then
        info "Apache Bench (ab) not available, skipping load test"
        info "Install with: apt-get install apache2-utils (Linux) or brew install apache2 (macOS)"
        return 0
    fi
    
    info "Running basic load test (10 concurrent requests, 100 total)..."
    local load_result=$(ab -n 100 -c 10 -q "$BACKEND_URL/actuator/health" 2>&1)
    
    local failed_requests=$(echo "$load_result" | grep "Failed requests:" | awk '{print $3}')
    local requests_per_sec=$(echo "$load_result" | grep "Requests per second:" | awk '{print $4}')
    
    if [[ "$failed_requests" == "0" ]]; then
        success "Load test passed: 0 failed requests"
        if [[ -n "$requests_per_sec" ]]; then
            info "Throughput: $requests_per_sec requests/second"
        fi
    else
        failure "Load test failed: $failed_requests failed requests"
    fi
}

# Test 10: Docker Container Health
test_docker_health() {
    section "10. Testing Docker Container Health"
    
    if ! command -v docker &> /dev/null; then
        info "Docker not available, skipping container health checks"
        return 0
    fi
    
    local containers=("issue-tracker-backend-prod" "issue-tracker-postgres-prod" "issue-tracker-redis-prod")
    
    for container in "${containers[@]}"; do
        if docker ps --format '{{.Names}}' | grep -q "^${container}$"; then
            local status=$(docker inspect --format='{{.State.Status}}' "$container" 2>/dev/null)
            local health=$(docker inspect --format='{{.State.Health.Status}}' "$container" 2>/dev/null || echo "no-healthcheck")
            
            if [[ "$status" == "running" ]]; then
                if [[ "$health" == "healthy" ]] || [[ "$health" == "no-healthcheck" ]]; then
                    success "Container $container: running and healthy"
                else
                    warning "Container $container: running but health status is $health"
                fi
            else
                failure "Container $container: not running (status: $status)"
            fi
        else
            warning "Container $container: not found"
        fi
    done
}

# Cleanup function
cleanup() {
    rm -f /tmp/test_jwt_token
}

# Main function
main() {
    log "${CYAN}╔════════════════════════════════════════════════════════╗${NC}"
    log "${CYAN}║  Personal Issue Tracker - Production Validation       ║${NC}"
    log "${CYAN}╚════════════════════════════════════════════════════════╝${NC}"
    echo
    
    info "Backend URL: $BACKEND_URL"
    info "Frontend URL: $FRONTEND_URL"
    info "Timeout: ${TIMEOUT}s"
    echo
    
    # Run all tests
    test_service_availability || true
    test_authentication || true
    test_crud_operations || true
    test_database || true
    test_performance || true
    test_security || true
    test_ssl || true
    test_error_handling || true
    test_load || true
    test_docker_health || true
    
    # Cleanup
    cleanup
    
    # Summary
    section "Validation Summary"
    
    local total_tests=$((TESTS_PASSED + TESTS_FAILED + TESTS_WARNING))
    
    log "${GREEN}✅ Tests passed: $TESTS_PASSED${NC}"
    
    if [[ $TESTS_WARNING -gt 0 ]]; then
        log "${YELLOW}⚠️  Tests with warnings: $TESTS_WARNING${NC}"
    fi
    
    if [[ $TESTS_FAILED -gt 0 ]]; then
        log "${RED}❌ Tests failed: $TESTS_FAILED${NC}"
        echo
        log "${RED}Production validation FAILED!${NC}"
        log "${YELLOW}Please review the failures above and fix them before deploying to production.${NC}"
        exit 1
    else
        echo
        log "${GREEN}╔════════════════════════════════════════════════════════╗${NC}"
        log "${GREEN}║  ✅ All production validation tests passed!           ║${NC}"
        log "${GREEN}║  System is ready for production deployment.           ║${NC}"
        log "${GREEN}╚════════════════════════════════════════════════════════╝${NC}"
        
        if [[ $TESTS_WARNING -gt 0 ]]; then
            echo
            log "${YELLOW}Note: There are $TESTS_WARNING warnings that should be reviewed.${NC}"
        fi
        
        exit 0
    fi
}

# Handle script arguments
case "${1:-validate}" in
    "validate")
        main
        ;;
    "help")
        echo "Usage: $0 [validate|help]"
        echo ""
        echo "Production Validation Script"
        echo "Performs comprehensive validation of the production deployment"
        echo ""
        echo "Commands:"
        echo "  validate - Run all validation tests (default)"
        echo "  help     - Show this help message"
        echo ""
        echo "Environment variables:"
        echo "  BACKEND_URL   - Backend URL (default: http://localhost:8080)"
        echo "  FRONTEND_URL  - Frontend URL (default: http://localhost:3000)"
        echo "  TIMEOUT       - Request timeout in seconds (default: 30)"
        echo ""
        echo "Tests performed:"
        echo "  1. Service Availability"
        echo "  2. Authentication Flow"
        echo "  3. CRUD Operations"
        echo "  4. Database Connectivity"
        echo "  5. Performance Metrics"
        echo "  6. Security Configuration"
        echo "  7. SSL/TLS Configuration"
        echo "  8. Error Handling"
        echo "  9. Load Handling"
        echo "  10. Docker Container Health"
        ;;
    *)
        log "${RED}Unknown command: $1${NC}"
        echo "Use 'help' for usage information."
        exit 1
        ;;
esac
