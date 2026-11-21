# LogiNext Driver Allocation System

## Overview

A REST-based driver allocation system developed using Java 17, Spring Boot, Spring Data JPA and PostgreSQL.

The application supports driver registration, driver management, and deterministic order allocation.

## Technology Stack

| Technology | Purpose |
|---|---|
| Java 17 | Programming language |
| Spring Boot 4.1.2 | REST application framework |
| Spring Data JPA | Database access |
| PostgreSQL 17 | Persistent storage |
| Flyway | Database schema migrations |
| Lombok | Reduce boilerplate |
| SpringDoc OpenAPI | Interactive API documentation |
| Maven | Build management |

## Business Rules

1. Only ACTIVE drivers are eligible for new allocations.
2. Allocate the lowest-numbered available driver.
3. A driver's next available time is calculated as `orderTime + travelTime`.
4. Reject an order when no eligible driver is available.
5. Preserve idempotency using the order reference.
6. Enforce chronological order processing.

## Running Locally

Prerequisites:

- Java 17
- Maven wrapper (included)
- PostgreSQL 17
- A configured `driver_allocation` database

Configure the database connection in `application.yml`.

Start the application:

```bash
./mvnw spring-boot:run
```

The local application is configured to run on port `8085`.

## API Documentation

Swagger UI:

`http://localhost:8085/swagger-ui/index.html`

OpenAPI JSON:

`http://localhost:8085/v3/api-docs`

## Driver Management APIs

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/drivers` | Register a driver |
| GET | `/api/v1/drivers` | Retrieve paginated drivers |
| GET | `/api/v1/drivers/{id}` | Retrieve a driver by ID |
| PATCH | `/api/v1/drivers/{id}/status` | Activate or deactivate a driver |

See `docs/API.md` for allocation API details and request examples.

## Architecture

The application uses a layered architecture:

Controller → Service → Repository → PostgreSQL

Allocation operations are coordinated using a PostgreSQL row-level lock to maintain consistency across concurrent requests.

## Documentation

- `docs/HLD.md` — High-Level Design
- `docs/LLD.md` — Low-Level Design
- `docs/API.md` — API reference
- `docs/DESIGN_DECISIONS.md` — Architectural trade-offs
