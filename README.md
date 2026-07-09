# Auth Service API

Authentication REST API built with Java 21, Spring Boot, Spring Security, JWT, Spring Data JPA, PostgreSQL, and Maven.

## Features

- User registration with password hashing
- User login with JWT access and refresh token generation
- USER and ADMIN roles
- Protected endpoint returning the current authenticated user
- Simple logout with in-memory token blacklist
- Request validation
- Improved authentication error responses
- PostgreSQL persistence
- Unit tests for JWT and authentication services
- Protected endpoint tests with MockMvc and the security filter enabled
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
| POST | `/auth/login` | Public | Authenticate and return access and refresh tokens |
| POST | `/auth/refresh` | Public | Generate new tokens from a valid refresh token |
| POST | `/auth/logout` | Protected | Blacklist the current access token and optional refresh token |
| GET | `/users/me` | Protected | Return the current authenticated user |

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
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer"
}
```

### Refresh Token

```http
POST /auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### Logout

```http
POST /auth/logout
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### Current User

```http
GET /users/me
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

Example response:

```json
{
  "id": 1,
  "name": "Hiago Rossi",
  "email": "hiago@example.com",
  "role": "USER",
  "createdAt": "2026-07-09T10:00:00"
}
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
| `JWT_SECRET` | required | Secret used to sign JWTs. Use at least 32 characters. |
| `JWT_EXPIRATION_MS` | `3600000` | Access token expiration in milliseconds |
| `JWT_REFRESH_EXPIRATION_MS` | `604800000` | Refresh token expiration in milliseconds |

See `src/main/resources/application-example.properties` for a complete example.

## Running with Docker

Start PostgreSQL:

```bash
docker compose up -d
```

Run the API:

```bash
export JWT_SECRET="replace-with-a-secure-secret-at-least-32-characters"
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
$env:JWT_SECRET="replace-with-a-secure-secret-at-least-32-characters"
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
- JWT secret is required through `JWT_SECRET`; it is not hardcoded in the application config.
- Access and refresh tokens are stateless JWTs signed with the same secret and separated by a token-type claim.
- Users are registered with the `USER` role by default; `ADMIN` is available for future authorization rules.
- Logout uses an in-memory token blacklist. This is useful for local/demo behavior, but production deployments should store revoked tokens in Redis or a database because memory is lost on restart.
- Controller tests use MockMvc and include a protected endpoint test with the JWT filter enabled.
- Service unit tests cover JWT generation/validation and authentication rules.
- OpenAPI is generated from the Spring MVC controllers with springdoc-openapi.

## Future Improvements

- Email verification flow
- Flyway database migrations
- Persistent refresh token storage with rotation
- Redis-backed token blacklist
- ADMIN-only endpoints to demonstrate authorization rules
- Dockerfile for the API service

## License

This project is licensed under the MIT License.
