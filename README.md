# Personal Issue Tracker

A Jira-inspired personal project management application built with Spring Boot and React TypeScript.

## 🏗️ Project Structure

```
personal-issue-tracker/
├── backend/                      # Spring Boot API
├── frontend/                     # React TypeScript SPA
├── infrastructure/               # Docker, K8s, Terraform
├── docs/                        # 📚 All Documentation (organized)
│   ├── architecture/            # System architecture docs
│   ├── development/             # Development guides
│   ├── fixes/                   # Bug fixes documentation
│   ├── improvements/            # Feature improvements
│   ├── security/                # Security documentation
│   ├── testing/                 # Testing guides and scripts
│   └── INDEX.md                 # Documentation index
├── scripts/                     # Automation scripts
├── logs/                        # Application logs
├── docker-compose.yml           # Development services
└── README.md                    # This file
```

## 📚 Documentation

All project documentation is organized in the [`docs/`](docs/) directory:

- **[Documentation Index](docs/INDEX.md)** - Complete documentation overview
- **[Development Guide](docs/DEVELOPMENT.md)** - Setup and development instructions
- **[Architecture](docs/architecture/)** - System design and architecture
- **[Security](docs/SECURITY.md)** - Security guidelines and practices
- **[Testing](docs/testing/)** - Testing strategies and scripts

## 🚀 Quick Start

### Prerequisites
- Java 21
- Maven 3.8+
- Node.js 18+
- Docker and Docker Compose

### 1. Setup Development Environment

```bash
# Setup backend
./scripts/setup/setup-backend.sh

# Setup frontend
./scripts/setup/setup-frontend.sh
```

### 2. Start Services

```bash
# Start database and cache services
docker-compose up -d

# Start backend (in another terminal)
cd backend && mvn spring-boot:run

# Start frontend (in another terminal)
cd frontend && npm run dev
```

### 3. Access Applications

- **Frontend**: http://localhost:5173
- **Backend API**: http://localhost:8080/api
- **API Documentation**: http://localhost:8080/api/swagger-ui.html

## 🛠️ Development

### Build Scripts

```bash
# Build everything
./scripts/build.sh all

# Build specific component
./scripts/build.sh backend
./scripts/build.sh frontend

# Clean build with tests
./scripts/build.sh all --clean --test
```

### Testing

```bash
# Fast development tests
./scripts/test-scripts.sh fast

# Complete test suite
./scripts/test-scripts.sh ci

# Backend tests only
cd backend && mvn test -Pfast-tests

# Frontend tests only
cd frontend && npm run test:run
```

## 📁 Module Documentation

Each module has its own detailed documentation:

- **[Backend](backend/README.en.md)** - Spring Boot API documentation
- **[Frontend](frontend/README.md)** - React TypeScript SPA documentation
- **[Infrastructure](infrastructure/README.en.md)** - Docker, deployment configuration
- **[Documentation](docs/README.en.md)** - Architecture, API docs, guides
- **[Scripts](scripts/README.en.md)** - Automation and build scripts

> 💡 **Tip**: Click on any folder in GitHub to see its specific README with detailed information about that module.

## 🐳 Docker

### Development
```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

### Production Build
```bash
# Build backend image
docker build -f infrastructure/docker/Dockerfile.backend -t personal-issue-tracker-backend .

# Build frontend image
docker build -f infrastructure/docker/Dockerfile.frontend -t personal-issue-tracker-frontend .
```

## 📚 Documentation

Comprehensive documentation is available in the `docs/` directory:

- **[API Documentation](docs/api/)** - REST API endpoints
- **[Architecture](docs/architecture/)** - System design and patterns
- **[Development Guide](docs/development/)** - Development workflows
- **[Testing Strategy](docs/testing/)** - Testing approaches and tools
- **[Deployment Guide](docs/deployment/)** - Production deployment

## 🔧 Configuration

### Environment Variables

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

## 🚀 Deployment

See [deployment documentation](docs/deployment/) for detailed deployment guides for different environments.

## 🤝 Contributing

1. Follow the project structure and conventions
2. Update documentation when making changes
3. Run tests before submitting changes
4. Use the provided scripts for consistency

## 📄 License

This project is for educational and personal use.

---

## 🌐 Language Versions

- **English**: [README.en.md](README.en.md)
- **Español**: [README.md](README.md)