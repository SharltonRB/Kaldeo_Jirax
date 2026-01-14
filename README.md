# Personal Issue Tracker

[🇪🇸 Versión en Español](README.es.md)

A Jira-inspired personal project management application built with Spring Boot and React TypeScript.

## 🚀 Quick Start

### Prerequisites
- Java 21
- Maven 3.8+
- Node.js 18+
- Docker and Docker Compose

### Setup and Run

```bash
# 1. Clone the repository
git clone <repository-url>
cd personal-issue-tracker

# 2. Start database and cache services
docker-compose up -d

# 3. Start backend (in another terminal)
cd backend && mvn spring-boot:run

# 4. Start frontend (in another terminal)
cd frontend && npm run dev
```

### Access Applications

- **Frontend**: http://localhost:5173
- **Backend API**: http://localhost:8080/api
- **API Documentation**: http://localhost:8080/api/swagger-ui.html

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
