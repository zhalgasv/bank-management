# Bank Card Management API

Backend application for managing bank cards. The project implements JWT authentication, role-based access, card management, masking/encryption of card numbers, user card operations, transfers between own cards, PostgreSQL storage, Liquibase migrations, and unit tests.

## Tech Stack

- Java 17
- Spring Boot
- Spring Security + JWT
- Spring Data JPA / Hibernate
- PostgreSQL
- Liquibase
- Docker Compose
- JUnit 5, Mockito, AssertJ
- Maven

## Features

### Authentication

- User registration
- User login
- JWT-based authentication
- Roles: `ADMIN`, `USER`

### Admin

- Create cards for users
- View all cards with pagination and filtering by status
- Block cards
- Activate cards
- Delete cards

### User

- View own cards with pagination and filtering by status
- Request card blocking
- Transfer money between own active cards
- View balance of own card

### Security

- Card number is encrypted before saving
- Card number hash is stored for uniqueness checks
- API responses expose only masked card number, for example `**** **** **** 1234`
- Users can access only their own cards
- Admin endpoints are protected by `ADMIN` role

## Requirements

- Java 17+
- Docker
- Maven Wrapper is included, so local Maven installation is not required

## Environment Variables

The application has default development values in `application.properties`.

| Variable | Description |
| --- | --- |
| `JWT_SECRET` | Base64 encoded secret for signing JWT tokens |
| `CARD_ENCRYPTION_KEY` | Base64 encoded AES key. Must decode to 16, 24, or 32 bytes |

Default dev values are already configured:

```properties
server.port=8083
spring.datasource.url=jdbc:postgresql://localhost:5434/bank_cards
spring.datasource.username=bank_user
spring.datasource.password=bank_password
```

## Run Locally

Start PostgreSQL:

```bash
docker compose up -d postgres
```

Run the application:

```bash
./mvnw spring-boot:run
```

The API will be available at:

```text
http://localhost:8083
```

Run tests:

```bash
./mvnw test
```

## Default Admin User

On application startup, a default admin user is created if it does not already exist:

```text
username: admin
password: admin12345
role: ADMIN
```

## API Examples

### Register User

```bash
curl -i -X POST http://localhost:8083/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "adil",
    "email": "adil@example.com",
    "password": "password123"
  }'
```

### Login

```bash
curl -X POST http://localhost:8083/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin12345"
  }'
```

Use the returned token in protected requests:

```bash
-H "Authorization: Bearer YOUR_TOKEN"
```

## Admin API

### Create Card

```bash
curl -i -X POST http://localhost:8083/api/admin/cards \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ADMIN_TOKEN" \
  -d '{
    "ownerId": 1,
    "expiryDate": "2029-12",
    "initialBalance": 10000.00
  }'
```

### View All Cards

```bash
curl -i "http://localhost:8083/api/admin/cards?page=0&size=10" \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

With status filter:

```bash
curl -i "http://localhost:8083/api/admin/cards?page=0&size=10&status=ACTIVE" \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

### Block Card

```bash
curl -i -X PATCH http://localhost:8083/api/admin/cards/1/block \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

### Activate Card

```bash
curl -i -X PATCH http://localhost:8083/api/admin/cards/1/activate \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

### Delete Card

```bash
curl -i -X DELETE http://localhost:8083/api/admin/cards/1 \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

## User API

### View Own Cards

```bash
curl -i "http://localhost:8083/api/cards/my?page=0&size=10" \
  -H "Authorization: Bearer USER_TOKEN"
```

With status filter:

```bash
curl -i "http://localhost:8083/api/cards/my?page=0&size=10&status=ACTIVE" \
  -H "Authorization: Bearer USER_TOKEN"
```

### Request Card Blocking

```bash
curl -i -X PATCH http://localhost:8083/api/cards/1/block-request \
  -H "Authorization: Bearer USER_TOKEN"
```

### Transfer Between Own Cards

```bash
curl -i -X POST http://localhost:8083/api/cards/transfer \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer USER_TOKEN" \
  -d '{
    "fromCardId": 1,
    "toCardId": 2,
    "amount": 500.00
  }'
```

Successful transfer returns:

```text
HTTP/1.1 204 No Content
```

### View Card Balance

```bash
curl -i http://localhost:8083/api/cards/1/balance \
  -H "Authorization: Bearer USER_TOKEN"
```

Example response:

```json
{
  "cardId": 1,
  "balance": 9500.00
}
```

## Card Statuses

```text
ACTIVE
BLOCKED
EXPIRED
```

## Error Handling

The API returns structured error responses:

```json
{
  "timestamp": "2026-06-27T12:00:00Z",
  "status": 400,
  "message": "Validation Failed",
  "path": "/api/auth/register",
  "validationErrors": {
    "email": "must be a well-formed email address"
  }
}
```

Common statuses:

- `400 Bad Request`: validation error or invalid transfer
- `401/403`: missing token or insufficient permissions
- `404 Not Found`: card or user not found
- `409 Conflict`: username or email already exists

## Database Migrations

Liquibase changelog:

```text
src/main/resources/db/changelog/db.changelog-master.yaml
```

Hibernate is configured with:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

This means the database schema is managed by Liquibase migrations, not generated automatically by Hibernate.

## Tests

Current test coverage includes:

- `CardDataProtectorTest`
  - card masking
  - SHA-256 hashing
  - AES/GCM encryption behavior
  - invalid card data validation
- `CardServiceTransferTest`
  - successful transfer between own cards
  - insufficient funds
  - transfer to the same card
- `BankManagementApplicationTests`
  - Spring context smoke test

Run:

```bash
./mvnw test
```

