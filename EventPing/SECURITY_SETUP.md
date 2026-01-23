# EventPing Security Infrastructure Setup

This document describes the security infrastructure and dependencies that have been set up for the EventPing application.

## Dependencies Added

### JWT Authentication
- `io.jsonwebtoken:jjwt-api:0.12.6` - JWT API
- `io.jsonwebtoken:jjwt-impl:0.12.6` - JWT implementation
- `io.jsonwebtoken:jjwt-jackson:0.12.6` - JWT Jackson integration

### Security and Validation
- `org.owasp.encoder:encoder:1.2.3` - OWASP encoder for XSS prevention
- `org.jsoup:jsoup:1.18.1` - HTML parsing and sanitization
- `spring-boot-starter-cache` - Caching support
- `spring-boot-starter-data-redis` - Redis support for distributed caching
- `com.github.ben-manes.caffeine:caffeine` - High-performance caching

### Testing
- `com.h2database:h2` - In-memory database for testing

## Configuration Files

### Application Properties
- `application.properties` - Base configuration with security settings
- `application-dev.properties` - Development environment configuration
- `application-test.properties` - Test environment configuration
- `application-prod.properties` - Production environment configuration

### Environment Templates
- `.env.example` - Environment variables template for production
- `docker-compose.env.example` - Docker environment variables template

## Configuration Properties Classes

### SecurityProperties
- JWT configuration (secret, expiration, issuer, audience)
- Password security settings (bcrypt rounds, complexity requirements)
- Session management (timeout, concurrent sessions)
- CORS configuration
- Security headers configuration

### RateLimitProperties
- Global rate limiting settings
- IP-based rate limiting
- User-based rate limiting
- Feature-specific rate limits (registration, login, event creation)
- Plan-based limits (free vs pro)

### ValidationProperties
- SQL injection detection settings
- XSS protection settings
- Email validation settings
- File upload restrictions

### AuditProperties
- Audit logging configuration
- Event types to log
- Retention settings

## Security Configuration Structure

The security infrastructure is organized into the following layers:

1. **Authentication Layer**: JWT-based authentication with refresh tokens
2. **Authorization Layer**: Role-based access control with resource ownership
3. **Input Validation Layer**: SQL injection and XSS protection
4. **Rate Limiting Layer**: Multi-tier rate limiting (IP, user, feature-based)
5. **Audit Logging Layer**: Comprehensive security event logging
6. **Data Protection Layer**: Encryption and secure data handling

## Environment-Specific Settings

### Development (`dev` profile)
- Relaxed rate limits for development
- Verbose logging for debugging
- Simple caching
- Optional Redis

### Test (`test` profile)
- In-memory H2 database
- Disabled rate limiting for most tests
- Fast bcrypt rounds for performance
- Minimal logging

### Production (`prod` profile)
- Strict security settings
- Environment variable-based configuration
- Redis-based caching and rate limiting
- SSL/TLS enforcement
- Comprehensive audit logging

## Next Steps

This setup provides the foundation for implementing:

1. JWT authentication service
2. Authorization service with role-based access control
3. Input validation and sanitization
4. Rate limiting service
5. Audit logging service
6. Security headers and CORS configuration
7. Database security enhancements
8. Error handling with information disclosure prevention

Each of these components will be implemented in subsequent tasks according to the security hardening specification.

## Security Notes

- All sensitive configuration values use environment variables in production
- Default development secrets are provided but must be changed for production
- The current SecurityConfig is intentionally permissive and will be hardened in later tasks
- Rate limiting can be enabled/disabled per environment
- Audit logging is configurable and can be tuned for different environments