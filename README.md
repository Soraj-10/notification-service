# Notification Service

A Spring Boot backend that sends email and SMS notifications, tracks their status, retries failures, and pushes live status updates over WebSocket.

## Features
- REST API to create, list, filter, view and resend notifications
- Email (Gmail SMTP) and SMS (Twilio) senders using the Strategy pattern
- Asynchronous sending with @Async
- Status tracking: PENDING, SENT, FAILED, with error messages
- Automatic retry (up to 3 attempts) and manual resend
- Pagination, filtering by status, and admin statistics
- JWT authentication, BCrypt passwords, USER and ADMIN roles
- Ownership: users see only their own notifications, admins see all
- Live status updates over WebSocket (JWT-checked handshake)
- PostgreSQL with Flyway migrations
- Swagger / OpenAPI docs
- Docker and Docker Compose
- Unit tests (JUnit, Mockito)

## Tech stack
Java 25, Spring Boot 4, Spring Data JPA, Spring Security, PostgreSQL, Flyway, WebSocket, springdoc-openapi, Docker, Maven.

## Run with Docker
1. Copy `.env.example` to `.env` and fill in the values (DB_USERNAME, DB_PASSWORD, MAIL_USERNAME, MAIL_PASSWORD, TWILIO_SID, TWILIO_TOKEN, TWILIO_FROM, JWT_SECRET).
2. Run: `docker compose up --build -d`
3. Open Swagger: http://localhost:8081/swagger-ui/index.html

## Using the API
1. POST /api/auth/register with username and password
2. POST /api/auth/login to get a token
3. Click Authorize in Swagger and paste the token
4. POST /api/notifications with type, recipient, subject, message

| Method | Endpoint | Access |
|---|---|---|
| POST | /api/auth/register | Public |
| POST | /api/auth/login | Public |
| POST | /api/notifications | Logged in |
| GET | /api/notifications | Own (admin sees all) |
| GET | /api/notifications/{id} | Owner or admin |
| POST | /api/notifications/{id}/resend | Owner or admin |
| GET | /api/notifications/stats | Admin only |

## Live updates
Open http://localhost:8081/ws-test.html, paste a JWT, and click Connect.

## Tests
`./mvnw test`

## Notes
- SMS needs a real Twilio account.
- Secrets live only in `.env`, which is git-ignored.