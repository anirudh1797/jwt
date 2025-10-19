# Migration Guide: Legacy to Modern Authentication Framework

This guide helps you migrate from the legacy authentication application to the modern, SOLID-compliant authentication framework.

## 🚀 Overview

The modernized authentication framework provides:
- **Java 21 LTS** with Spring Boot 3.x
- **SOLID principles** implementation
- **Modern security practices** (Argon2, JWT improvements, CSRF protection)
- **Extensible architecture** with multiple authentication strategies
- **Comprehensive testing** and monitoring

## 📋 Migration Checklist

### 1. Environment Setup
- [ ] Upgrade to Java 21 LTS
- [ ] Update to Spring Boot 3.x
- [ ] Migrate from `javax.*` to `jakarta.*` packages
- [ ] Update database to MySQL 8.0+ or compatible

### 2. Dependencies Migration

#### Legacy Dependencies (Remove)
```xml
<!-- Remove these legacy dependencies -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter</artifactId>
    <version>2.6.2</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt</artifactId>
    <version>0.9.1</version>
</dependency>
<dependency>
    <groupId>javax.validation</groupId>
    <artifactId>validation-api</artifactId>
</dependency>
```

#### Modern Dependencies (Add)
```xml
<!-- Add these modern dependencies -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter</artifactId>
    <version>3.2.0</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.3</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.3</version>
</dependency>
<dependency>
    <groupId>org.bouncycastle</groupId>
    <artifactId>bcprov-jdk18on</artifactId>
    <version>1.77</version>
</dependency>
```

### 3. Code Migration

#### 3.1 Package Structure Changes

**Legacy Structure:**
```
com.spring.jwt/
├── controllers/
├── models/
├── repository/
├── security/
└── payload/
```

**Modern Structure:**
```
com.auth.framework.core/
├── config/
├── domain/
├── dto/
├── exception/
├── repository/
├── security/
├── service/
└── strategy/
```

#### 3.2 Entity Migration

**Legacy User Entity:**
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(max = 20)
    private String username;
    
    @NotBlank
    @Size(max = 50)
    @Email
    private String email;
    
    @NotBlank
    @Size(max = 120)
    private String password;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles")
    private Set<Role> roles = new HashSet<>();
}
```

**Modern User Entity:**
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(max = 50)
    @Column(unique = true)
    private String username;
    
    @NotBlank
    @Size(max = 100)
    @Email
    @Column(unique = true)
    private String email;
    
    @NotBlank
    @Size(max = 255)
    private String password;
    
    // Additional security fields
    private Boolean enabled = true;
    private Boolean accountNonLocked = true;
    private Boolean accountNonExpired = true;
    private Boolean credentialsNonExpired = true;
    private LocalDateTime lastLogin;
    private Integer failedLoginAttempts = 0;
    private LocalDateTime lockedUntil;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles")
    private Set<Role> roles = new HashSet<>();
}
```

#### 3.3 Security Configuration Migration

**Legacy Security Config:**
```java
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable()
            .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .authorizeRequests().antMatchers("/api/v1/user/**").permitAll()
            .anyRequest().authenticated();
    }
}
```

**Modern Security Config:**
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> 
                auth.requestMatchers("/api/v1/auth/**").permitAll()
                    .requestMatchers("/api/v1/public/**").permitAll()
                    .anyRequest().authenticated()
            );
        return http.build();
    }
}
```

#### 3.4 JWT Service Migration

**Legacy JWT Utils:**
```java
@Component
public class JwtUtils {
    @Value("${jwtKey.secret}")
    private String jwtSecret;
    
    public String generateJwtToken(UserDetailsImpl userPrincipal) {
        return generateTokenFromUsername(userPrincipal.getUsername());
    }
    
