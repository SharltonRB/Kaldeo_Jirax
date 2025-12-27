# Personal Issue Tracker

A Jira-inspired personal project management application built with Spring Boot and React TypeScript.

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
mvn spring-boot:run
```

The application will be available at `http://localhost:8080/api`

### 3. Run Tests

```bash
# Run all tests
mvn test

# Run only unit tests
mvn test -Dtest="*Test"

# Run only integration tests
mvn test -Dtest="*IT"
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

The project includes comprehensive testing setup:

- **Unit Tests**: JUnit 5 for standard unit testing
- **Integration Tests**: Testcontainers with PostgreSQL for database integration
- **Property-Based Tests**: QuickTheories for property-based testing
- **Test Profiles**: Separate configuration for test environment

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