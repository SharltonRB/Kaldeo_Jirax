#!/bin/bash

# Script para ejecutar diferentes tipos de tests de manera optimizada

set -e

echo "🧪 Personal Issue Tracker - Test Scripts"
echo "========================================"

case "${1:-help}" in
    "fast")
        echo "🚀 Ejecutando tests rápidos (sin property tests)..."
        mvn clean test -Pfast-tests
        ;;
    "quick-property")
        echo "⚡ Ejecutando property tests rápidos (10 casos por test)..."
        mvn clean test -Pquick-property-tests
        ;;
    "unit")
        echo "🔧 Ejecutando solo tests unitarios..."
        mvn test -Dtest="!*PropertyTest,!*IntegrationTest"
        ;;
    "property")
        echo "🎲 Ejecutando property tests con configuración normal (25 casos)..."
        mvn test -Dtest="*PropertyTest"
        ;;
    "integration")
        echo "🔗 Ejecutando tests de integración..."
        mvn test -Dtest="*IntegrationTest"
        ;;
    "ci")
        echo "🏗️ Ejecutando suite completa para CI (100 casos por property test)..."
        mvn clean test -Pci-tests
        ;;
    "compile")
        echo "🔨 Solo compilando sin ejecutar tests..."
        mvn clean compile test-compile
        ;;
    "install-fast"|"install")
        echo "📦 Instalando con tests rápidos..."
        mvn clean install -Pfast-tests
        ;;
    "install-skip")
        echo "📦 Instalando sin tests..."
        mvn clean install -DskipTests
        ;;
    "build")
        echo "🏗️ Build completo rápido (alias para install-fast)..."
        mvn clean install -Pfast-tests
        ;;
    "help"|*)
        echo "Uso: $0 [comando]"
        echo ""
        echo "Comandos disponibles:"
        echo "  fast            - Tests rápidos (excluye property tests)"
        echo "  quick-property  - Property tests rápidos (10 casos)"
        echo "  unit           - Solo tests unitarios"
        echo "  property       - Property tests normales (25 casos)"
        echo "  integration    - Tests de integración"
        echo "  ci             - Suite completa para CI (100 casos)"
        echo "  compile        - Solo compilar sin ejecutar tests"
        echo "  install        - mvn install con tests rápidos (RECOMENDADO)"
        echo "  install-fast   - Alias para install"
        echo "  install-skip   - mvn install sin tests"
        echo "  build          - Alias para install-fast"
        echo "  help           - Mostrar esta ayuda"
        echo ""
        echo "🚀 Comandos más usados:"
        echo "  ./test-scripts.sh install    # Para build diario (8 segundos)"
        echo "  ./test-scripts.sh fast       # Para verificar tests (8 segundos)"
        echo "  ./test-scripts.sh ci         # Para verificación completa (2-3 min)"
        echo ""
        echo "💡 Tip: 'install' es ahora el comando por defecto recomendado"
        ;;
esac