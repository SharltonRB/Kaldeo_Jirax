#!/bin/bash

# Script para build limpio y rápido del proyecto
# Uso: ./scripts/clean-build.sh

set -e

echo "🧹 Starting clean build process..."

# Cambiar al directorio del backend
cd backend

echo "📦 Cleaning previous builds..."
mvn clean -q

echo "🔧 Compiling with minimal logging..."
mvn compile -q -Pclean-build

echo "🧪 Running tests with minimal output..."
mvn test -q -Pclean-build

echo "📦 Packaging application..."
mvn package -q -Pclean-build -DskipTests

echo "✅ Clean build completed successfully!"
echo ""
echo "📊 Build Summary:"
echo "  - Compilation: ✅ Success"
echo "  - Tests: ✅ Passed"
echo "  - Package: ✅ Created"
echo ""
echo "🚀 Application ready to run!"