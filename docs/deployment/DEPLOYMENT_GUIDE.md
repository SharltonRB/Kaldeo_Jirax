# Deployment Guide - Personal Issue Tracker

This guide covers the complete deployment process for the Personal Issue Tracker application, including CI/CD pipeline setup, manual deployment, and rollback procedures.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [CI/CD Pipeline Setup](#cicd-pipeline-setup)
3. [Manual Deployment](#manual-deployment)
4. [Database Migrations](#database-migrations)
5. [Rollback Procedures](#rollback-procedures)
6. [Monitoring and Health Checks](#monitoring-and-health-checks)
7. [Troubleshooting](#troubleshooting)

## Prerequisites

### Required Software

- Docker 24.0+ and Docker Compose 2.0+
- Git 2.30+
- Maven 3.9+ (for backend builds)
- Node.js 20+ and npm (for frontend builds)
- PostgreSQL 15+ (for database)

### Required Credentials

- Database credentials (username, password)
- JWT secret key (minimum 32 characters)
- Redis password (optional but recommended)
- SSL certificates (for HTTPS)
- Container registry credentials (for Docker images)

### Environment Setup

1. Clone the repository:
```bash
git clone <repository-url>
cd personal-issue-tracker
```

2. Create production environment file:
```bash
cp .env.prod.example .env.prod
```

3. Update `.env.prod` with your production values:
```bash
# Database Configuration
DATABASE_URL=jdbc:postgresql://postgres:5432/issue_tracker
DB_USERNAME=postgres
DB_PASSWORD=<your-secure-password>
POSTGRES_DB=issue_tracker
POSTGRES_USER=postgres
POSTGRES_PASSWORD=<your-secure-password>

# JWT Configuration
JWT_SECRET=<your-secure-jwt-secret-base64-encoded>
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000

# Application Configuration
CORS_ALLOWED_ORIGINS=https://yourdomain.com
BACKEND_PORT=8080
FRONTEND_PORT=80

# Redis Configuration (optional)
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=<your-redis-password>

# Monitoring
PROMETHEUS_ENABLED=true
LOG_LEVEL=INFO
```

## CI/CD Pipeline Setup

### GitHub Actions

The project includes a complete CI/CD pipeline using GitHub Actions (`.github/workflows/ci-cd.yml`).

#### Pipeline Stages

1. **Backend Build & Test**
   - Builds Java application with Maven
   - Runs unit tests and property-based tests
   - Uploads build artifacts

2. **Frontend Build & Test**
   - Builds React application with Vite
   - Runs linter and tests
   - Uploads build artifacts

3. **Security Scan**
   - Runs Trivy vulnerability scanner
   - Uploads results to GitHub Security

4. **Docker Build**
   - Builds and pushes Docker images to registry
   - Tags images with branch name and commit SHA

5. **Deploy to Staging** (develop branch)
   - Deploys to staging environment
   - Runs health checks

6. **Deploy to Production** (main branch)
   - Creates backup before deployment
   - Deploys to production environment
   - Runs smoke tests
   - Automatic rollback on failure

#### Required GitHub Secrets

Configure these secrets in your GitHub repository settings:

```
# Staging Environment
STAGING_HOST=staging.example.com
STAGING_USER=deploy
STAGING_SSH_KEY=<private-ssh-key>

# Production Environment
PROD_HOST=app.example.com
PROD_USER=deploy
PROD_SSH_KEY=<private-ssh-key>

# Container Registry
GITHUB_TOKEN=<automatically-provided>
```

#### Triggering Deployments

- **Automatic**: Push to `main` (production) or `develop` (staging)
- **Manual**: Use GitHub Actions "Run workflow" button

## Manual Deployment

### Full Production Deployment

Use the automated deployment script:

```bash
./scripts/deploy-production.sh
```

This script will:
1. Check prerequisites
2. Validate environment configuration
3. Create backup
4. Build application
5. Deploy with Docker Compose
6. Run health checks
7. Display deployment status

### Step-by-Step Manual Deployment

If you need more control, follow these steps:

#### 1. Build Backend

```bash
cd backend
./mvnw clean package -DskipTests
cd ..
```

#### 2. Build Frontend

```bash
cd frontend
npm ci
npm run build
cd ..
```

#### 3. Build Docker Images

```bash
docker-compose -f docker-compose.prod.yml build
```

#### 4. Start Services

```bash
docker-compose -f docker-compose.prod.yml --env-file .env.prod up -d
```

#### 5. Verify Deployment

```bash
# Check service status
docker-compose -f docker-compose.prod.yml ps

# Check backend health
curl http://localhost:8080/actuator/health

# View logs
docker-compose -f docker-compose.prod.yml logs -f
```

## Database Migrations

### Running Migrations

Use the migration script for safe database updates:

```bash
./scripts/migrate-database.sh migrate
```

This will:
1. Check database connectivity
2. Show current migration status
3. Validate migration scripts
4. Create backup
5. Run pending migrations
6. Show updated status

### Migration Commands

```bash
# Show migration status
./scripts/migrate-database.sh status

# Validate migrations
./scripts/migrate-database.sh validate

# Repair migration history
./scripts/migrate-database.sh repair

# Baseline existing database
./scripts/migrate-database.sh baseline
```

### Manual Migration

If you need to run migrations manually:

```bash
cd backend
./mvnw flyway:migrate \
  -Dflyway.url="jdbc:postgresql://localhost:5432/issue_tracker" \
  -Dflyway.user="postgres" \
  -Dflyway.password="your-password"
```

## Rollback Procedures

### Automated Rollback

Use the rollback script to restore from a previous backup:

```bash
./scripts/rollback.sh
```

This will:
1. List available backups
2. Let you select a backup to restore
3. Create pre-rollback backup
4. Stop application
5. Restore database
6. Restore Docker images (if available)
7. Start application
8. Verify rollback

### Manual Rollback

#### 1. List Available Backups

```bash
./scripts/rollback.sh list
```

#### 2. Stop Application

```bash
docker-compose -f docker-compose.prod.yml down
```

#### 3. Restore Database

```bash
# Start database only
docker-compose -f docker-compose.prod.yml up -d postgres

# Restore from backup
cat backups/backup_YYYYMMDD_HHMMSS_database.sql | \
  docker exec -i issue-tracker-postgres-prod \
  psql -U postgres -d issue_tracker
```

#### 4. Start Application

```bash
docker-compose -f docker-compose.prod.yml up -d
```

### Emergency Rollback

If the automated rollback fails:

1. **Stop all services**:
```bash
docker-compose -f docker-compose.prod.yml down
```

2. **Restore database volume**:
```bash
docker run --rm \
  -v personal-issue-tracker_postgres_prod_data:/data \
  -v $(pwd)/backups:/backup \
  alpine tar xzf /backup/backup_YYYYMMDD_HHMMSS_postgres_volume.tar.gz -C /data
```

3. **Start services**:
```bash
docker-compose -f docker-compose.prod.yml up -d
```

## Monitoring and Health Checks

### Health Check Endpoints

- **Backend Health**: `http://localhost:8080/actuator/health`
- **Backend Metrics**: `http://localhost:8080/actuator/metrics`
- **Database Health**: Check via Docker health status

### Checking Service Status

```bash
# All services
docker-compose -f docker-compose.prod.yml ps

# Backend health
curl http://localhost:8080/actuator/health | jq

# Database connectivity
docker exec issue-tracker-postgres-prod pg_isready -U postgres

# Redis connectivity
docker exec issue-tracker-redis-prod redis-cli ping
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

### Monitoring Metrics

Access Prometheus metrics:
```bash
curl http://localhost:8080/actuator/prometheus
```

Key metrics to monitor:
- `http_server_requests_seconds`: Request latency
- `jvm_memory_used_bytes`: Memory usage
- `hikari_connections_active`: Database connections
- `system_cpu_usage`: CPU usage

## Troubleshooting

### Common Issues

#### 1. Database Connection Failed

**Symptoms**: Backend fails to start, connection errors in logs

**Solutions**:
```bash
# Check database is running
docker-compose -f docker-compose.prod.yml ps postgres

# Check database logs
docker-compose -f docker-compose.prod.yml logs postgres

# Verify credentials
docker exec issue-tracker-postgres-prod \
  psql -U postgres -d issue_tracker -c "SELECT 1"

# Restart database
docker-compose -f docker-compose.prod.yml restart postgres
```

#### 2. Migration Failed

**Symptoms**: Flyway migration errors, schema version conflicts

**Solutions**:
```bash
# Check migration status
./scripts/migrate-database.sh status

# Repair migration history
./scripts/migrate-database.sh repair

# If needed, restore from backup
./scripts/rollback.sh
```

#### 3. Out of Memory

**Symptoms**: Backend crashes, OOM errors in logs

**Solutions**:
```bash
# Check memory usage
docker stats

# Increase memory limits in docker-compose.prod.yml
# Under backend service:
deploy:
  resources:
    limits:
      memory: 4G  # Increase from 2G

# Restart with new limits
docker-compose -f docker-compose.prod.yml up -d backend
```

#### 4. Slow Performance

**Symptoms**: High response times, timeouts

**Solutions**:
```bash
# Check database connections
docker exec issue-tracker-postgres-prod \
  psql -U postgres -d issue_tracker \
  -c "SELECT count(*) FROM pg_stat_activity"

# Check slow queries
docker exec issue-tracker-postgres-prod \
  psql -U postgres -d issue_tracker \
  -c "SELECT query, calls, total_time FROM pg_stat_statements ORDER BY total_time DESC LIMIT 10"

# Increase connection pool size
# In .env.prod:
DB_POOL_SIZE=40  # Increase from 20

# Restart backend
docker-compose -f docker-compose.prod.yml restart backend
```

#### 5. SSL Certificate Issues

**Symptoms**: HTTPS not working, certificate errors

**Solutions**:
```bash
# Check certificate files
ls -la infrastructure/docker/ssl/

# Verify certificate validity
openssl x509 -in infrastructure/docker/ssl/cert.pem -text -noout

# Regenerate certificates
./scripts/generate-ssl-certs.sh

# Restart nginx
docker-compose -f docker-compose.prod.yml restart nginx
```

### Getting Help

1. Check application logs:
```bash
docker-compose -f docker-compose.prod.yml logs -f
```

2. Check deployment logs:
```bash
cat logs/deployment.log
```

3. Run health checks:
```bash
./scripts/deploy-production.sh health
```

4. Check system resources:
```bash
docker stats
df -h
free -h
```

## Best Practices

### Before Deployment

1. ✅ Test in staging environment
2. ✅ Review all pending changes
3. ✅ Backup current production data
4. ✅ Verify all environment variables
5. ✅ Check disk space and resources
6. ✅ Notify team of deployment window

### During Deployment

1. ✅ Monitor logs in real-time
2. ✅ Watch health check endpoints
3. ✅ Verify database migrations
4. ✅ Test critical user flows
5. ✅ Check performance metrics

### After Deployment

1. ✅ Verify all services are healthy
2. ✅ Run smoke tests
3. ✅ Monitor error rates
4. ✅ Check application logs
5. ✅ Verify backup was created
6. ✅ Document any issues
7. ✅ Notify team of completion

### Security Checklist

- [ ] All default passwords changed
- [ ] JWT secret is strong and unique
- [ ] SSL/TLS certificates are valid
- [ ] Database is not publicly accessible
- [ ] Rate limiting is enabled
- [ ] CORS is properly configured
- [ ] Security headers are set
- [ ] Logs don't contain sensitive data
- [ ] Backups are encrypted
- [ ] Access logs are monitored

## Additional Resources

- [Production Deployment Guide](PRODUCTION_DEPLOYMENT_GUIDE.md)
- [Security Checklist](SECURITY_CHECKLIST.md)
- [Flyway Troubleshooting](FLYWAY_TROUBLESHOOTING.md)
- [Testing Strategy](testing/TESTING_STRATEGY.md)
