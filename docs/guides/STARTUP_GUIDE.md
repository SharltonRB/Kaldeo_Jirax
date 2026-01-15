# 🚀 Guía de Inicio - Personal Issue Tracker

## 📋 Resumen del Problema y Solución

### ❌ Problema Original
El script `start-dev.sh` estaba buscando PostgreSQL instalado localmente en tu Mac (con Homebrew), pero la base de datos está configurada para correr en Docker. Esto causaba el error "PostgreSQL is not running".

### ✅ Solución Implementada
Ahora el script:
1. Verifica que Docker esté corriendo
2. Levanta automáticamente los contenedores de PostgreSQL y Redis
3. Espera a que la base de datos esté lista
4. Inicia el backend y frontend

## 🎯 Cómo Iniciar el Proyecto

### Opción 1: Inicio Automático (Recomendado)

```bash
# Desde la raíz del proyecto
./scripts/start-dev.sh
```

Este script hace TODO automáticamente:
- ✅ Verifica que Docker esté corriendo
- ✅ **Verifica si PostgreSQL ya está corriendo**
  - Si ya está corriendo: Lo usa (no lo reinicia)
  - Si no está corriendo: Lo levanta y espera a que esté listo
- ✅ **Verifica si Redis ya está corriendo**
  - Si ya está corriendo: Lo usa (no lo reinicia)
  - Si no está corriendo: Lo levanta
- ✅ Inicia el backend (Spring Boot)
- ✅ Inicia el frontend (Vite/React)

### Opción 2: Inicio Manual (Paso a Paso)

Si prefieres tener más control:

```bash
# 1. Levantar Docker containers
docker-compose up -d postgres redis

# 2. Verificar que PostgreSQL esté listo
docker exec issue-tracker-postgres pg_isready -U postgres

# 3. Iniciar backend (en una terminal)
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 4. Iniciar frontend (en otra terminal)
cd frontend
npm run dev
```

## 🛑 Cómo Detener el Proyecto

```bash
# Detiene todo: backend, frontend y Docker containers
./scripts/stop-dev.sh
```

## 🔍 Verificar Estado de los Servicios

```bash
# Ver el estado de todos los servicios
./scripts/check-services.sh
```

Este script te muestra:
- ✅ Si Docker está corriendo
- ✅ Si PostgreSQL está corriendo y aceptando conexiones
- ✅ Si Redis está corriendo
- ✅ Si el backend está corriendo
- ✅ Si el frontend está corriendo
- ✅ Qué puertos están en uso

## 📊 Arquitectura del Sistema

```
┌─────────────────────────────────────────────────────────┐
│                    Tu Computadora                        │
│                                                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │
│  │   Frontend   │  │   Backend    │  │    Docker    │ │
│  │   (Vite)     │  │ (Spring Boot)│  │              │ │
│  │              │  │              │  │  ┌────────┐  │ │
│  │ Port: 3000   │  │ Port: 8080   │  │  │Postgres│  │ │
│  │              │  │              │  │  │Port:   │  │ │
│  │              │  │              │  │  │5432    │  │ │
│  └──────┬───────┘  └──────┬───────┘  │  └────────┘  │ │
│         │                 │           │              │ │
│         │                 │           │  ┌────────┐  │ │
│         │                 └───────────┼─→│ Redis  │  │ │
│         │                             │  │Port:   │  │ │
│         └─────────────────────────────┼─→│6379    │  │ │
│                                       │  └────────┘  │ │
│                                       └──────────────┘ │
└─────────────────────────────────────────────────────────┘
```

## 🔧 Configuración de la Base de Datos

### Configuración en Docker (docker-compose.yml)
```yaml
postgres:
  image: postgres:15-alpine
  ports:
    - "5432:5432"  # Puerto expuesto a tu Mac
  environment:
    POSTGRES_DB: issue_tracker_dev
    POSTGRES_USER: postgres
    POSTGRES_PASSWORD: postgres
```

### Configuración del Backend (application-dev.yml)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/issue_tracker_dev
    username: postgres
    password: postgres
