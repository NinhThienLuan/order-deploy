# Auth Service

Authentication and Security Microservice for the Franchise system.

## Features
- User authentication and authorization
- JWT token generation and validation (with Redis caching)
- Role-based access control (RBAC)
- User account management
- Password reset/forgot password flow
- Email notifications
- Security configuration
- Profile management

## Technology Stack
- Spring Boot 3.3.5
- Spring Security
- Spring Data JPA  
- Spring Cloud Netflix Eureka (Service Discovery)
- Redis (Token caching)
- PostgreSQL / H2 Database
- MapStruct (Entity-DTO mapping)
- JWT (Authentication)
- Spring Mail (Email notifications)

## Running the Service

### Development (H2 Database)
```bash
./mvnw spring-boot:run
```
Default profile uses H2 in-memory database.

### With PostgreSQL
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

### Production
```bash
./mvnw clean package
java -jar target/auth-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod
```

### With Docker
```bash
docker build -t auth-service .
docker run -p 8082:8082 auth-service
```

## Configuration

### Profiles
- `dev` - H2 database, verbose logging, H2 console enabled
- `postgres` - PostgreSQL database for local development  
- `prod` - PostgreSQL for production, minimal logging

### Environment Variables (Production)
- `DB_URL` - PostgreSQL connection URL
- `DB_USERNAME` - Database username
- `DB_PASSWORD` - Database password
- `REDIS_HOST` - Redis server host
- `REDIS_PORT` - Redis server port
- `JWT_SECRET` - JWT signing secret (min 32 characters)
- `MAIL_HOST` - SMTP server host
- `MAIL_USERNAME` - Email username
- `MAIL_PASSWORD` - Email password
- `EUREKA_SERVER_URL` - Eureka server URL

## API Documentation
Once the service is running, access Swagger UI at:
- http://localhost:8082/swagger-ui.html

## Endpoints
- `/api/v1/auth/**` - Authentication endpoints (login, register, refresh token)
- `/api/v1/accounts/**` - Account management
- `/api/v1/roles/**` - Role management
- `/api/v1/permissions/**` - Permission management
- `/api/v1/forget-password/**` - Password reset flow
- `/h2-console` - H2 database console (dev profile only)
- `/actuator/health` - Health check endpoint

## Database Setup

### PostgreSQL
```sql
CREATE DATABASE auth_db;
```

### Redis
Ensure Redis is running on localhost:6379 or configure via environment variables.

## Notes
- Default JWT secret is for development only. Use strong secret in production.
- Mail configuration is optional for dev but required for password reset feature.
- Redis is required for refresh token storage.
