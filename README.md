# Cloud Expense Tracker

**Frontend:** https://dg7iiaitu5wcv.cloudfront.net  
**API Docs:** https://d29e2avpnvyf5w.cloudfront.net/swagger-ui/index.html  
**Backend API:** https://d29e2avpnvyf5w.cloudfront.net

A production-oriented personal finance REST API built with Spring Boot 3.5 and Java 21. The application provides authenticated expense management, email verification, password recovery, refresh-token based authentication, receipt storage in Amazon S3, Redis caching and distributed rate limiting, analytics, observability, automated tests, and Docker-based deployment.

## Tech Stack

- Java 21
- Spring Boot 3.5
- Spring Security
- JWT authentication
- Refresh tokens with persistence and revocation
- Spring Data JPA / Hibernate
- PostgreSQL
- Flyway
- Redis
- Bucket4j + Lettuce for distributed rate limiting
- Amazon S3
- AWS SDK for Java
- Spring Mail with Gmail SMTP
- springdoc-openapi / Swagger UI
- Docker
- Docker Compose
- GitHub Actions
- Spring Boot Actuator
- Micrometer / Prometheus
- JUnit 5
- Mockito
- MockMvc
- Testcontainers
- MapStruct
- Lombok
- Agent API keys
- Idempotency keys for JSON transaction mutations
- Machine-readable validation metadata for external agents

## Features

### Authentication & Account Security

- User registration
- Email format validation
- Password confirmation during registration
- Email verification with expiring verification tokens
- Login with JWT access tokens
- Persistent refresh tokens
- Refresh-token rotation
- Refresh-token revocation
- Logout
- Password reset with expiring reset tokens
- Password reset revokes all existing refresh tokens for the user
- Disabled/unverified-user enforcement during authentication
- BCrypt password hashing
- Global exception handling
- Correlation/request ID logging

Recovery-token storage is constrained to one active verification token and one active password-reset token per user through Flyway migration `V13`.

### AI / Agent-Consumable API

The REST API is designed to be consumed by both the human React application and
external AI agents or automation clients. The backend does **not** contain an AI
chatbot or conversation engine; it exposes the application's existing business
capabilities in a machine-friendly way.

- Public agent capability discovery through `GET /api/agent/capabilities`
- OpenAPI discovery through `/v3/api-docs`
- User-created, expiring and revocable agent API keys
- API keys are stored only as SHA-256 hashes
- `X-API-Key` authentication for external agents
- JSON representations of transaction create/update operations
- Structured validation responses containing field metadata
- Current transaction types and categories exposed through capability discovery
- Idempotency keys for JSON POST/PUT transaction mutations
- User-scoped authorization remains enforced for agent requests
- Optimistic locking remains enforced for JSON updates
- Existing multipart endpoints remain unchanged for the React UI and receipt uploads
- Destructive operations remain normal authenticated API operations; the backend
  does not implement an AI conversation/confirmation workflow

The agent layer is an additional client interface over the same business services.
It does not duplicate expense, analytics, authorization, or persistence logic.

### Expense Management

- Create expenses
- Retrieve a single expense
- Retrieve the authenticated user's expenses
- Update expenses
- Delete expenses
- User-specific data access
- Pagination
- Sorting through Spring Data `Pageable`
- Filtering by:
  - Multiple categories
  - Minimum amount
  - Maximum amount
  - Start date
  - End date
- Validation of amount ranges and date ranges
- Future expense dates are rejected
- Optimistic locking using an expense version
- Soft deletion of expenses
- Auditing fields
- Optional receipt attachment

### Receipt Storage

- Receipt uploads through multipart requests
- Amazon S3 object storage
- Private S3 objects
- UUID-based receipt object keys
- Presigned URLs for temporary receipt access
- Supported receipt types:
  - JPEG
  - PNG
  - WEBP
  - PDF
- Maximum receipt size: 10 MB
- Receipt replacement uploads the new object before deleting the old object
- Storage validation for file size, content type, and filename

### Analytics

The analytics API provides:

- Complete analytics dashboard
- Income/expense summary
- Category summary
- Monthly summary
- Spending trends
- Recent transactions

