# Architecture Documentation

## System Overview

The Personal Issue Tracker is a full-stack web application following a modern microservices-inspired architecture with clear separation of concerns.

## High-Level Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│                 │    │                 │    │                 │
│    Frontend     │◄──►│     Backend     │◄──►│    Database     │
│  (React + TS)   │    │  (Spring Boot)  │    │  (PostgreSQL)   │
│                 │    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                                ▼
                       ┌─────────────────┐
                       │                 │
                       │      Cache      │
                       │     (Redis)     │
                       │                 │
                       └─────────────────┘
```

## Technology Stack

### Frontend
- **Framework**: React 18 with TypeScript
- **Build Tool**: Vite
- **Styling**: Tailwind CSS
- **State Management**: React Query (TanStack Query)
- **Forms**: React Hook Form + Zod validation
- **Routing**: React Router DOM
- **Testing**: Vitest + Testing Library

### Backend
- **Framework**: Spring Boot 3.2.1
- **Language**: Java 21
- **Database**: PostgreSQL 15
- **Cache**: Redis 7
- **Authentication**: JWT with Spring Security
- **API Documentation**: OpenAPI 3 (Swagger)
- **Testing**: JUnit 5, Testcontainers, QuickTheories

### Infrastructure
- **Containerization**: Docker + Docker Compose
- **Database Migrations**: Flyway
- **Monitoring**: Spring Boot Actuator
- **Logging**: Logback with structured logging

## Architecture Patterns

### Backend Patterns

#### Layered Architecture
```
┌─────────────────────────────────────┐
│           Controllers               │ ← REST API Layer
├─────────────────────────────────────┤
│            Services                 │ ← Business Logic Layer
├─────────────────────────────────────┤
│          Repositories               │ ← Data Access Layer
├─────────────────────────────────────┤
│            Entities                 │ ← Domain Model Layer
└─────────────────────────────────────┘
```

#### Key Patterns Used
- **Repository Pattern**: Data access abstraction
- **DTO Pattern**: Data transfer between layers
- **Service Layer Pattern**: Business logic encapsulation
- **Dependency Injection**: Loose coupling via Spring IoC
- **Exception Handling**: Global exception handling with @ControllerAdvice

### Frontend Patterns

#### Component Architecture
```
┌─────────────────────────────────────┐
│              Pages                  │ ← Route Components
├─────────────────────────────────────┤
│            Components               │ ← Reusable UI Components
├─────────────────────────────────────┤
│             Hooks                   │ ← Custom React Hooks
├─────────────────────────────────────┤
│            Services                 │ ← API Communication
├─────────────────────────────────────┤
│             Utils                   │ ← Utilities & Helpers
└─────────────────────────────────────┘
```

#### Key Patterns Used
- **Custom Hooks**: Logic reuse and state management
- **Compound Components**: Complex UI component composition
- **Render Props**: Component logic sharing
- **Higher-Order Components**: Cross-cutting concerns
- **Context API**: Global state management

## Security Architecture

### Authentication Flow
```
1. User Login → Backend validates credentials
2. Backend generates JWT token
3. Frontend stores token securely
4. Subsequent requests include JWT in Authorization header
5. Backend validates JWT on each request
```

### Security Measures
- **JWT Authentication**: Stateless authentication
- **CORS Configuration**: Cross-origin request handling
- **Input Validation**: Request validation with Bean Validation
- **SQL Injection Prevention**: JPA/Hibernate parameterized queries
- **XSS Prevention**: Content Security Policy headers
- **Rate Limiting**: Bucket4j for API rate limiting

## Data Architecture

### Database Design
- **Primary Database**: PostgreSQL for ACID compliance
- **Cache Layer**: Redis for session storage and caching
- **Migration Strategy**: Flyway for version-controlled schema changes

### Data Flow
```
Frontend → API → Service Layer → Repository → Database
    ↑                                            ↓
    └─────────── Response ←─────────────────────┘
```

## Deployment Architecture

### Development Environment
```
Docker Compose
├── PostgreSQL (port 5432)
├── Redis (port 6379)
├── Backend (port 8080)
└── Frontend (port 5173)
```

### Production Considerations
- **Container Orchestration**: Kubernetes ready
- **Load Balancing**: Nginx reverse proxy
- **Database**: Managed PostgreSQL service
- **Cache**: Managed Redis service
- **Monitoring**: Application metrics and health checks

## Quality Attributes

### Performance
- **Caching Strategy**: Redis for frequently accessed data
- **Database Optimization**: Proper indexing and query optimization
- **Frontend Optimization**: Code splitting and lazy loading

### Scalability
- **Stateless Design**: Horizontal scaling capability
- **Database Connection Pooling**: Efficient resource utilization
- **Caching**: Reduced database load

### Maintainability
- **Clean Architecture**: Clear separation of concerns
- **Comprehensive Testing**: Unit, integration, and property-based tests
- **Documentation**: Inline code documentation and architectural docs

### Reliability
- **Error Handling**: Graceful error handling and recovery
- **Health Checks**: Application and dependency health monitoring
- **Logging**: Structured logging for debugging and monitoring

## Future Enhancements

### Planned Improvements
- [ ] Event-driven architecture with message queues
- [ ] Microservices decomposition
- [ ] API versioning strategy
- [ ] Advanced caching strategies
- [ ] Real-time notifications with WebSockets
- [ ] Audit logging and compliance features

## Language Versions

- **English**: [README.en.md](README.en.md)
- **Español**: [README.md](README.md)