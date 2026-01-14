# Production Deployment Guide

[Versión en Español](PRODUCTION_DEPLOYMENT_GUIDE.es.md)

## Overview

This guide explains how to safely deploy the Personal Issue Tracker to production using Docker Compose.

## 🔒 Security First

### What's Safe in Git

✅ **These files ARE safe to commit:**
- `docker-compose.prod.yml` - Uses environment variables, no hardcoded secrets
- `.env.prod.example` - Template with placeholder values
- `backend/.env.prod.template` - Backend configuration template
- Configuration files using `${VARIABLE}` syntax

❌ **These files should NEVER be committed:**
- `.env.prod` - Contains real production secrets
- `docker-compose.override.yml` - May contain local secrets
- Any file with actual passwords, API keys, or tokens

### Why docker-compose.prod.yml is Safe

The `docker-compose.prod.yml` file uses environment variable substitution:

```yaml
environment:
  POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}  # ✅ Safe - reads from .env.prod
  JWT_SECRET: ${JWT_SECRET}                # ✅ Safe - reads from .env.prod
```

**NOT** hardcoded values like:
```yaml
environment:
  POSTGRES_PASSWORD: "MySecretPassword123"  # ❌ NEVER do this!
```

## 📋 Pre-Deployment Checklist

Before deploying to production:

- [ ] Server/VM is provisioned and accessible
- [ ] Docker and Docker Compose are installed
- [ ] Firewall rules are configured
- [ ] Domain name is configured (if applicable)
- [ ] SSL certificates are obtained (if using HTTPS)
- [ ] Backup strategy is in place

## 🚀 Deployment Steps

### 1. Clone the Repository

```bash
# On your production server
git clone <repository-url>
cd personal-issue-tracker
```

### 2. Create Production Environment File

```bash
# Copy the example file
cp .env.prod.example .env.prod

# Edit with your actual values
nano .env.prod  # or vim, vi, etc.
```

### 3. Generate Secure Secrets

```bash
# Generate a strong JWT secret
openssl rand -base64 64

# Generate strong passwords
openssl rand -base64 32
```

### 4. Configure Environment Variables

Edit `.env.prod` with your actual production values:

```bash
# Database
POSTGRES_PASSWORD=<your-strong-password>

# Redis
REDIS_PASSWORD=<your-strong-password>

# JWT (use the generated secret from step 3)
JWT_SECRET=<your-generated-jwt-secret>

# CORS (your actual domain)
CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com

# Frontend API URL (your actual domain)
VITE_API_BASE_URL=https://yourdomain.com/api
```

### 5. Build and Start Services

```bash
# Load environment variables and start services
docker-compose --env-file .env.prod -f docker-compose.prod.yml up -d

# Or if you want to build fresh images
docker-compose --env-file .env.prod -f docker-compose.prod.yml up -d --build
```

### 6. Verify Deployment

```bash
# Check service status
docker-compose -f docker-compose.prod.yml ps

# Check logs
docker-compose -f docker-compose.prod.yml logs -f

# Test health endpoints
curl http://localhost:8080/actuator/health
```

### 7. Configure Nginx (Optional)

If using the Nginx reverse proxy:

```bash
# Start with nginx profile
docker-compose --env-file .env.prod -f docker-compose.prod.yml --profile nginx up -d

# Place your SSL certificates in infrastructure/docker/ssl/
# Update infrastructure/docker/nginx-prod.conf with your domain
```

## 🔄 Updates and Maintenance

### Updating the Application

```bash
# Pull latest changes
git pull origin main

# Rebuild and restart services
docker-compose --env-file .env.prod -f docker-compose.prod.yml up -d --build

# Or restart specific service
docker-compose -f docker-compose.prod.yml restart backend
```

### Viewing Logs

```bash
# All services
docker-compose -f docker-compose.prod.yml logs -f

# Specific service
docker-compose -f docker-compose.prod.yml logs -f backend

# Last 100 lines
docker-compose -f docker-compose.prod.yml logs --tail=100 backend
```

### Database Backup

```bash
# Backup database
docker exec issue-tracker-postgres-prod pg_dump -U postgres issue_tracker > backup_$(date +%Y%m%d_%H%M%S).sql

# Restore database
docker exec -i issue-tracker-postgres-prod psql -U postgres issue_tracker < backup.sql
```

