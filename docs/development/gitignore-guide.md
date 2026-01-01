# .gitignore Guide

## Overview

This document explains the comprehensive `.gitignore` configuration for the Personal Issue Tracker project, which covers both backend (Spring Boot) and frontend (React TypeScript) components.

## Structure

The `.gitignore` is organized into logical sections for easy maintenance and understanding:

### 1. Java & Spring Boot (Backend)
- Compiled Java files (`.class`, `.jar`, etc.)
- Maven build artifacts (`target/`, dependency files)
- Spring Boot specific configurations (local profiles, secrets)
- JVM crash logs and performance dumps

### 2. Node.js & React (Frontend)
- Node modules and package manager files
- Build outputs (`dist/`, `build/`, `.next/`)
- Runtime data and process files
- Frontend tooling cache (ESLint, Parcel, Vite, etc.)

### 3. Logs & Monitoring
- Application logs from both backend and frontend
- JVM specific logs (GC, heap dumps, thread dumps)
- NPM/Yarn debug logs

### 4. Databases & Cache
- Local database files (SQLite, H2)
- Database dumps and backups
- Docker volume data directories

### 5. Docker & Infrastructure
- Docker Compose override files
- Kubernetes secrets and local configurations
- Terraform state files and variable files

### 6. Environment & Secrets
- Environment variable files (except examples)
- Security certificates and keys
- API keys and authentication tokens
- Cloud provider credentials

### 7. IDE & Editors
- Configuration for all major IDEs (IntelliJ, Eclipse, VS Code, etc.)
- Editor-specific files and caches
- Workspace and project files

### 8. Operating Systems
- OS-specific files (macOS, Windows, Linux)
- System thumbnails and metadata

### 9. Testing & Coverage
- Test result files and reports
- Code coverage outputs
- JaCoCo execution data

### 10. Build & Deployment
- Build artifacts and distribution files
- Package files and archives
- Gradle files (if used in future)

### 11. Development & Temporary
- Temporary files and caches
- Personal notes and scratch files
- Local development overrides

### 12. Kiro IDE Configuration
- Kiro-specific settings and cache
- Preserves specs and steering files
- Excludes sensitive user configurations

## Important Inclusions

### What IS tracked:
- Source code (`src/` directories)
- Configuration templates (`.env.example`)
- Documentation (`docs/`, `README.md` files)
- Build scripts (`scripts/`)
- Docker configuration (`docker-compose.yml`, Dockerfiles)
- Kiro specs and steering files (`.kiro/specs/`, `.kiro/steering/`)

### What is NOT tracked:
- Build outputs and compiled files
- Dependencies (`node_modules/`, `target/`)
- Local configuration files
- Sensitive data (keys, passwords, tokens)
- IDE-specific files
- OS-specific files
- Logs and temporary files

## Security Considerations

The `.gitignore` is designed with security in mind:

1. **Secrets Protection**: All potential secret files are ignored
2. **Environment Isolation**: Local configurations are excluded
3. **Credential Safety**: API keys, certificates, and tokens are ignored
4. **Database Security**: Connection strings and credentials are excluded

## Maintenance

### Adding New Patterns
When adding new tools or technologies:

1. Add patterns to the appropriate section
2. Use comments to explain complex patterns
3. Test patterns with `git check-ignore <file>`
4. Update this documentation

### Testing Ignore Patterns
```bash
# Check if a file would be ignored
git check-ignore path/to/file

# List all ignored files in a directory
git check-ignore path/to/directory/*

# Show which .gitignore rule matches a file
git check-ignore -v path/to/file
```

### Common Patterns Explained

| Pattern | Explanation |
|---------|-------------|
| `*.log` | Ignores all log files |
| `target/` | Ignores Maven build directory |
| `node_modules/` | Ignores NPM dependencies |
| `.env.*` | Ignores all environment files |
| `!.env.example` | Exception: tracks example env file |
| `backend/target/` | Ignores only backend build directory |

## Best Practices

1. **Be Specific**: Use path prefixes when possible (`backend/target/` vs `target/`)
2. **Use Comments**: Explain complex or project-specific patterns
3. **Group Related**: Keep related patterns together
4. **Test Changes**: Always test new patterns before committing
5. **Document Exceptions**: Explain why certain files are tracked despite patterns

## Troubleshooting

### File Still Tracked After Adding to .gitignore
```bash
# Remove from tracking but keep local file
git rm --cached path/to/file

# Remove directory from tracking
git rm -r --cached path/to/directory/
```

### Check What's Being Ignored
```bash
# Show ignored files
git status --ignored

# Show ignored files in specific directory
git status --ignored path/to/directory/
```

### Verify .gitignore Rules
```bash
# Test if file would be ignored
git check-ignore -v path/to/file

# List all .gitignore files affecting a path
git check-ignore --no-index path/to/file
```