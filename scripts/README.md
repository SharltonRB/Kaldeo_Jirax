# Scripts de Automatización

## Descripción
Scripts para automatizar tareas comunes de desarrollo, testing y despliegue.

## Scripts Disponibles

### 🧪 Testing
- `test-scripts.sh` - Scripts de testing automatizado

### ⚙️ Setup
- `setup/setup-frontend.sh` - Configuración inicial del frontend (Linux/macOS)
- `setup/setup-frontend.bat` - Configuración inicial del frontend (Windows)
- `setup/setup-backend.sh` - Configuración inicial del backend

### 🔒 Seguridad
- `security-audit.sh` - Escáner de vulnerabilidades de seguridad
- `pre-commit-security-check.sh` - Validación de seguridad pre-commit
- `install-security-hooks.sh` - Instalar hooks de seguridad de git

### 🏗️ Build
- `build.sh` - Script de automatización de construcción

## Próximos Scripts a Implementar

### 🧪 Test
```bash
scripts/test.sh [backend|frontend|all]
```

### 🚀 Deploy
```bash
scripts/deploy.sh [dev|staging|prod]
```

## Uso

### Configuración Inicial
```bash
# Frontend
./scripts/setup/setup-frontend.sh

# Backend
./scripts/setup/setup-backend.sh
```

### Testing
```bash
# Ejecutar todos los tests
./scripts/test-scripts.sh
```

### Seguridad
```bash
# Auditoría de seguridad
./scripts/security-audit.sh scan

# Instalar hooks de seguridad
./scripts/install-security-hooks.sh
```

### Build
```bash
# Construir todo
./scripts/build.sh all
```

## Convenciones
- Todos los scripts deben ser ejecutables (`chmod +x`)
- Usar `set -e` para fallar rápido
- Incluir mensajes informativos
- Validar prerrequisitos antes de ejecutar