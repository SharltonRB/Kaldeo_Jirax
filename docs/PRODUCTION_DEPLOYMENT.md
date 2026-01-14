# Production Deployment Guide

This guide covers the complete production deployment process for the Personal Issue Tracker application.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Environment Setup](#environment-setup)
3. [SSL/TLS Configuration](#ssltls-configuration)
4. [Database Setup](#database-setup)
5. [Backend Deployment](#backend-deployment)
6. [Frontend Deployment](#frontend-deployment)
7. [Monitoring Setup](#monitoring-setup)
8. [Security Hardening](#security-hardening)
9. [Performance Optimization](#performance-optimization)
10. [Troubleshooting](#troubleshooting)

## Prerequisites

### System Requirements

- **Operating System**: Linux (Ubuntu 20.04+ recommended) or macOS
- **Docker**: Version 20.10+
- **Docker Compose**: Version 2.0+
- **Memory**: Minimum 4GB RAM (8GB recommended)
- **Storage**: Minimum 20GB free space
- **Network**: Stable internet connection for downloads

### Required Tools

```bash
# Install Docker (Ubuntu)
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Verify installations
docker --version
docker-compose --version
```

## Environment Setup

### 1. Clone and Prepare Repository

```bash
git clone <repository-url>
cd personal-issue-tracker
```

### 2. Create Production Environment File

```bash
# Copy template and customize
cp backend/.env.prod.template .env.prod

# Edit with your production values
nano .env.prod
```

### 3. Generate Secure Secrets

```bash
# Generate JWT secret (minimum 256 bits)
openssl rand -base64 64

# Generate database passwords
openssl rand -base64 32

# Generate Redis password
openssl rand -base64 24
```

### 4. Update Environment Variables

Edit `.env.prod` with your secure values:

```bash
# Database Configuration
DATABASE_URL=jdbc:postgresql://postgres:5432/issue_tracker_prod
DB_USERNAME=issue_tracker_user
DB_PASSWORD=your_secure_db_password_here
POSTGRES_PASSWORD=your_secure_postgres_password_here

# JWT Configuration
JWT_SECRET=your_base64_encoded_jwt_secret_here

# Redis Configuration
REDIS_PASSWORD=your_secure_redis_password_here

# Domain Configuration
CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com
VITE_API_BASE_URL=https://yourdomain.com/api
```

## SSL/TLS Configuration

### Option 1: Self-Signed Certificates (Development/Testing)

```bash
# Generate self-signed certificates
./scripts/generate-ssl-certs.sh

# Update environment variables
SSL_ENABLED=true
SSL_KEY_STORE=/app/ssl/keystore.p12
SSL_KEY_STORE_PASSWORD=changeit
```

### Option 2: Let's Encrypt (Production)

```bash
# Install Certbot
sudo apt-get update
sudo apt-get install certbot

# Generate certificates
sudo certbot certonly --standalone -d yourdomain.com -d www.yourdomain.com

# Convert to PKCS12 format
sudo openssl pkcs12 -export \
  -in /etc/letsencrypt/live/yourdomain.com/fullchain.pem \
  -inkey /etc/letsencrypt/live/yourdomain.com/privkey.pem \
  -out /path/to/ssl/keystore.p12 \
  -name issuetracker \
  -passout pass:your_keystore_password
```

### Option 3: Custom CA Certificates

```bash
# Copy your certificates to infrastructure/docker/ssl/
cp your-cert.pem infrastructure/docker/ssl/cert.pem
cp your-key.pem infrastructure/docker/ssl/key.pem

# Create PKCS12 keystore
openssl pkcs12 -export \
  -in infrastructure/docker/ssl/cert.pem \
  -inkey infrastructure/docker/ssl/key.pem \
  -out infrastructure/docker/ssl/keystore.p12 \
  -name issuetracker
```

## Database Setup

### 1. Production Database Configuration

The application uses PostgreSQL with optimized settings for production:

```yaml
# PostgreSQL configuration is in infrastructure/docker/postgresql.conf
shared_buffers = 256MB
effective_cache_size = 1GB
work_mem = 4MB
maintenance_work_mem = 64MB
```

### 2. Database Backup Strategy

```bash
# Create backup directory
mkdir -p backups

# Manual backup
docker exec issue-tracker-postgres-prod pg_dump -U issue_tracker_user issue_tracker_prod > backups/backup_$(date +%Y%m%d_%H%M%S).sql

# Automated backup (add to crontab)
0 2 * * * /path/to/project/scripts/backup-database.sh
```

## Backend Deployment

### 1. Build and Deploy

```bash
# Full production deployment
./scripts/deploy-production.sh

# Or step by step:
# 1. Build backend
cd backend
./mvnw clean package -DskipTests -Pprod

# 2. Build Docker images
docker-compose -f docker-compose.prod.yml build

# 3. Start services
docker-compose -f docker-compose.prod.yml --env-file .env.prod up -d
```

### 2. Verify Backend Deployment

```bash
# Check service health
curl http://localhost:8080/actuator/health

# Check application logs
docker-compose -f docker-compose.prod.yml logs -f backend

# Check metrics
curl http://localhost:8080/actuator/metrics
```

## Frontend Deployment

### Option 1: Docker Deployment (Recommended)

```bash
# Build optimized frontend
./scripts/build-frontend.sh

# Frontend is included in docker-compose.prod.yml
# Access at http://localhost (port 80) or https://localhost (port 443)
```

### Option 2: CDN Deployment (AWS S3 + CloudFront)

```bash
# Set up AWS credentials
aws configure

# Set environment variables
export S3_BUCKET=your-frontend-bucket
export CLOUDFRONT_DISTRIBUTION_ID=your-distribution-id

# Deploy to S3 and invalidate CloudFront
./scripts/deploy-to-s3.sh
```

### Option 3: Static File Server

```bash
# Build frontend
cd frontend
npm run build:prod

# Serve with nginx, Apache, or any static file server
# Point document root to frontend/dist/
```

## Monitoring Setup

### 1. Enable Monitoring Stack

```bash
# Start with monitoring profile
docker-compose -f docker-compose.prod.yml -f infrastructure/monitoring/docker-compose.monitoring.yml --profile monitoring up -d

# Access monitoring services:
# Prometheus: http://localhost:9090
# Grafana: http://localhost:3001 (admin/admin)
# Alertmanager: http://localhost:9093
```

### 2. Configure Alerting

Edit `infrastructure/monitoring/alertmanager.yml`:

```yaml
global:
  smtp_smarthost: 'your-smtp-server:587'
  smtp_from: 'alerts@yourdomain.com'
  smtp_auth_username: 'your-smtp-username'
  smtp_auth_password: 'your-smtp-password'

receivers:
  - name: 'critical-alerts'
    email_configs:
      - to: 'admin@yourdomain.com'
        subject: '🚨 CRITICAL: {{ .GroupLabels.alertname }}'
```

### 3. Set Up Log Aggregation

```bash
# Logs are automatically structured in production
# View logs:
docker-compose -f docker-compose.prod.yml logs -f

# For centralized logging, consider:
# - ELK Stack (Elasticsearch, Logstash, Kibana)
# - Fluentd + Elasticsearch
# - AWS CloudWatch Logs
# - Google Cloud Logging
```

## Security Hardening

### 1. Network Security

```bash
# Configure firewall (Ubuntu)
sudo ufw enable
sudo ufw allow 22/tcp    # SSH
sudo ufw allow 80/tcp    # HTTP
sudo ufw allow 443/tcp   # HTTPS
sudo ufw deny 8080/tcp   # Block direct backend access
```

### 2. Container Security

```bash
# Run security scan
docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \
  aquasec/trivy image personal-issue-tracker_backend:latest

# Update base images regularly
docker-compose -f docker-compose.prod.yml pull
docker-compose -f docker-compose.prod.yml up -d
```

### 3. Application Security

- **Rate Limiting**: Configured in application-prod.yml
- **CORS**: Configured for your domain only
- **Security Headers**: Implemented in Nginx configuration
- **Input Validation**: Jakarta Bean Validation on all endpoints
- **SQL Injection Prevention**: Parameterized queries with JPA

## Performance Optimization

### 1. Database Optimization

```bash
# Monitor database performance
docker exec -it issue-tracker-postgres-prod psql -U issue_tracker_user -d issue_tracker_prod

# Check slow queries
SELECT query, mean_time, calls FROM pg_stat_statements ORDER BY mean_time DESC LIMIT 10;

# Analyze table statistics
ANALYZE;
```

### 2. Application Performance

```bash
# Monitor JVM metrics
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# Monitor HTTP metrics
curl http://localhost:8080/actuator/metrics/http.server.requests

# Check connection pool
curl http://localhost:8080/actuator/metrics/hikaricp.connections.active
```

### 3. Frontend Performance

```bash
# Analyze bundle size
cd frontend
npm run build:analyze

# Test performance
npm install -g lighthouse
lighthouse http://localhost --output html --output-path ./lighthouse-report.html
```

## Troubleshooting

### Common Issues

#### 1. Application Won't Start

```bash
# Check logs
docker-compose -f docker-compose.prod.yml logs backend

# Common causes:
# - Database connection issues
# - Invalid JWT secret
# - Port conflicts
# - Insufficient memory
```

#### 2. Database Connection Issues

```bash
# Test database connectivity
docker exec -it issue-tracker-postgres-prod pg_isready -U issue_tracker_user

# Check database logs
docker-compose -f docker-compose.prod.yml logs postgres

# Verify environment variables
docker exec issue-tracker-backend-prod env | grep DB_
```

#### 3. SSL/TLS Issues

```bash
# Test SSL configuration
openssl s_client -connect localhost:8080 -servername localhost

# Check certificate validity
openssl x509 -in infrastructure/docker/ssl/cert.pem -text -noout

# Verify keystore
keytool -list -keystore infrastructure/docker/ssl/keystore.p12 -storepass changeit
```

#### 4. Performance Issues

```bash
# Check resource usage
docker stats

# Monitor application metrics
curl http://localhost:8080/actuator/metrics/system.cpu.usage

# Check database performance
docker exec -it issue-tracker-postgres-prod psql -U issue_tracker_user -d issue_tracker_prod -c "SELECT * FROM pg_stat_activity;"
```

### Health Checks

```bash
# Application health
curl http://localhost:8080/actuator/health

# Database health
docker exec issue-tracker-postgres-prod pg_isready

# Redis health
docker exec issue-tracker-redis-prod redis-cli ping

# Frontend health
curl http://localhost/health
```

### Backup and Recovery

```bash
# Create full backup
./scripts/deploy-production.sh backup

# Restore from backup
docker exec -i issue-tracker-postgres-prod psql -U issue_tracker_user -d issue_tracker_prod < backups/backup_20240115_120000.sql

# Test restore
docker-compose -f docker-compose.prod.yml down
docker volume rm personal-issue-tracker_postgres_prod_data
docker-compose -f docker-compose.prod.yml up -d postgres
# Wait for postgres to be ready, then restore
```

## Maintenance

### Regular Tasks

1. **Update Dependencies**: Monthly security updates
2. **Certificate Renewal**: Every 90 days for Let's Encrypt
3. **Database Maintenance**: Weekly VACUUM and ANALYZE
4. **Log Rotation**: Automated via logback configuration
5. **Backup Verification**: Weekly restore tests
6. **Security Scans**: Monthly container and dependency scans

### Monitoring Checklist

- [ ] Application health endpoints responding
- [ ] Database connections within limits
- [ ] Memory usage under 80%
- [ ] Disk space above 15% free
- [ ] SSL certificates valid for >30 days
- [ ] Backup processes completing successfully
- [ ] No critical alerts in monitoring system

## Support

For additional support:

1. Check application logs: `docker-compose -f docker-compose.prod.yml logs`
2. Review monitoring dashboards: Grafana at http://localhost:3001
3. Consult the troubleshooting section above
4. Check GitHub issues for known problems

---

**Security Note**: Always keep your production environment updated with the latest security patches and follow security best practices for your specific deployment environment.