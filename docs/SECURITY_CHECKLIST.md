# Security Checklist

[Versión en Español](SECURITY_CHECKLIST.es.md)

## Overview

This document provides a security checklist for the Personal Issue Tracker project to ensure sensitive information is not exposed in the public GitHub repository.

## ✅ Security Measures Implemented

### 1. Environment Variables Protection

#### Files Protected by .gitignore
- ✅ All `.env` files (except `.env.example` and `.env.*.template`)
- ✅ `backend/.env` - Contains JWT secrets and database credentials
- ✅ `frontend/.env` - Contains API configuration
- ✅ `frontend/.env.production` - Production API URLs
- ✅ `frontend/.env.staging` - Staging environment configuration

#### Template Files (Safe to Commit)
- ✅ `backend/.env.example` - Example configuration without secrets
- ✅ `backend/.env.prod.template` - Production template with placeholders
- ✅ `frontend/.env.example` - Frontend example configuration

### 2. Configuration Files

#### Spring Boot Configuration
All production configuration files use environment variables:
- ✅ `application-prod.yml` - Uses `${VARIABLE}` syntax for all sensitive data
- ✅ No hardcoded passwords, secrets, or API keys
- ✅ JWT secrets loaded from environment variables
- ✅ Database credentials loaded from environment variables
- ✅ Redis passwords loaded from environment variables

#### Safe Configuration Files (Committed)
- ✅ `application.yml` - Base configuration without secrets
- ✅ `application-dev.yml` - Development defaults (non-sensitive)
- ✅ `application-test.yml` - Test configuration (non-sensitive)
- ✅ `.testcontainers.properties` - Local Docker configuration (non-sensitive)

### 3. Docker and Infrastructure

#### Protected Files
- ✅ `docker-compose.override.yml` - Local overrides with potential secrets
- ✅ `docker-compose.local.yml` - Local development with credentials
- ✅ `infrastructure/k8s/secrets/` - Kubernetes secrets directory
- ✅ `infrastructure/terraform/*.tfvars` - Terraform variables with secrets

#### Safe Files (Committed)
- ✅ `docker-compose.yml` - Base development configuration
- ✅ `docker-compose.prod.yml` - Production template using environment variables

### 4. Certificates and Keys

All certificate and key files are protected:
- ✅ `*.key` - Private keys
- ✅ `*.pem` - PEM certificates
- ✅ `*.p12` - PKCS12 keystores
- ✅ `*.jks` - Java keystores
- ✅ `certs/` - Certificate directories
- ✅ `ssl/` - SSL directories

### 5. Cloud Credentials

All cloud provider credentials are protected:
- ✅ `.aws/` - AWS credentials
- ✅ `gcp-credentials.json` - Google Cloud credentials
- ✅ `.azure/` - Azure credentials
- ✅ `api-keys.txt` - API keys
- ✅ `tokens.txt` - Authentication tokens

### 6. Database Files

Protected database files:
- ✅ `*.sql.backup` - Database backups
- ✅ `*.dump` - Database dumps
- ✅ `backup/` and `backups/` - Backup directories
- ✅ Database volume directories (postgres_data/, redis_data/, etc.)

### 7. IDE and System Files

Excluded unnecessary files:
- ✅ `.DS_Store` - macOS system files
- ✅ `.idea/` - IntelliJ IDEA configuration
- ✅ `.vscode/` - VS Code configuration (except shared settings)
- ✅ `Thumbs.db` - Windows thumbnail cache
- ✅ `*.swp`, `*.swo` - Vim swap files

### 8. Build Artifacts and Dependencies

Excluded build outputs:
- ✅ `backend/target/` - Maven build output
- ✅ `frontend/node_modules/` - NPM dependencies
- ✅ `frontend/dist/` - Frontend build output
- ✅ `*.class`, `*.jar` - Compiled Java files

### 9. Logs and Temporary Files

Protected log files:
- ✅ `logs/` - Application logs directory
- ✅ `*.log` - All log files
- ✅ `*.tmp`, `*.temp` - Temporary files
- ✅ `cache/`, `.cache/` - Cache directories

### 10. Personal and Development Files

Excluded personal files:
- ✅ `TODO.md`, `NOTES.md` - Personal notes
- ✅ `personal-notes/` - Personal documentation
- ✅ `scratch/` - Scratch files
- ✅ `*.local`, `*.personal`, `*.private` - Personal configurations

## 🔒 Sensitive Information Guidelines

### What NEVER to Commit

1. **Passwords and Secrets**
   - Database passwords
   - JWT secrets
   - API keys
   - OAuth client secrets
   - Encryption keys

2. **Credentials**
   - Cloud provider credentials (AWS, GCP, Azure)
   - Service account keys
   - SSH private keys
   - SSL/TLS certificates and private keys

