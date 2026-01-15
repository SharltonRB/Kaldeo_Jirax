# Development Credentials

## Test Users

All test users use the same password for development convenience.

### Credentials

| Email | Password | Name | Description |
|-------|----------|------|-------------|
| john.doe@example.com | password123 | John Doe | Regular user with sample projects |
| jane.smith@example.com | password123 | Jane Smith | Regular user with analytics projects |
| admin@example.com | password123 | Admin User | Admin user |

## Password Hash Information

- **Algorithm**: BCrypt
- **Rounds**: 10 (matches Spring Security default)
- **Password**: `password123`

## Generating New Hashes

If you need to generate new password hashes:

```bash
# Using Python script (recommended)
python3 backend/generate_bcrypt_hash.py

# Or using the Java class (requires compiled project)
cd backend
./generate_correct_hashes.sh
```

## Security Notes

⚠️ **IMPORTANT**: These credentials are for **DEVELOPMENT ONLY**

- Never use these passwords in production
- Never commit production credentials to version control
- Always use strong, unique passwords in production
- Use environment variables for production credentials

## Resetting Development Database

If you need to reset your development database with fresh data:

```bash
# Stop all containers and remove volumes
docker-compose down -v

# Start fresh
docker-compose up -d postgres

# Run the application (migrations will apply automatically)
./scripts/start-dev.sh
```