## 🛡️ Security Best Practices

### 1. Environment Variables

- ✅ Use `.env.prod` file (gitignored)
- ✅ Use strong, unique passwords for each service
- ✅ Rotate secrets regularly
- ✅ Never commit `.env.prod` to Git
- ✅ Use different secrets for dev/staging/production

### 2. Secret Management

For enhanced security, consider using:

- **Docker Secrets** (Docker Swarm)
- **AWS Secrets Manager**
- **HashiCorp Vault**
- **Azure Key Vault**
- **Google Cloud Secret Manager**

Example with Docker Secrets:

```bash
# Create secrets
echo "my-postgres-password" | docker secret create postgres_password -
echo "my-jwt-secret" | docker secret create jwt_secret -

# Reference in docker-compose.prod.yml (already configured)
```

### 3. Network Security

- ✅ Use firewall rules to restrict access
- ✅ Only expose necessary ports
- ✅ Use HTTPS/TLS for all external communication
- ✅ Keep Docker and system packages updated
- ✅ Use non-root users in containers (already configured)

### 4. Monitoring

```bash
# Check resource usage
docker stats

# Check health status
docker-compose -f docker-compose.prod.yml ps

# Monitor logs for errors
docker-compose -f docker-compose.prod.yml logs -f | grep ERROR
```

## 🔧 Troubleshooting

### Services Won't Start

```bash
# Check logs
docker-compose -f docker-compose.prod.yml logs

# Check if ports are already in use
netstat -tulpn | grep -E ':(80|443|5432|6379|8080)'

# Verify environment variables are loaded
docker-compose --env-file .env.prod -f docker-compose.prod.yml config
```

### Database Connection Issues

```bash
# Check if PostgreSQL is healthy
docker exec issue-tracker-postgres-prod pg_isready -U postgres

# Check database logs
docker-compose -f docker-compose.prod.yml logs postgres

# Verify credentials
docker exec -it issue-tracker-postgres-prod psql -U postgres -d issue_tracker
```

### Backend Not Responding

```bash
# Check backend logs
docker-compose -f docker-compose.prod.yml logs backend

# Check health endpoint
curl http://localhost:8080/actuator/health

# Restart backend
docker-compose -f docker-compose.prod.yml restart backend
```

## 📊 Performance Tuning

### Resource Limits

The `docker-compose.prod.yml` includes resource limits. Adjust based on your server:

```yaml
deploy:
  resources:
    limits:
      memory: 2G      # Adjust based on available RAM
      cpus: '1.0'     # Adjust based on available CPUs
```

### Database Optimization

Edit `infrastructure/docker/postgresql.conf` for production settings:

```conf
max_connections = 100
shared_buffers = 256MB
effective_cache_size = 1GB
work_mem = 4MB
```

## 🔄 Backup Strategy

### Automated Backups

Create a backup script:

```bash
#!/bin/bash
# backup.sh

BACKUP_DIR="/backups"
DATE=$(date +%Y%m%d_%H%M%S)

# Database backup
docker exec issue-tracker-postgres-prod pg_dump -U postgres issue_tracker > \
  $BACKUP_DIR/db_backup_$DATE.sql

# Compress
gzip $BACKUP_DIR/db_backup_$DATE.sql

# Keep only last 30 days
find $BACKUP_DIR -name "db_backup_*.sql.gz" -mtime +30 -delete
```

Add to crontab:

```bash
# Run daily at 2 AM
0 2 * * * /path/to/backup.sh
```

## 📚 Additional Resources

- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Docker Security Best Practices](https://docs.docker.com/engine/security/)
- [PostgreSQL Production Checklist](https://www.postgresql.org/docs/current/runtime-config.html)
- [Spring Boot Production Best Practices](https://docs.spring.io/spring-boot/docs/current/reference/html/deployment.html)

## 🆘 Support

If you encounter issues:

1. Check the logs: `docker-compose -f docker-compose.prod.yml logs`
2. Review this guide and the security checklist
3. Check the troubleshooting section
4. Open an issue in the repository

---

**Remember**: Never commit `.env.prod` or any file with real secrets to Git!

**Last Updated**: January 14, 2026