Analytics can be filtered by:

- From date
- To date
- Category
- Transaction type
- Recent-result limit

Analytics requests are validated and the recent-transactions limit is bounded to protect the application from excessively large requests.

### Redis Caching

Redis is used for:

- Expense lookup caching
- Analytics dashboard caching
- Analytics summary caching
- Category analytics caching
- Monthly analytics caching
- Spending trend caching
- Recent expense caching

Different analytics caches use different TTLs based on how frequently the data is expected to change.

Expense mutations invalidate the relevant expense and analytics caches.

### Rate Limiting

Rate limiting uses Bucket4j backed by Redis through Lettuce, allowing limits to be shared across application instances.

Production limits include:

| Tier | Limit | Purpose |
|---|---:|---|
| Authentication | 5 requests/minute/IP | Login and other authentication endpoints |
| Email actions | 3 requests/hour/IP | Password reset and verification-email requests |
| Receipt/multipart writes | 10 requests/minute/IP | Expense create/update requests containing multipart data |
| Analytics | 30 requests/minute/IP | Analytics endpoints |
| General API | 100 requests/minute/IP | Other API requests |

When a limit is exceeded, the API returns HTTP `429 Too Many Requests` with a `Retry-After` header.

The rate limiter also returns `X-RateLimit-Remaining`.

Trusted proxy configuration is available for deployments where the application needs to resolve the original client IP from `X-Forwarded-For`.

### Email

The application uses Spring Mail with Gmail SMTP:

```text
smtp.gmail.com:587
```

Email flows include:

- Registration verification email
- Password reset email
- Asynchronous email processing
- Transactional event listeners using `AFTER_COMMIT`

Email links are generated from `APP_FRONTEND_URL`, so verification and password-reset links can point to the deployed frontend.

> The current email implementation uses asynchronous Spring events and `@Async`; it is not a durable message-queue/outbox implementation.

## API Overview

Base path:

```text
/api
```

### Authentication

```text
POST /api/auth/register
GET  /api/auth/verify?token={token}
POST /api/auth/resend-verification
POST /api/auth/login
POST /api/auth/refresh
POST /api/auth/forgot-password
POST /api/auth/reset-password
POST /api/auth/logout
```

Registration requires a password confirmation.

The login response contains:

```json
{
  "accessToken": "...",
  "refreshToken": "..."
}
```

Refresh-token rotation is used when `/api/auth/refresh` is called: the old refresh token is revoked and a new refresh token is issued.

Password reset uses a token supplied in the reset request. Completing a password reset revokes all refresh tokens belonging to that user.

### Agent Access & Discovery

```text
GET    /api/agent/capabilities
POST   /api/agent-tokens
DELETE /api/agent-tokens/{id}
```

`GET /api/agent/capabilities` is public and returns non-user-specific metadata
describing the agent-consumable API contract.

`POST /api/agent-tokens` requires a normal user JWT. The generated `et_...` API key
is displayed once, stored server-side only as a SHA-256 hash, and can be revoked
through `DELETE /api/agent-tokens/{id}`.

External agents authenticate with:

```http
X-API-Key: et_...
```

Agent API keys are:
- user-scoped
- revocable
- automatically expiring
- limited to a maximum lifetime of 365 days
- never stored in plaintext

### JSON Transaction API

The existing multipart endpoints remain available for the React application and
receipt uploads.

Agents and other integrations can use JSON:

```text
POST /api/expenses
PUT  /api/expenses/{id}
```

with:

```http
Content-Type: application/json
X-API-Key: et_...
Idempotency-Key: <unique-key>
```

The same business services, validation, authorization, optimistic locking and
persistence are used as the human-facing API.

A request may intentionally be incomplete. The API returns a structured
`400 VALIDATION_ERROR` response that preserves the existing `errors` map and adds
machine-readable `fields` metadata. An external agent can interpret that response,
ask the human for missing information, and retry with a complete request.

Example:

```json
{
  "amount": 850
}
```

can result in missing-field information for `title`, `type`, `category`, and
`expenseDate`.

### Idempotency

JSON transaction `POST` and `PUT` requests support the `Idempotency-Key` header.

