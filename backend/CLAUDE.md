# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

All commands run from the `backend/` directory using the Maven Wrapper.

```sh
./mvnw spring-boot:run          # Run the application (port 8080)
./mvnw test                     # Run all tests
./mvnw package                  # Build JAR
./mvnw package -DskipTests      # Build JAR without tests

# Run a single test class
./mvnw test -Dtest=CreateProductUseCaseTest

# Run a single test method
./mvnw test -Dtest=CreateProductUseCaseTest#shouldSaveProductWhenInputIsValid
```

**Prerequisites:**
- **Tests**: Docker Desktop must be running — Testcontainers spins up a real `postgres:16-alpine` automatically.
- **App**: A PostgreSQL instance on `localhost:5432` (credentials in `application.properties`). Quickstart:
  ```sh
  docker run --name inventario-db \
    -e POSTGRES_USER=inventario_user -e POSTGRES_PASSWORD=inventario_password \
    -e POSTGRES_DB=inventario_db -p 5432:5432 -d postgres:16-alpine
  ```
  Hibernate DDL is set to `update` — tables are auto-created on first boot.

## Architecture

Strict Clean Architecture in three layers. The dependency rule is absolute: `domain` has zero external dependencies; `usecase` depends only on `domain`; `infrastructure` depends on both.

```
com.master.inventario/
├── domain/          # Pure Java — no Spring, no JPA
│   ├── model/       # Product, Batch — hand-written constructors/getters, no Lombok
│   ├── repository/  # ProductRepository, BatchRepository — plain Java interfaces (the ports)
│   └── exception/   # DomainException (base) + typed subclasses per business rule
│
├── usecase/         # One class per use case, single execute() method, plain Java
│
└── infrastructure/
    ├── config/      # UseCaseConfig (explicit @Bean wiring), WebConfig (CORS + security headers), PersistenceConfig, OpenApiConfig
    ├── persistence/
    │   ├── entity/     # ProductEntity, BatchEntity (@Entity)
    │   ├── repository/ # ProductJpaRepository, BatchJpaRepository (Spring Data)
    │   ├── mapper/     # ProductMapper, BatchMapper — toDomain() / toEntity()
    │   └── adapter/    # ProductRepositoryAdapter, BatchRepositoryAdapter — implement domain interfaces
    └── web/
        ├── controller/ # ProductController, StockEntryController
        ├── dto/        # Java records for request/response
        ├── mapper/     # ProductDtoMapper, StockEntryDtoMapper — toDomain() / toResponse()
        └── exception/  # GlobalExceptionHandler, ErrorResponse
```

## Key Conventions

**Use cases are NOT Spring beans.** They are plain Java objects instantiated explicitly as `@Bean` in `UseCaseConfig`. This keeps the use case layer free of any framework annotations.

**Adapter naming:** `<Entity>RepositoryAdapter implements <Entity>Repository` (domain interface). JPA repositories are named `<Entity>JpaRepository`.

**Mapper naming:** persistence mappers are `<Entity>Mapper`; web mappers are `<Entity>DtoMapper`.

**Error response format** is uniform — `{code, message, timestamp (Instant), path}` — handled centrally by `GlobalExceptionHandler` via `@RestControllerAdvice`. All business rule violations throw a subclass of `DomainException`.

**CORS** is configured via `app.cors.allowed-origins` in `application.properties` (defaults to `http://localhost:4200`), implemented in `WebConfig` — there is no Spring Security dependency.

**Pageable**: default page size 10, maximum silently capped at 100, enforced in `WebConfig`. Controllers use `@ParameterObject` on `Pageable` parameters so springdoc renders them as individual query params in Swagger UI.

**OpenAPI / Swagger UI**: powered by `springdoc-openapi-starter-webmvc-ui`. Available at `http://localhost:8080/swagger-ui.html` (JSON at `/v3/api-docs`). Global metadata defined in `OpenApiConfig`. The CSP in `WebConfig` is relaxed for `/swagger-ui/**`, `/v3/api-docs/**`, and `/webjars/**` paths to allow Swagger UI assets to load.

**Known CVEs**: two transitive CVEs from Spring Boot dependencies are documented in `SECURITY.md`. `commons-compress` is pinned to 1.27.1 via `<commons-compress.version>` in `pom.xml`. `commons-lang3` CVE-2025-48924 has no fix available and is accepted as a known risk.

## Test Strategies

Three distinct test types are used:

1. **Use case unit tests** — manual `mock(ProductRepository.class)` in `@BeforeEach`, no Spring context. Fast.
2. **Persistence integration tests** — `@SpringBootTest` + `@Testcontainers` with a static `PostgreSQLContainer`. Schema is `create-drop`. Each test calls `deleteAllInBatch()` in `@BeforeEach`.
3. **Controller slice tests** — `@WebMvcTest` + `MockMvc` with `@MockitoBean` for use cases and `@Import` for mappers/exception handler.

Tests mirror the main package structure under `src/test/java/com/master/inventario/`.

## Stack

Java 17, Spring Boot 3.5.x, PostgreSQL 16, JUnit 5 + Mockito, Testcontainers, springdoc-openapi 2.8.9.
