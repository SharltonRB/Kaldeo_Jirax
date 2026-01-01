# Infrastructure

## Descripción
Configuración de infraestructura para el Personal Issue Tracker.

## Estructura
```
infrastructure/
├── docker/
│   ├── init-db.sql           # Script de inicialización de BD
│   ├── Dockerfile.backend    # Dockerfile para backend
│   └── Dockerfile.frontend   # Dockerfile para frontend
├── k8s/                      # Manifiestos de Kubernetes (futuro)
└── terraform/                # Infrastructure as Code (futuro)
```

## Docker

### Desarrollo Local
```bash
# Levantar servicios (desde la raíz del proyecto)
docker-compose up -d

# Ver logs
docker-compose logs -f

# Parar servicios
docker-compose down
```

### Construcción de Imágenes
```bash
# Backend
docker build -f infrastructure/docker/Dockerfile.backend -t personal-issue-tracker-backend .

# Frontend
docker build -f infrastructure/docker/Dockerfile.frontend -t personal-issue-tracker-frontend .
```

## Servicios

### PostgreSQL
- **Puerto**: 5432
- **Base de datos**: issuetracker
- **Usuario**: postgres
- **Password**: postgres

### Redis
- **Puerto**: 6379

## Próximos Pasos
- [ ] Crear Dockerfiles optimizados
- [ ] Configurar manifiestos de Kubernetes
- [ ] Implementar Infrastructure as Code con Terraform