- Same user + same key + same request → the original operation result is reused.
- Same user + same key + different request → HTTP `409 Conflict`.
- Keys are limited to 128 characters.
- The key is scoped to the authenticated user.
- A SHA-256 request hash is persisted with the idempotency record.

This protects agents from creating duplicate transactions when a network failure
causes a retry.

### Expenses

```text
POST   /api/expenses
GET    /api/expenses
GET    /api/expenses/{id}
PUT    /api/expenses/{id}
DELETE /api/expenses/{id}
```

Create and update expense endpoints use `multipart/form-data` because an optional receipt can be included.

Expense list filtering supports multiple category values together with amount and date filters.

Pagination is supported through Spring Data `Pageable`, with a configured default page size of 20 and maximum page size of 100.

### Analytics

```text
GET /api/analytics/dashboard
GET /api/analytics/summary
GET /api/analytics/categories
GET /api/analytics/monthly
GET /api/analytics/trend
GET /api/analytics/recent
```

Common query parameters include:

```text
from
to
category
type
limit
```

The authenticated user is used to scope analytics results.

## API Documentation

OpenAPI documentation is generated automatically with springdoc-openapi.

Swagger UI:

```text
/swagger-ui/index.html
```

OpenAPI JSON:

```text
/v3/api-docs
```

The OpenAPI configuration exposes a Bearer JWT security scheme for authenticated endpoints.

### Authorizing Swagger requests

#### Human / JWT authentication

1. Register an account.
2. Verify the account email.
3. Login through `POST /api/auth/login`.
4. Copy the returned access token.
5. Click **Authorize** in Swagger UI.
6. Paste the token into the Bearer Authentication field.
7. Use the authenticated endpoints.

#### Agent API-key authentication

1. Login normally.
2. Create an agent API key with `POST /api/agent-tokens`.
3. Copy the `et_...` token immediately; it is shown only once.
4. In Swagger UI, authorize using the **Agent API Key** security scheme.
5. Use `X-API-Key` for endpoints that support external-agent authentication.

The public capability endpoint does not require authentication:

```text
GET /api/agent/capabilities
```

## Architecture

```text
                 Internet / AI Clients
                            |
                  +---------+---------+
                  |                   |
                  v                   v
              React UI          External Agents
                  |                   |
                  | JWT               | X-API-Key
                  +---------+---------+
                            |
                            v
                    +---------------+
                    |    Spring     |
                    |     Boot      |
                    +-------+-------+
                            |
              +-------------+-------------+
              |             |             |
              v             v             v
         PostgreSQL       Redis          S3
          Database       Cache &       Receipts
                         Rate Limit
              |
              v
        Flyway Migrations

Spring Boot
    |
    +---- Spring Security / JWT
    |
    +---- Expense & Analytics Services
    |
    +---- Gmail SMTP
    |
    +---- AWS S3
```

### AWS Deployment

The backend is containerized and can be deployed on Amazon EC2 using Docker Compose.

The application uses an AWS IAM role for S3 access through the AWS SDK rather than requiring long-lived AWS credentials inside the application.

```text
EC2
 |
 +-- Spring Boot container
 |
 +-- PostgreSQL container
 |
 +-- Redis container
 |
 +-- IAM instance role
          |
          v
      Amazon S3
```

The application can also be placed behind an HTTPS-capable reverse proxy/CDN/load-balancing layer without changing the Spring Boot application itself.

## Authentication Flow

```text
Register
   |
   v
Account created as disabled
   |
   v
Verification email
   |
   v
Email verified
   |
   v
Account enabled
   |
   v
Login
   |
   +----> Access Token
   |
   +----> Refresh Token
             |
             v
       Persisted in database
```

Authenticated requests use the access JWT.

When the access token needs to be renewed:

```text
Refresh Token
      |
      v
POST /api/auth/refresh
      |
      v
Validate old token
      |
      v
Revoke old token
      |
      +----> Issue new access token
      |
      +----> Issue new refresh token
```

Logging out revokes the supplied refresh token.

Password reset revokes all refresh tokens for the affected user.

## Email Verification Flow

