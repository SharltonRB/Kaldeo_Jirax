# Documentación de Arquitectura

> **[English Version](README.md)** | Versión en Español

## Descripción General del Sistema

El Personal Issue Tracker es una aplicación web full-stack que sigue una arquitectura moderna inspirada en microservicios con clara separación de responsabilidades.

## Arquitectura de Alto Nivel

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│                 │    │                 │    │                 │
│    Frontend     │◄──►│     Backend     │◄──►│  Base de Datos  │
│  (React + TS)   │    │  (Spring Boot)  │    │  (PostgreSQL)   │
│                 │    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                                ▼
                       ┌─────────────────┐
                       │                 │
                       │      Caché      │
                       │     (Redis)     │
                       │                 │
                       └─────────────────┘
```

## Stack Tecnológico

### Frontend
- **Framework**: React 18 con TypeScript
- **Herramienta de Build**: Vite
- **Estilos**: Tailwind CSS
- **Gestión de Estado**: React Query (TanStack Query)
- **Formularios**: React Hook Form + validación Zod
- **Enrutamiento**: React Router DOM
- **Pruebas**: Vitest + Testing Library

### Backend
- **Framework**: Spring Boot 3.2.1
- **Lenguaje**: Java 21
- **Base de Datos**: PostgreSQL 15
- **Caché**: Redis 7
- **Autenticación**: JWT con Spring Security
- **Documentación API**: OpenAPI 3 (Swagger)
- **Pruebas**: JUnit 5, Testcontainers, QuickTheories

### Infraestructura
- **Contenedorización**: Docker + Docker Compose
- **Migraciones de Base de Datos**: Flyway
- **Monitoreo**: Spring Boot Actuator
- **Logging**: Logback con logging estructurado

## Patrones de Arquitectura

### Patrones del Backend

#### Arquitectura en Capas
```
┌─────────────────────────────────────┐
│         Controladores               │ ← Capa API REST
├─────────────────────────────────────┤
│           Servicios                 │ ← Capa de Lógica de Negocio
├─────────────────────────────────────┤
│         Repositorios                │ ← Capa de Acceso a Datos
├─────────────────────────────────────┤
│          Entidades                  │ ← Capa de Modelo de Dominio
└─────────────────────────────────────┘
```

#### Patrones Clave Utilizados
- **Patrón Repository**: Abstracción de acceso a datos
- **Patrón DTO**: Transferencia de datos entre capas
- **Patrón Service Layer**: Encapsulación de lógica de negocio
- **Inyección de Dependencias**: Bajo acoplamiento vía Spring IoC
- **Manejo de Excepciones**: Manejo global de excepciones con @ControllerAdvice

### Patrones del Frontend

#### Arquitectura de Componentes
```
┌─────────────────────────────────────┐
│             Páginas                 │ ← Componentes de Ruta
├─────────────────────────────────────┤
│          Componentes                │ ← Componentes UI Reutilizables
├─────────────────────────────────────┤
│             Hooks                   │ ← Custom React Hooks
├─────────────────────────────────────┤
│           Servicios                 │ ← Comunicación API
├─────────────────────────────────────┤
│            Utils                    │ ← Utilidades y Helpers
└─────────────────────────────────────┘
```

#### Patrones Clave Utilizados
- **Custom Hooks**: Reutilización de lógica y gestión de estado
- **Compound Components**: Composición de componentes UI complejos
- **Render Props**: Compartir lógica de componentes
- **Higher-Order Components**: Preocupaciones transversales
- **Context API**: Gestión de estado global

## Arquitectura de Seguridad

### Flujo de Autenticación
```
1. Login de Usuario → Backend valida credenciales
2. Backend genera token JWT
3. Frontend almacena token de forma segura
4. Solicitudes subsecuentes incluyen JWT en header Authorization
5. Backend valida JWT en cada solicitud
```

### Medidas de Seguridad
- **Autenticación JWT**: Autenticación sin estado
- **Configuración CORS**: Manejo de solicitudes cross-origin
- **Validación de Entrada**: Validación de solicitudes con Bean Validation
- **Prevención de Inyección SQL**: Consultas parametrizadas JPA/Hibernate
- **Prevención XSS**: Headers de Content Security Policy
- **Limitación de Tasa**: Bucket4j para limitación de tasa de API

## Arquitectura de Datos

### Diseño de Base de Datos
- **Base de Datos Principal**: PostgreSQL para cumplimiento ACID
- **Capa de Caché**: Redis para almacenamiento de sesiones y caché
- **Estrategia de Migración**: Flyway para cambios de esquema versionados

### Flujo de Datos
```
Frontend → API → Capa de Servicio → Repositorio → Base de Datos
    ↑                                                  ↓
    └──────────────── Respuesta ←─────────────────────┘
```

## Arquitectura de Despliegue

### Entorno de Desarrollo
```
Docker Compose
├── PostgreSQL (puerto 5432)
├── Redis (puerto 6379)
├── Backend (puerto 8080)
└── Frontend (puerto 5173)
```

### Consideraciones de Producción
- **Orquestación de Contenedores**: Listo para Kubernetes
- **Balanceo de Carga**: Proxy inverso Nginx
- **Base de Datos**: Servicio PostgreSQL administrado
- **Caché**: Servicio Redis administrado
- **Monitoreo**: Métricas de aplicación y verificaciones de salud

## Atributos de Calidad

### Rendimiento
- **Estrategia de Caché**: Redis para datos accedidos frecuentemente
- **Optimización de Base de Datos**: Indexación apropiada y optimización de consultas
- **Optimización de Frontend**: Code splitting y lazy loading

### Escalabilidad
- **Diseño Sin Estado**: Capacidad de escalado horizontal
- **Pool de Conexiones de Base de Datos**: Utilización eficiente de recursos
- **Caché**: Carga reducida de base de datos

### Mantenibilidad
- **Arquitectura Limpia**: Clara separación de responsabilidades
- **Pruebas Completas**: Pruebas unitarias, de integración y basadas en propiedades
- **Documentación**: Documentación de código inline y docs arquitecturales

### Confiabilidad
- **Manejo de Errores**: Manejo y recuperación de errores elegante
- **Verificaciones de Salud**: Monitoreo de salud de aplicación y dependencias
- **Logging**: Logging estructurado para depuración y monitoreo

## Recursos Adicionales

- [Guía de Despliegue](../deployment/DEPLOYMENT_GUIDE.es.md)
- [Guía de Mantenimiento](../operations/MAINTENANCE_GUIDE.es.md)
- [Guía de Monitoreo](../operations/MONITORING_GUIDE.es.md)
- [Lista de Verificación de Seguridad](../security/SECURITY_CHECKLIST.es.md)

---

**Versión**: 1.0.0  
**Última Actualización**: 14 de enero de 2026
