# Modern Authentication Framework

A modern, SOLID-compliant, and extensible authentication framework built with Java 21 and Spring Boot 3.x.

## 🚀 Features

### Core Capabilities
- **Java 21 LTS** with Spring Boot 3.x
- **SOLID Principles** implementation throughout
- **Modern Security Practices** (Argon2 password hashing, JWT with RS256, CSRF protection)
- **Extensible Architecture** with Strategy Pattern for multiple auth types
- **Comprehensive Testing** with JUnit 5 and Mockito
- **Security Audit Logging** and centralized exception handling

### Authentication Methods
- Username/Password authentication
- Email/Password authentication
- OAuth 2.1 / OIDC support (Google, Azure Entra ID)
- API Key authentication
- LDAP integration
- SAML support
- Custom authentication strategies

### Security Features
- **Argon2id** password hashing (resistant to side-channel attacks)
- **JWT tokens** with RS256 signature algorithm
- **Refresh token rotation** for enhanced security
- **Account lockout** after failed login attempts
- **CSRF protection** and secure CORS configuration
- **Input validation** with comprehensive error handling
- **Security audit logging** for compliance

## 📁 Project Structure

```
auth-framework/
├── auth-core/                    # Core authentication library
│   ├── src/main/java/com/auth/framework/core/
│   │   ├── config/              # Configuration classes
│   │   ├── domain/              # Entity models
│   │   ├── dto/                 # Data Transfer Objects
│   │   ├── exception/           # Custom exceptions
│   │   ├── repository/          # Data access layer
│   │   ├── security/            # Security components
│   │   ├── service/             # Business logic services
│   │   └── strategy/            # Authentication strategies
│   └── src/test/                # Unit tests
├── auth-demo/                    # Demo application
│   ├── src/main/java/com/auth/framework/demo/
│   │   ├── controller/          # REST controllers
│   │   └── config/              # Demo-specific configuration
│   └── src/test/                # Integration tests
└── README.md                    # This file
```

## 🛠️ Quick Start

### Prerequisites
- Java 21 LTS
- Maven 3.8+
- MySQL 8.0+ (or compatible database)
- Docker (optional, for containerized deployment)

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd auth-framework
   ```

2. **Configure database**
   ```bash
   # Create MySQL database
   mysql -u root -p
   CREATE DATABASE auth_framework;
   ```

3. **Set environment variables**
   ```bash
   export JWT_SECRET="your-256-bit-secret-key-here-must-be-at-least-256-bits-long"
   export DATABASE_URL="jdbc:mysql://localhost:3306/auth_framework?useSSL=true&serverTimezone=UTC"
   export DATABASE_USERNAME="root"
   export DATABASE_PASSWORD="your-password"
   ```

4. **Build the project**
   ```bash
   ./gradlew clean build
   ```

5. **Run the demo application**
   ```bash
   ./gradlew :auth-demo:bootRun
   ```

The demo application will be available at `http://localhost:8080/demo`

## 🔧 Configuration

### Core Configuration (auth-core)

```yaml
auth:
  jwt:
    secret: ${JWT_SECRET:your-256-bit-secret-key}
    access-token-expiration: 900  # 15 minutes
    refresh-token-expiration: 86400  # 24 hours
    issuer: auth-framework
    audience: auth-framework-users
    max-refresh-tokens-per-user: 5
  
  security:
    password:
      min-length: 8
      max-length: 128
      require-uppercase: true
      require-lowercase: true
      require-digit: true
      require-special-char: true
    
    account:
      max-failed-attempts: 5
      lockout-duration-minutes: 30
```

### Demo Application Configuration (auth-demo)

```yaml
spring:
  datasource:
    url: ${DATABASE_URL:jdbc:mysql://localhost:3306/auth_framework}
    username: ${DATABASE_USERNAME:root}
    password: ${DATABASE_PASSWORD:password}

server:
  port: 8080
  servlet:
    context-path: /demo
```

## 📚 API Documentation

### Authentication Endpoints

#### Login with Username/Password
```http
POST /api/v1/auth/login/username
Content-Type: application/json

{
  "username": "john_doe",
  "password": "SecurePassword123!"
}
```

#### Login with Email/Password
```http
POST /api/v1/auth/login/email
Content-Type: application/json

{
  "email": "john.doe@example.com",
  "password": "SecurePassword123!"
}
```

#### Refresh Token
```http
POST /api/v1/auth/refresh
Content-Type: application/json

{
  "refreshToken": "your-refresh-token-here"
}
```

#### Logout
```http
POST /api/v1/auth/logout
Content-Type: application/json

{
  "userId": 123
}
```

### User Management Endpoints

#### Get All Users (Admin only)
```http
GET /api/v1/users
Authorization: Bearer your-access-token
```

#### Get User Profile
```http
GET /api/v1/users/profile
Authorization: Bearer your-access-token
```

#### Change Password
```http
POST /api/v1/users/{id}/change-password
Authorization: Bearer your-access-token
Content-Type: application/json

{
  "currentPassword": "OldPassword123!",
  "newPassword": "NewPassword123!"
}
```

## 🏗️ Architecture Patterns

### SOLID Principles Implementation

