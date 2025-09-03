# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a multi-module Maven project implementing a financial transaction system with two REST servers:
- **Module 1**: Transaction Server - Handles financial transactions between users
- **Module 2**: User Service - Manages user information and account balances

## Build Commands

```bash
# Build entire project
mvn clean install

# Build specific module
mvn clean install -pl module1
mvn clean install -pl module2

# Run tests
mvn test

# Run specific module tests
mvn test -pl module1
mvn test -pl module2

# Run a single test class
mvn test -Dtest=YourTestClassName

# Run a single test method
mvn test -Dtest=YourTestClassName#methodName

# Package application
mvn package

# Skip tests during build
mvn clean install -DskipTests

# Run Spring Boot applications
cd module1 && mvn spring-boot:run
cd module2 && mvn spring-boot:run
```

## Architecture

### Transaction Flow
1. Module 1 receives transaction requests (User A sending money to User B)
2. Module 1 sends request to Module 2 with:
   - Unique transaction ID (cache-friendly)
   - Array of user IDs
3. Module 2 returns user information including account balances
4. Module 1 validates:
   - Transaction IDs match
   - User A has sufficient balance
5. Module 1 updates balances and returns transaction status

### Package Structure
The project follows a **package-by-layer** architecture:
```
ch.innovation.ai.tools.demo
├── controller/     # REST controllers
├── service/        # Business logic
├── repository/     # Data access layer
├── model/          # JPA entities
├── dto/            # Data Transfer Objects (use Records)
└── config/         # Configuration classes
```

## Technology Stack

- **Java 21** - Latest LTS features including Records for DTOs
- **Spring Boot 3.5.5** - Core framework
- **Maven** - Build tool
- **SQLite** - Database (configurable location via application.yml)
- **Flyway** - Database migrations
- **JPA/Hibernate** - ORM
- **Spring Rest Client** - HTTP client
- **OpenAPI v3/Swagger** - API specification
- **JUnit 5** - Unit testing
- **Mockito** - Mocking framework
- **WireMock** - API stubbing for integration tests

## Development Guidelines

### Test-Driven Development (TDD)
1. **Always start with a plan** for the feature implementation
2. **Write unit tests first** before implementation
3. **Write both unit and integration tests**
4. **Verify JPA models and database schema** support the implementation
5. **Run tests** - if they fail, restart from planning
6. **Never modify tests** to make implementation pass (unless tests don't match feature goals)

### Testing Strategy
- Unit tests: Use Mockito for mocking dependencies
- Integration tests: Use @SpringBootTest with WireMock for external services
- Database tests: Use in-memory H2 or Testcontainers for test profile
- Test location: `src/test/java` mirroring source package structure

### Code Quality Standards
- **Constructor injection** for dependencies (no field injection)
- **Java Records** for DTOs and immutable data carriers
- **Configuration**: Use application.yml with @ConfigurationProperties
- **Error handling**: Implement @ControllerAdvice with custom exceptions
- **Logging**: Use SLF4J with clear, informative messages

### Before Starting Implementation
1. **Search for latest library versions** using Web Search
2. **Plan the implementation** thoroughly
3. **Check existing code patterns** in the codebase
4. **Verify database schema** supports the feature

## Database Configuration

- Database type: SQLite
- Default location: Project root (configurable in application.yml)
- Migrations: Managed by Flyway in `src/main/resources/db/migration/`

## Module-Specific Configurations

### Module 1 (Transaction Server)
- Port: Configure in `module1/src/main/resources/application.yml`
- Handles transaction validation and balance updates

### Module 2 (User Service)
- Port: Configure in `module2/src/main/resources/application.yml`
- Provides user information and account balances