    public String generateTokenFromUsername(String username) {
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(new Date())
            .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
            .signWith(SignatureAlgorithm.HS512, jwtSecret)
            .compact();
    }
}
```

**Modern JWT Service:**
```java
@Service
public class ModernJwtService implements JwtService {
    
    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", user.getUsername());
        claims.put("email", user.getEmail());
        claims.put("userId", user.getId());
        claims.put("roles", user.getRoles().stream()
                .map(role -> role.getName().name())
                .toList());
        claims.put("type", "access");
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuer(issuer)
                .setAudience(audience)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs * 1000L))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }
}
```

### 4. Configuration Migration

#### 4.1 Application Properties

**Legacy Properties:**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/test?useSSL=true
spring.datasource.username=root
spring.datasource.password=P3ar$on2288

jwtKey.secret=springSecretKey
jwtKey.expirationMs=60000
jwtKey.refreshExpirationMs=120000

server.port=8080
logging.level.org.springframework.security=TRACE
```

**Modern Properties:**
```yaml
spring:
  datasource:
    url: ${DATABASE_URL:jdbc:mysql://localhost:3306/auth_framework?useSSL=true&serverTimezone=UTC}
    username: ${DATABASE_USERNAME:root}
    password: ${DATABASE_PASSWORD:password}
    driver-class-name: com.mysql.cj.jdbc.Driver

auth:
  jwt:
    secret: ${JWT_SECRET:your-256-bit-secret-key-here-must-be-at-least-256-bits-long}
    access-token-expiration: ${JWT_ACCESS_EXPIRATION:900}
    refresh-token-expiration: ${JWT_REFRESH_EXPIRATION:86400}
    issuer: ${JWT_ISSUER:auth-framework}
    audience: ${JWT_AUDIENCE:auth-framework-users}

server:
  port: ${SERVER_PORT:8080}

logging:
  level:
    com.auth.framework: INFO
    org.springframework.security: WARN
```

### 5. API Migration

#### 5.1 Authentication Endpoints

**Legacy Endpoints:**
```
POST /api/v1/user/auth/token
POST /api/v1/user/auth/refreshtoken
POST /api/v1/user/logout
```

**Modern Endpoints:**
```
POST /api/v1/auth/login/username
POST /api/v1/auth/login/email
POST /api/v1/auth/refresh
POST /api/v1/auth/logout
```

#### 5.2 Request/Response Format

**Legacy Login Request:**
```json
{
  "username": "john_doe",
  "password": "password123"
}
```

**Modern Login Request:**
```json
{
  "username": "john_doe",
  "password": "password123",
  "ipAddress": "192.168.1.1",
  "userAgent": "Mozilla/5.0..."
}
```

**Legacy Login Response:**
```json
{
  "token": "jwt_token_here",
  "type": "Bearer",
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "roles": ["ROLE_USER"]
}
```

**Modern Login Response:**
```json
{
  "user": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "roles": ["ROLE_USER"]
  },
  "accessToken": "jwt_access_token_here",
  "refreshToken": "refresh_token_here",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "expiresAt": "2024-01-01T12:15:00",
  "sessionId": "sess_1234567890_123",
  "lastLogin": "2024-01-01T12:00:00"
}
```

### 6. Database Migration

#### 6.1 Schema Updates

**Add new columns to users table:**
```sql
ALTER TABLE users 
ADD COLUMN first_name VARCHAR(50),
ADD COLUMN last_name VARCHAR(50),
ADD COLUMN is_enabled BOOLEAN DEFAULT TRUE,
ADD COLUMN is_account_non_locked BOOLEAN DEFAULT TRUE,
ADD COLUMN is_account_non_expired BOOLEAN DEFAULT TRUE,
ADD COLUMN is_credentials_non_expired BOOLEAN DEFAULT TRUE,
ADD COLUMN last_login TIMESTAMP,
ADD COLUMN failed_login_attempts INT DEFAULT 0,
ADD COLUMN locked_until TIMESTAMP,
ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- Update existing users
UPDATE users SET 
  is_enabled = TRUE,
  is_account_non_locked = TRUE,
  is_account_non_expired = TRUE,
  is_credentials_non_expired = TRUE,
  created_at = NOW(),
  updated_at = NOW();
```

#### 6.2 Password Migration

