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

Example:

```bash
curl -X POST http://localhost:8080/api/v1/transaction-imports \
  -F "csv=@bank_transactions.csv" 
```

Response:
```
{
  "importTransactionId": "431b3f08-c80c-452d-b851-a85e6921b0df",
  "status": "COMPLETED",
  "importedRows": 100000,
  "skippedRows": 0,
  "errors": []
}
```

---

## Monthly statistics

```http
GET /api/v1/transaction-statistics/monthly
```

Optional filters:

```text
?from=2024-01&to=2024-12
```

Response:
```text
[
  {
    "month": "2026-01",
    "transactionCount": 9096,
    "income": 25679590.68,
    "expense": 19514222.49,
    "net": 6165368.19
  },
  {
    "month": "2026-03",
    "transactionCount": 10000,
    "income": 28525681.71,
    "expense": 21320988.94,
    "net": 7204692.77
  },
  {
    "month": "2026-04",
    "transactionCount": 10000,
    "income": 27414518.80,
    "expense": 21756343.48,
    "net": 5658175.32
  }
]
```

---

## Category statistics

```http
GET /api/v1/transaction-statistics/categories?category=...
```

Response:
```text
[
  {
    "month": "2026-01",
    "transactionCount": 1320,
    "income": 3296883.42,
    "expense": 3305299.17,
    "net": -8415.75
  },
  {
    "month": "2026-03",
    "transactionCount": 1471,
    "income": 3704773.49,
    "expense": 3651991.45,
    "net": 52782.04
  }, (...)
```

---

## Sort by IBAN

```http
GET /api/v1/transaction-statistics/iban?iban=...
```

Response:
```text
{
  "content": [
    {
      "iban": "PL04062624303764478473683593",
      "transactionDate": "2026-07",
      "currency": "PLN",
      "transactionType": "RENT",
      "amount": -1971.4
    },
    {
      "iban": "PL04062624303764478473683593",
      "transactionDate": "2026-07",
      "currency": "PLN",
      "transactionType": "TRANSFER",
      "amount": -8656.53
    },
    {
      "iban": "PL04062624303764478473683593",
      "transactionDate": "2026-07",
      "currency": "PLN",
      "transactionType": "TRANSPORT",
      "amount": -3934.56
    },(...)
```

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
