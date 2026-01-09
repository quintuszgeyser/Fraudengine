FraudEngine
===========

FraudEngine is a Spring Boot–based fraud detection service that evaluates
financial transactions using configurable, rule‑based logic.

The application supports both REST (JSON) and ISO‑8583 message processing,
persists all transactions, and exposes simple APIs for investigation and
monitoring.

This repository contains the full application, including Docker support for
local development.

---

What FraudEngine Does
---------------------

FraudEngine:
- Accepts transactions via REST API
- Accepts ISO‑8583 messages over TCP
- Applies multiple fraud detection rules per transaction
- Flags suspicious transactions
- Stores all transactions in PostgreSQL
- Allows querying of flagged and historical transactions

---

Fraud Rules
-----------

Fraud detection is implemented as a set of independent rules.
Each rule can be enabled, disabled, and tuned via configuration.

Currently implemented rules:

- High Amount  
  Flags transactions above a configurable threshold.

- Location Risk  
  Flags transactions from configured risky locations, with optional whitelist.

- Velocity  
  Flags accounts with a high number of transactions in a short time window.

Rules are evaluated together, and a transaction is flagged if any rule matches.

---

Running with Docker (Recommended)
---------------------------------

The easiest way to run FraudEngine is using Docker Compose.

This will start:
- A PostgreSQL database
- The FraudEngine application

### Requirements
- Docker
- Docker Compose

### Configuration

Create a file at:

docker\.env

Example:

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

### Start the stack

From the project root:

docker compose up --build

The application will be available at:
- REST API: http://localhost:8080
- ISO‑8583 TCP listener: port 8037

---

Running Without Docker
----------------------

Requirements:
- Java 21
- Maven
- PostgreSQL

Build:

mvn clean install

Run:

mvn spring-boot:run

---

REST API Overview
-----------------

Base path: /api

Create a transaction:

POST /api/transactions

Get flagged transactions:

GET /api/fraud-flags

Search transactions by PAN and date range:

POST /api/transactions/search



*an example insomnia package is part of this github repo that can showcase possible calls.


---

ISO‑8583 Support
----------------

FraudEngine includes a built‑in ISO‑8583 server.

- Listens on a configurable TCP port (default 8037)
- Parses incoming messages using jPOS
- Applies fraud rules
- Returns a 0210 response
- Uses response code 00 (approved) or 05 (declined)
- Can include rule hit information in DE44



---

Project Structure
-----------------

capitec.fraudengine
  controllers     REST APIs
  iso             ISO‑8583 server and utilities
  service
    rules         Fraud rules
  model           JPA entities
  repository      Data access

---

Notes
-----

This project provides a rule‑based fraud detection foundation.
It does not guarantee detection of all fraudulent activity and should be
used as part of a broader risk strategy.

---

License
-------

No license specified.