# Utility Billing System

Spring Boot backend for utility billing: customers, meters, readings, tariffs, bills, payments, and role-based access (Admin, Operator, Finance, Customer).

## Requirements

- Java 17+
- PostgreSQL
- Maven (or use `./mvnw`)

## Setup

1. Clone the repository.
2. Copy `src/main/resources/application.properties.example` to `application.properties` and set your database and mail credentials.
3. Create the database: `utility_billing_db`
4. Run the application:

```bash
./mvnw spring-boot:run
```

5. Open Swagger UI: `http://localhost:8081/swagger-ui.html`

## Default admin (seeded on first run)

| Email | Password |
|-------|----------|
| admin@utility.com | Admin@123 |

## API roles

| Role | Main responsibilities |
|------|------------------------|
| ADMIN | Configure tariffs, approve bills, manage users |
| OPERATOR | Capture meter readings |
| FINANCE | Approve bills and process payments |
| CUSTOMER | View bills and payment history |

## License

MIT
