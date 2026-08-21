# Agent-Consumable API

This document describes the agent-consumability layer of the Expense Tracker API.

The application does **not** contain an AI chatbot. External agents such as ChatGPT,
Claude, Gemini, or custom automation can call the same authenticated REST API that
the human React client uses.

## Design principle

The backend reports **structured facts**. The client decides how to present them.

- React presents validation errors as a form error.
- An AI agent interprets the same validation metadata and can ask the human for missing information.
- Business logic, authorization, and persistence remain shared.

## Discovery

Public capability metadata:

```http
GET /api/agent/capabilities
```

The complete OpenAPI contract is also available at:

```text
/v3/api-docs
/swagger-ui/index.html
```

`/api/agent/capabilities` contains no user data.

## Authentication

User operations require authentication.

Human clients can use:

```http
Authorization: Bearer <JWT>
```

For external agents, a user can create a dedicated API key:

```http
POST /api/agent-tokens
Authorization: Bearer <JWT>
```

The plaintext API key is returned once. It is stored server-side only as a SHA-256
hash and can be revoked by the user.

The external agent then uses:

```http
X-API-Key: et_...
```

This avoids giving an external agent the user's password or refresh token.

Agent keys are user-scoped and expire automatically. They are deliberately separate
from JWT access/refresh tokens.

The agent must never bypass authorization or attempt to use another user's
credentials.

## Creating a transaction

For normal JSON transactions, agents should use:

```http
POST /api/expenses
Content-Type: application/json
Authorization: Bearer <JWT>
Idempotency-Key: <unique-key>
```

Example:

```json
{
  "title": "Dinner",
  "amount": 850.00,
  "type": "EXPENSE",
  "category": "FOOD",
  "expenseDate": "2026-08-21"
}
```

Receipt uploads continue to use the existing multipart endpoint. JSON agent requests
do not upload receipt files.

## Multi-step interaction

An agent can intentionally start with incomplete information:

```json
{
  "amount": 850
}
```

The API returns HTTP 400 while preserving the existing `errors` map used by the
human frontend and adding structured metadata:

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
      "required": true,
      "allowedValues": []
    },
    "type": {
      "message": "Expense type is required",
      "required": true,
      "allowedValues": ["INCOME", "EXPENSE"]
    }
  }
}
```

The external agent can interpret this response, ask the human for the missing
information, and retry with a complete request.

The backend does **not** manage the conversation.

## Allowed transaction values

The API exposes the current enum values through `/api/agent/capabilities` and
through validation metadata when an enum is invalid.

Current transaction types:

- `INCOME`
- `EXPENSE`

Categories are sourced directly from the application's `Category` enum, so the
agent does not need to guess valid values.

## Idempotency

Agents can retry requests because networks fail and an agent may not know whether
a previous request succeeded.

For JSON POST and PUT transaction mutations, send:

```http
Idempotency-Key: <unique-key>
```

The key is scoped to the authenticated user and may contain up to 128 characters.

The server stores a SHA-256 hash of the request.

### Same key + same request

The existing transaction is returned instead of creating/updating it again.

### Same key + different request

The API returns:

```http
409 Conflict
```

This prevents an agent from accidentally reusing a key for a different operation.

The idempotency table should be periodically cleaned. A 24-hour retention window is
sufficient for this application's retry use case.

The repository includes `ops/idempotency-cleanup.sh`, which can be scheduled from
the same EC2 cron setup used by the application's other maintenance jobs.

Example EC2 cron entry:

```cron
45 3 * * * /opt/expense-tracker/cleanup/idempotency-cleanup.sh >> /opt/expense-tracker/cleanup/logs/idempotency.log 2>&1
```

Or run the included script manually:

```bash
/opt/expense-tracker/ops/idempotency-cleanup.sh
```

## Updates

Agents can use:

```http
PUT /api/expenses/{id}
Content-Type: application/json
Authorization: Bearer <JWT>
Idempotency-Key: <unique-key>
```

The current `version` is still required.

This preserves optimistic concurrency:

```text
Agent reads version 3
       ↓
Agent updates with version 3
       ↓
success → version becomes 4
```

A stale version still returns HTTP 409.

Idempotency protects against the agent retrying the same update after an uncertain
network result.

## Reads and analytics

The existing authenticated read and analytics endpoints remain available.

Agents can use them to answer requests such as:

> "How much did I spend on food this month?"

The API returns structured data; the external agent converts it into natural language.

## Rate limiting

The existing distributed Bucket4j + Redis rate limiting remains in place.

- JSON agent transaction requests use the normal API tier.
- Multipart receipt uploads continue to use the stricter upload tier.
- Analytics requests use the analytics tier.
- Authentication and email actions retain their dedicated limits.

HTTP 429 responses now include a machine-readable `type` and `retryAfterSeconds`
while retaining the existing human-readable `error` field.

## Destructive operations

`DELETE /api/expenses/{id}` remains a normal authenticated API operation.

The backend enforces ownership and authorization, but it does not attempt to manage
human confirmation conversations.

External agents should obtain explicit human confirmation before destructive actions.

This keeps the API client-neutral:

```text
Backend → validates authorization and operation
AI     → decides whether human confirmation is appropriate
React  → can continue using its existing confirmation UI
```

## Human compatibility

The existing multipart endpoints remain unchanged:

```http
POST /api/expenses
Content-Type: multipart/form-data

PUT /api/expenses/{id}
Content-Type: multipart/form-data
```

These are still used by the React application, particularly when receipts are
uploaded.

The JSON endpoints are additional representations of the same business operations.

No React migration is required for the agent-consumability layer.

## Security model

An AI agent is treated as an API client, not as a trusted principal.

Agent API keys are user-scoped, expiring, revocable credentials. The server stores
only a SHA-256 hash of each key.

The following remain mandatory:

- JWT authentication
- per-user authorization
- optimistic locking
- validation
- rate limiting
- S3 access controls
- existing Spring Security rules

The agent must never receive direct PostgreSQL or S3 credentials.

## Example complete flow

```text
Human:
"Add an ₹850 expense."

Agent:
POST /api/expenses
{
  "amount": 850
}

API:
400 VALIDATION_ERROR
missing title/type/category/date

Agent:
asks human for missing information

Human:
"Dinner, expense, food, today."

Agent:
POST /api/expenses
{
  "title": "Dinner",
  "amount": 850,
  "type": "EXPENSE",
  "category": "FOOD",
  "expenseDate": "2026-08-21"
}
Idempotency-Key: 01J...

API:
201 Created

Agent:
"Dinner for ₹850 was added."
```