**Migrate existing passwords to Argon2:**
```java
@Service
public class PasswordMigrationService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordService passwordService;
    
    @Transactional
    public void migratePasswords() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            if (user.getPassword().startsWith("$2a$")) {
                // Already migrated (BCrypt)
                continue;
            }
            
            // Re-encode with Argon2
            String newPassword = passwordService.encode(user.getPassword());
            user.setPassword(newPassword);
            userRepository.save(user);
        }
    }
}
```

### 7. Testing Migration

#### 7.1 Test Dependencies

**Add test dependencies:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

#### 7.2 Test Configuration

**Create test profile:**
```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    username: sa
    password: 
  jpa:
    hibernate:
      ddl-auto: create-drop

auth:
  jwt:
    secret: test-secret-key
    access-token-expiration: 60
    refresh-token-expiration: 300
```

### 8. Deployment Migration

#### 8.1 Environment Variables

**Set required environment variables:**
```bash
export JWT_SECRET="your-256-bit-secret-key-here-must-be-at-least-256-bits-long"
export DATABASE_URL="jdbc:mysql://localhost:3306/auth_framework"
export DATABASE_USERNAME="root"
export DATABASE_PASSWORD="password"
export JWT_ACCESS_EXPIRATION="900"
export JWT_REFRESH_EXPIRATION="86400"
```

#### 8.2 Docker Configuration

**Create Dockerfile:**
```dockerfile
FROM openjdk:21-jre-slim
COPY auth-demo-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

**Create docker-compose.yml:**
```yaml
version: '3.8'
services:
  auth-framework:
    build: .
    ports:
      - "8080:8080"
    environment:
      - JWT_SECRET=your-256-bit-secret-key
      - DATABASE_URL=jdbc:mysql://mysql:3306/auth_framework
      - DATABASE_USERNAME=root
      - DATABASE_PASSWORD=password
    depends_on:
      - mysql
  
  mysql:
    image: mysql:8.0
    environment:
      - MYSQL_ROOT_PASSWORD=password
      - MYSQL_DATABASE=auth_framework
    ports:
      - "3306:3306"
```

### 9. Monitoring and Observability

#### 9.1 Health Checks

**Add actuator endpoints:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when-authorized
```

#### 9.2 Logging Configuration

**Configure structured logging:**
```yaml
logging:
  level:
    com.auth.framework: INFO
    org.springframework.security: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
  file:
    name: logs/auth-framework.log
```

### 10. Rollback Plan

#### 10.1 Database Rollback
```sql
-- Remove new columns if needed
ALTER TABLE users DROP COLUMN first_name;
ALTER TABLE users DROP COLUMN last_name;
-- ... other columns
```

#### 10.2 Application Rollback
- Keep legacy application running in parallel
- Use feature flags to switch between old and new authentication
- Monitor error rates and performance metrics

## 🚨 Common Issues and Solutions

### Issue 1: Package Import Errors
**Problem:** `javax.*` imports not found
**Solution:** Replace all `javax.*` imports with `jakarta.*`

### Issue 2: Security Configuration Errors
**Problem:** `WebSecurityConfigurerAdapter` not found
**Solution:** Use `SecurityFilterChain` bean instead

### Issue 3: JWT Token Validation Errors
**Problem:** Token validation failing
**Solution:** Update JWT library and ensure proper key configuration

### Issue 4: Database Connection Issues
**Problem:** Connection refused to database
**Solution:** Update database URL format and driver class

### Issue 5: Password Validation Errors
**Problem:** Existing passwords not meeting new requirements
**Solution:** Implement gradual password migration strategy

## 📞 Support

For migration support:
1. Check the [documentation](README.md)
2. Review the [examples](examples/)
3. Create an issue in the repository
4. Contact the development team

## ✅ Post-Migration Checklist

- [ ] All tests passing
- [ ] Security scan completed
- [ ] Performance testing done
- [ ] Monitoring configured
- [ ] Documentation updated
- [ ] Team training completed
- [ ] Rollback plan tested

---

**Migration completed successfully! 🎉**

Your authentication system is now modern, secure, and ready for the future.