```text
Registration
     |
     v
User disabled
     |
     v
UserRegisteredEvent
     |
     v
AFTER_COMMIT + @Async
     |
     v
Verification token created
     |
     v
Gmail SMTP
     |
     v
Verification link
     |
     v
GET /api/auth/verify?token=...
     |
     v
User enabled
```

Verification and password-reset tokens are persisted in PostgreSQL and expire after their configured lifetime.

## Password Reset Flow

```text
POST /api/auth/forgot-password
          |
          v
Password reset event
          |
          v
Existing reset token removed
          |
          v
New reset token created
          |
          v
Email sent asynchronously
          |
          v
Frontend reset page receives token
          |
          v
POST /api/auth/reset-password
          |
          v
Password changed
          |
          v
All user's refresh tokens revoked
```

Only the latest recovery token is retained for each user.

## Receipt Upload Flow

```text
Client
  |
  | multipart/form-data
  v
Spring Boot
  |
  +--> Validate size/type/name
  |
  v
Amazon S3
  |
  v
Receipt object key stored on Expense
  |
  v
Presigned URL generated
  |
  v
Client
```

When replacing an existing receipt:

```text
Upload new receipt
       |
       v
Update database
       |
       v
Delete old S3 object
```

This ordering avoids deleting the only valid receipt before a replacement upload succeeds.

## Optimistic Locking

Expenses use JPA optimistic locking through a `version` field.

Example:

```text
Client A reads expense
version = 5

Client B reads expense
version = 5

Client A updates
version = 6

Client B updates using version = 5
        |
        v
OptimisticLockConflictException
```

This prevents a stale client from silently overwriting a newer update.

## Soft Delete and Auditing

Expenses use Hibernate soft deletion:

```text
@SoftDelete
```

Deleted expenses are therefore excluded from normal queries rather than immediately removed from the database.

Expenses also inherit auditing fields from `BaseAuditableEntity`.

## Database & Migrations

PostgreSQL is the primary relational database.

Flyway manages schema evolution through versioned migrations:

```text
V1__Create_users_table.sql
V2__Create_expenses_table.sql
V3__Create_refresh_tokens_table.sql
...
V13__Enforce_single_recovery_token_per_user.sql
```

The migrations cover:

- Users
- Expenses
- Refresh tokens
- Account enablement
- Email verification tokens
- Password reset tokens
- Expense type
- Analytics indexes
- Auditing columns
- Optimistic-lock versioning
- Soft deletion
- Single active recovery token constraints
- Idempotency records for safe JSON transaction retries (`V14`)
- User-scoped agent API keys (`V15`)

Production schema changes should be introduced through new Flyway migrations rather than relying on Hibernate to modify the production schema.

## Observability

Spring Boot Actuator is exposed on a separate management port:

```text
8081
```

The application exposes:

- Health
- Liveness
- Readiness
- Metrics
- Prometheus

The readiness health group checks:

```text
readinessState
database
Redis
```

The application also contains an S3 health indicator that verifies access to the configured bucket.

Example:

```bash
docker exec expense-tracker-app \
  wget -qO- http://localhost:8081/actuator/health/readiness
```

The application uses correlation/request IDs and structured JSON logging in production.

## Docker Compose

The Docker Compose stack contains:

```text
Docker Compose
 |
 +-- expense-tracker-app
 |
 +-- expense-tracker-db
 |
 +-- expense-tracker-redis
```

Start the stack:

```bash
docker compose up -d --build
```

Check status:

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

PostgreSQL data is persisted through a Docker volume.

Redis uses AOF persistence through a Docker volume.

The application container exposes:

```text
8080
```

The management port:

```text
8081
```

is used internally by the container health check and is not published by the Compose configuration.

PostgreSQL and Redis ports are also not published to the host by the Compose configuration.

## Configuration

The application uses environment variables for secrets and environment-specific settings.

Important variables include:

