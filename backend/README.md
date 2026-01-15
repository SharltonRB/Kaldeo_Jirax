# Personal Issue Tracker - Backend

## Description
Backend for the Personal Issue Tracker developed with Spring Boot 3.2.1 and Java 21.

## Quick Start

### First Time Setup

1. **Reset and initialize the database:**
   ```bash
   ./scripts/reset-dev-database.sh
   ```

2. **Start the application:**
   ```bash
   ./scripts/start-dev.sh
   ```

3. **Login with test credentials:**
   - Email: `john.doe@example.com`
   - Password: `password123`
   
   See `backend/DEVELOPMENT_CREDENTIALS.md` for all test users.

### Troubleshooting

#### Flyway Migration Errors

If you see errors like "Migrations have failed validation" or "Detected resolved migration not applied to database":

**Solution 1: Reset the database (recommended for development)**
```bash
./scripts/reset-dev-database.sh
./scripts/start-dev.sh
```

**Solution 2: Check migration status**
```bash
./scripts/check-flyway-status.sh
```

#### Authentication Errors

If you can't login with the test credentials:

1. Make sure you're using the correct password: `password123`
2. Reset the database to ensure correct password hashes:
   ```bash
   ./scripts/reset-dev-database.sh
   ```

## Development Credentials

All test users use the password: `password123`

- john.doe@example.com - Regular user with sample projects
- jane.smith@example.com - Regular user with analytics projects  
- admin@example.com - Admin user

See `DEVELOPMENT_CREDENTIALS.md` for complete details.

## Technologies
- **Framework**: Spring Boot 3.2.1
- **Java**: 21
- **Database**: PostgreSQL
- **Authentication**: JWT with BCrypt password hashing
- **Cache**: Redis
- **Migrations**: Flyway
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