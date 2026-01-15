# Guía de Monitoreo y Logging - Personal Issue Tracker

> **[English Version](MONITORING_GUIDE.md)** | Versión en Español

Esta guía cubre monitoreo, logging y observabilidad para la aplicación Personal Issue Tracker.

## Tabla de Contenidos

1. [Descripción General](#descripción-general)
2. [Endpoints de Verificación de Salud](#endpoints-de-verificación-de-salud)
3. [Métricas y Monitoreo](#métricas-y-monitoreo)
4. [Configuración de Logging](#configuración-de-logging)
5. [Seguimiento de Errores](#seguimiento-de-errores)
6. [Monitoreo de Rendimiento](#monitoreo-de-rendimiento)
7. [Alertas](#alertas)
8. [Solución de Problemas](#solución-de-problemas)

## Descripción General

La aplicación incluye capacidades completas de monitoreo y logging:

- **Verificaciones de Salud**: Indicadores de salud personalizados para base de datos, recursos de aplicación y dependencias
- **Métricas**: Métricas compatibles con Prometheus para monitorear rendimiento de aplicación
- **Logging**: Logging estructurado con IDs de correlación para rastreo de solicitudes
- **Seguimiento de Errores**: Detección y alertas automáticas de errores
- **Monitoreo de Rendimiento**: Métricas de rendimiento en tiempo real y detección de operaciones lentas

## Endpoints de Verificación de Salud

### Spring Boot Actuator Health

**Endpoint**: `GET /actuator/health`

Retorna estado general de salud de la aplicación:

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
    }
  }
}
```

### Endpoints de Monitoreo Personalizados

**Resumen de Métricas**: `GET /monitoring/metrics`

Retorna métricas de aplicación:

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

**Estadísticas de Errores**: `GET /monitoring/errors`

Retorna estadísticas de seguimiento de errores:

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

**Información del Sistema**: `GET /monitoring/system`

Retorna información del sistema:

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

## Métricas y Monitoreo

### Métricas de Prometheus

**Endpoint**: `GET /actuator/prometheus`

Expone métricas compatibles con Prometheus para scraping.

### Métricas Clave

#### Métricas de Aplicación

- `auth_success_total`: Total de autenticaciones exitosas
- `auth_failure_total`: Total de autenticaciones fallidas
- `auth_duration_seconds`: Histograma de duración de autenticación
- `issue_created_total`: Total de issues creados
- `issue_creation_duration_seconds`: Histograma de duración de creación de issues
- `project_created_total`: Total de proyectos creados
- `project_creation_duration_seconds`: Histograma de duración de creación de proyectos
- `sprint_created_total`: Total de sprints creados
- `dashboard_load_duration_seconds`: Histograma de duración de carga de dashboard
- `error_count_total`: Total de errores por tipo
- `database_slow_query_total`: Total de consultas lentas de base de datos

#### Métricas JVM

- `jvm_memory_used_bytes`: Uso de memoria JVM
- `jvm_memory_max_bytes`: Memoria máxima JVM
- `jvm_gc_pause_seconds`: Tiempo de pausa de recolección de basura
- `jvm_threads_live`: Conteo actual de hilos
- `jvm_classes_loaded`: Conteo de clases cargadas

#### Métricas HTTP

- `http_server_requests_seconds`: Duración de solicitud HTTP
- `http_server_requests_seconds_count`: Conteo de solicitudes HTTP
- `http_server_requests_seconds_sum`: Tiempo total de solicitudes HTTP

#### Métricas de Base de Datos

- `hikari_connections_active`: Conexiones activas de base de datos
- `hikari_connections_idle`: Conexiones inactivas de base de datos
- `hikari_connections_pending`: Solicitudes de conexión pendientes
- `hikari_connections_timeout_total`: Conteo de timeouts de conexión

### Configuración de Prometheus

Ejemplo de configuración `prometheus.yml`:

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

### Dashboards de Grafana

Importar el dashboard de Grafana proporcionado para visualización:

1. Abrir Grafana
2. Ir a Dashboards → Import
3. Subir `infrastructure/monitoring/grafana-dashboard.json`
4. Seleccionar fuente de datos Prometheus
5. Hacer clic en Import

Paneles clave del dashboard:
- Tasa de solicitudes y latencia
- Tasa de error por tipo
- Uso de memoria y CPU
- Estado del pool de conexiones de base de datos
- Tasa de éxito/fallo de autenticación
- Detección de operaciones lentas

## Configuración de Logging

### Niveles de Log

Configurar niveles de log vía variables de entorno:

```bash
# Logs de aplicación
LOG_LEVEL=INFO

# Logs de seguridad
SECURITY_LOG_LEVEL=WARN

# Logs de solicitudes
REQUEST_LOG_LEVEL=INFO
```

### Archivos de Log

Los logs se escriben en los siguientes archivos:

- **Log de Aplicación**: `logs/application.log`
  - Todos los logs de aplicación
  - Rotado diariamente, máx 50MB por archivo
  - Mantenido por 30 días

- **Log de Seguridad**: `logs/security.log`
  - Eventos de autenticación y autorización
  - Errores relacionados con seguridad
  - Mantenido por 90 días

- **Log de Rendimiento**: `logs/performance.log`
  - Detección de operaciones lentas
  - Métricas de rendimiento
  - Mantenido por 30 días

### Formato de Log

#### Desarrollo (Consola)

```
2024-01-15 10:30:00.123 [correlation-id-123] [http-nio-8080-exec-1] INFO  c.i.service.AuthenticationService - User authenticated: user@example.com
```

#### Producción (JSON)

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

### IDs de Correlación

Cada solicitud recibe un ID de correlación para rastreo:

```bash
# Ver logs para solicitud específica
grep "correlation-id-123" logs/application.log
```

### Ver Logs

```bash
# Seguir logs de aplicación
tail -f logs/application.log

# Ver últimas 100 líneas
tail -n 100 logs/application.log

# Buscar errores
grep "ERROR" logs/application.log

# Ver logs de seguridad
tail -f logs/security.log

# Ver logs de rendimiento
tail -f logs/performance.log

# Logs de Docker
docker-compose -f docker-compose.prod.yml logs -f backend
```

## Seguimiento de Errores

### Detección Automática de Errores

La aplicación rastrea y categoriza errores automáticamente:

- **Fallos de Autenticación**: Intentos de inicio de sesión fallidos
- **Errores de Validación**: Fallos de validación de entrada
- **Errores de Base de Datos**: Errores de conectividad y consultas de base de datos
- **Errores de Servicios Externos**: Fallos de servicios de terceros

### Umbrales de Error

Las alertas se activan cuando:
- Más de 10 errores por minuto para cualquier tipo de error
- Ocurre cualquier error crítico
- Problemas de conectividad de base de datos
- Uso de memoria excede 90%
- Uso de CPU excede 90%

### Logs de Errores

Los errores se registran con contexto completo:

```
2024-01-15 10:30:00.123 [correlation-id-123] ERROR c.i.monitoring.ErrorTrackingService - Error tracked - Type: DATABASE_ERROR, Message: Connection timeout
java.sql.SQLException: Connection timeout
    at com.zaxxer.hikari.pool.HikariPool.getConnection(HikariPool.java:197)
    ...
```

## Monitoreo de Rendimiento

### Detección de Operaciones Lentas

Las operaciones que exceden umbrales se registran automáticamente:

- **Autenticación**: > 1000ms
- **Creación de Issue**: > 500ms
- **Creación de Proyecto**: > 500ms
- **Carga de Dashboard**: > 2000ms
- **Consultas de Base de Datos**: > 1000ms

Ejemplo de log:

```
2024-01-15 10:30:00.123 WARN c.i.monitoring.PerformanceMonitoringService - Slow dashboard load detected: 2500ms
```

### Métricas de Rendimiento

Ver métricas de rendimiento:

```bash
# Obtener métricas actuales
curl http://localhost:8080/monitoring/metrics

# Obtener métricas de Prometheus
curl http://localhost:8080/actuator/prometheus | grep duration
```

### Rendimiento de Consultas de Base de Datos

Monitorear consultas lentas:

```sql
-- Ver consultas lentas
SELECT query, calls, total_time, mean_time
FROM pg_stat_statements
ORDER BY mean_time DESC
LIMIT 10;

-- Reiniciar estadísticas
SELECT pg_stat_statements_reset();
```

## Alertas

### Configuración de Alertas

Configurar alertas en `infrastructure/monitoring/alert_rules.yml`:

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
          summary: "Alta tasa de errores detectada"
          description: "Tasa de error es {{ $value }} errores/segundo"
      
      - alert: HighMemoryUsage
        expr: jvm_memory_used_bytes / jvm_memory_max_bytes > 0.9
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "Alto uso de memoria"
          description: "Uso de memoria es {{ $value | humanizePercentage }}"
      
      - alert: SlowRequests
        expr: histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m])) > 2
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Solicitudes HTTP lentas"
          description: "Latencia percentil 95 es {{ $value }}s"
```

## Solución de Problemas

### Alto Uso de Memoria

```bash
# Verificar métricas de memoria
curl http://localhost:8080/monitoring/metrics | jq '.memory'

# Activar recolección de basura
curl -X POST http://localhost:8080/actuator/gc

# Analizar heap dump
jmap -dump:live,format=b,file=heap.bin <pid>
jhat heap.bin
```

### Alta Tasa de Errores

```bash
# Verificar estadísticas de errores
curl http://localhost:8080/monitoring/errors

# Ver logs de errores
grep "ERROR" logs/application.log | tail -n 50

# Verificar tipo de error específico
grep "DATABASE_ERROR" logs/application.log
```

### Rendimiento Lento

```bash
# Verificar métricas de rendimiento
curl http://localhost:8080/actuator/metrics/http.server.requests

# Ver operaciones lentas
grep "Slow.*detected" logs/performance.log

# Verificar conexiones de base de datos
curl http://localhost:8080/actuator/metrics/hikari.connections.active
```

### Problemas de Base de Datos

```bash
# Verificar salud de base de datos
curl http://localhost:8080/actuator/health/db

# Ver logs de base de datos
docker-compose -f docker-compose.prod.yml logs postgres

# Verificar conexiones activas
docker exec issue-tracker-postgres-prod \
  psql -U postgres -d issue_tracker \
  -c "SELECT count(*) FROM pg_stat_activity WHERE state = 'active'"
```

## Mejores Prácticas

### Monitoreo

1. ✅ Configurar Prometheus y Grafana para visualización de métricas
2. ✅ Configurar alertas para métricas críticas
3. ✅ Monitorear tasas y tipos de errores
4. ✅ Rastrear operaciones lentas y optimizar
5. ✅ Revisar logs regularmente
6. ✅ Configurar agregación de logs (ELK, Splunk, etc.)

### Logging

1. ✅ Usar niveles de log apropiados
2. ✅ Incluir IDs de correlación en todos los logs
3. ✅ No registrar información sensible
4. ✅ Usar logging estructurado en producción
5. ✅ Rotar logs para prevenir problemas de espacio en disco
6. ✅ Archivar logs antiguos para cumplimiento

### Rendimiento

1. ✅ Monitorear tiempos de respuesta
2. ✅ Rastrear rendimiento de consultas de base de datos
3. ✅ Optimizar operaciones lentas
4. ✅ Monitorear uso de memoria y CPU
5. ✅ Usar caché efectivamente
6. ✅ Perfilar aplicación regularmente

## Recursos Adicionales

- [Guía de Despliegue](../deployment/DEPLOYMENT_GUIDE.es.md)
- [Lista de Verificación de Seguridad](../security/SECURITY_CHECKLIST.es.md)
- [Guía de Mantenimiento](MAINTENANCE_GUIDE.es.md)
- [Documentación de Prometheus](https://prometheus.io/docs/)
- [Documentación de Grafana](https://grafana.com/docs/)

---

**Versión**: 1.0.0  
**Última Actualización**: 14 de enero de 2026