#### Single Responsibility Principle (SRP)
- Each class has a single, well-defined responsibility
- `PasswordService` only handles password operations
- `JwtService` only handles JWT token operations
- `AuthenticationStrategy` implementations handle specific auth types

#### Open/Closed Principle (OCP)
- Easy to add new authentication strategies without modifying existing code
- New auth types can be added by implementing `AuthenticationStrategy` interface
- Configuration is externalized and extensible

#### Liskov Substitution Principle (LSP)
- All `AuthenticationStrategy` implementations are interchangeable
- `UserDetailsImpl` can be substituted for any `UserDetails` implementation
- Service implementations can be swapped without breaking functionality

#### Interface Segregation Principle (ISP)
- Focused interfaces for specific responsibilities
- `PasswordService` interface only contains password-related methods
- `JwtService` interface only contains JWT-related methods

#### Dependency Inversion Principle (DIP)
- High-level modules depend on abstractions, not concretions
- Services depend on interfaces, not concrete implementations
- Dependency injection is used throughout

### Design Patterns

#### Strategy Pattern
- Different authentication methods (username/password, OAuth2, API keys)
- Pluggable authentication strategies
- Easy to add new authentication types

#### Factory Pattern
- `AuthenticationManager` acts as a factory for authentication strategies
- Automatic strategy registration and discovery

#### Repository Pattern
- Data access abstraction
- Clean separation between business logic and data access

## 🔒 Security Best Practices

### Password Security
- **Argon2id** hashing algorithm (winner of Password Hashing Competition)
- Configurable password complexity requirements
- Protection against timing attacks
- Secure random password generation

### Token Security
- **JWT with RS256** signature algorithm
- Short-lived access tokens (15 minutes default)
- Refresh token rotation
- Token blacklisting for revocation
- Secure token storage recommendations

### Account Security
- Account lockout after failed attempts
- Configurable lockout duration
- Account status tracking (enabled/disabled)
- Credential expiration support

### Input Validation
- Comprehensive input validation
- SQL injection prevention
- XSS protection
- CSRF protection

## 🧪 Testing

### Running Tests
```bash
# Run all tests
./gradlew test

# Run tests with coverage
./gradlew jacocoTestReport

# Run specific test class
./gradlew test --tests PasswordServiceTest

# Run integration tests
./gradlew integrationTest
```

### Test Coverage
- Unit tests for all service classes
- Integration tests for API endpoints
- Security tests for authentication flows
- Performance tests for password hashing

## 🚀 Deployment

### Docker Deployment
```dockerfile
FROM openjdk:21-jre-slim
COPY auth-demo-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Environment Variables
```bash
# Required
JWT_SECRET=your-256-bit-secret-key
DATABASE_URL=jdbc:mysql://localhost:3306/auth_framework
DATABASE_USERNAME=root
DATABASE_PASSWORD=password

# Optional
JWT_ACCESS_EXPIRATION=900
JWT_REFRESH_EXPIRATION=86400
SERVER_PORT=8080
LOG_LEVEL=INFO
```

## 🔧 Adding New Authentication Types

### 1. Create Authentication Request DTO
```java
public class OAuth2Request extends AuthenticationRequest {
    private String code;
    private String state;
    // ... getters and setters
}
```

### 2. Implement Authentication Strategy
```java
@Component
public class OAuth2Strategy implements AuthenticationStrategy {
    @Override
    public AuthenticationResult authenticate(AuthenticationRequest request) {
        // OAuth2 authentication logic
    }
    
    @Override
    public boolean supports(AuthenticationType authType) {
        return AuthenticationType.OAUTH2_GOOGLE.equals(authType);
    }
    
    // ... other methods
}
```

### 3. Register Strategy
The strategy will be automatically registered by Spring's dependency injection.

## 📊 Monitoring and Observability

### Health Checks
- `/actuator/health` - Application health status
- `/actuator/info` - Application information
- `/actuator/metrics` - Application metrics

### Logging
- Structured logging with Logback
- Security audit logs
- Performance metrics
- Error tracking

### Metrics
- Authentication success/failure rates
- Token generation and validation metrics
- Password strength distribution
- Account lockout statistics

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

For support and questions:
- Create an issue in the repository
- Check the [documentation](docs/)
- Review the [examples](examples/)

## 🔄 Migration from Legacy Systems

### From Spring Boot 2.x
1. Update dependencies to Spring Boot 3.x
2. Replace deprecated `WebSecurityConfigurerAdapter` with `SecurityFilterChain`
3. Update JWT library to latest version
4. Migrate from `javax.*` to `jakarta.*` packages

### From Basic Authentication
1. Implement JWT-based authentication
2. Add refresh token support
3. Implement proper password hashing
4. Add account lockout mechanisms

## 🎯 Roadmap

- [ ] OAuth 2.1 / OIDC integration
- [ ] Multi-factor authentication (MFA)
- [ ] Biometric authentication support
- [ ] Advanced threat detection
- [ ] GraphQL API support
- [ ] Kubernetes deployment manifests
- [ ] Performance benchmarking suite
- [ ] Security audit tools

---

**Built with ❤️ using Java 21, Spring Boot 3.x, and modern security practices.**