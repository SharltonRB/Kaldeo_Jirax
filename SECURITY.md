# Security Guidelines

## 🔒 Environment Variables & Secrets

### ⚠️ NEVER COMMIT THESE FILES:
- `.env` files (already in .gitignore)
- Any file containing real passwords, API keys, or JWT secrets
- Production configuration files

### ✅ SAFE TO COMMIT:
- `.env.example` files (with placeholder values)
- Development configuration with placeholder secrets
- Test configuration with test-only credentials

## 🛡️ JWT Secrets

### Development
- Current development JWT secret is a placeholder
- Safe for local development only
- **NEVER use in production**

### Production
Generate a secure JWT secret:
```bash
# Generate a secure 256-bit secret
openssl rand -base64 64
```

## 🗄️ Database Credentials

### Development
- Username: `postgres`
- Password: `postgres` (default for local development)
- Database: `issue_tracker_dev`

### Production
- Use strong, unique passwords
- Store in environment variables
- Never hardcode in source code

## 📝 Configuration Files

### Safe Development Defaults
These files contain safe development defaults:
- `backend/src/main/resources/application.yml`
- `backend/src/main/resources/application-dev.yml`
- `frontend/.env.example`
- `backend/.env.example`

### Sensitive Files (Git Ignored)
- `backend/.env`
- `frontend/.env`
- Any `application-local*.yml`
- Any `application-secret*.yml`

## 🚨 Security Checklist

Before deploying to production:

- [ ] Generate new JWT secret (256+ bits)
- [ ] Use strong database passwords
- [ ] Update all default credentials
- [ ] Enable HTTPS/SSL
- [ ] Review all environment variables
- [ ] Ensure no secrets in source code
- [ ] Test with production-like environment

## 🔍 Audit Commands

Check for potential secrets in code:
```bash
# Search for potential secrets (run from project root)
grep -r "password\|secret\|key" --exclude-dir=node_modules --exclude-dir=target --exclude-dir=.git .

# Check what's being tracked by git
git ls-files | grep -E "\.(env|properties|yml)$"
```

## 📞 Reporting Security Issues

If you find a security vulnerability:
1. **DO NOT** create a public issue
2. Contact the maintainers privately
3. Provide detailed information about the vulnerability
4. Allow time for the issue to be addressed before disclosure