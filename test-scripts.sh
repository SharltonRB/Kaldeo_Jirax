#!/bin/bash

# Testing Scripts for Personal Issue Tracker
# Provides convenient commands for different testing scenarios

set -e

echo "🧪 Personal Issue Tracker - Testing Scripts"
echo "=========================================="

case "$1" in
    "fast"|"h2"|"")
        echo "🚀 Running FAST tests with H2 database..."
        echo "   - Perfect for local development"
        echo "   - No Docker required"
        echo "   - Runs in seconds"
        echo ""
        mvn test -Dtest='!**/*TestcontainersTest'
        ;;
    
    "production"|"postgres"|"testcontainers")
        echo "🐘 Running PRODUCTION tests with PostgreSQL (Testcontainers)..."
        echo "   - Production parity testing"
        echo "   - Requires Docker"
        echo "   - Takes longer but more accurate"
        echo ""
        mvn test -Dspring.profiles.active=testcontainers
        ;;
    
    "property")
        echo "🔬 Running PROPERTY tests (H2 - Fast)..."
        echo "   - Property-based testing"
        echo "   - Fast feedback loop"
        echo ""
        mvn test -Dtest="*PropertyTest"
        ;;
    
    "ci")
        echo "🤖 Running CI/CD test suite..."
        echo "   Step 1: Fast tests (H2)"
        mvn test -Dtest='!**/*TestcontainersTest'
        echo ""
        echo "   Step 2: Production tests (PostgreSQL)"
        mvn test -Dspring.profiles.active=testcontainers
        echo ""
        echo "✅ All CI/CD tests passed!"
        ;;
    
    "install")
        echo "📦 Running Maven clean install (without Docker tests)..."
        echo "   - Compiles, tests, and packages the application"
        echo "   - Skips Testcontainers tests (no Docker required)"
        echo "   - Creates JAR file in target/"
        echo ""
        mvn clean install -Dtest='!**/*TestcontainersTest'
        ;;
    
    "help"|"-h"|"--help")
        echo "Available commands:"
        echo ""
        echo "  fast, h2          - Fast tests with H2 (default)"
        echo "  production        - Production tests with PostgreSQL"
        echo "  property          - Property tests (H2)"
        echo "  ci                - Full CI/CD test suite"
        echo "  install           - Maven clean install (without Docker tests)"
        echo "  help              - Show this help"
        echo ""
        echo "Examples:"
        echo "  ./test-scripts.sh                    # Fast H2 tests"
        echo "  ./test-scripts.sh production         # PostgreSQL tests"
        echo "  ./test-scripts.sh property           # Property tests"
        echo "  ./test-scripts.sh install            # Clean install"
        echo "  ./test-scripts.sh ci                 # Full CI suite"
        ;;
    
    *)
        echo "❌ Unknown command: $1"
        echo "Run './test-scripts.sh help' for available commands"
        exit 1
        ;;
esac