3. **Personal Information**
   - Email addresses (except in documentation)
   - Phone numbers
   - Personal API tokens
   - User data or PII

4. **Production Configuration**
   - Production database URLs with credentials
   - Production API endpoints with authentication
   - Production service URLs
   - Internal network information

### What is Safe to Commit

1. **Template Files**
   - `.env.example` files with placeholder values
   - `.env.*.template` files with variable names
   - Configuration templates with `${VARIABLE}` syntax

2. **Development Defaults**
   - Local development URLs (localhost)
   - Default development ports
   - Non-sensitive feature flags
   - Public API endpoints (without authentication)

3. **Documentation**
   - Setup instructions
   - Architecture diagrams
   - API documentation
   - Development guides

## 🛡️ Security Best Practices

### Before Committing

1. **Review Changes**
   ```bash
   git diff
   git status
   ```

2. **Check for Secrets**
   ```bash
   # Search for potential secrets
   git diff | grep -i "password\|secret\|key\|token"
   ```

3. **Verify .gitignore**
   ```bash
   # Check if file is ignored
   git check-ignore <filename>
   ```

### Environment Variables

1. **Use Environment Variables**
   - Never hardcode secrets in code
   - Use `${VARIABLE}` syntax in configuration files
   - Load from environment or secret management systems

2. **Document Required Variables**
   - List all required environment variables in `.env.example`
   - Provide descriptions and example values
   - Document in README and deployment guides

3. **Separate Environments**
   - Use different secrets for dev, staging, and production
   - Never use production secrets in development
   - Rotate secrets regularly

### Secret Management

1. **Development**
   - Use `.env` files (gitignored)
   - Use local secret management tools
   - Never share secrets via chat or email

2. **Production**
   - Use secret management services (AWS Secrets Manager, HashiCorp Vault, etc.)
   - Use environment variables in deployment platforms
   - Implement secret rotation policies

3. **CI/CD**
   - Use CI/CD platform secret management
   - Never log secrets in CI/CD output
   - Use masked variables in CI/CD logs

## 🔍 Verification Commands

### Check for Committed Secrets

```bash
# Check if sensitive files are tracked
git ls-files | grep -E "\.env$|\.env\.|secret|password|key"

# Search for potential secrets in committed files
git grep -i "password\|secret\|api_key\|token" -- '*.yml' '*.properties' '*.json'

# Check .gitignore effectiveness
git status --ignored
```

### Verify Environment Files

```bash
# List all .env files
find . -name ".env*" -not -path "*/node_modules/*"

# Check which are ignored
find . -name ".env*" -not -path "*/node_modules/*" | xargs -I {} git check-ignore {}
```

### Audit Configuration Files

```bash
# Check for hardcoded values in Spring Boot configs
grep -r "password:" backend/src/main/resources/
grep -r "secret:" backend/src/main/resources/

# Should only find ${VARIABLE} references, not actual values
```

## 📋 Pre-Deployment Checklist

Before deploying to production:

- [ ] All secrets are stored in environment variables or secret management
- [ ] No `.env` files are committed (except `.example` and `.template`)
- [ ] All configuration files use `${VARIABLE}` syntax for secrets
- [ ] Production URLs and endpoints are not hardcoded
- [ ] SSL/TLS certificates are properly secured
- [ ] Database credentials are not in version control
- [ ] API keys are loaded from environment
- [ ] JWT secrets are strong and unique per environment
- [ ] Cloud credentials are not committed
- [ ] Backup files are gitignored
- [ ] Logs are gitignored
- [ ] Build artifacts are gitignored

## 🚨 If Secrets Are Accidentally Committed

If you accidentally commit secrets:

1. **Immediately Rotate the Secret**
   - Change the password/key/token immediately
   - Update in all environments

2. **Remove from Git History**
   ```bash
   # Use git filter-branch or BFG Repo-Cleaner
   # WARNING: This rewrites history
   git filter-branch --force --index-filter \
     "git rm --cached --ignore-unmatch <file-with-secret>" \
     --prune-empty --tag-name-filter cat -- --all
   ```

3. **Force Push (if necessary)**
   ```bash
   git push origin --force --all
   ```

4. **Notify Team**
   - Inform team members about the incident
   - Ensure everyone updates their local repositories
   - Document the incident for future reference

## 📚 Additional Resources

- [OWASP Secrets Management Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Secrets_Management_Cheat_Sheet.html)
- [GitHub Security Best Practices](https://docs.github.com/en/code-security/getting-started/best-practices-for-preventing-data-leaks-in-your-organization)
- [12-Factor App: Config](https://12factor.net/config)

---

**Last Updated**: January 14, 2026
**Review Frequency**: Quarterly or after major changes
