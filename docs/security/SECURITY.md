# Security Policy

## 🔒 Security Overview

This document outlines the security practices and policies for the Personal Issue Tracker project.

## 🚨 Critical Security Rules

### ❌ NEVER Commit These Files
- `.env` files with real credentials
- Private keys (`.key`, `.pem`, `.p12`)
- Certificates and keystores
- Database connection strings with passwords
- API keys or authentication tokens
- Personal configuration files

### ✅ ALWAYS Use These Practices
- Environment variables for all sensitive configuration
- Strong, unique JWT secrets (minimum 256 bits)
- `.env.example` files as templates
- Regular security audits
- Proper input validation

## 🛡️ Security Checklist

### Before Committing Code
- [ ] Run security audit: `./scripts/security-audit.sh scan`
- [ ] Verify no sensitive data in files
- [ ] Check .gitignore compliance
- [ ] Use environment variables for secrets
- [ ] Update .env.example if needed

### Environment Configuration
- [ ] Use strong JWT secrets in production
- [ ] Configure proper database credentials
- [ ] Set up HTTPS in production
- [ ] Enable proper CORS configuration
- [ ] Configure rate limiting

### Code Security
- [ ] Validate all user inputs
- [ ] Use parameterized queries
- [ ] Implement proper authentication
- [ ] Add authorization checks
- [ ] Handle errors securely

## 🔧 Security Tools

### Automated Security Audit
```bash
# Run complete security scan
./scripts/security-audit.sh scan

# Apply automatic fixes
./scripts/security-audit.sh fix

# Generate security report
./scripts/security-audit.sh report
```

### Manual Security Checks
```bash
# Check for sensitive files
git status --ignored

# Verify .gitignore patterns
./scripts/validate-gitignore.sh check

# Search for potential secrets
grep -r -i "password\|secret\|key" . --exclude-dir=node_modules
```

## 🚨 Security Incident Response

### If Sensitive Data is Committed
1. **Immediately** remove the sensitive data
2. Change all exposed credentials
3. Force push to rewrite history (if possible)
4. Notify team members
5. Update security practices

### Commands to Remove Sensitive Data
```bash
# Remove file from git history
git filter-branch --force --index-filter \
  'git rm --cached --ignore-unmatch path/to/sensitive/file' \
  --prune-empty --tag-name-filter cat -- --all

# Alternative: Use BFG Repo-Cleaner
java -jar bfg.jar --delete-files sensitive-file.txt
```

## 🔐 Environment Variables Guide

### Required Environment Variables

#### Backend (.env)
```bash
# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/issue_tracker_prod
DB_USERNAME=your_username
DB_PASSWORD=your_secure_password

# JWT - Use strong, unique secrets
JWT_SECRET=your-256-bit-secret-key
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password
```

#### Frontend (.env)
```bash
# API Configuration
VITE_API_BASE_URL=https://your-api-domain.com

# Environment
VITE_NODE_ENV=production
```

### Generating Secure Secrets
```bash
# Generate JWT secret (256 bits)
openssl rand -base64 32

# Generate strong password
openssl rand -base64 24

# Generate UUID
uuidgen
```

## 🛠️ Security Configuration

### JWT Security
- Use minimum 256-bit secrets
- Set appropriate expiration times
- Implement token refresh mechanism
- Store tokens securely in frontend

### Database Security
- Use connection pooling
- Implement proper timeouts
- Use parameterized queries
- Regular security updates

### API Security
- Implement rate limiting
- Use HTTPS in production
- Validate all inputs
- Implement proper CORS
- Add security headers

### Infrastructure Security
- Use Docker secrets in production
- Implement network segmentation
- Regular security updates
- Monitor for vulnerabilities

## 📊 Security Monitoring

### Automated Checks
- Pre-commit hooks for sensitive data
- Regular dependency vulnerability scans
- Automated security testing in CI/CD
- Log monitoring for security events

### Manual Reviews
- Monthly security audits
- Code review for security issues
- Configuration review
- Access control review

## 📚 Security Resources

### Internal Documentation
- [Security Guidelines](security-guidelines.md)
- [.gitignore Guide](../development/gitignore-guide.md)
- [Environment Configuration](../deployment/)

### External Resources
- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [JWT Best Practices](https://tools.ietf.org/html/rfc8725)

## 🆘 Contact

For security concerns or questions:
1. Run the security audit script
2. Check this documentation
3. Review the security guidelines
4. Consult with the team lead

## 📝 Security Updates

This security policy should be reviewed and updated:
- After any security incident
- When adding new technologies
- At least quarterly
- When security best practices change

---

**¡Recuerda: La seguridad es responsabilidad de todos!**

## Versiones de Idioma

- **English**: [SECURITY.en.md](SECURITY.en.md)
- **Español**: [SECURITY.md](SECURITY.md)