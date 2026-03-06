# EduApp REST API

A Spring Boot REST API for managing teachers and users in an educational context, featuring JWT-based authentication, role-based access control, soft-delete, file uploads, and async report generation.

## Tech Stack

- **Java 21** / **Spring Boot 3**
- **Spring Security** — stateless JWT authentication
- **Spring Data JPA** + **Flyway** — schema migrations, no DDL auto-update
- **MySQL 8+**
- **Lombok**, **Jakarta Validation**
- **OpenAPI / Swagger UI** — interactive API docs
- **Logback** — structured logging with MDC (request-scoped context)

## Requirements

- Java 21
- MySQL 8+
- Gradle (wrapper included)

## Database Setup

Create a MySQL database and user:

```sql
CREATE DATABASE schoolapp9csrpro CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER 'user9'@'localhost' IDENTIFIED BY '12345';
GRANT ALL PRIVILEGES ON schoolapp9csrpro.* TO 'user9'@'localhost';
FLUSH PRIVILEGES;
```

Flyway runs all migrations automatically on startup from `src/main/resources/db/migration/`.

## Configuration

The active profile is `dev` by default. Dev settings live in `src/main/resources/application-dev.properties`.

| Property | Default (dev) | Override via |
|---|---|---|
| DB host | `localhost` | `MYSQL_HOST` |
| DB port | `3306` | `MYSQL_PORT` |
| DB name | `schoolapp9csrpro` | `MYSQL_DB` |
| DB user | `user9` | `MYSQL_USER` |
| DB password | `12345` | `MYSQL_PASSWORD` |
| JWT secret | *(dev value)* | `app.security.secret-key` |
| JWT expiration | `10800000` ms (3h) | `app.security.jwt-expiration` |
| CORS origins | *(dev value)* | `allowed.origins` (comma-separated) |
| BCrypt strength | `12` | `security.bcrypt.strength` |

> **Production**: never commit real secrets. Inject `app.security.secret-key` via environment variable.

## Build & Run

```bash
./gradlew build          # Compile and package
./gradlew bootRun        # Start with the dev profile
./gradlew test           # Run all tests
./gradlew test --tests "gr.aueb.cf.eduapp.SomeTest"  # Run a single test
./gradlew clean          # Clean build output
```

The server starts on port **8080**.

## API Overview

Base path: `/api/v1`

### Authentication

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/auth/authenticate` | Public | Log in and receive a JWT |

**Request body:**
```json
{ "username": "alice", "password": "Secret1!" }
```
**Response:**
```json
{ "token": "<jwt>" }
```

Use the token in subsequent requests:
```
Authorization: Bearer <token>
```

### Users

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/users` | Public | Register a new user |
| GET | `/users/{uuid}` | Bearer | Get user by UUID |

### Teachers

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/teachers` | Public | Create a teacher |
| GET | `/teachers` | Bearer | List teachers (paginated + filtered) |
| GET | `/teachers/{uuid}` | Bearer | Get teacher by UUID |
| PUT | `/teachers/{uuid}` | Bearer | Update a teacher |
| DELETE | `/teachers/{uuid}` | Bearer | Soft-delete a teacher |
| POST | `/teachers/{uuid}/amka-file` | Public | Upload AMKA document file |

**Pagination defaults:** `page=0`, `size=5`. Pass `page`, `size`, and `sort` as query params.

### Eligible Report (async)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/eligible/report` | Public | Start async report generation; returns `jobId` |
| GET | `/eligible/report/{jobId}` | Public | Poll for report status/result |

## Password Policy

Passwords must be at least 8 characters and contain:
- One digit
- One lowercase letter
- One uppercase letter
- One special character (`!@#$%^&+=`)

## Error Responses

All errors return a JSON body. Validation failures include per-field messages.

| HTTP Status | Cause |
|---|---|
| 400 | Validation error |
| 401 | Missing or invalid JWT |
| 403 | Insufficient permissions |
| 404 | Resource not found |
| 409 | Resource already exists |
| 500 | File upload failure or unexpected error |

## Data Model

```
roles ──< roles_capabilities >── capabilities
  |
users ──── teachers ──── personal_information ──── attachments
                |
             regions
```

All entities extend `AbstractEntity` and support **soft delete** (`deleted` flag + `deletedAt`). Records are never hard-deleted. External identifiers are UUIDs — internal `id` columns are never exposed in the API.

## API Docs (Swagger UI)

Once the application is running, open:

```
http://localhost:8080/swagger-ui.html
```

## Project Structure

```
src/main/java/gr/aueb/cf/eduapp/
  api/           REST controllers
  authentication/  JWT logic + UserDetails
  core/          Error handler, exceptions, filters, OpenAPI config
  dto/           Request/response records (Java records + Jakarta validation)
  mapper/        Entity <-> DTO conversion
  model/         JPA entities
  repository/    Spring Data repositories
  security/      Filter chain, JWT filter, CORS, entry points
  service/       Business logic (interface + implementation)
  validator/     Custom Spring Validators
```