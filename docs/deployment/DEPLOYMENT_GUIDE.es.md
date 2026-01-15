# Guía de Despliegue - Personal Issue Tracker

> **[English Version](DEPLOYMENT_GUIDE.md)** | Versión en Español

Esta guía cubre el proceso completo de despliegue para la aplicación Personal Issue Tracker, incluyendo configuración de pipeline CI/CD, despliegue manual y procedimientos de rollback.

## Tabla de Contenidos

1. [Requisitos Previos](#requisitos-previos)
2. [Configuración del Pipeline CI/CD](#configuración-del-pipeline-cicd)
3. [Despliegue Manual](#despliegue-manual)
4. [Migraciones de Base de Datos](#migraciones-de-base-de-datos)
5. [Procedimientos de Rollback](#procedimientos-de-rollback)
6. [Monitoreo y Verificaciones de Salud](#monitoreo-y-verificaciones-de-salud)
7. [Solución de Problemas](#solución-de-problemas)

## Requisitos Previos

### Software Requerido

- Docker 24.0+ y Docker Compose 2.0+
- Git 2.30+
- Maven 3.9+ (para compilaciones del backend)
- Node.js 20+ y npm (para compilaciones del frontend)
- PostgreSQL 15+ (para base de datos)

### Credenciales Requeridas

- Credenciales de base de datos (usuario, contraseña)
- Clave secreta JWT (mínimo 32 caracteres)
- Contraseña de Redis (opcional pero recomendado)
- Certificados SSL (para HTTPS)
- Credenciales del registro de contenedores (para imágenes Docker)

### Configuración del Entorno

1. Clonar el repositorio:
```bash
git clone <repository-url>
cd personal-issue-tracker
```

2. Crear archivo de entorno de producción:
```bash
cp .env.prod.example .env.prod
```

3. Actualizar `.env.prod` con sus valores de producción:
```bash
# Configuración de Base de Datos
DATABASE_URL=jdbc:postgresql://postgres:5432/issue_tracker
DB_USERNAME=postgres
DB_PASSWORD=<su-contraseña-segura>
POSTGRES_DB=issue_tracker
POSTGRES_USER=postgres
POSTGRES_PASSWORD=<su-contraseña-segura>

# Configuración JWT
JWT_SECRET=<su-secreto-jwt-seguro-codificado-en-base64>
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000

# Configuración de Aplicación
CORS_ALLOWED_ORIGINS=https://sudominio.com
BACKEND_PORT=8080
FRONTEND_PORT=80

# Configuración Redis (opcional)
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=<su-contraseña-redis>

# Monitoreo
PROMETHEUS_ENABLED=true
LOG_LEVEL=INFO
```

## Configuración del Pipeline CI/CD

### GitHub Actions

El proyecto incluye un pipeline CI/CD completo usando GitHub Actions (`.github/workflows/ci-cd.yml`).

#### Etapas del Pipeline

1. **Compilación y Pruebas del Backend**
   - Compila aplicación Java con Maven
   - Ejecuta pruebas unitarias y pruebas basadas en propiedades
   - Sube artefactos de compilación

2. **Compilación y Pruebas del Frontend**
   - Compila aplicación React con Vite
   - Ejecuta linter y pruebas
   - Sube artefactos de compilación

3. **Escaneo de Seguridad**
   - Ejecuta escáner de vulnerabilidades Trivy
   - Sube resultados a GitHub Security

4. **Compilación Docker**
   - Compila y sube imágenes Docker al registro
   - Etiqueta imágenes con nombre de rama y SHA del commit

5. **Despliegue a Staging** (rama develop)
   - Despliega a entorno de staging
   - Ejecuta verificaciones de salud

6. **Despliegue a Producción** (rama main)
   - Crea respaldo antes del despliegue
   - Despliega a entorno de producción
   - Ejecuta pruebas de humo
   - Rollback automático en caso de fallo

#### Secretos Requeridos de GitHub

Configure estos secretos en la configuración de su repositorio GitHub:

```
# Entorno Staging
STAGING_HOST=staging.example.com
STAGING_USER=deploy
STAGING_SSH_KEY=<clave-ssh-privada>

# Entorno Producción
PROD_HOST=app.example.com
PROD_USER=deploy
PROD_SSH_KEY=<clave-ssh-privada>

# Registro de Contenedores
GITHUB_TOKEN=<proporcionado-automáticamente>
```

#### Activar Despliegues

- **Automático**: Push a `main` (producción) o `develop` (staging)
- **Manual**: Usar botón "Run workflow" de GitHub Actions

## Despliegue Manual

### Despliegue Completo a Producción

Use el script de despliegue automatizado:

```bash
./scripts/deploy-production.sh
```

Este script:
1. Verifica requisitos previos
2. Valida configuración del entorno
3. Crea respaldo
4. Compila aplicación
5. Despliega con Docker Compose
6. Ejecuta verificaciones de salud
7. Muestra estado del despliegue

### Despliegue Manual Paso a Paso

Si necesita más control, siga estos pasos:

#### 1. Compilar Backend

```bash
cd backend
./mvnw clean package -DskipTests
cd ..
```

#### 2. Compilar Frontend

```bash
cd frontend
npm ci
npm run build
cd ..
```

#### 3. Compilar Imágenes Docker

```bash
docker-compose -f docker-compose.prod.yml build
```

#### 4. Iniciar Servicios

```bash
docker-compose -f docker-compose.prod.yml --env-file .env.prod up -d
```

#### 5. Verificar Despliegue

```bash
# Verificar estado de servicios
docker-compose -f docker-compose.prod.yml ps

# Verificar salud del backend
curl http://localhost:8080/actuator/health

# Ver logs
docker-compose -f docker-compose.prod.yml logs -f
```

## Migraciones de Base de Datos

### Ejecutar Migraciones

Use el script de migración para actualizaciones seguras de base de datos:

```bash
./scripts/migrate-database.sh migrate
```

Esto:
1. Verifica conectividad de base de datos
2. Muestra estado actual de migración
3. Valida scripts de migración
4. Crea respaldo
5. Ejecuta migraciones pendientes
6. Muestra estado actualizado

### Comandos de Migración

```bash
# Mostrar estado de migración
./scripts/migrate-database.sh status

# Validar migraciones
./scripts/migrate-database.sh validate

# Reparar historial de migración
./scripts/migrate-database.sh repair

# Establecer línea base en base de datos existente
./scripts/migrate-database.sh baseline
```

## Procedimientos de Rollback

### Rollback Automatizado

Use el script de rollback para restaurar desde un respaldo anterior:

```bash
./scripts/rollback.sh
```

Esto:
1. Lista respaldos disponibles
2. Le permite seleccionar un respaldo para restaurar
3. Crea respaldo pre-rollback
4. Detiene aplicación
5. Restaura base de datos
6. Restaura imágenes Docker (si están disponibles)
7. Inicia aplicación
8. Verifica rollback

### Rollback Manual

#### 1. Listar Respaldos Disponibles

```bash
./scripts/rollback.sh list
```

#### 2. Detener Aplicación

```bash
docker-compose -f docker-compose.prod.yml down
```

#### 3. Restaurar Base de Datos

```bash
# Iniciar solo base de datos
docker-compose -f docker-compose.prod.yml up -d postgres

# Restaurar desde respaldo
cat backups/backup_YYYYMMDD_HHMMSS_database.sql | \
  docker exec -i issue-tracker-postgres-prod \
  psql -U postgres -d issue_tracker
```

#### 4. Iniciar Aplicación

```bash
docker-compose -f docker-compose.prod.yml up -d
```

## Monitoreo y Verificaciones de Salud

### Endpoints de Verificación de Salud

- **Salud del Backend**: `http://localhost:8080/actuator/health`
- **Métricas del Backend**: `http://localhost:8080/actuator/metrics`
- **Salud de Base de Datos**: Verificar vía estado de salud de Docker

### Verificar Estado de Servicios

```bash
# Todos los servicios
docker-compose -f docker-compose.prod.yml ps

# Salud del backend
curl http://localhost:8080/actuator/health | jq

# Conectividad de base de datos
docker exec issue-tracker-postgres-prod pg_isready -U postgres

# Conectividad de Redis
docker exec issue-tracker-redis-prod redis-cli ping
```

### Ver Logs

```bash
# Todos los servicios
docker-compose -f docker-compose.prod.yml logs -f

# Servicio específico
docker-compose -f docker-compose.prod.yml logs -f backend

# Últimas 100 líneas
docker-compose -f docker-compose.prod.yml logs --tail=100 backend
```

## Solución de Problemas

### Problemas Comunes

#### 1. Fallo de Conexión a Base de Datos

**Síntomas**: Backend falla al iniciar, errores de conexión en logs

**Soluciones**:
```bash
# Verificar que base de datos esté ejecutándose
docker-compose -f docker-compose.prod.yml ps postgres

# Verificar logs de base de datos
docker-compose -f docker-compose.prod.yml logs postgres

# Verificar credenciales
docker exec issue-tracker-postgres-prod \
  psql -U postgres -d issue_tracker -c "SELECT 1"

# Reiniciar base de datos
docker-compose -f docker-compose.prod.yml restart postgres
```

#### 2. Migración Fallida

**Síntomas**: Errores de migración Flyway, conflictos de versión de esquema

**Soluciones**:
```bash
# Verificar estado de migración
./scripts/migrate-database.sh status

# Reparar historial de migración
./scripts/migrate-database.sh repair

# Si es necesario, restaurar desde respaldo
./scripts/rollback.sh
```

#### 3. Falta de Memoria

**Síntomas**: Backend se cae, errores OOM en logs

**Soluciones**:
```bash
# Verificar uso de memoria
docker stats

# Aumentar límites de memoria en docker-compose.prod.yml
# Bajo servicio backend:
deploy:
  resources:
    limits:
      memory: 4G  # Aumentar desde 2G

# Reiniciar con nuevos límites
docker-compose -f docker-compose.prod.yml up -d backend
```

## Mejores Prácticas

### Antes del Despliegue

1. ✅ Probar en entorno de staging
2. ✅ Revisar todos los cambios pendientes
3. ✅ Respaldar datos de producción actuales
4. ✅ Verificar todas las variables de entorno
5. ✅ Verificar espacio en disco y recursos
6. ✅ Notificar al equipo sobre ventana de despliegue

### Durante el Despliegue

1. ✅ Monitorear logs en tiempo real
2. ✅ Vigilar endpoints de verificación de salud
3. ✅ Verificar migraciones de base de datos
4. ✅ Probar flujos críticos de usuario
5. ✅ Verificar métricas de rendimiento

### Después del Despliegue

1. ✅ Verificar que todos los servicios estén saludables
2. ✅ Ejecutar pruebas de humo
3. ✅ Monitorear tasas de error
4. ✅ Verificar logs de aplicación
5. ✅ Verificar que se creó el respaldo
6. ✅ Documentar cualquier problema
7. ✅ Notificar al equipo sobre finalización

### Lista de Verificación de Seguridad

- [ ] Todas las contraseñas predeterminadas cambiadas
- [ ] Secreto JWT es fuerte y único
- [ ] Certificados SSL/TLS son válidos
- [ ] Base de datos no es accesible públicamente
- [ ] Limitación de tasa está habilitada
- [ ] CORS está configurado correctamente
- [ ] Encabezados de seguridad están establecidos
- [ ] Logs no contienen datos sensibles
- [ ] Respaldos están encriptados
- [ ] Logs de acceso son monitoreados

## Recursos Adicionales

- [Guía de Mantenimiento](../operations/MAINTENANCE_GUIDE.es.md)
- [Guía de Monitoreo](../operations/MONITORING_GUIDE.es.md)
- [Lista de Verificación de Seguridad](../security/SECURITY_CHECKLIST.es.md)
- [Estrategia de Pruebas](../testing/TESTING_STRATEGY.md)

---

**Versión**: 1.0.0  
**Última Actualización**: 14 de enero de 2026
