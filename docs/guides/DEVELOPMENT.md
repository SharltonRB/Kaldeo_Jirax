# Development Setup & Security

## 🚀 Quick Start

### 1. Clone and Setup
```bash
git clone <repository>
cd personal-issue-tracker
```

### 2. Backend Setup
```bash
cd backend
cp .env.example .env
# Edit .env with your local database credentials
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 3. Frontend Setup
```bash
cd frontend
cp .env.example .env
# Edit .env if needed (defaults should work)
npm install
npm run dev
```

### 4. Database Setup
```bash
# Start PostgreSQL (using Docker)
docker-compose up -d postgres

# Or use local PostgreSQL
createdb issue_tracker_dev
```

## 🔒 Security Notes for Developers

### Development Credentials (SAFE for local dev)
- **Database**: `postgres/postgres`
- **Test Users**: All use password `password123`
- **JWT Secret**: Development placeholder (auto-generated)

### ⚠️ NEVER in Production
- Change all default passwords
- Generate new JWT secrets
- Use environment variables for all secrets
- Enable HTTPS/SSL

### Test Users (Development Only)
```
john.doe@example.com / password123
jane.smith@example.com / password123
admin@example.com / password123
```

## 📁 File Structure

### Safe to Commit
- `*.example` files
- Configuration templates
- Development defaults
- Test data (non-sensitive)

### Never Commit (Auto-ignored)
- `.env` files
- `*.sh` scripts (contain env vars)
- `application-local*.yml`
- Any file with real secrets

## 🛠️ Development Scripts

### Start Everything
```bash
# Option 1: Manual (recommended for development)
# Terminal 1: Backend
cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Terminal 2: Frontend  
cd frontend && npm run dev

# Option 3: Using Docker
docker-compose up
```

### Stop Everything
```bash
# Kill by process name
pkill -f "PersonalIssueTrackerApplication"
pkill -f "vite"

# Or kill by port
lsof -ti:8080 | xargs kill -9
lsof -ti:3000 | xargs kill -9
```

## 🧪 Testing

### Backend Tests
```bash
cd backend
mvn test
```

### Frontend Tests
```bash
cd frontend
npm test
```

### Integration Tests
```bash
# Start backend first, then:
cd frontend
npm run test:e2e
```

## 🔍 Security Audit

### Check for Secrets
```bash
# Search for potential secrets
grep -r "password\|secret\|key" --exclude-dir=node_modules --exclude-dir=target .

# Check git tracking
git ls-files | grep -E "\.(env|properties)$"
```

### Verify .gitignore
```bash
# These should return empty (files ignored)
git status --porcelain | grep "\.env"
git status --porcelain | grep "\.sh"
```

## 📚 Additional Resources

- [Security Guidelines](SECURITY.md)
- [API Documentation](docs/api/)
- [Architecture Overview](docs/architecture/)
- [Deployment Guide](docs/deployment/)