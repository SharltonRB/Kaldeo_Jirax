#!/bin/bash

# Pre-commit security check
# This script should be installed as a git pre-commit hook

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}🔍 Running pre-commit security checks...${NC}"

# Verificar archivos que están siendo committeados
staged_files=$(git diff --cached --name-only)

if [ -z "$staged_files" ]; then
    echo -e "${GREEN}✓ No files to check${NC}"
    exit 0
fi

# Patrones de archivos que nunca deberían ser committeados
forbidden_patterns=(
    "\.env$"
    "\.key$"
    "\.pem$"
    "\.p12$"
    "\.jks$"
    "application-local\.yml$"
    "application-secret\.yml$"
    "credentials\.json$"
    "keystore\.jks$"
    "truststore\.jks$"
)

# Verificar archivos prohibidos
echo "Checking for forbidden files..."
for file in $staged_files; do
    for pattern in "${forbidden_patterns[@]}"; do
        if echo "$file" | grep -qE "$pattern"; then
            echo -e "${RED}✗ BLOCKED: Attempting to commit forbidden file: $file${NC}"
            echo -e "${RED}  This file type should never be committed to version control${NC}"
            exit 1
        fi
    done
done

# Verificar contenido sensible en archivos que se están committeando
echo "Checking for sensitive content..."
sensitive_patterns=(
    "password\s*=\s*['\"][^'\"]{3,}"
    "secret\s*=\s*['\"][^'\"]{3,}"
    "api[_-]?key\s*=\s*['\"][^'\"]{3,}"
    "private[_-]?key"
    "BEGIN\s+(RSA\s+)?PRIVATE\s+KEY"
)

for file in $staged_files; do
    if [ -f "$file" ]; then
        for pattern in "${sensitive_patterns[@]}"; do
            if grep -qiE "$pattern" "$file" 2>/dev/null; then
                # Excluir archivos de test que pueden tener datos de prueba
                if [[ "$file" =~ Test\.java$ ]] || [[ "$file" =~ \.test\. ]] || [[ "$file" =~ /test/ ]]; then
                    echo -e "${YELLOW}⚠ Warning: Found potential sensitive data in test file: $file${NC}"
                    continue
                fi
                
                echo -e "${RED}✗ BLOCKED: Found potential sensitive data in: $file${NC}"
                echo -e "${RED}  Pattern: $pattern${NC}"
                echo -e "${YELLOW}  If this is test data, ensure it's clearly marked as such${NC}"
                exit 1
            fi
        done
    fi
done

# Verificar que archivos .env.example existen si hay archivos .env
if echo "$staged_files" | grep -q "\.env"; then
    echo -e "${RED}✗ BLOCKED: Attempting to commit .env file${NC}"
    echo -e "${RED}  Use .env.example instead and set real values via environment variables${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Security checks passed${NC}"
exit 0