```env
SPRING_PROFILES_ACTIVE=dev

DB_USERNAME=your_username
DB_PASSWORD=your_password

JWT_SECRET=your_jwt_secret

ACCESS_TOKEN_EMS=900000
REFRESH_TOKEN_EDATE=30
VERIFICATION_TOKEN_EHR=24
PASSWORD_RESET_TOKEN_EHR=1

MAIL_USERNAME=your_gmail_address
MAIL_PASSWORD=your_gmail_app_password
APP_CONTACT_EMAIL=your_contact_email

AWS_REGION=ap-south-1
AWS_BUCKET_NAME=your_bucket

APP_FRONTEND_URL=http://localhost:5173
```

The variable names above match the current Docker Compose/application configuration.

Never commit `.env` files or credentials to source control.

### Token configuration

The application supports configurable lifetimes for:

- Access JWT
- Refresh tokens
- Email verification tokens
- Password reset tokens

### File limits

```text
Maximum receipt size: 10 MB
Maximum multipart request size: 10 MB
```

Supported receipt content types:

```text
image/jpeg
image/png
image/webp
application/pdf
```

### Pagination safety

```text
Default page size: 20
Maximum page size: 100
```

## Local Development

### Prerequisites

- Java 21
- Docker
- Docker Compose
- PostgreSQL/Redis provided by Docker Compose
- AWS account and S3 bucket for receipt functionality
- Gmail account/app password for email functionality

Start the complete local stack:

```bash
docker compose up -d --build
```

Check:

```bash
docker compose ps
```

Readiness:

```bash
docker exec expense-tracker-app \
  wget -qO- http://localhost:8081/actuator/health/readiness
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI:

```text
http://localhost:8080/v3/api-docs
```

## Testing

The project uses:

- JUnit
- Mockito
- MockMvc
- Spring Security Test
- Testcontainers
- PostgreSQL Testcontainer
- Redis Testcontainer

Integration tests use containerized infrastructure instead of requiring a developer's local PostgreSQL/Redis installation.

Run the test suite:

```bash
./mvnw test
```

## CI

GitHub Actions is configured to build and test the application automatically.

The repository contains:

```text
.github/workflows/ci.yml
```

The CI workflow verifies the project through automated builds/tests.

## Project Structure

```text
src/
├── main/
│   ├── java/
│   │   └── com/aditya/expensetracker/expense_tracker/
│   │       ├── config/
│   │       ├── controller/
│   │       ├── dto/
│   │       ├── entity/
│   │       ├── event/
│   │       ├── exception/
│   │       ├── health/
│   │       ├── listener/
│   │       ├── logging/
│   │       ├── mapper/
│   │       ├── repository/
│   │       ├── security/
│   │       ├── service/
│   │       ├── specification/
│   │       └── validation/
│   └── resources/
│       ├── db/migration/
│       ├── templates/
│       ├── application.properties
│       ├── application-dev.properties
│       ├── application-prod.properties
│       └── logback-spring.xml
└── test/
    └── java/
        └── com/aditya/expensetracker/expense_tracker/
```

## Security

The application includes:

- BCrypt password hashing
- JWT access-token authentication
- User-scoped, expiring, revocable agent API keys (`X-API-Key`)
- User-scoped expiring/revocable agent API keys
- Persisted refresh tokens
- Refresh-token rotation
- Refresh-token revocation
- Password-reset token expiration
- Email-verification token expiration
- Disabled-user enforcement
- Request validation
- Global exception handling
- Distributed rate limiting
- Private S3 objects
- Presigned receipt URLs
- IAM-based S3 authentication
- PostgreSQL and Redis isolated inside Docker Compose
- Separate Actuator management port
- Environment-based secret configuration
- Correlation/request IDs for tracing requests

## Current Limitations / Future Improvements

The current implementation is intentionally a single Spring Boot service. Potential future improvements include:

- Transactional outbox for durable domain events
- Durable messaging with RabbitMQ, Amazon SQS, or Kafka
- Retry and dead-letter handling for email delivery
- Managed PostgreSQL with Amazon RDS
- Managed Redis with Amazon ElastiCache
- Infrastructure as Code with Terraform
- Fully automated AWS deployment through CI/CD
- Custom domain and end-to-end HTTPS architecture
- Centralized log aggregation
- Horizontal scaling behind a load balancer

## Author

Aditya Shukla

GitHub:

https://github.com/tiger3768



## Agent-Consumable API

The application is designed as an **agent-ready REST API** as well as a human-facing
web application. External AI agents can consume the same business API used by the
React frontend.

This is **not an MCP server** and the Spring Boot application does not contain an AI
chatbot or conversation engine. The backend exposes structured capabilities; an
external AI client decides how to interpret the user's request, ask clarification
questions, analyze data, or generate reports.

### Agent architecture

```text
                         Expense Tracker
                               |
                 +-------------+-------------+
                 |                           |
              React UI                 External AI Agent
                 |                           |
              JWT auth                  X-API-Key
                 |                           |
                 +-------------+-------------+
                               |
                         Spring Boot API
                               |
                  +------------+------------+
                  |            |             |
              PostgreSQL     Redis           S3
