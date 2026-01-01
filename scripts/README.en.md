# Automation Scripts

## Description
Scripts to automate common development, testing, and deployment tasks.

## Available Scripts

### 🧪 Testing
- `test-scripts.sh` - Automated testing scripts

### ⚙️ Setup
- `setup/setup-frontend.sh` - Frontend initial setup (Linux/macOS)
- `setup/setup-frontend.bat` - Frontend initial setup (Windows)
- `setup/setup-backend.sh` - Backend initial setup

### 🔒 Security
- `security-audit.sh` - Security vulnerability scanner
- `pre-commit-security-check.sh` - Pre-commit security validation
- `install-security-hooks.sh` - Install git security hooks

### 🏗️ Build
- `build.sh` - Build automation script

## Next Scripts to Implement

### 🧪 Test
```bash
scripts/test.sh [backend|frontend|all]
```

### 🚀 Deploy
```bash
scripts/deploy.sh [dev|staging|prod]
```

## Usage

### Initial Setup
```bash
# Frontend
./scripts/setup/setup-frontend.sh

# Backend
./scripts/setup/setup-backend.sh
```

### Testing
```bash
# Run all tests
./scripts/test-scripts.sh
```

### Security
```bash
# Security audit
./scripts/security-audit.sh scan

# Install security hooks
./scripts/install-security-hooks.sh
```

### Build
```bash
# Build everything
./scripts/build.sh all
```

## Conventions
- All scripts must be executable (`chmod +x`)
- Use `set -e` to fail fast
- Include informative messages
- Validate prerequisites before execution