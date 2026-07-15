# EduApp REST API

A Spring Boot REST API for handling teachers and users at a school. It has JWT login, role-based permissions, soft delete, file uploads for attachments, and generates reports asynchronously in the background.

## Tech Stack

- Java 21, Spring Boot 3
- Spring Security for JWT-based, stateless authentication
- Spring Data JPA + Flyway for the database and migrations (no auto DDL)
- MySQL 8+
- Lombok and Jakarta Validation to cut down on boilerplate and validate input
- OpenAPI / Swagger UI for API docs you can try out in the browser
- Logback for logging, with MDC so each request has its own trace context

## Requirements

- Java 21
- MySQL 8+
- Gradle (wrapper included, no need to install it separately)

## Database Setup

Before running the app, create the database and a user for it:

```sql
CREATE DATABASE schoolapp9csrpro CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER 'user9'@'localhost' IDENTIFIED BY '12345';
GRANT ALL PRIVILEGES ON schoolapp9csrpro.* TO 'user9'@'localhost';
FLUSH PRIVILEGES;
```

Flyway takes care of the schema automatically — it runs every migration in `src/main/resources/db/migration/` the first time the app starts.

## Configuration

The app runs with the `dev` profile by default, and its settings are in `src/main/resources/application-dev.properties`.

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

> Production tip: don't commit real secrets. Set `app.security.secret-key` as an environment variable instead.

## Build & Run

```bash
./gradlew build          # Compile and package
./gradlew bootRun        # Start with the dev profile
./gradlew test           # Run all tests
./gradlew test --tests "gr.aueb.cf.eduapp.SomeTest"  # Run a single test
./gradlew clean          # Clean build output
```

The app runs on port **8080** by default.

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

Then use that token for every other request:
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

Pagination defaults to `page=0` and `size=5` — change that with the `page`, `size`, and `sort` query params.

### Eligible Report (async)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/eligible/report` | Public | Start async report generation; returns `jobId` |
| GET | `/eligible/report/{jobId}` | Public | Poll for report status/result |

## Password Policy

Passwords need at least 8 characters, plus:
- a digit
- a lowercase letter
- an uppercase letter
- a special character (`!@#$%^&+=`)

## Error Responses

Errors always come back as JSON. Validation errors also include a message for each field that failed.

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

Every entity extends `AbstractEntity` and uses soft delete — a `deleted` flag plus a `deletedAt` timestamp — so nothing actually gets removed from the database. Entities are identified by UUID outside the app; the internal `id` column never shows up in the API.

## API Docs (Swagger UI)

Once the app is running, just open:

```
http://localhost:8080/swagger-ui.html
```

## Project Structure

```
src/main/java/gr/aueb/cf/eduapp/
  api/             REST controllers
  authentication/  JWT logic + UserDetails
  core/            Error handling, exceptions, filters, OpenAPI setup
  dto/             Request/response objects (Java records + validation)
  mapper/          Converts entities to DTOs and back
  model/           JPA entities
  repository/      Spring Data repositories
  security/        Filter chain, JWT filter, CORS, entry points
  service/         Business logic
  validator/       Custom validators
```