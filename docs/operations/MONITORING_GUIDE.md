# Monitoring and Logging Guide - Personal Issue Tracker

This guide covers monitoring, logging, and observability for the Personal Issue Tracker application.

## Table of Contents

1. [Overview](#overview)
2. [Health Check Endpoints](#health-check-endpoints)
3. [Metrics and Monitoring](#metrics-and-monitoring)
4. [Logging Configuration](#logging-configuration)
5. [Error Tracking](#error-tracking)
6. [Performance Monitoring](#performance-monitoring)
7. [Alerting](#alerting)
8. [Troubleshooting](#troubleshooting)

## Overview

The application includes comprehensive monitoring and logging capabilities:

- **Health Checks**: Custom health indicators for database, application resources, and dependencies
- **Metrics**: Prometheus-compatible metrics for monitoring application performance
- **Logging**: Structured logging with correlation IDs for request tracing
- **Error Tracking**: Automatic error detection and alerting
- **Performance Monitoring**: Real-time performance metrics and slow operation detection

## Health Check Endpoints

### Spring Boot Actuator Health

**Endpoint**: `GET /actuator/health`

Returns overall application health status:

```json
{
  "status": "UP",
  "components": {
    "database": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "responseTime": "15ms",
        "activeConnections": 5,
        "databaseSize": "125.45 MB"
      }
    },
    "application": {
      "status": "UP",
      "details": {
        "memory": {
          "heapUsed": "512.00 MB",
          "heapMax": "2.00 GB",
          "heapUsagePercent": "25.00%",
          "nonHeapUsed": "128.00 MB"
        },
        "cpu": {
          "systemLoadAverage": 2.5,
          "availableProcessors": 8,
          "loadPerProcessor": "31.25%"
        },
        "uptime": "2d 5h 30m"
      }
    },
    "redis": {
      "status": "UP"
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": "500.00 GB",
        "free": "250.00 GB",
        "threshold": "1.00 GB"
      }
    }
  }
}
```

### Custom Monitoring Endpoints

**Metrics Summary**: `GET /monitoring/metrics`

Returns application metrics:

```json
{
  "memory": {
    "heapUsed": "512.00 MB",
    "heapMax": "2.00 GB",
    "heapUsagePercent": "25.00%",
    "nonHeapUsed": "128.00 MB"
  },
  "cpu": {
    "availableProcessors": 8,
    "systemLoadAverage": 2.5
  },
  "runtime": {
    "uptime": "2d 5h 30m",
    "startTime": 1704067200000
  },
  "threads": {
    "threadCount": 45,
    "peakThreadCount": 52,
    "daemonThreadCount": 38
  }
}
```

**Error Statistics**: `GET /monitoring/errors`

Returns error tracking statistics:

```json
{
  "AUTH_FAILURE": {
    "totalCount": 15,
    "recentCount": 3,
    "lastOccurrence": "2024-01-15T10:30:00Z"
  },
  "DATABASE_ERROR": {
    "totalCount": 2,
    "recentCount": 0,
    "lastOccurrence": "2024-01-15T09:15:00Z"
  }
}
```

**System Information**: `GET /monitoring/system`

Returns system information:

```json
{
  "javaVersion": "21.0.1",
  "javaVendor": "Eclipse Adoptium",
  "osName": "Linux",
  "osVersion": "5.15.0",
  "osArch": "amd64",
  "availableProcessors": 8,
  "totalMemory": "2.00 GB",
  "freeMemory": "1.50 GB",
  "maxMemory": "2.00 GB"
}
```

## Metrics and Monitoring

### Prometheus Metrics

**Endpoint**: `GET /actuator/prometheus`

Exposes Prometheus-compatible metrics for scraping.

### Key Metrics

#### Application Metrics

- `auth_success_total`: Total successful authentications
- `auth_failure_total`: Total failed authentications
- `auth_duration_seconds`: Authentication duration histogram
- `issue_created_total`: Total issues created
- `issue_creation_duration_seconds`: Issue creation duration histogram
- `project_created_total`: Total projects created
- `project_creation_duration_seconds`: Project creation duration histogram
- `sprint_created_total`: Total sprints created
- `dashboard_load_duration_seconds`: Dashboard load duration histogram
- `error_count_total`: Total errors by type
- `database_slow_query_total`: Total slow database queries

#### JVM Metrics

- `jvm_memory_used_bytes`: JVM memory usage
- `jvm_memory_max_bytes`: JVM maximum memory
- `jvm_gc_pause_seconds`: Garbage collection pause time
- `jvm_threads_live`: Current thread count
- `jvm_classes_loaded`: Loaded class count

#### HTTP Metrics

- `http_server_requests_seconds`: HTTP request duration
- `http_server_requests_seconds_count`: HTTP request count
- `http_server_requests_seconds_sum`: Total HTTP request time

#### Database Metrics

- `hikari_connections_active`: Active database connections
- `hikari_connections_idle`: Idle database connections
- `hikari_connections_pending`: Pending connection requests
- `hikari_connections_timeout_total`: Connection timeout count

### Prometheus Configuration

Example `prometheus.yml` configuration:

```yaml
scrape_configs:
  - job_name: 'issue-tracker'
    metrics_path: '/actuator/prometheus'
    scrape_interval: 15s
    static_configs:
      - targets: ['localhost:8080']
        labels:
          application: 'personal-issue-tracker'
          environment: 'production'
```

### Grafana Dashboards

Import the provided Grafana dashboard for visualization:

1. Open Grafana
2. Go to Dashboards → Import
3. Upload `infrastructure/monitoring/grafana-dashboard.json`
4. Select Prometheus data source
5. Click Import

Key dashboard panels:
- Request rate and latency
- Error rate by type
- Memory and CPU usage
- Database connection pool status
- Authentication success/failure rate
- Slow operation detection

## Logging Configuration

### Log Levels

Configure log levels via environment variables:

```bash
# Application logs
LOG_LEVEL=INFO

# Security logs
SECURITY_LOG_LEVEL=WARN

# Request logs
REQUEST_LOG_LEVEL=INFO
```

### Log Files

Logs are written to the following files:

- **Application Log**: `logs/application.log`
  - All application logs
  - Rotated daily, max 50MB per file
  - Kept for 30 days

- **Security Log**: `logs/security.log`
  - Authentication and authorization events
  - Security-related errors
  - Kept for 90 days

- **Performance Log**: `logs/performance.log`
  - Slow operation detection
  - Performance metrics
  - Kept for 30 days

### Log Format

#### Development (Console)

```
2024-01-15 10:30:00.123 [correlation-id-123] [http-nio-8080-exec-1] INFO  c.i.service.AuthenticationService - User authenticated: user@example.com
```

#### Production (JSON)

```json
{
  "timestamp": "2024-01-15T10:30:00.123Z",
  "level": "INFO",
  "thread": "http-nio-8080-exec-1",
  "logger": "com.issuetracker.service.AuthenticationService",
  "message": "User authenticated: user@example.com",
  "correlationId": "correlation-id-123",
  "app": "personal-issue-tracker",
  "environment": "production"
}
```

### Correlation IDs

Every request is assigned a correlation ID for tracing:

```bash
# View logs for specific request
grep "correlation-id-123" logs/application.log
```

### Viewing Logs

```bash
# Tail application logs
tail -f logs/application.log

# View last 100 lines
tail -n 100 logs/application.log

# Search for errors
grep "ERROR" logs/application.log

# View security logs
tail -f logs/security.log

# View performance logs
tail -f logs/performance.log

# Docker logs
docker-compose -f docker-compose.prod.yml logs -f backend
```

## Error Tracking

### Automatic Error Detection

The application automatically tracks and categorizes errors:

- **Authentication Failures**: Failed login attempts
- **Validation Errors**: Input validation failures
- **Database Errors**: Database connectivity and query errors
- **External Service Errors**: Third-party service failures

### Error Thresholds

Alerts are triggered when:
- More than 10 errors per minute for any error type
- Any critical error occurs
- Database connectivity issues
- Memory usage exceeds 90%
- CPU usage exceeds 90%

### Error Logs

Errors are logged with full context:

```
2024-01-15 10:30:00.123 [correlation-id-123] ERROR c.i.monitoring.ErrorTrackingService - Error tracked - Type: DATABASE_ERROR, Message: Connection timeout
java.sql.SQLException: Connection timeout
    at com.zaxxer.hikari.pool.HikariPool.getConnection(HikariPool.java:197)
    ...
```

## Performance Monitoring

### Slow Operation Detection

Operations exceeding thresholds are automatically logged:

- **Authentication**: > 1000ms
- **Issue Creation**: > 500ms
- **Project Creation**: > 500ms
- **Dashboard Load**: > 2000ms
- **Database Queries**: > 1000ms

Example log:

```
2024-01-15 10:30:00.123 WARN c.i.monitoring.PerformanceMonitoringService - Slow dashboard load detected: 2500ms
```

### Performance Metrics

View performance metrics:

```bash
# Get current metrics
curl http://localhost:8080/monitoring/metrics

# Get Prometheus metrics
curl http://localhost:8080/actuator/prometheus | grep duration
```

### Database Query Performance

Monitor slow queries:

```sql
-- View slow queries
SELECT query, calls, total_time, mean_time
FROM pg_stat_statements
ORDER BY mean_time DESC
LIMIT 10;

-- Reset statistics
SELECT pg_stat_statements_reset();
```

## Alerting

### Alert Configuration

Configure alerts in `infrastructure/monitoring/alert_rules.yml`:

```yaml
groups:
  - name: application_alerts
    interval: 30s
    rules:
      - alert: HighErrorRate
        expr: rate(error_count_total[5m]) > 0.1
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High error rate detected"
          description: "Error rate is {{ $value }} errors/second"
      
      - alert: HighMemoryUsage
        expr: jvm_memory_used_bytes / jvm_memory_max_bytes > 0.9
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "High memory usage"
          description: "Memory usage is {{ $value | humanizePercentage }}"
      
      - alert: SlowRequests
        expr: histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m])) > 2
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Slow HTTP requests"
          description: "95th percentile latency is {{ $value }}s"
```

### Alert Channels

Configure alert channels in `infrastructure/monitoring/alertmanager.yml`:

```yaml
route:
  receiver: 'team-email'
  group_by: ['alertname', 'severity']
  group_wait: 30s
  group_interval: 5m
  repeat_interval: 4h

receivers:
  - name: 'team-email'
    email_configs:
      - to: 'team@example.com'
        from: 'alerts@example.com'
        smarthost: 'smtp.example.com:587'
        auth_username: 'alerts@example.com'
        auth_password: 'password'
```

## Troubleshooting

### High Memory Usage

```bash
# Check memory metrics
curl http://localhost:8080/monitoring/metrics | jq '.memory'

# Trigger garbage collection
curl -X POST http://localhost:8080/actuator/gc

# Analyze heap dump
jmap -dump:live,format=b,file=heap.bin <pid>
jhat heap.bin
```

### High Error Rate

```bash
# Check error statistics
curl http://localhost:8080/monitoring/errors

# View error logs
grep "ERROR" logs/application.log | tail -n 50

# Check specific error type
grep "DATABASE_ERROR" logs/application.log
```

### Slow Performance

```bash
# Check performance metrics
curl http://localhost:8080/actuator/metrics/http.server.requests

# View slow operations
grep "Slow.*detected" logs/performance.log

# Check database connections
curl http://localhost:8080/actuator/metrics/hikari.connections.active
```

### Database Issues

```bash
# Check database health
curl http://localhost:8080/actuator/health/db

# View database logs
docker-compose -f docker-compose.prod.yml logs postgres

# Check active connections
docker exec issue-tracker-postgres-prod \
  psql -U postgres -d issue_tracker \
  -c "SELECT count(*) FROM pg_stat_activity WHERE state = 'active'"
```

## Best Practices

### Monitoring

1. ✅ Set up Prometheus and Grafana for metrics visualization
2. ✅ Configure alerts for critical metrics
3. ✅ Monitor error rates and types
4. ✅ Track slow operations and optimize
5. ✅ Review logs regularly
6. ✅ Set up log aggregation (ELK, Splunk, etc.)

### Logging

1. ✅ Use appropriate log levels
2. ✅ Include correlation IDs in all logs
3. ✅ Don't log sensitive information
4. ✅ Use structured logging in production
5. ✅ Rotate logs to prevent disk space issues
6. ✅ Archive old logs for compliance

### Performance

1. ✅ Monitor response times
2. ✅ Track database query performance
3. ✅ Optimize slow operations
4. ✅ Monitor memory and CPU usage
5. ✅ Use caching effectively
6. ✅ Profile application regularly

## Additional Resources

- [Deployment Guide](DEPLOYMENT_GUIDE.md)
- [Security Checklist](SECURITY_CHECKLIST.md)
- [Production Deployment Guide](PRODUCTION_DEPLOYMENT_GUIDE.md)
- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)
