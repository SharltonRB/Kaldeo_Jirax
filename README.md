# Personal Issue Tracker

A Jira-inspired personal project management application built with Spring Boot and React TypeScript.

## 🚀 Quick Start

### Fast Development Build (Recommended)
```bash
# Build completo optimizado en ~8 segundos
./test-scripts.sh build

# O usando Maven directamente
mvn clean install -Pfast-tests
```

### Testing Options
```bash
./test-scripts.sh help           # Ver todas las opciones disponibles
./test-scripts.sh fast           # Tests rápidos sin property tests (8 seg)
./test-scripts.sh install        # Build rápido recomendado (8 seg)
./test-scripts.sh ci             # Tests completos para CI (2-3 min)
```

> **⚡ Optimización**: Los tests han sido optimizados para desarrollo rápido. Ver [README_TESTING.md](README_TESTING.md) para detalles completos de la optimización.

## Prerequisites

- Java 21
- Maven 3.8+
- Docker and Docker Compose
- Node.js 18+ (for frontend)

## Quick Start

### 1. Start Development Database

```bash
docker-compose up -d postgres redis
```

### 2. Run the Application

```bash
# Desarrollo rápido (recomendado)
./test-scripts.sh build && mvn spring-boot:run

# O método tradicional
mvn spring-boot:run
```

The application will be available at `http://localhost:8080/api`

### 3. Run Tests

```bash
# Tests rápidos para desarrollo diario (8 segundos)
./test-scripts.sh fast

# Build completo optimizado (8 segundos)
./test-scripts.sh install

# Tests completos para CI (2-3 minutos)
./test-scripts.sh ci

# Métodos tradicionales (más lentos)
mvn test                    # Todos los tests (~5+ minutos)
mvn test -Dtest="AuthenticationPropertyTest"  # Test específico
```

## Development Setup

### Database Setup

The application uses PostgreSQL for development and production. A Docker Compose file is provided for easy setup:

```bash
# Start all services (PostgreSQL + Redis)
docker-compose up -d

# Start only PostgreSQL
docker-compose up -d postgres

# View logs
docker-compose logs -f postgres

# Stop services
docker-compose down
```

### Environment Profiles

- `dev` - Development profile (default)
- `test` - Testing profile with H2 in-memory database
- `prod` - Production profile

### Testing Infrastructure

The project includes comprehensive testing setup with a hybrid approach:

#### Test Configuration
- **H2 Tests (Default)**: Fast in-memory database for rapid development feedback
- **Testcontainers Tests**: PostgreSQL containers for production parity (Linux/CI)
- **Property-Based Tests**: QuickTheories for comprehensive property validation

#### Test Profiles
- `test` - H2 in-memory database (fast, no Docker required)
- `testcontainers` - PostgreSQL via Testcontainers (production parity)

#### Running Tests

```bash
# 🚀 MÉTODOS OPTIMIZADOS (RECOMENDADOS)

# Tests rápidos para desarrollo (8 segundos)
./test-scripts.sh fast

# Build completo optimizado (8 segundos)  
./test-scripts.sh install

# Property tests rápidos (30 segundos)
./test-scripts.sh quick-property

# Tests completos para CI (2-3 minutos)
./test-scripts.sh ci

# 📝 MÉTODOS TRADICIONALES (MÁS LENTOS)

# Todos los tests con configuración original (~5+ minutos)
mvn test

# Tests específicos
mvn test -Dtest="AuditTrailPropertyTest"

# Testcontainers (PostgreSQL - Linux/CI)
mvn test -Dspring.profiles.active=testcontainers -Dtestcontainers.enabled=true
```

> **💡 Tip**: Usa `./test-scripts.sh install` para desarrollo diario. Es 40x más rápido que `mvn clean install` tradicional.

#### Known Issues
- Testcontainers may have connectivity issues on macOS with Docker Desktop
- See `TESTCONTAINERS_TROUBLESHOOTING.md` for details and workarounds
- H2 tests provide excellent coverage for daily development

### Project Structure

```
src/
├── main/java/com/issuetracker/
│   ├── config/          # Configuration classes
│   ├── controller/      # REST API controllers
│   ├── service/         # Business service layer
│   ├── repository/      # Data access layer
│   ├── entity/          # JPA entities
│   ├── dto/             # Data Transfer Objects
│   └── exception/       # Custom exceptions
├── main/resources/
│   ├── application*.yml # Configuration files
│   └── db/migration/    # Flyway database migrations
└── test/java/com/issuetracker/
    ├── base/            # Base test classes
    ├── config/          # Test configuration
    └── testcontainers/  # Testcontainer utilities
```

## Configuration

### Environment Variables

For production deployment, set the following environment variables:

```bash
DATABASE_URL=jdbc:postgresql://localhost:5432/issue_tracker_prod
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password
JWT_SECRET=your_jwt_secret_key
REDIS_HOST=localhost
REDIS_PORT=6379
```

### JWT Configuration

The application uses JWT for authentication. Configure the following properties:

- `jwt.secret`: Secret key for JWT signing (use environment variable in production)
- `jwt.expiration`: Token expiration time in milliseconds
- `jwt.refresh-expiration`: Refresh token expiration time in milliseconds

## API Documentation

Once the application is running, API documentation is available at:
- Swagger UI: `http://localhost:8080/api/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api/v3/api-docs`

## Health Checks

Health check endpoints are available at:
- Application health: `http://localhost:8080/api/actuator/health`
- Application info: `http://localhost:8080/api/actuator/info`
- Metrics: `http://localhost:8080/api/actuator/metrics`