```

The REST API remains the canonical business interface. Agent requests use the same
services, repositories, authorization rules, validation and persistence as normal
requests.

### Capability discovery

Public discovery endpoint:

```text
GET /api/agent/capabilities
```

It exposes:

- API version
- authentication model
- supported transaction types
- supported categories
- available operations
- validation contract
- idempotency behavior

It contains no user-specific financial data.

The complete OpenAPI contract is available at:

```text
/v3/api-docs
/swagger-ui/index.html
```

### Agent API keys

A human user creates an agent credential through:

```text
POST /api/agent-tokens
Authorization: Bearer <JWT>
```

Example request:

```json
{
  "name": "My AI Assistant",
  "expirationDays": 30
}
```

The API returns an `et_...` token **once**.

The server stores only a SHA-256 hash of the token. The token is:

- user-scoped
- revocable
- automatically expiring
- limited to 1–365 days
- separate from JWT access and refresh tokens

An external agent uses:

```http
X-API-Key: et_...
```

The agent must never receive the user's password, refresh token, database
credentials, or S3 credentials.

### Agent transaction flow

The intended multi-step interaction is:

```text
Human:
"Add an ₹850 expense."

        |
        v

Agent sends incomplete JSON:
{
  "amount": 850
}

        |
        v

API returns:
400 VALIDATION_ERROR
with missing-field metadata

        |
        v

Agent asks the human for the missing information

        |
        v

Agent sends the completed request

        |
        v

201 Created
```

For example:

```http
POST /api/expenses
Content-Type: application/json
X-API-Key: et_...
Idempotency-Key: unique-request-key
```

```json
{
  "title": "Dinner",
  "amount": 850.00,
  "type": "EXPENSE",
  "category": "FOOD",
  "expenseDate": "2026-08-21"
}
```

The backend does not manage the conversation. It only reports structured facts and
enforces the normal API rules.

### Machine-readable validation

Incomplete or invalid JSON requests return HTTP `400` while preserving the
existing `errors` map used by the React application and adding structured metadata
for agents.

Conceptually:

```json
{
  "status": 400,
  "type": "VALIDATION_ERROR",
  "message": "Request contains missing or invalid fields.",
  "errors": {
    "title": "Title is required",
    "type": "Expense type is required",
    "category": "Category is required",
    "expenseDate": "Expense date is required"
  },
  "fields": {
    "title": {
      "message": "Title is required",
      "required": true
    },
    "type": {
      "message": "Expense type is required",
      "required": true,
      "allowedValues": ["INCOME", "EXPENSE"]
    }
  }
}
```

This lets an external agent determine what information is missing without requiring
a separate AI-specific workflow API.

### JSON versus multipart requests

The React application can continue using:

```text
POST /api/expenses
PUT  /api/expenses/{id}
Content-Type: multipart/form-data
```

for normal human interaction and receipt uploads.

External agents can use:

```text
POST /api/expenses
PUT  /api/expenses/{id}
Content-Type: application/json
```

for transaction operations that do not include receipt files.

These are two representations of the same business operations; they do not create
duplicate business logic.

### Idempotency

Agents may retry a request when a network failure leaves them unsure whether the
operation succeeded.

For JSON transaction creation and update, send:

```http
Idempotency-Key: <unique-key>
```

The key:

- is scoped to the authenticated user
- can contain up to 128 characters
- is associated with a SHA-256 hash of the request
- prevents duplicate processing when the same request is retried

Behavior:

```text
Same user + same key + same request
        -> original result is reused

