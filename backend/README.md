# Personal Issue Tracker - Backend

## Descripción
Backend del Personal Issue Tracker desarrollado con Spring Boot 3.2.1 y Java 21.

## Tecnologías
- **Framework**: Spring Boot 3.2.1
- **Java**: 21
- **Base de datos**: PostgreSQL
- **Autenticación**: JWT
- **Cache**: Redis
- **Testing**: JUnit 5, Testcontainers, QuickTheories

## Estructura del proyecto
```
backend/
├── src/main/java/com/issuetracker/
│   ├── config/          # Configuraciones
│   ├── controller/      # Controladores REST
│   ├── dto/            # Data Transfer Objects
│   ├── entity/         # Entidades JPA
│   ├── exception/      # Manejo de excepciones
│   ├── repository/     # Repositorios JPA
│   ├── security/       # Configuración de seguridad
│   ├── service/        # Lógica de negocio
│   └── util/           # Utilidades
├── src/main/resources/
│   ├── db/migration/   # Scripts de Flyway
│   └── application.yml # Configuración
└── src/test/           # Tests
```

## Comandos principales

### Desarrollo
```bash
# Ejecutar aplicación
mvn spring-boot:run

# Tests rápidos (sin property tests)
mvn test -Pfast-tests

# Property tests rápidos
mvn test -Pquick-property-tests

# Tests completos para CI
mvn test -Pci-tests
```

### Base de datos
```bash
# Migrar base de datos
mvn flyway:migrate

# Limpiar base de datos
mvn flyway:clean
```

## Configuración
Ver `src/main/resources/application.yml` para configuración de la aplicación.

## Documentación de API
La documentación de la API se encuentra en `/docs/api/`.