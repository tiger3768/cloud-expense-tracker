# Cloud Expense Tracker

**Try it out:** [http://43.204.112.246:8080](http://43.204.112.246:8080) &nbsp;|&nbsp; **API docs:** [Swagger UI](http://43.204.112.246:8080/swagger-ui/index.html)

A production-oriented backend REST API for tracking personal expenses, built with Spring Boot and deployed to AWS using Docker Compose.

## Tech Stack

- Java 21
- Spring Boot
- Spring Security
- JWT Authentication
- Spring Data JPA
- Hibernate
- PostgreSQL
- Flyway
- Redis
- Amazon S3
- Spring Mail
- springdoc-openapi (Swagger UI)
- Docker
- Docker Compose
- GitHub Actions
- AWS EC2
- AWS IAM
- JUnit
- Mockito
- MockMvc
- Testcontainers

## Features

### Authentication & Security

- User registration
- Email verification
- User login
- JWT access tokens
- Refresh tokens
- Refresh token revocation
- Password reset
- BCrypt password hashing
- Disabled-user enforcement
- Rate limiting
- Global exception handling
- Request ID / correlation ID logging

### Expense Management

- Create expenses
- Retrieve expenses
- Update expenses
- Delete expenses
- Expense filtering
- Pagination
- Optimistic locking
- User-specific expense access

### Receipt Storage

- Receipt uploads
- Amazon S3 object storage
- Presigned URLs for receipt access
- Receipt replacement
- Private S3 objects

### Analytics

- Analytics dashboard
- Expense summary
- Category analytics
- Monthly analytics
- Spending trends
- Recent expenses

### Caching

- Redis
- Spring Cache
- Analytics caching
- Expense caching
- Cache invalidation after expense changes

### Database

- PostgreSQL
- Spring Data JPA
- Hibernate
- Flyway database migrations

### Email

- Email verification
- Password reset emails
- Asynchronous email processing
- SMTP integration

### Observability

- Spring Boot Actuator
- Liveness checks
- Readiness checks
- Application metrics
- Prometheus endpoint
- Structured application logging

### API Documentation

- OpenAPI 3 specification generated via springdoc-openapi
- Interactive Swagger UI for exploring and testing endpoints directly in the browser
- Request/response schemas documented for every endpoint

## API Documentation

Interactive, browsable API docs are available via Swagger UI, generated automatically from the codebase with springdoc-openapi — no separate deployment step or manually-maintained docs required.

- Swagger UI: `/swagger-ui/index.html`
- Raw OpenAPI 3 spec (JSON): `/v3/api-docs`

Live deployment:

- Swagger UI: [http://43.204.112.246:8080/swagger-ui/index.html](http://43.204.112.246:8080/swagger-ui/index.html)
- OpenAPI spec: [http://43.204.112.246:8080/v3/api-docs](http://43.204.112.246:8080/v3/api-docs)

Locally (after starting the stack, see [Local Development](#local-development)):

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI spec: `http://localhost:8080/v3/api-docs`

Both paths are publicly reachable without a token — only the underlying API endpoints require authentication, so anyone can browse available endpoints and schemas before signing up.

### Authorizing requests in Swagger UI

Most endpoints require a bearer JWT. To call them from the Swagger UI:

1. Call `POST /api/auth/register`, then `POST /api/auth/login` to obtain an access token.
2. Click **Authorize** (top right of the Swagger UI page).
3. Paste the access token into the **Bearer Authentication** field (just the token — Swagger UI adds the `Bearer ` prefix for you).
4. All subsequent "Try it out" requests will include the token until it expires or you log out.

## Architecture

```text
                         Internet
                            |
                            v
                    +---------------+
                    |    AWS EC2    |
                    |               |
                    | Docker Compose|
                    +-------+-------+
                            |
             +--------------+--------------+
             |              |              |
             v              v              v
       Spring Boot     PostgreSQL       Redis
             |
             +--------------------+
             |                    |
             v                    v
          Amazon S3             SMTP
        Receipt Storage          Email
```

### AWS Architecture

```text
                         Internet
                            |
                            v
                    +---------------+
                    |      EC2      |
                    |               |
                    | Spring Boot   |
                    | PostgreSQL    |
                    | Redis         |
                    +-------+-------+
                            |
                     IAM Instance Role
                            |
                            v
                           S3
```

The EC2 instance accesses Amazon S3 through an IAM role. Long-lived AWS access keys are not required inside the application container.

## Authentication Flow

```text
Register
   |
   v
Email Verification
   |
   v
Login
   |
   v
Access Token + Refresh Token
   |
   v
Authenticated API Requests
```

JWT authentication validates the token and the current user account status before creating the Spring Security authentication context.

## Optimistic Locking

Expenses use optimistic locking to prevent lost updates.

```text
Client A reads expense
version = 5

Client B reads expense
version = 5

Client A updates
version = 6

Client B attempts update with version = 5
        |
        v
Optimistic Lock Conflict
```

A stale update is rejected instead of silently overwriting a newer update.

## Receipt Upload Flow

```text
Client
   |
   v
Spring Boot
   |
   v
Amazon S3
   |
   v
S3 Object Key stored with Expense
   |
   v
Presigned URL returned by API
```

When replacing a receipt, the new receipt is uploaded before the old receipt is deleted, to avoid losing the existing receipt if the new upload fails.

## Redis Caching

Redis is used through Spring Cache for frequently accessed data such as:

- Expense retrieval
- Analytics dashboard
- Analytics summary
- Category analytics
- Monthly analytics
- Spending trends
- Recent expenses

Relevant caches are evicted when expense data changes.

## Rate Limiting

Rate limiting is applied to protect sensitive and high-traffic endpoints, including authentication and email-related operations.

This helps reduce:

- Brute-force authentication attempts
- Repeated password reset requests
- Verification email abuse
- Excessive API requests

## Database Migrations

Flyway manages database schema changes through versioned migration scripts.

Example:

```text
V1__...
V2__...
V3__...
```

Production schema evolution is therefore managed explicitly, rather than relying on Hibernate's automatic schema generation.

## Observability

Spring Boot Actuator provides:

- Health checks
- Liveness state
- Readiness state
- Metrics
- Prometheus endpoint

The application uses:

```text
8080 - REST API
8081 - Actuator / management
```

Port `8081` is kept internal and is not exposed publicly by Docker Compose.

Example readiness check:

```bash
docker exec expense-tracker-app \
  wget -qO- http://localhost:8081/actuator/health/readiness
```

Expected:

```json
{
  "status": "UP"
}
```

## Docker

The application and its infrastructure run through Docker Compose.

```text
Docker Compose
 |
 +-- Spring Boot
 |
 +-- PostgreSQL
 |
 +-- Redis
```

Start the stack:

```bash
docker compose up -d --build
```

Check containers:

```bash
docker compose ps
```

View application logs:

```bash
docker compose logs -f app
```

Stop the stack:

```bash
docker compose down
```

## Environment Variables

Create a `.env` file for local or server configuration.

Example:

```env
SPRING_PROFILES_ACTIVE=dev

DB_USERNAME=your_username
DB_PASSWORD=your_password

JWT_SECRET=your_jwt_secret

ACCESS_TOKEN_EMS=900000
REFRESH_TOKEN_EDATE=30
VERIFICATION_TOKEN_EHR=24
PASSWORD_RESET_TOKEN_EHR=1

MAIL_USERNAME=your_email
MAIL_PASSWORD=your_app_password
APP_CONTACT_EMAIL=your_email

AWS_REGION=your_region
AWS_BUCKET_NAME=your_bucket

APP_BASE_URL=http://localhost:8080
```

Never commit `.env` or credentials to the repository.

## Local Development

### Prerequisites

- Java 21
- Docker
- Docker Compose
- AWS account for S3 functionality
- SMTP credentials for email functionality

Start the application:

```bash
docker compose up -d --build
```

Check container status:

```bash
docker compose ps
```

Check application readiness:

```bash
docker exec expense-tracker-app \
  wget -qO- http://localhost:8081/actuator/health/readiness
```

Once running, explore the API via Swagger UI at `http://localhost:8080/swagger-ui/index.html`.

## AWS Deployment

The application is deployed to an AWS EC2 instance using Docker Compose.

Deployment components:

- Amazon EC2
- Docker
- Docker Compose
- PostgreSQL
- Redis
- Amazon S3
- AWS IAM

### S3 Authentication

The EC2 instance uses an IAM instance role for S3 access:

```text
Spring Boot
    |
    v
AWS SDK
    |
    v
EC2 IAM Role
    |
    v
Amazon S3
```

AWS access keys do not need to be stored inside the application container.

### Network Exposure

The deployment exposes the application on port `8080`.

The following services are not publicly exposed:

```text
5432 - PostgreSQL
6379 - Redis
8081 - Actuator
```

SSH access is restricted through the EC2 security group.

## Testing

The project uses:

- JUnit
- Mockito
- MockMvc
- Testcontainers

Testcontainers is used for integration testing with infrastructure such as PostgreSQL and Redis.

Run tests:

```bash
./mvnw test
```

## CI/CD

GitHub Actions is used to build and test the application automatically.

The CI pipeline verifies that the application builds successfully and that automated tests pass.

## Project Structure

```text
src/
└── main/
    └── java/
        └── com/aditya/expensetracker/expense_tracker/
            ├── config/
            ├── controller/
            ├── dto/
            ├── entity/
            ├── event/
            ├── exception/
            ├── listener/
            ├── mapper/
            ├── repository/
            ├── security/
            ├── service/
            └── specification/
```

## Security Considerations

The application implements several security measures:

- BCrypt password hashing
- JWT authentication
- Refresh token persistence and revocation
- Email verification
- Password reset token expiration
- Disabled-user enforcement
- Rate limiting
- Private S3 objects
- Presigned S3 URLs
- IAM-based AWS authentication on EC2
- PostgreSQL and Redis are not publicly exposed
- Actuator management port is not publicly exposed
- Secrets are supplied through environment variables

## Future Improvements

Potential future improvements include:

- Transactional outbox for durable asynchronous events
- Durable message broker such as RabbitMQ or Amazon SQS
- Retry and dead-letter handling for email delivery
- HTTPS with a custom domain
- Reverse proxy or API Gateway
- Managed PostgreSQL using Amazon RDS
- Managed Redis using Amazon ElastiCache
- Centralized logging
- Infrastructure as Code using Terraform
- Automated AWS deployment through CI/CD

## Author

Aditya Shukla

GitHub: [https://github.com/tiger3768](https://github.com/tiger3768)
