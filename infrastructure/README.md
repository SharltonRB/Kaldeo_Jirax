# Infrastructure

## Description
Infrastructure configuration for the Personal Issue Tracker.

## Structure
```
infrastructure/
├── docker/
│   ├── init-db.sql           # Database initialization script
│   ├── Dockerfile.backend    # Backend Dockerfile
│   └── Dockerfile.frontend   # Frontend Dockerfile
├── k8s/                      # Kubernetes manifests (future)
└── terraform/                # Infrastructure as Code (future)
```

## Docker

### Local Development
```bash
# Start services (from project root)
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

### Building Images
```bash
# Backend
docker build -f infrastructure/docker/Dockerfile.backend -t personal-issue-tracker-backend .

# Frontend
docker build -f infrastructure/docker/Dockerfile.frontend -t personal-issue-tracker-frontend .
```

## Services

### PostgreSQL
- **Port**: 5432
- **Database**: issuetracker
- **User**: postgres
- **Password**: postgres

### Redis
- **Port**: 6379

## Next Steps
- [ ] Create optimized Dockerfiles
- [ ] Configure Kubernetes manifests
- [ ] Implement Infrastructure as Code with Terraform