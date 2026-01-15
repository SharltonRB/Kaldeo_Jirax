# Guía de Mantenimiento y Solución de Problemas

> **[English Version](MAINTENANCE_GUIDE.md)** | Versión en Español

## Descripción General

Esta guía proporciona información completa para mantener y solucionar problemas de la aplicación Personal Issue Tracker en producción.

## Tabla de Contenidos

1. [Tareas de Mantenimiento Regular](#tareas-de-mantenimiento-regular)
2. [Monitoreo y Alertas](#monitoreo-y-alertas)
3. [Problemas Comunes y Soluciones](#problemas-comunes-y-soluciones)
4. [Mantenimiento de Base de Datos](#mantenimiento-de-base-de-datos)
5. [Optimización de Rendimiento](#optimización-de-rendimiento)
6. [Mantenimiento de Seguridad](#mantenimiento-de-seguridad)
7. [Respaldo y Recuperación](#respaldo-y-recuperación)
8. [Procedimientos de Solución de Problemas](#procedimientos-de-solución-de-problemas)
9. [Procedimientos de Emergencia](#procedimientos-de-emergencia)
10. [Limitaciones Conocidas](#limitaciones-conocidas)

## Tareas de Mantenimiento Regular

### Tareas Diarias

#### 1. Monitoreo de Verificación de Salud

```bash
# Ejecutar verificación de salud automatizada
./scripts/health-check.sh

# Verificar logs de aplicación para errores
tail -f backend/logs/application.log | grep ERROR

# Monitorear recursos del sistema
docker stats
```

#### 2. Revisar Logs de Errores

```bash
# Verificar fallos de autenticación
grep "AUTH_FAILURE" backend/logs/security.log

# Verificar errores de base de datos
grep "DATABASE_ERROR" backend/logs/application.log

# Verificar problemas de rendimiento
grep "Slow.*detected" backend/logs/performance.log
```

### Tareas Semanales

#### 1. Mantenimiento de Base de Datos

```bash
# Ejecutar VACUUM para recuperar almacenamiento
docker exec issue-tracker-postgres-prod psql -U postgres -d issue_tracker -c "VACUUM ANALYZE;"

# Verificar tamaño de base de datos
docker exec issue-tracker-postgres-prod psql -U postgres -d issue_tracker -c "SELECT pg_size_pretty(pg_database_size('issue_tracker'));"

# Revisar consultas lentas
docker exec issue-tracker-postgres-prod psql -U postgres -d issue_tracker -c "SELECT query, mean_time, calls FROM pg_stat_statements ORDER BY mean_time DESC LIMIT 10;"
```

#### 2. Verificación de Rotación de Logs

```bash
# Verificar tamaños de archivos de log
du -h backend/logs/*.log

# Verificar que rotación de logs funciona
ls -lh backend/logs/*.gz

# Limpiar logs antiguos (más de 30 días)
find backend/logs -name "*.gz" -mtime +30 -delete
```

#### 3. Revisión de Seguridad

```bash
# Verificar intentos de inicio de sesión fallidos
grep "Failed login" backend/logs/security.log | wc -l

# Revisar efectividad de limitación de tasa
curl http://localhost:8080/monitoring/errors | jq '.AUTH_FAILURE'

# Verificar actividad sospechosa
grep "SUSPICIOUS" backend/logs/security.log
```

### Tareas Mensuales

#### 1. Actualizaciones de Dependencias

```bash
# Verificar actualizaciones de dependencias del backend
cd backend
./mvnw versions:display-dependency-updates

# Verificar actualizaciones de dependencias del frontend
cd frontend
npm outdated

# Actualizar dependencias (¡probar primero en staging!)
./mvnw versions:use-latest-releases
npm update
```

#### 2. Escaneo de Seguridad

```bash
# Escanear imágenes Docker para vulnerabilidades
docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \
  aquasec/trivy image personal-issue-tracker_backend:latest

# Escanear dependencias
./mvnw dependency-check:check
npm audit
```

#### 3. Revisión de Rendimiento

```bash
# Revisar métricas de rendimiento
curl http://localhost:8080/monitoring/metrics | jq

# Verificar rendimiento de base de datos
docker exec issue-tracker-postgres-prod psql -U postgres -d issue_tracker \
  -c "SELECT schemaname, tablename, pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size FROM pg_tables ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC LIMIT 10;"

# Revisar tasas de acierto de caché
curl http://localhost:8080/actuator/metrics/cache.gets | jq
```

#### 4. Verificación de Respaldos

```bash
# Listar respaldos recientes
ls -lh backups/

# Probar restauración de respaldo (en entorno de prueba)
./scripts/rollback.sh list
./scripts/rollback.sh test-restore
```

### Tareas Trimestrales

#### 1. Renovación de Certificado SSL

```bash
# Verificar expiración de certificado
openssl x509 -in infrastructure/docker/ssl/cert.pem -noout -enddate

# Renovar certificados Let's Encrypt (si aplica)
sudo certbot renew

# Actualizar aplicación con nuevos certificados
./scripts/deploy-production.sh
```

#### 2. Planificación de Capacidad

```bash
# Revisar crecimiento de base de datos
docker exec issue-tracker-postgres-prod psql -U postgres -d issue_tracker \
  -c "SELECT schemaname, tablename, n_live_tup FROM pg_stat_user_tables ORDER BY n_live_tup DESC;"

# Revisar tendencias de uso de disco
df -h

# Revisar tendencias de uso de memoria
free -h
docker stats --no-stream
```

#### 3. Pruebas de Recuperación ante Desastres

```bash
# Probar procedimiento completo de respaldo y restauración
./scripts/deploy-production.sh backup
./scripts/rollback.sh

# Verificar integridad de datos después de restauración
./scripts/production-validation.sh
```

## Monitoreo y Alertas

### Métricas Clave a Monitorear

#### Métricas de Aplicación

- **Tiempo de Respuesta**: Debe ser < 500ms para percentil 95
- **Tasa de Error**: Debe ser < 1% del total de solicitudes
- **Uso de Memoria**: Debe ser < 80% de memoria asignada
- **Uso de CPU**: Debe ser < 70% promedio
- **Sesiones Activas**: Monitorear picos inusuales

#### Métricas de Base de Datos

- **Pool de Conexiones**: Conexiones activas deben ser < 80% del tamaño del pool
- **Rendimiento de Consultas**: Consultas lentas (> 1000ms) deben investigarse
- **Tamaño de Base de Datos**: Monitorear tasa de crecimiento
- **Retraso de Replicación**: Debe ser < 1 segundo (si usa replicación)

#### Métricas de Infraestructura

- **Espacio en Disco**: Debe tener > 15% de espacio libre
- **Latencia de Red**: Debe ser < 100ms
- **Salud de Contenedores**: Todos los contenedores deben estar "healthy"
- **Éxito de Respaldos**: Respaldos diarios deben completarse exitosamente

## Problemas Comunes y Soluciones

### Problema 1: Aplicación No Inicia

**Síntomas**:
- Contenedor sale inmediatamente
- Errores "Connection refused"
- Logs de aplicación muestran errores de inicio

**Diagnóstico**:
```bash
# Verificar estado de contenedor
docker-compose -f docker-compose.prod.yml ps

# Verificar logs
docker-compose -f docker-compose.prod.yml logs backend

# Verificar conectividad de base de datos
docker exec issue-tracker-postgres-prod pg_isready
```

**Soluciones**:

1. **Base de datos no lista**:
```bash
# Esperar a que base de datos esté lista
docker-compose -f docker-compose.prod.yml up -d postgres
sleep 10
docker-compose -f docker-compose.prod.yml up -d backend
```

2. **Configuración inválida**:
```bash
# Verificar variables de entorno
docker exec issue-tracker-backend-prod env | grep -E "DB_|JWT_"

# Verificar application.yml
docker exec issue-tracker-backend-prod cat /app/config/application-prod.yml
```

### Problema 2: Alto Uso de Memoria

**Síntomas**:
- Uso de memoria > 90%
- OutOfMemoryError en logs
- Aplicación no responde

**Diagnóstico**:
```bash
# Verificar uso de memoria
curl http://localhost:8080/monitoring/metrics | jq '.memory'

# Verificar fugas de memoria
docker exec issue-tracker-backend-prod jmap -heap <PID>
```

**Soluciones**:

1. **Aumentar asignación de memoria**:
```yaml
# En docker-compose.prod.yml
services:
  backend:
    deploy:
      resources:
        limits:
          memory: 4G  # Aumentar desde 2G
```

2. **Activar recolección de basura**:
```bash
curl -X POST http://localhost:8080/actuator/gc
```

### Problema 3: Rendimiento Lento

**Síntomas**:
- Tiempos de respuesta > 2 segundos
- Timeouts
- Usuarios se quejan de lentitud

**Diagnóstico**:
```bash
# Verificar tiempos de respuesta
curl -w "@curl-format.txt" -o /dev/null -s http://localhost:8080/actuator/health

# Verificar rendimiento de base de datos
docker exec issue-tracker-postgres-prod psql -U postgres -d issue_tracker \
  -c "SELECT query, calls, total_time, mean_time FROM pg_stat_statements ORDER BY mean_time DESC LIMIT 10;"

# Verificar pool de conexiones
curl http://localhost:8080/actuator/metrics/hikaricp.connections.active
```

**Soluciones**:

1. **Optimizar consultas de base de datos**:
```sql
-- Agregar índices faltantes
CREATE INDEX idx_issues_user_project ON issues(user_id, project_id);
CREATE INDEX idx_issues_status ON issues(status);

-- Actualizar estadísticas
ANALYZE;
```

2. **Aumentar pool de conexiones**:
```yaml
# En application-prod.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 40  # Aumentar desde 20
```

## Mantenimiento de Base de Datos

### Mantenimiento Regular

```bash
# VACUUM para recuperar almacenamiento
docker exec issue-tracker-postgres-prod psql -U postgres -d issue_tracker -c "VACUUM FULL ANALYZE;"

# Reindexar para mejorar rendimiento
docker exec issue-tracker-postgres-prod psql -U postgres -d issue_tracker -c "REINDEX DATABASE issue_tracker;"

# Actualizar estadísticas
docker exec issue-tracker-postgres-prod psql -U postgres -d issue_tracker -c "ANALYZE;"
```

### Monitorear Salud de Base de Datos

```sql
-- Verificar tamaños de tablas
SELECT schemaname, tablename, 
       pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_tables 
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC 
LIMIT 10;

-- Verificar uso de índices
SELECT schemaname, tablename, indexname, idx_scan, idx_tup_read, idx_tup_fetch
FROM pg_stat_user_indexes
ORDER BY idx_scan DESC;
```

## Optimización de Rendimiento

### Optimización a Nivel de Aplicación

1. **Habilitar caché**:
```yaml
spring:
  cache:
    type: redis
    cache-names: projects, issues, sprints
```

2. **Optimizar consultas**:
```java
// Usar paginación
@Query("SELECT i FROM Issue i WHERE i.user.id = :userId")
Page<Issue> findByUserId(@Param("userId") Long userId, Pageable pageable);

// Usar fetch joins para evitar consultas N+1
@Query("SELECT i FROM Issue i LEFT JOIN FETCH i.labels WHERE i.id = :id")
Optional<Issue> findByIdWithLabels(@Param("id") Long id);
```

### Optimización a Nivel de Base de Datos

1. **Agregar índices**:
```sql
-- Índices compuestos para consultas comunes
CREATE INDEX idx_issues_user_project_status ON issues(user_id, project_id, status);
CREATE INDEX idx_issues_sprint ON issues(sprint_id) WHERE sprint_id IS NOT NULL;
CREATE INDEX idx_audit_logs_issue_created ON audit_logs(issue_id, created_at DESC);
```

## Mantenimiento de Seguridad

### Tareas de Seguridad Regulares

1. **Actualizar dependencias**:
```bash
# Verificar vulnerabilidades de seguridad
./mvnw dependency-check:check
npm audit

# Actualizar dependencias
./mvnw versions:use-latest-releases
npm update
```

2. **Revisar logs de acceso**:
```bash
# Verificar actividad sospechosa
grep "401\|403" backend/logs/application.log

# Verificar intentos de fuerza bruta
grep "AUTH_FAILURE" backend/logs/security.log | awk '{print $NF}' | sort | uniq -c | sort -rn
```

## Respaldo y Recuperación

### Respaldos Automatizados

```bash
# Configurar respaldos diarios (agregar a crontab)
0 2 * * * /path/to/project/scripts/deploy-production.sh backup

# Verificar respaldos
ls -lh backups/

# Probar restauración (en entorno de prueba)
./scripts/rollback.sh test-restore
```

### Respaldo Manual

```bash
# Respaldo completo
./scripts/deploy-production.sh backup

# Solo base de datos
docker exec issue-tracker-postgres-prod pg_dump -U postgres issue_tracker > backup.sql

# Volúmenes Docker
docker run --rm -v personal-issue-tracker_postgres_prod_data:/data -v $(pwd)/backups:/backup alpine tar czf /backup/postgres_volume.tar.gz -C /data .
```

## Procedimientos de Emergencia

### Aplicación Caída

1. **Verificar estado de contenedor**:
```bash
docker-compose -f docker-compose.prod.yml ps
```

2. **Reiniciar aplicación**:
```bash
docker-compose -f docker-compose.prod.yml restart backend
```

3. **Si reinicio falla, verificar logs**:
```bash
docker-compose -f docker-compose.prod.yml logs backend
```

4. **Si problema de base de datos, reiniciar base de datos**:
```bash
docker-compose -f docker-compose.prod.yml restart postgres
```

5. **Si todo falla, reinicio completo**:
```bash
docker-compose -f docker-compose.prod.yml down
docker-compose -f docker-compose.prod.yml up -d
```

## Limitaciones Conocidas

### Limitaciones Actuales

1. **Un Solo Sprint Activo**: Solo un sprint puede estar activo a la vez por usuario
2. **Sin Colaboración en Tiempo Real**: Cambios de otros usuarios requieren refrescar página
3. **Opciones de Exportación Limitadas**: Sin funcionalidad de exportación de datos integrada
4. **Sin Notificaciones por Email**: Sistema no envía notificaciones por correo
5. **Sin Aplicación Móvil**: Interfaz solo web (diseño responsivo disponible)
6. **Sin Adjuntos**: No se pueden adjuntar archivos a issues
7. **Sin Seguimiento de Tiempo**: Sin funcionalidad de seguimiento de tiempo integrada
8. **Sin Campos Personalizados**: No se pueden agregar campos personalizados a issues
9. **Sin Webhooks**: Sin soporte de webhooks para integraciones
10. **Sin Limitación de Tasa por Usuario**: Limitación de tasa es global, no por usuario

### Soluciones Alternativas

1. **Múltiples Sprints Activos**: Planificar sprints secuencialmente
2. **Actualizaciones en Tiempo Real**: Refrescar página regularmente o usar extensión de auto-refresh
3. **Exportación de Datos**: Contactar administrador para exportaciones de base de datos
4. **Notificaciones por Email**: Configurar monitoreo/alertas externas
5. **Acceso Móvil**: Usar interfaz web responsiva en navegadores móviles
6. **Adjuntos**: Usar descripciones de issues para enlazar archivos externos
7. **Seguimiento de Tiempo**: Usar comentarios para registrar tiempo invertido
8. **Campos Personalizados**: Usar etiquetas o campos de descripción
9. **Integraciones**: Usar endpoints de API directamente
10. **Limitación de Tasa**: Monitorear uso y escalar infraestructura según necesidad

---

**Versión**: 1.0  
**Última Actualización**: Enero 2026  
**Mantenido Por**: Administrador del Sistema
