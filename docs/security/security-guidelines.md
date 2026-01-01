# Security Guidelines

## Environment Variables
- Never commit `.env` files with real credentials
- Always use `.env.example` for templates
- Use environment variables for all sensitive configuration

## JWT Configuration
- Never use default JWT secrets
- Use strong, randomly generated secrets in production
- Rotate secrets regularly

## Database Security
- Never commit database passwords
- Use environment variables for database configuration
- Use connection pooling and proper timeouts

## API Security
- Implement proper authentication and authorization
- Use HTTPS in production
- Implement rate limiting
- Validate all inputs

## File Security
- Keep sensitive files out of version control
- Use proper file permissions
- Encrypt sensitive data at rest
