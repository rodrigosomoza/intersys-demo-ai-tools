# Context
This a multimodule maven project containing two submodules that are rest servers: module 1 and module 2.

# Tech Stack

This project needs to use spring boot version 3.5.5 dependencies with Java version 21 features.
* **Java:** Use the latest Long-Term Support (LTS) version of Java (e.g., Java 21 or later) unless project constraints dictate otherwise.
* **Spring Boot:** Always use the latest stable release of Spring Boot 3.x (or the latest major stable version available) for new features or projects.
* **Build Tool:** Use Maven as the build tool. Ensure the `pom.xml` uses the latest stable Spring Boot parent POM and compatible plugin versions.

## Tech per domain:
- Http Client -> Spring Rest Client
- GRPC Schema type -> Protobuff
- Api Specification -> Openapi v3 Swagger
- Database -> sqlite
- Database migration management system -> flyway
- Database Location -> Root of the project, path is configurable through application properties file
- JPA
- Hibernate
- Tests -> Junit 5

# Guidelines
## Step 1
* Always look for the latest version release for the library we use. Use the Web Search to search for the versions.
* Start an implementation with a planning
* After defining the plan, follow the Test Driven Development pattern, start with unit testings.
# Step 2
* For Test, do Unit tests and integration tests.
* Use Mockito for mocking and Wiremock for stub a remote service api
* Reanalyse the logic of the unit tests to make sure it respects the plan and the feature we want to implement
## Step 3
* Start with the implementation by verifying that the jpa model and database schema have everything needed (tables, column) to support the logic of the 
implementation
* Test the implementation by running the unit tests and integration tests
* If the tests fail, Re start from step 1
* Don't edit the unit tests because the implementations fails, only edit the tests if they don't suits the feature goals

# Project Structure

* **Packaging:** Strongly prefer a **package-by-layer** structure over **package-by-feature**. This means avoid grouping all code related to a specific feature or domain concept (like "posts", "users", or "orders") together in the same package hierarchy. Prefer structuring packages based solely on technical layers (like "controllers", "services", "repositories").

    * **Example:**

      **PREFER THIS (Package-by-Layer):**
        ```
        com.example.application
        ├── controller
        │   ├── PostController.java
        │   └── UserController.java
        │
        ├── service
        │   ├── PostService.java
        │   └── UserService.java
        │
        ├── repository
        │   ├── PostRepository.java
        │   └── UserRepository.java
        │
        └── model  (or domain/entity)
            ├── Post.java
            └── User.java
        ```

      **AVOID THIS (Package-by-Feature):**
        ```
        com.example.application
        ├── posts                     # Feature: Posts
        │   ├── PostController.java   # Controller for Posts
        │   ├── PostService.java      # Service logic for Posts
        │   ├── PostRepository.java   # Data access for Posts
        │   ├── Post.java             # Domain/Entity for Posts
        │   └── dto                   # Data Transfer Objects specific to Posts
        │       ├── PostCreateRequest.java
        │       └── PostSummaryResponse.java
        │
        ├── users                     # Feature: Users
        │   ├── UserController.java
        │   ├── UserService.java
        │   ├── UserRepository.java
        │   └── User.java
        │
        └── common                    # Optional: Truly shared utilities/config
            └── exception
                └── ResourceNotFoundException.java
        ```

# Java Language Features
* **Data Carriers:** Use Java **Records** (`record`) for immutable data transfer objects (DTOs), value objects, or simple data aggregates whenever possible. Prefer records over traditional classes with getters, setters, `equals()`, `hashCode()`, and `toString()` for these use cases.
* **Immutability:** Favor immutability for objects where appropriate, especially for DTOs and configuration properties.

# General Code Quality
* **Readability:** Write clean, readable, and maintainable code.
* **Comments:** Add comments only where necessary to explain complex logic or non-obvious decisions. Code should be self-documenting where possible.
* **API Design:** Design RESTful APIs with clear resource naming, proper HTTP methods, and consistent request/response formats.

## Spring Framework Best Practices
* **Dependency Injection:** Use **constructor injection** for mandatory dependencies. Avoid field injection.
* **Configuration:** Use `application.properties` or `application.yml` for application configuration. Leverage Spring Boot's externalized configuration mechanisms (profiles, environment variables, etc.). Use `@ConfigurationProperties` for type-safe configuration binding.
* **Error Handling:** Implement consistent exception handling, potentially using `@ControllerAdvice` and custom exception classes. Provide meaningful error responses.
* **Logging:** Use SLF4j with a suitable backend (like Logback, included by default in Spring Boot starters) for logging. Write clear and informative log messages.

## Testing
* **Unit Tests:** Write unit tests for services and components using JUnit 5 and Mockito.
* **Integration Tests:** Write integration tests using `@SpringBootTest`. For database interactions, consider using Testcontainers or an in-memory database (like H2) configured only for the test profile. Ensure integration tests cover the controller layer and key application flows.
* **Test Location:** Place tests in the standard `src/test/java` directory, mirroring the source package structure.