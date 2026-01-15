# Personal Issue Tracker

[🇪🇸 Versión en Español](README.es.md)

A Jira-inspired personal project management application built with Spring Boot and React TypeScript.

## 🚀 Quick Start

### Prerequisites
- Java 21
- Maven 3.8+
- Node.js 18+
- Docker and Docker Compose
- Python 3 (for password hash utilities)

### First Time Setup

```bash
# 1. Clone the repository
git clone <repository-url>
cd personal-issue-tracker

# 2. Reset and initialize database
./scripts/reset-dev-database.sh

# 3. Start the application
./scripts/start-dev.sh
```

### Login Credentials

**Email**: `john.doe@example.com`  
**Password**: `password123`

See `backend/DEVELOPMENT_CREDENTIALS.md` for all test users.

### Access Applications

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080/api
- **Health Check**: http://localhost:8080/api/actuator/health

## 🔧 Troubleshooting

### Flyway Migration Errors

If you see "Migrations have failed validation" errors:

```bash
./scripts/reset-dev-database.sh
./scripts/start-dev.sh
```

### Authentication Errors

If you can't login with the test credentials:

```bash
./scripts/reset-dev-database.sh
./scripts/start-dev.sh
```

### Check Migration Status

```bash
./scripts/check-flyway-status.sh
```

See [Flyway Troubleshooting Guide](docs/FLYWAY_TROUBLESHOOTING.md) for detailed solutions.

## 📁 Project Structure

```
personal-issue-tracker/
├── backend/          # Spring Boot API
├── frontend/         # React TypeScript SPA
├── infrastructure/   # Docker, K8s, Terraform
├── docs/            # 📚 Complete Documentation
├── scripts/         # Automation scripts
└── logs/            # Application logs
```

## 📚 Documentation

Complete documentation is available in the [`docs/`](docs/) directory:

- **[Documentation Index](docs/INDEX.md)** - Complete documentation overview
- **[Development Guide](docs/DEVELOPMENT.md)** - Detailed setup and development instructions
- **[Architecture](docs/architecture/)** - System design and architecture
- **[Security](docs/SECURITY.md)** - Security guidelines and best practices
- **[Testing](docs/testing/)** - Testing strategies and guides
- **[Production Deployment](docs/PRODUCTION_DEPLOYMENT.md)** - Production deployment guide

## 🛠️ Key Features

- **Project Management**: Create and manage multiple projects
- **Issue Tracking**: Track tasks, bugs, and stories with different priorities
- **Sprint Planning**: Plan and manage sprints with calendar integration
- **Kanban Board**: Visual task management with drag-and-drop
- **Real-time Updates**: Live notifications and status updates
- **User Authentication**: Secure JWT-based authentication
- **Responsive Design**: Modern UI with Tailwind CSS

## 🧪 Testing

```bash
# Backend tests
cd backend && mvn test

# Frontend tests
cd frontend && npm run test:run

# Complete test suite
./scripts/test-scripts.sh ci
```

## 🐳 Docker

```bash
# Development
docker-compose up -d

# Production build
docker-compose -f docker-compose.prod.yml up -d
```

## 🤝 Contributing

1. Follow the project structure and conventions
2. Update documentation when making changes
3. Run tests before submitting changes
4. See [Development Guide](docs/DEVELOPMENT.md) for detailed guidelines

## 📄 License

This project is for educational and personal use.

---

**For detailed documentation, guides, and architecture information, see the [`docs/`](docs/) directory.**
