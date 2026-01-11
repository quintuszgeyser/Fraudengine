***************************************************************************
*                                                                         *
*                             FRAUDENGINE                                 *
*                                                                         *
***************************************************************************

FraudEngine is a Spring Boot–based fraud detection service that evaluates
financial transactions using configurable, rule‑based logic.

The application supports both REST (JSON) and ISO‑8583 message processing,
persists all transactions, and exposes APIs for investigation, monitoring,
and operational health.

This repository contains the full production‑grade application, including
Docker support for local development and testing.

===========================================================================
 WHAT FRAUDENGINE DOES
===========================================================================

FraudEngine:

  * Accepts transactions via REST API
  * Accepts ISO‑8583 messages over a raw TCP listener
  * Applies multiple fraud detection rules per transaction
  * Flags suspicious transactions
  * Persists all transactions in PostgreSQL
  * Allows querying of flagged and historical transactions
  * Exposes health and monitoring endpoints

===========================================================================
 FRAUD RULES
===========================================================================

Fraud detection is implemented as a pluggable rule engine.
Each rule is evaluated independently and can be enabled, disabled,
or tuned via configuration.

Currently implemented rules:

  [1] HIGH AMOUNT
      - Flags transactions above a configurable monetary threshold

  [2] LOCATION RISK
      - Flags transactions originating from configured risky locations
      - Optional country or region whitelisting

  [3] VELOCITY
      - Flags accounts performing a high number of transactions within
        a short rolling time window

A transaction is flagged if ANY rule matches.

===========================================================================
 RUNNING WITH DOCKER (RECOMMENDED)
===========================================================================

The easiest way to run FraudEngine locally is with Docker Compose.

This will start:
  * PostgreSQL
  * FraudEngine application
  * ISO‑8583 TCP listener

-----------------------------------
 REQUIREMENTS
-----------------------------------

  * Docker
  * Docker Compose

-----------------------------------
 DOCKER CONFIGURATION
-----------------------------------

Ensure there is a file named, here config can be easily configured for the fraud rules:

  docker/.env

Example configuration:

  SERVER_PORT=8080
  ISO8583_PORT=8037

  POSTGRES_DB=frauddb
  POSTGRES_USER=frauduser
  POSTGRES_PASSWORD=Fraud

  DB_URL=jdbc:postgresql://fraudengine-postgres:5432/frauddb
  DB_USERNAME=frauduser
  DB_PASSWORD=Fraud

  FRAUD_HIGH_AMOUNT_ENABLED=true
  FRAUD_HIGH_AMOUNT_THRESHOLD=1000

  FRAUD_RULES_LOCATION_ENABLED=true
  FRAUD_RULES_LOCATION_RISKY_VALUES=UNKNOWN,RISKY-COUNTRY,NORTH-KOREA,IRAN
  FRAUD_RULES_LOCATION_WHITELIST_VALUES=SOUTH-AFRICA,NAMIBIA,BOTSWANA

  FRAUD_RULES_VELOCITY_WINDOW_MINUTES=5
  FRAUD_RULES_VELOCITY_MAX_COUNT=10

-----------------------------------
 STARTING THE APPLICATION
-----------------------------------

From the project root directory:
*Ensure Docker is running.

1)Build the SNAPSHOT JAR (tests included)
Run:   mvnw clean package
  
2)Then run docker:
Run:  docker compose up --build

Application endpoints:

  REST API        : http://localhost:8080
  Swagger UI     : http://localhost:8080/swagger-ui/index.html
  ISO‑8583 TCP   : localhost:8037
  Health Check   : http://localhost:8080/actuator/health

To stop the stack:

  docker compose down

===========================================================================
 RUNNING WITHOUT DOCKER
===========================================================================

Requirements:

  * Java 21
  * Maven (or Maven Wrapper)
  * PostgreSQL 16+

-----------------------------------
 BUILD AND RUN COMMANDS
-----------------------------------

Run all tests:

  ./mvnw clean verify

This runs:
  * Unit tests
  * Service tests
  * Repository tests (H2 in‑memory database)

Build the snapshot JAR:

  ./mvnw clean package

Generated artifact:

  target/fraudengine-0.0.1-SNAPSHOT.jar

Run the application:

  java -jar target/fraudengine-0.0.1-SNAPSHOT.jar

OR via Maven:

  ./mvnw spring-boot:run

===========================================================================
 REST API OVERVIEW
===========================================================================

Base path:

  /api

Create a transaction:

  POST /api/transactions

Retrieve flagged transactions:

  GET /api/fraud-flags

Search transactions by PAN and date range:

  POST /api/transactions/search

An example Insomnia API collection is included in this repository to
demonstrate supported API calls.

===========================================================================
 ISO‑8583 SUPPORT
===========================================================================

FraudEngine includes a built‑in ISO‑8583 server implemented using jPOS.

Features:

  * Listens on a configurable TCP port (default 8037)
  * Parses incoming ISO‑8583 messages
  * Applies fraud detection rules
  * Persists transaction data
  * Returns 0210 response messages
  * Uses response codes:
      - 00 (approved)
      - 05 (declined)
   includes rule hit details in DE44

IMPORTANT NOTE:

This listener uses raw TCP and is NOT HTTP‑based.
Testing must be done using tools such as:
  * Astrex
  * telnet
  * nc
  * Test‑NetConnection (Windows)

===========================================================================
 PROJECT STRUCTURE
===========================================================================

  capitec.fraudengine
    |-- controllers     REST controllers
    |-- iso             ISO‑8583 server and utilities
    |-- service
    |     |-- rules     Fraud detection rules
    |-- model           JPA entities
    |-- repository      Data access layer

===========================================================================
 TESTING STRATEGY
===========================================================================

  * Unit tests for fraud rules and business services
  * Repository tests using H2 (PostgreSQL compatibility mode)
  * Full runtime validation using Docker Compose

Tests are executed using:

  ./mvnw clean verify

Docker is NOT required to run tests.

===========================================================================
 NOTES
===========================================================================

This project provides a rule‑based fraud detection foundation.
It does not guarantee detection of all fraudulent activity and should be
used as part of a broader risk and monitoring strategy.

===========================================================================
 LICENSE
===========================================================================

No license specified.


***************************************************************************
*                               END OF FILE                               *
***************************************************************************