```

**Nota**: El backend se conecta a `localhost:5432` porque Docker expone ese puerto en tu Mac.

## 🐛 Solución de Problemas Comunes

### Problema 1: "Docker is not running"
**Solución**: Abre Docker Desktop y espera a que inicie completamente.

### Problema 2: "Port 5432 is already in use"
**Causa**: Tienes PostgreSQL instalado localmente con Homebrew corriendo.

**Solución**:
```bash
# Opción A: Detener PostgreSQL local
brew services stop postgresql

# Opción B: Cambiar el puerto de Docker
# Edita docker-compose.yml y cambia "5432:5432" a "5433:5432"
# Luego actualiza application-dev.yml para usar puerto 5433
```

### Problema 3: "Backend fails to start"
**Diagnóstico**:
```bash
# Ver logs del backend
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Ver logs de PostgreSQL
docker-compose logs postgres
```

**Soluciones comunes**:
- Verifica que PostgreSQL esté corriendo: `docker ps`
- Verifica que Flyway migrations hayan corrido: `docker-compose logs postgres`
- Limpia y reconstruye: `cd backend && mvn clean install`

### Problema 4: "Frontend fails to start"
**Solución**:
```bash
cd frontend
rm -rf node_modules package-lock.json
npm install
npm run dev
```

### Problema 5: "Database connection refused"
**Solución**:
```bash
# Reiniciar contenedor de PostgreSQL
docker-compose restart postgres

# Verificar que esté aceptando conexiones
docker exec issue-tracker-postgres pg_isready -U postgres
```

## 📝 Comandos Útiles de Docker

```bash
# Ver contenedores corriendo
docker-compose ps

# Ver logs de PostgreSQL
docker-compose logs -f postgres

# Ver logs de Redis
docker-compose logs -f redis

# Reiniciar un servicio
docker-compose restart postgres

# Detener todos los contenedores
docker-compose stop

# Detener y eliminar contenedores (mantiene datos)
docker-compose down

# Detener, eliminar contenedores Y eliminar datos
docker-compose down -v

# Entrar al contenedor de PostgreSQL
docker exec -it issue-tracker-postgres psql -U postgres -d issue_tracker_dev

# Ver tablas en la base de datos
docker exec -it issue-tracker-postgres psql -U postgres -d issue_tracker_dev -c "\dt"
```

## 🎯 URLs de Acceso

Una vez que todo esté corriendo:

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **Backend Health**: http://localhost:8080/actuator/health
- **PostgreSQL**: localhost:5432 (desde tu Mac)
- **Redis**: localhost:6379 (desde tu Mac)

## 👤 Credenciales de Prueba

```
Email: john.doe@example.com
Password: password123
```

## 🔄 Flujo de Inicio Completo

```
1. Usuario ejecuta: ./scripts/start-dev.sh
                    ↓
2. Script verifica: ¿Docker está corriendo?
                    ↓
3. Script ejecuta: docker-compose up -d postgres redis
                    ↓
4. Script espera: PostgreSQL esté listo (pg_isready)
                    ↓
5. Script inicia: Backend (mvn spring-boot:run)
                    ↓
6. Script espera: 15 segundos
                    ↓
7. Script inicia: Frontend (npm run dev)
                    ↓
8. ✅ Todo listo: http://localhost:3000
```

## 💡 Mejores Prácticas

1. **Siempre usa el script start-dev.sh** - Maneja todo automáticamente
2. **Verifica el estado** con `./scripts/check-services.sh` si algo falla
3. **Detén correctamente** con `./scripts/stop-dev.sh` para limpiar procesos
4. **Revisa los logs** si algo no funciona:
   - Backend: En la terminal donde corrió
   - PostgreSQL: `docker-compose logs postgres`
   - Frontend: En la terminal donde corrió

## 🆘 ¿Necesitas Ayuda?

Si algo no funciona:

1. Ejecuta: `./scripts/check-services.sh`
2. Revisa qué servicio está fallando
3. Consulta la sección "Solución de Problemas" arriba
4. Revisa los logs del servicio que falla

## 📚 Recursos Adicionales

- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Vite Documentation](https://vitejs.dev/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
