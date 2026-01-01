# Personal Issue Tracker

Una aplicación de gestión de proyectos personales inspirada en Jira, construida con Spring Boot y React TypeScript.

## 🏗️ Estructura del Proyecto

```
personal-issue-tracker/
├── backend/                      # API Spring Boot
├── frontend/                     # SPA React TypeScript
├── infrastructure/               # Docker, K8s, Terraform
├── docs/                        # Documentación
├── scripts/                     # Scripts de automatización
├── logs/                        # Logs de aplicación
├── docker-compose.yml           # Servicios de desarrollo
└── README.md                    # Este archivo
```

## 🚀 Inicio Rápido

### Prerrequisitos
- Java 21
- Maven 3.8+
- Node.js 18+
- Docker y Docker Compose

### 1. Configurar Entorno de Desarrollo

```bash
# Configurar backend
./scripts/setup/setup-backend.sh

# Configurar frontend
./scripts/setup/setup-frontend.sh
```

### 2. Iniciar Servicios

```bash
# Iniciar servicios de base de datos y cache
docker-compose up -d

# Iniciar backend (en otra terminal)
cd backend && mvn spring-boot:run

# Iniciar frontend (en otra terminal)
cd frontend && npm run dev
```

### 3. Acceder a las Aplicaciones

- **Frontend**: http://localhost:5173
- **API Backend**: http://localhost:8080/api
- **Documentación API**: http://localhost:8080/api/swagger-ui.html

## 🛠️ Desarrollo

### Scripts de Construcción

```bash
# Construir todo
./scripts/build.sh all

# Construir componente específico
./scripts/build.sh backend
./scripts/build.sh frontend

# Construcción limpia con tests
./scripts/build.sh all --clean --test
```

### Testing

```bash
# Tests rápidos de desarrollo
./scripts/test-scripts.sh fast

# Suite completa de tests
./scripts/test-scripts.sh ci

# Solo tests del backend
cd backend && mvn test -Pfast-tests

# Solo tests del frontend
cd frontend && npm run test:run
```

## 📁 Documentación de Módulos

Cada módulo tiene su propia documentación detallada:

- **[Backend](backend/README.md)** - Documentación de la API Spring Boot
- **[Frontend](frontend/README.md)** - Documentación de la SPA React TypeScript
- **[Infraestructura](infrastructure/README.md)** - Configuración de Docker y despliegue
- **[Documentación](docs/README.md)** - Arquitectura, documentos de API, guías
- **[Scripts](scripts/README.md)** - Scripts de automatización y construcción

> 💡 **Consejo**: Haz clic en cualquier carpeta en GitHub para ver su README específico con información detallada sobre ese módulo.

## 🐳 Docker

### Desarrollo
```bash
# Iniciar todos los servicios
docker-compose up -d

# Ver logs
docker-compose logs -f

# Detener servicios
docker-compose down
```

### Construcción para Producción
```bash
# Construir imagen del backend
docker build -f infrastructure/docker/Dockerfile.backend -t personal-issue-tracker-backend .

# Construir imagen del frontend
docker build -f infrastructure/docker/Dockerfile.frontend -t personal-issue-tracker-frontend .
```

## 📚 Documentación

La documentación completa está disponible en el directorio `docs/`:

- **[Documentación de API](docs/api/)** - Endpoints de la API REST
- **[Arquitectura](docs/architecture/)** - Diseño del sistema y patrones
- **[Guía de Desarrollo](docs/development/)** - Flujos de trabajo de desarrollo
- **[Estrategia de Testing](docs/testing/)** - Enfoques y herramientas de testing
- **[Guía de Despliegue](docs/deployment/)** - Despliegue en producción

## 🔧 Configuración

### Variables de Entorno

```bash
# Backend
DATABASE_URL=jdbc:postgresql://localhost:5432/issue_tracker_dev
DB_USERNAME=postgres
DB_PASSWORD=postgres
JWT_SECRET=your_jwt_secret_key
REDIS_HOST=localhost
REDIS_PORT=6379

# Frontend
VITE_API_BASE_URL=http://localhost:8080/api
```

## 🚀 Despliegue

Ver [documentación de despliegue](docs/deployment/) para guías detalladas de despliegue en diferentes entornos.

## 🤝 Contribuir

1. Seguir la estructura y convenciones del proyecto
2. Actualizar documentación al hacer cambios
3. Ejecutar tests antes de enviar cambios
4. Usar los scripts proporcionados para consistencia

## 📄 Licencia

Este proyecto es para uso educativo y personal.

---

## 🌐 Versiones de Idioma

- **English**: [README.en.md](README.en.md)
- **Español**: [README.md](README.md)