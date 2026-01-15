# Maintenance and Troubleshooting Guide

## Overview

This guide provides comprehensive information for maintaining and troubleshooting the Personal Issue Tracker application in production.

## Table of Contents

1. [Regular Maintenance Tasks](#regular-maintenance-tasks)
2. [Monitoring and Alerts](#monitoring-and-alerts)
3. [Common Issues and Solutions](#common-issues-and-solutions)
4. [Database Maintenance](#database-maintenance)
5. [Performance Optimization](#performance-optimization)
6. [Security Maintenance](#security-maintenance)
7. [Backup and Recovery](#backup-and-recovery)
8. [Troubleshooting Procedures](#troubleshooting-procedures)
9. [Emergency Procedures](#emergency-procedures)
10. [Known Limitations](#known-limitations)

## Regular Maintenance Tasks

### Daily Tasks

#### 1. Health Check Monitoring

```bash
# Run automated health check
./scripts/health-check.sh

# Check application logs for errors
tail -f backend/logs/application.log | grep ERROR

# Monitor system resources
docker stats
```

#### 2. Review Error Logs

```bash
# Check for authentication failures
grep "AUTH_FAILURE" backend/logs/security.log

# Check for database errors
grep "DATABASE_ERROR" backend/logs/application.log

# Check for performance issues
grep "Slow.*detected" backend/logs/performance.log
```

### Weekly Tasks

#### 1. Database Maintenance

```bash
# Run VACUUM to reclaim storage
docker exec issue-tracker-postgres-prod psql -U postgres -d issue_tracker -c "VACUUM ANALYZE;"

# Check database size
docker exec issue-tracker-postgres-prod psql -U postgres -d issue_tracker -c "SELECT pg_size_pretty(pg_database_size('issue_tracker'));"

# Review slow queries
docker exec issue-tracker-postgres-prod psql -U postgres -d issue_tracker -c "SELECT query, mean_time, calls FROM pg_stat_statements ORDER BY mean_time DESC LIMIT 10;"
```

#### 2. Log Rotation Verification

```bash
# Check log file sizes
du -h backend/logs/*.log

# Verify log rotation is working
ls -lh backend/logs/*.gz

# Clean up old logs (older than 30 days)
find backend/logs -name "*.gz" -mtime +30 -delete
```

#### 3. Security Review

```bash
# Check for failed login attempts
grep "Failed login" backend/logs/security.log | wc -l

# Review rate limiting effectiveness
curl http://localhost:8080/monitoring/errors | jq '.AUTH_FAILURE'

# Check for suspicious activity
grep "SUSPICIOUS" backend/logs/security.log
```

### Monthly Tasks

#### 1. Dependency Updates

```bash
# Check for backend dependency updates
cd backend
./mvnw versions:display-dependency-updates

# Check for frontend dependency updates
cd frontend
npm outdated

# Update dependencies (test in staging first!)
./mvnw versions:use-latest-releases
npm update
```

#### 2. Security Scanning

```bash
# Scan Docker images for vulnerabilities
docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \
  aquasec/trivy image personal-issue-tracker_backend:latest

# Scan dependencies
./mvnw dependency-check:check
npm audit
```

#### 3. Performance Review

```bash
# Review performance metrics
curl http://localhost:8080/monitoring/metrics | jq

# Check database performance
docker exec issue-tracker-postgres-prod psql -U postgres -d issue_tracker \
  -c "SELECT schemaname, tablename, pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size FROM pg_tables ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC LIMIT 10;"

# Review cache hit rates
curl http://localhost:8080/actuator/metrics/cache.gets | jq
```

#### 4. Backup Verification

```bash
# List recent backups
ls -lh backups/

# Test backup restoration (in test environment)
./scripts/rollback.sh list
./scripts/rollback.sh test-restore
```

### Quarterly Tasks

#### 1. SSL Certificate Renewal

```bash
# Check certificate expiration
openssl x509 -in infrastructure/docker/ssl/cert.pem -noout -enddate

# Renew Let's Encrypt certificates (if applicable)
sudo certbot renew

# Update application with new certificates
./scripts/deploy-production.sh
```

#### 2. Capacity Planning

```bash
# Review database growth
docker exec issue-tracker-postgres-prod psql -U postgres -d issue_tracker \
  -c "SELECT schemaname, tablename, n_live_tup FROM pg_stat_user_tables ORDER BY n_live_tup DESC;"

# Review disk usage trends
df -h

# Review memory usage trends
free -h
docker stats --no-stream
```

#### 3. Disaster Recovery Testing

```bash
# Test full backup and restore procedure
./scripts/deploy-production.sh backup
./scripts/rollback.sh

# Verify data integrity after restore
./scripts/production-validation.sh
```

## Monitoring and Alerts

### Key Metrics to Monitor

#### Application Metrics

- **Response Time**: Should be < 500ms for 95th percentile
- **Error Rate**: Should be < 1% of total requests
- **Memory Usage**: Should be < 80% of allocated memory
- **CPU Usage**: Should be < 70% average
- **Active Sessions**: Monitor for unusual spikes

#### Database Metrics

- **Connection Pool**: Active connections should be < 80% of pool size
- **Query Performance**: Slow queries (> 1000ms) should be investigated
- **Database Size**: Monitor growth rate
- **Replication Lag**: Should be < 1 second (if using replication)

#### Infrastructure Metrics

- **Disk Space**: Should have > 15% free space
- **Network Latency**: Should be < 100ms
- **Container Health**: All containers should be "healthy"
- **Backup Success**: Daily backups should complete successfully

### Setting Up Alerts

#### Prometheus Alert Rules

Edit `infrastructure/monitoring/alert_rules.yml`:

```yaml
groups:
  - name: critical_alerts
    interval: 30s
    rules:
      - alert: HighErrorRate
        expr: rate(error_count_total[5m]) > 0.1
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "High error rate detected"
          
      - alert: HighMemoryUsage
        expr: jvm_memory_used_bytes / jvm_memory_max_bytes > 0.9
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "Memory usage above 90%"
          
      - alert: DatabaseDown
        expr: up{job="postgres"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Database is down"
```

## Common Issues and Solutions

### Issue 1: Application Won't Start

**Symptoms**:
- Container exits immediately
- "Connection refused" errors
- Application logs show startup errors

**Diagnosis**:
```bash
# Check container status
docker-compose -f docker-compose.prod.yml ps

# Check logs
docker-compose -f docker-compose.prod.yml logs backend

# Check database connectivity
docker exec issue-tracker-postgres-prod pg_isready
```

**Solutions**:

1. **Database not ready**:
```bash
# Wait for database to be ready
docker-compose -f docker-compose.prod.yml up -d postgres
sleep 10
docker-compose -f docker-compose.prod.yml up -d backend
```

2. **Invalid configuration**:
```bash
# Verify environment variables
docker exec issue-tracker-backend-prod env | grep -E "DB_|JWT_"

# Check application.yml
docker exec issue-tracker-backend-prod cat /app/config/application-prod.yml
```

3. **Port conflict**:
```bash
# Check if port is in use
lsof -i :8080

# Kill process using port
kill -9 <PID>
```

### Issue 2: High Memory Usage

**Symptoms**:
- Memory usage > 90%
- OutOfMemoryError in logs
- Application becomes unresponsive

**Diagnosis**:
```bash
# Check memory usage
curl http://localhost:8080/monitoring/metrics | jq '.memory'

# Check for memory leaks
docker exec issue-tracker-backend-prod jmap -heap <PID>
```

**Solutions**:

1. **Increase memory allocation**:
```yaml
# In docker-compose.prod.yml
services:
  backend:
    deploy:
      resources:
        limits:
          memory: 4G  # Increase from 2G
```

2. **Trigger garbage collection**:
```bash
curl -X POST http://localhost:8080/actuator/gc
```

3. **Analyze heap dump**:
```bash
# Generate heap dump
docker exec issue-tracker-backend-prod jmap -dump:live,format=b,file=/tmp/heap.bin <PID>

# Copy heap dump
docker cp issue-tracker-backend-prod:/tmp/heap.bin ./heap.bin

# Analyze with jhat or VisualVM
jhat heap.bin
```

### Issue 3: Slow Performance

**Symptoms**:
- Response times > 2 seconds
- Timeouts
- Users complaining about slowness

**Diagnosis**:
```bash
# Check response times
curl -w "@curl-format.txt" -o /dev/null -s http://localhost:8080/actuator/health

# Check database performance
docker exec issue-tracker-postgres-prod psql -U postgres -d issue_tracker \
  -c "SELECT query, calls, total_time, mean_time FROM pg_stat_statements ORDER BY mean_time DESC LIMIT 10;"

# Check connection pool
curl http://localhost:8080/actuator/metrics/hikaricp.connections.active
```

**Solutions**:

1. **Optimize database queries**:
```sql
-- Add missing indexes
CREATE INDEX idx_issues_user_project ON issues(user_id, project_id);
CREATE INDEX idx_issues_status ON issues(status);

-- Update statistics
ANALYZE;
```

2. **Increase connection pool**:
```yaml
# In application-prod.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 40  # Increase from 20
```

3. **Enable caching**:
```yaml
# In application-prod.yml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 600000  # 10 minutes
```

### Issue 4: Database Connection Errors

**Symptoms**:
- "Connection refused" errors
- "Too many connections" errors
- Application can't connect to database

**Diagnosis**:
```bash
# Check database status
docker exec issue-tracker-postgres-prod pg_isready

# Check active connections
docker exec issue-tracker-postgres-prod psql -U postgres -d issue_tracker \
  -c "SELECT count(*) FROM pg_stat_activity;"

# Check max connections
docker exec issue-tracker-postgres-prod psql -U postgres -d issue_tracker \
  -c "SHOW max_connections;"
```

**Solutions**:

1. **Restart database**:
```bash
docker-compose -f docker-compose.prod.yml restart postgres
```

2. **Increase max connections**:
```conf
# In infrastructure/docker/postgresql.conf
max_connections = 200  # Increase from 100
```

3. **Kill idle connections**:
```sql
SELECT pg_terminate_backend(pid) 
FROM pg_stat_activity 
WHERE state = 'idle' 
AND state_change < current_timestamp - INTERVAL '10 minutes';
```

### Issue 5: Authentication Failures

**Symptoms**:
- Users can't log in
- "Invalid credentials" errors
- JWT token errors

**Diagnosis**:
```bash
# Check authentication logs
grep "AUTH_FAILURE" backend/logs/security.log

# Verify JWT configuration
docker exec issue-tracker-backend-prod env | grep JWT_

# Test authentication endpoint
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'
```

**Solutions**:

1. **Verify JWT secret**:
```bash
# Ensure JWT_SECRET is set and valid
echo $JWT_SECRET | base64 -d | wc -c  # Should be >= 32 bytes
```

2. **Check password hashes**:
```bash
# Verify password hashes in database
docker exec issue-tracker-postgres-prod psql -U postgres -d issue_tracker \
  -c "SELECT email, substring(password_hash, 1, 10) FROM users LIMIT 5;"
```

3. **Reset user password**:
```bash
# Generate new password hash
python3 backend/generate_bcrypt_hash.py

# Update in database
docker exec issue-tracker-postgres-prod psql -U postgres -d issue_tracker \
  -c "UPDATE users SET password_hash = '<new_hash>' WHERE email = 'user@example.com';"
```

## Database Maintenance

### Regular Maintenance

```bash
# VACUUM to reclaim storage
docker exec issue-tracker-postgres-prod psql -U postgres -d issue_tracker -c "VACUUM FULL ANALYZE;"

# Reindex to improve performance
docker exec issue-tracker-postgres-prod psql -U postgres -d issue_tracker -c "REINDEX DATABASE issue_tracker;"

# Update statistics
docker exec issue-tracker-postgres-prod psql -U postgres -d issue_tracker -c "ANALYZE;"
```

### Monitoring Database Health

```sql
-- Check table sizes
SELECT schemaname, tablename, 
       pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_tables 
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC 
LIMIT 10;

-- Check index usage
SELECT schemaname, tablename, indexname, idx_scan, idx_tup_read, idx_tup_fetch
FROM pg_stat_user_indexes
ORDER BY idx_scan DESC;

-- Check for bloat
SELECT schemaname, tablename, 
       pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS total_size,
       pg_size_pretty(pg_relation_size(schemaname||'.'||tablename)) AS table_size,
       pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename) - pg_relation_size(schemaname||'.'||tablename)) AS index_size
FROM pg_tables
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC
LIMIT 10;
```

## Performance Optimization

### Application-Level Optimization

1. **Enable caching**:
```yaml
spring:
  cache:
    type: redis
    cache-names: projects, issues, sprints
```

2. **Optimize queries**:
```java
// Use pagination
@Query("SELECT i FROM Issue i WHERE i.user.id = :userId")
Page<Issue> findByUserId(@Param("userId") Long userId, Pageable pageable);

// Use fetch joins to avoid N+1 queries
@Query("SELECT i FROM Issue i LEFT JOIN FETCH i.labels WHERE i.id = :id")
Optional<Issue> findByIdWithLabels(@Param("id") Long id);
```

3. **Configure connection pool**:
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 40
      minimum-idle: 10
      connection-timeout: 20000
      idle-timeout: 300000
```

### Database-Level Optimization

1. **Add indexes**:
```sql
-- Composite indexes for common queries
CREATE INDEX idx_issues_user_project_status ON issues(user_id, project_id, status);
CREATE INDEX idx_issues_sprint ON issues(sprint_id) WHERE sprint_id IS NOT NULL;
CREATE INDEX idx_audit_logs_issue_created ON audit_logs(issue_id, created_at DESC);
```

2. **Optimize PostgreSQL configuration**:
```conf
# In postgresql.conf
shared_buffers = 256MB
effective_cache_size = 1GB
work_mem = 4MB
maintenance_work_mem = 64MB
random_page_cost = 1.1
effective_io_concurrency = 200
```

## Security Maintenance

### Regular Security Tasks

1. **Update dependencies**:
```bash
# Check for security vulnerabilities
./mvnw dependency-check:check
npm audit

# Update dependencies
./mvnw versions:use-latest-releases
npm update
```

2. **Review access logs**:
```bash
# Check for suspicious activity
grep "401\|403" backend/logs/application.log

# Check for brute force attempts
grep "AUTH_FAILURE" backend/logs/security.log | awk '{print $NF}' | sort | uniq -c | sort -rn
```

3. **Rotate secrets**:
```bash
# Generate new JWT secret
openssl rand -base64 64

# Update in environment
# Restart application
docker-compose -f docker-compose.prod.yml restart backend
```

## Backup and Recovery

### Automated Backups

```bash
# Set up daily backups (add to crontab)
0 2 * * * /path/to/project/scripts/deploy-production.sh backup

# Verify backups
ls -lh backups/

# Test restore (in test environment)
./scripts/rollback.sh test-restore
```

### Manual Backup

```bash
# Full backup
./scripts/deploy-production.sh backup

# Database only
docker exec issue-tracker-postgres-prod pg_dump -U postgres issue_tracker > backup.sql

# Docker volumes
docker run --rm -v personal-issue-tracker_postgres_prod_data:/data -v $(pwd)/backups:/backup alpine tar czf /backup/postgres_volume.tar.gz -C /data .
```

### Recovery Procedures

```bash
# Full system restore
./scripts/rollback.sh

# Database only restore
cat backup.sql | docker exec -i issue-tracker-postgres-prod psql -U postgres -d issue_tracker

# Verify after restore
./scripts/production-validation.sh
```

## Troubleshooting Procedures

### Step-by-Step Troubleshooting

1. **Identify the problem**:
   - Check application logs
   - Check health endpoints
   - Review monitoring dashboards

2. **Gather information**:
   - Error messages
   - Stack traces
   - System metrics
   - Recent changes

3. **Isolate the issue**:
   - Is it application, database, or infrastructure?
   - Is it affecting all users or specific users?
   - When did it start?

4. **Apply solution**:
   - Try least disruptive solution first
   - Document what you try
   - Monitor the results

5. **Verify fix**:
   - Run health checks
   - Test affected functionality
   - Monitor for recurrence

6. **Document**:
   - What happened
   - What caused it
   - How it was fixed
   - How to prevent it

## Emergency Procedures

### Application Down

1. **Check container status**:
```bash
docker-compose -f docker-compose.prod.yml ps
```

2. **Restart application**:
```bash
docker-compose -f docker-compose.prod.yml restart backend
```

3. **If restart fails, check logs**:
```bash
docker-compose -f docker-compose.prod.yml logs backend
```

4. **If database issue, restart database**:
```bash
docker-compose -f docker-compose.prod.yml restart postgres
```

5. **If all else fails, full restart**:
```bash
docker-compose -f docker-compose.prod.yml down
docker-compose -f docker-compose.prod.yml up -d
```

### Data Corruption

1. **Stop application immediately**:
```bash
docker-compose -f docker-compose.prod.yml stop backend
```

2. **Create emergency backup**:
```bash
docker exec issue-tracker-postgres-prod pg_dump -U postgres issue_tracker > emergency_backup.sql
```

3. **Assess damage**:
```bash
# Check database integrity
docker exec issue-tracker-postgres-prod psql -U postgres -d issue_tracker -c "SELECT * FROM pg_stat_database WHERE datname = 'issue_tracker';"
```

4. **Restore from last good backup**:
```bash
./scripts/rollback.sh
```

5. **Verify data integrity**:
```bash
./scripts/production-validation.sh
```

### Security Breach

1. **Isolate the system**:
```bash
# Stop accepting new connections
docker-compose -f docker-compose.prod.yml stop nginx
```

2. **Preserve evidence**:
```bash
# Copy all logs
cp -r backend/logs/ incident_logs_$(date +%Y%m%d_%H%M%S)/
```

3. **Assess the breach**:
```bash
# Check for unauthorized access
grep "401\|403" backend/logs/security.log
grep "SUSPICIOUS" backend/logs/security.log
```

4. **Rotate all secrets**:
```bash
# Generate new JWT secret
# Update database passwords
# Update Redis password
```

5. **Notify stakeholders**:
   - Document the incident
   - Notify affected users
   - Report to authorities if required

## Known Limitations

### Current Limitations

1. **Single Active Sprint**: Only one sprint can be active at a time per user
2. **No Real-time Collaboration**: Changes by other users require page refresh
3. **Limited Export Options**: No built-in data export functionality
4. **No Email Notifications**: System doesn't send email notifications
5. **No Mobile App**: Web-only interface (responsive design available)
6. **No Attachments**: Cannot attach files to issues
7. **No Time Tracking**: No built-in time tracking functionality
8. **No Custom Fields**: Cannot add custom fields to issues
9. **No Webhooks**: No webhook support for integrations
10. **No API Rate Limiting Per User**: Rate limiting is global, not per-user

### Workarounds

1. **Multiple Active Sprints**: Plan sprints sequentially
2. **Real-time Updates**: Refresh page regularly or use browser auto-refresh extension
3. **Data Export**: Contact administrator for database exports
4. **Email Notifications**: Set up external monitoring/alerting
5. **Mobile Access**: Use responsive web interface on mobile browsers
6. **Attachments**: Use issue descriptions to link to external files
7. **Time Tracking**: Use comments to log time spent
8. **Custom Fields**: Use labels or description fields
9. **Integrations**: Use API endpoints directly
10. **Rate Limiting**: Monitor usage and scale infrastructure as needed

### Planned Improvements

- Real-time collaboration with WebSocket
- Email notification system
- Data export functionality
- File attachment support
- Custom fields
- Webhook support
- Enhanced mobile experience
- Time tracking
- Advanced reporting
- API rate limiting per user

---

**Version**: 1.0  
**Last Updated**: January 2026  
**Maintained By**: System Administrator

