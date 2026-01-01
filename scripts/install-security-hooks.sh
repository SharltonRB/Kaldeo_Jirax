#!/bin/bash

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

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

# Verificar que estamos en un repositorio git
if [ ! -d ".git" ]; then
    log_error "This is not a git repository. Please run from the project root."
    exit 1
fi

log "Installing security git hooks..."

# Crear directorio de hooks si no existe
mkdir -p .git/hooks

# Instalar pre-commit hook
log "Installing pre-commit security check..."
cp scripts/pre-commit-security-check.sh .git/hooks/pre-commit
chmod +x .git/hooks/pre-commit

# Crear pre-push hook para auditoría completa
log "Creating pre-push security audit..."
cat > .git/hooks/pre-push << 'EOF'
#!/bin/bash

echo "🔍 Running security audit before push..."

# Ejecutar auditoría de seguridad
if ! ./scripts/security-audit.sh scan > /dev/null 2>&1; then
    echo "❌ Security audit failed. Please fix issues before pushing."
    echo "Run './scripts/security-audit.sh scan' for details."
    exit 1
fi

echo "✅ Security audit passed"
exit 0
EOF

chmod +x .git/hooks/pre-push

# Crear commit-msg hook para verificar mensajes de commit
log "Creating commit message security check..."
cat > .git/hooks/commit-msg << 'EOF'
#!/bin/bash

commit_msg_file=$1
commit_msg=$(cat "$commit_msg_file")

# Verificar que no se mencionen credenciales en el mensaje de commit
if echo "$commit_msg" | grep -qiE "password|secret|key|token|credential"; then
    echo "❌ Commit message contains potentially sensitive words."
    echo "Please avoid mentioning credentials in commit messages."
    exit 1
fi

exit 0
EOF

chmod +x .git/hooks/commit-msg

log_success "Security git hooks installed successfully!"

echo ""
echo "Installed hooks:"
echo "  ✓ pre-commit  - Prevents committing sensitive files and data"
echo "  ✓ pre-push    - Runs security audit before pushing"
echo "  ✓ commit-msg  - Prevents sensitive words in commit messages"
echo ""
echo "To test the hooks:"
echo "  ./scripts/pre-commit-security-check.sh"
echo "  ./scripts/security-audit.sh scan"
echo ""
echo "To bypass hooks (emergency only):"
echo "  git commit --no-verify"
echo "  git push --no-verify"