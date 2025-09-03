# Financial Transaction System

A multi-module Spring Boot application demonstrating financial transactions between users.

## Project Structure

- **Module 1 (Transaction Service)** - Port 8080: Handles financial transactions
- **Module 2 (User Service)** - Port 8081: Manages user information and balances

## Architecture

The system follows the package-by-layer architecture with:
- Controllers for REST APIs
- Services for business logic  
- Repositories for data access
- DTOs using Java Records
- JPA entities for database mapping

## Transaction Flow

1. Module 1 receives transaction request (User A → User B, amount)
2. Module 1 generates unique transaction ID
3. Module 1 calls Module 2 to get user information (balances)
4. Module 1 validates:
   - Transaction IDs match
   - Sender has sufficient balance
   - Users exist
5. If valid, Module 1 updates balances via Module 2
6. Module 1 returns transaction result

## Tech Stack

- Java 21
- Spring Boot 3.3.5
- SQLite with Flyway migrations
- JPA/Hibernate
- Spring Rest Client
- JUnit 5 + Mockito + WireMock
- OpenAPI 3/Swagger

## Building and Running

### Build
```bash
mvn clean install
```

### Run Module 2 (User Service) - Port 8081
```bash
cd module2
mvn spring-boot:run
```

### Run Module 1 (Transaction Service) - Port 8080  
```bash
cd module1
mvn spring-boot:run
```

## API Documentation

- Module 1 Swagger UI: http://localhost:8080/swagger-ui.html
- Module 2 Swagger UI: http://localhost:8081/swagger-ui.html

## Sample Usage

### Get User Information
```bash
curl -X POST http://localhost:8081/api/users/info \
  -H "Content-Type: application/json" \
  -d '{
    "transactionId": "TXN-12345",
    "userIds": [1, 2]
  }'
```

### Process Transaction
```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "senderId": 1,
    "receiverId": 2,  
    "amount": 500.00
  }'
```

## Database

- **Module 1**: `transaction-service.db` (transactions table)
- **Module 2**: `user-service.db` (users table with sample data)

Sample users are automatically created via Flyway migration:
- User 1: John Doe - $5000
- User 2: Jane Smith - $3000  
- User 3: Bob Johnson - $10000
- User 4: Alice Williams - $7500
- User 5: Charlie Brown - $2000

## Testing

```bash
# Run all tests
mvn test

# Run specific module tests  
cd module1 && mvn test
cd module2 && mvn test
```

The implementation follows Test-Driven Development (TDD) with comprehensive unit tests for services and controllers.