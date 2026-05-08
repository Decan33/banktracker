# BankTracker

BankTracker is a Spring Boot application for importing monthly bank transaction CSV files into MongoDB and exposing aggregated transaction statistics.

The project focuses on:

* CSV transaction imports
* Validation and resilient parsing
* MongoDB aggregations
* Monthly/category/IBAN statistics
* Integration testing with Testcontainers

---

# Features

## Transaction import

Upload CSV files containing bank transactions.

The importer:

* validates rows
* skips invalid records
* collects parsing errors
* stores import metadata
* prevents duplicate imports using file checksum

## Statistics

The API exposes:

* monthly statistics
* statistics grouped by category
* filtering by IBAN
* pageable transaction retrieval

## Logging

The application supports:

* readable local development logs
* structured JSON production logs

## Testing

Integration tests use:

* Testcontainers
* real MongoDB containers
* aggregation verification

---

# Tech Stack

* Java 17
* Spring Boot
* MongoDB
* Spring Data MongoDB
* FastCSV
* Testcontainers
* Docker Compose
* Lombok
* Swagger / OpenAPI

---

# Running the project

## Requirements

Install:

* Java 17+
* Docker Desktop
* Docker Compose

---

# Running MongoDB

Start MongoDB locally:

```bash
docker compose up -d
```

The application expects MongoDB on:

```text
localhost:27017
```

---

# Docker Compose

```yaml
services:
  mongodb:
    image: 'mongo:7.0'
    container_name: banktracker-db
    environment:
      - 'MONGO_INITDB_DATABASE=banktracker'
      - 'MONGO_INITDB_ROOT_PASSWORD=banktracker'
      - 'MONGO_INITDB_ROOT_USERNAME=banktracker'
    ports:
      - '27017:27017'
```

---

# Running the application
```bash
./gradlew bootRun
```

Application runs on:

```text
http://localhost:8080
```

---

# Swagger / OpenAPI

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI specification:

```text
http://localhost:8080/v3/api-docs
```

---

# API Overview

## Import transactions

```http
POST /api/v1/transaction-imports
```

Multipart form:

| Field | Type    |
| ----- | ------- |
| csv   | file    |
| iban  | string  |

Example:

```bash
curl -X POST http://localhost:8080/api/v1/transaction-imports \
  -F "csv=@bank_transactions.csv" 
```

---

## Monthly statistics

```http
GET /api/v1/transaction-statistics/months
```

Optional filters:

```text
?from=2024-01&to=2024-12&iban=PL...
```

---

## Category statistics

```http
GET /api/v1/transaction-statistics/categories?category=...
```

---

## Transactions

```http
GET /api/v1/transactions
```

Example:

```text
/api/v1/transactions?page=0&size=20&sort=transactionDate,desc
```

---

# CSV format

Expected CSV format:

```csv
iban,transactionDate,currency,transactionType,amount
PL94107510605753807963141749,2024-01,PLN,SALARY,5000.00
PL94107510605753807963141749,2024-01,PLN,GROCERIES,-120.50
```

### Note:
Project supports **only** PLN currency - it was a design compromise done late into the project development. There is a room to improve on it, namely introduce basic conversion between currencies (for example USD being `amount*3.5`).

---

# Supported transaction types

```text
SALARY
GROCERIES
RENT
TRANSPORT
ENTERTAINMENT
TRANSFER
OTHER
```

---

# Synthetic CSV generation

If a client does not supply application with their data, a synthetic CSV file can be generated with help of `generate_csv.py` script.
The generator supports:
* configurable row count
* configurable corruption rate (percentage of how rare an invalid cell can occurr)
* repeated IBAN pools for aggregation testing (can be resized for IBANs to be unique or to be more spread apart)

Example:

```bash
python generate_csv.py 2026 05
```
### Note:
First and second argument was introduced to speed up testing functionalities - editing a script to generate random dates is possible with one function invocation inside.

Generated files contain:

```csv
iban,transactionDate,currency,transactionType,amount
```

The generator intentionally produces some invalid rows to test parser resilience (can be opted out with `corruption_rate` being equal to `0.00`).

---

# Running tests

Run all tests:

```bash
./gradlew clean test
```

The project uses:

* JUnit 5
* Testcontainers
* MongoDB Testcontainers
* AssertJ
* Mockito

Integration tests require Docker Desktop running.

---

# Logging

The project supports profile-based logging.

## Local development

Readable colored logs:

```text
HH:mm:ss.SSS INFO [thread] logger - message
```

## Production

Structured JSON logs.

---

# Project Structure

```text
controller/
service/
repository/
aggregation/
model/
model/error/
model/response/
exception/
util/
```

# Import Strategy

The importer itself focuses around idempotency, checking for invalid rows and informing user what errors were found during an import. When importing, a status is returned as `COMPLETED_WITH_ERRORS`.