Same user + same key + different request
        -> 409 Conflict
```

The corresponding database migration is:

```text
V14__Create_idempotency_records.sql
```

The current repository does **not** contain an idempotency cleanup script. The
idempotency records can instead be maintained through the EC2 operational cleanup
process used for this deployment.

### Optimistic locking

JSON updates still require the current expense `version`.

```text
Agent reads version 3
        |
        v
PUT with version 3
        |
        v
Success -> version 4
```

A stale version results in the existing optimistic-lock conflict behavior.

Idempotency and optimistic locking solve different problems:

- **Idempotency** protects against duplicate retries.
- **Optimistic locking** protects against stale concurrent updates.

### Reads and analytics

Agents can consume the existing authenticated endpoints:

```text
GET /api/expenses
GET /api/expenses/{id}

GET /api/analytics/dashboard
GET /api/analytics/summary
GET /api/analytics/categories
GET /api/analytics/monthly
GET /api/analytics/trend
GET /api/analytics/recent
```

This enables external agents to perform higher-level tasks such as:

- summarize spending
- compare months
- analyze category trends
- identify unusual transactions
- explain spending patterns
- prepare a financial report

The backend supplies the financial data; the external AI performs the reasoning and
presentation.

### Destructive operations

`DELETE /api/expenses/{id}` remains a normal authenticated, user-scoped operation.

The backend enforces authentication and authorization, but **does not implement a
mandatory AI confirmation protocol**.

The capability metadata recommends that external agents obtain human confirmation
before destructive actions. An AI client may choose how to handle confirmation based
on the user's instruction and its own safety policy.

This means:

```text
Backend:
"Is this request authenticated and authorized?"

AI client:
"Should I ask the human for confirmation?"

React:
"Can continue using its normal confirmation UI."
```

If a future version requires backend-enforced confirmation, that would be a separate
security feature rather than part of the current agent API.

### Human compatibility

No React migration is required for the agent-consumability layer.

The human application continues to use:

- JWT authentication
- existing multipart expense create/update
- receipt uploads
- existing CRUD operations
- existing analytics APIs
- existing validation responses

The new JSON endpoints, agent API keys, capability discovery and idempotency support
are additive.

### Example: AI financial analysis

Because the agent can authenticate and read the existing transaction and analytics
endpoints, an external AI can perform workflows such as:

```text
"Analyze my spending for the last six months."

        |
        v

GET /api/expenses
GET /api/analytics/categories
GET /api/analytics/monthly
GET /api/analytics/trend

        |
        v

AI analyzes the returned data

        |
        v

Natural-language explanation / report
```

The AI can also generate an external artifact such as a DOCX report if the AI
environment has document-generation capabilities. No DOCX generation service is
required inside the Expense Tracker backend.

### Security boundary

An agent API key is a credential for a user, not a privileged system credential.

The following remain enforced:

- authentication
- per-user authorization
- validation
- rate limiting
- optimistic locking
- S3 access controls
- existing Spring Security rules

Agent API keys do not grant access to another user's data.

## Current Limitations / Future Improvements

The current implementation is intentionally a single Spring Boot service. Potential
future improvements include:

- Backend-enforced confirmation tokens for destructive agent operations
- Transactional outbox for durable domain events
- Durable messaging with RabbitMQ, Amazon SQS, or Kafka
- Retry and dead-letter handling for email delivery
- Managed PostgreSQL with Amazon RDS
- Managed Redis with Amazon ElastiCache
- Infrastructure as Code with Terraform
- Fully automated AWS deployment through CI/CD
- Custom domain and end-to-end HTTPS architecture
- Centralized log aggregation
- Horizontal scaling behind a load balancer
- Optional MCP adapter if a native MCP integration becomes useful

The current agent layer deliberately does **not** implement an AI chatbot, LLM
integration, conversation state, or MCP server.

## Author

Aditya Shukla

GitHub:

https://github.com/tiger3768
