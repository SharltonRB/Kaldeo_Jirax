# Personal Issue Tracker - Backend

## Description
Backend for the Personal Issue Tracker developed with Spring Boot 3.2.1 and Java 21.

## Technologies
- **Framework**: Spring Boot 3.2.1
- **Java**: 21
- **Database**: PostgreSQL
- **Authentication**: JWT
- **Cache**: Redis
- **Testing**: JUnit 5, Testcontainers, QuickTheories

## Project Structure
```
backend/
├── src/main/java/com/issuetracker/
│   ├── config/          # Configurations
│   ├── controller/      # REST Controllers
│   ├── dto/            # Data Transfer Objects
│   ├── entity/         # JPA Entities
│   ├── exception/      # Exception Handling
│   ├── repository/     # JPA Repositories
│   ├── security/       # Security Configuration
│   ├── service/        # Business Logic
│   └── util/           # Utilities
├── src/main/resources/
│   ├── db/migration/   # Flyway Scripts
│   └── application.yml # Configuration
└── src/test/           # Tests
```

## Main Commands

### Development
```bash
# Run application
mvn spring-boot:run

# Fast tests (without property tests)
mvn test -Pfast-tests

# Quick property tests
mvn test -Pquick-property-tests

# Complete tests for CI
mvn test -Pci-tests
```

### Database
```bash
# Migrate database
mvn flyway:migrate

# Clean database
mvn flyway:clean
```

## Configuration
See `src/main/resources/application.yml` for application configuration.

## API Documentation
API documentation is available at `/docs/api/`.