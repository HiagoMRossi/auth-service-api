# auth-service-api

A simple and production-style authentication REST API built with Spring Boot, Spring Security, JWT, and PostgreSQL.

## Features

- User registration
- Login with JWT
- Protected endpoint
- Request validation
- Global exception handling
- PostgreSQL persistence
- Basic tests

## Tech Stack

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- PostgreSQL
- Maven
- JWT
- MockMvc / JUnit

## Project Structure

- `controller`: REST endpoints for authentication and protected routes
- `service`: business logic for authentication and JWT operations
- `repository`: Spring Data JPA repositories
- `entity`: JPA entities mapped to the database
- `dto`: request and response payload classes
- `config`: security and JWT-related configuration
- `exception`: custom exceptions and global exception handling

## API Endpoints

### `POST /auth/register`

Creates a new user account.

Access: Public

Example request:

```http
POST /auth/register
Content-Type: application/json

{
  "name": "Hiago Rossi",
  "email": "hiago@example.com",
  "password": "123456"
}
```

Example response:

```json
{
  "id": 1,
  "name": "Hiago Rossi",
  "email": "hiago@example.com",
  "createdAt": "2026-04-22T17:00:00"
}
```

### `POST /auth/login`

Authenticates a user and returns a JWT token.

Access: Public

Example request:

```http
POST /auth/login
Content-Type: application/json

{
  "email": "hiago@example.com",
  "password": "123456"
}
```

Example response:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer"
}
```

### `GET /users/me`

Simple protected endpoint used to verify authenticated access.

Access: Protected

Example request:

```http
GET /users/me
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

Example response:

```json
{
  "message": "You are authenticated"
}
```

## Error Responses

- `400 Bad Request`: invalid request body or validation error
- `401 Unauthorized`: invalid credentials or missing/invalid authentication
- `409 Conflict`: e-mail already registered

## How to Run Locally

1. Install and start PostgreSQL locally.
2. Create a database named `auth_service_api`.
3. Review `src/main/resources/application.properties` and adjust credentials if needed.
4. Run the application with Maven Wrapper:

```bash
./mvnw spring-boot:run
```

On Windows PowerShell, use:

```powershell
.\mvnw spring-boot:run
```

The API will start on `http://localhost:8080`.

## How to Test

Run the test suite with Maven Wrapper:

```bash
./mvnw test
```

On Windows PowerShell, use:

```powershell
.\mvnw test
```

## Future Improvements

- Refresh token support
- Roles and authorities
- Docker setup
- Swagger / OpenAPI documentation
- Database migrations with Flyway

## Author

Hiago Rossi
