# Auth Service API

Authentication REST API built with Java 21, Spring Boot, Spring Security, JWT, Spring Data JPA, PostgreSQL, and Maven.

## Features

- User registration with password hashing
- User login with JWT access token generation
- Protected endpoint for authenticated users
- Request validation
- Global exception handling
- PostgreSQL persistence
- HTTP endpoint tests with MockMvc
- OpenAPI documentation with Swagger UI

## Architecture

- `controller`: REST endpoints for authentication and user access
- `service`: authentication flow, JWT generation, and user lookup
- `repository`: Spring Data JPA repositories
- `entity`: JPA user model
- `dto`: request and response payloads
- `config`: Spring Security and JWT configuration
- `exception`: custom exceptions and API error handling

The API keeps authentication rules in the service layer, exposes DTOs at the HTTP boundary, and stores users through Spring Data JPA.

## API Endpoints

| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/auth/register` | Public | Register a new user |
| POST | `/auth/login` | Public | Authenticate and return a JWT |
| GET | `/users/me` | Protected | Verify the current authenticated user |

### Register

```http
POST /auth/register
Content-Type: application/json

{
  "name": "Hiago Rossi",
  "email": "hiago@example.com",
  "password": "123456"
}
```

### Login

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

### Current User

```http
GET /users/me
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

## OpenAPI

After starting the application, access:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/auth_service_api` | PostgreSQL JDBC URL |
| `DB_USERNAME` | `postgres` | Database username |
| `DB_PASSWORD` | `postgres` | Database password for local development |
| `DDL_AUTO` | `update` | Hibernate schema strategy |
| `SHOW_SQL` | `false` | Enables SQL logging |
| `FORMAT_SQL` | `false` | Formats SQL logs |
| `JWT_SECRET` | development placeholder | Secret used to sign JWTs. Replace it before running outside local development. |
| `JWT_EXPIRATION_MS` | `3600000` | Token expiration in milliseconds |

See `src/main/resources/application-example.properties` for a complete example.

## Running with Docker

Start PostgreSQL:

```bash
docker compose up -d
```

Run the API:

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

## Running Tests

```bash
./mvnw test
```

On Windows PowerShell:

```powershell
.\mvnw.cmd test
```

## Technical Decisions

- Passwords are hashed before persistence using Spring Security.
- JWT settings are externalized through environment variables.
- Controller tests use MockMvc and mocked services to validate HTTP behavior quickly.
- OpenAPI is generated from the Spring MVC controllers with springdoc-openapi.

## Future Improvements

- Refresh token support
- Roles and authorities
- Email verification flow
- Flyway database migrations
- Integration tests for the full authentication flow
- Dockerfile for the API service

## License

This project is licensed under the MIT License.
