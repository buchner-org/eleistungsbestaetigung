# DCS Core

The Quarkus service implementing the **eLeistungsbestätigung Bestätigungssystem** (Digital Service Confirmation System). This module is the runnable application: it exposes the billing, app, and test REST interfaces, persists and processes confirmation requests, signs response documents, and notifies insurants.

## Overview

DCS Core is the confirmation system a health insurance company operates. Service providers submit confirmation requests over the SECON-secured billing interface, insurants confirm or decline them through their insurance app, and the service returns a digitally signed response document. It provides:

- **Billing Interface**: SECON-encrypted FHIR endpoints for participation queries, confirmation requests, polling, and callbacks
- **App Interface**: JWT-authenticated REST endpoints for insurant registration and confirmation handling
- **Test Interface**: Basic-auth endpoints for creating confirmation requests and generating test data
- **Confirmation Processing**: Scheduled handling of pending requests including timeout-driven cancellation
- **Digital Signatures**: XAdES detached signatures with RFC 3161 timestamps over response documents
- **Certificate Management**: Daily download and caching of the ITSG trusted certificate directory
- **Push Notifications**: Pluggable notification services to inform insurants about pending requests
- **Process Logging & Statistics**: Asynchronous, batched persistence of process log entries and usage statistics

## Features

- **Quarkus 3.35.2**: Fast startup, live reload, and optional native compilation
- **Java 25+**: Modern Java language features (records, sealed classes, virtual threads)
- **FHIR R4**: Profile-conformant FHIR exchange through [dcs-fhir-model](../dcs-fhir-model)
- **SECON Transport Security**: Encryption and signature through [diga-secon-jaxrs](../diga-secon-jaxrs)
- **Multi-Database Support**: MariaDB and SQL Server out of the box, with Flyway migrations maintained per dialect
- **Encryption at Rest**: AES-encrypted database columns and KVNR pseudonymisation
- **MicroProfile OpenAPI**: Generated OpenAPI specification and Swagger UI
- **MicroProfile Fault Tolerance**: Retries on outbound billing callbacks
- **Health Checks**: SmallRye Health liveness and readiness probes

## Requirements

- **Java 25** or higher
- **Maven 3.8+**
- A **MariaDB** or **SQL Server** database (PostgreSQL migrations exist, see [Database Migrations](#database-migrations))
- A **keystore** holding the confirmation system's signing key and certificate
- A **TSA keystore** for timestamp requests
- Access to the **ITSG trust center** for the trusted certificate directory

## Building

```bash
./mvnw clean package
```

Or on Windows:

```bash
mvnw.cmd clean package
```

Build a native executable (requires GraalVM):

```bash
./mvnw clean package -Pnative
```

## Running

### Development Mode

```bash
./mvnw quarkus:dev
```

The application starts on `http://localhost:8080` with Swagger UI at `http://localhost:8080/q/swagger-ui/`.

### Production Mode

```bash
./mvnw clean package -DskipTests
java -jar target/quarkus-app/quarkus-run.jar
```

### Docker

```bash
docker build -f src/main/docker/Dockerfile.jvm -t dcs-core:latest .

docker run -p 8080:8080 \
  -e QUARKUS_DATASOURCE_DB_KIND=mariadb \
  -e QUARKUS_DATASOURCE_JDBC_URL=jdbc:mariadb://db:3306/dcs \
  -e QUARKUS_DATASOURCE_USERNAME=dcs \
  -e QUARKUS_DATASOURCE_PASSWORD=secret \
  -e DCS_KEYSTORE_FILE=/secrets/keystore.p12 \
  -e DCS_KEYSTORE_PASSWORD=keystorepass \
  -e DCS_CRYPT_AES_KEY=<base64-aes-key> \
  -e DCS_KVNR_PSEUDONYM_SECRET=<secret> \
  dcs-core:latest
```

## Project Structure

```
src/main/java/de/yoshteq/dcs/
├── api/                        # REST resources and providers
│   ├── AppServiceResource.java # App backend service interface
│   ├── FhirProvider.java       # FHIR body reader/writer
│   ├── *ExceptionMapper.java   # Error mapping to HTTP responses
│   ├── app/                    # App API implementation and description generators
│   ├── billing/                # Billing API and callback client
│   └── test/                   # Test API implementation and test data generator
├── confirmation/               # Confirmation requests, state machine, scheduled handler
├── insurant/                   # Insurant registration and KVNR pseudonymisation
├── performer/                  # Performer organizations and their participation
├── billing/                    # Outbound billing callback delivery
├── secon/                      # ITSG certificate download and SECON directory
├── signing/                    # XAdES signing and timestamping
├── crypto/                     # AES encryption of persisted values
├── push/                       # Push notification services (default, Barmer)
├── logging/                    # Process log entries and batched persistence
├── statistics/                 # Usage statistics collection and aggregation
└── task/                       # Shared scheduling helpers

src/main/resources/
├── application.properties      # Default configuration
├── db/migration/{mariadb,mssql,postgres}/  # Flyway migrations per dialect
├── service_descriptions_*.properties       # Service descriptions per service type
└── service_type_*.properties               # Service type labels (de, en)

src/main/docker/Dockerfile.jvm  # JVM container image
```

## API Endpoints

### Billing API (Basic Auth, role `billing`, SECON)

All billing endpoints consume and produce `multipart/form-data` with a SECON-encrypted FHIR XML payload.

- `POST /billing/participation-status-query` - Query whether an insurant participates in the process
- `POST /billing/confirmation-request` - Submit confirmation requests for one or more charge items
- `POST /billing/polling/poll` - Fetch available response documents for a requesting system
- `POST /billing/polling/confirm` - Acknowledge previously polled response documents

### App API (JWT Bearer Auth)

Implements [dcs-app-api](../dcs-app-api):

- `GET /app/insurant` - Query the insurant's participation status
- `POST /app/insurant` - Register the insurant for participation
- `DELETE /app/insurant` - Deregister the insurant
- `GET /app/confirmation-requests` - List pending confirmation requests
- `POST /app/confirmation-requests/{id}/response` - Confirm or decline a request

Service and service type descriptions are localized via the `Accept-Language` header.

### App Service API (JWT Bearer Auth)

- `DELETE /app-service/insurant` - Deregister an insurant from the app backend; returns `501 Not Implemented` unless `dcs.app-service.endpoint.enabled` is `true`

### Test API (Basic Auth, role `testapi`)

Implements [dcs-test-api](../dcs-test-api):

- `POST /test-api/confirmation-requests` - Create a confirmation request
- `GET /test-api/confirmation-requests` - List confirmation requests
- `GET /test-api/confirmation-requests/{id}` - Retrieve a confirmation request
- `DELETE /test-api/confirmation-requests/{id}` - Delete a confirmation request
- `POST /test-api/generate-data?serviceType=...` - Generate test confirmation requests

### Management Endpoints

- `GET /q/health` - Aggregated health status
- `GET /q/health/live` - Liveness probe
- `GET /q/health/ready` - Readiness probe
- `GET /q/openapi` - OpenAPI specification (JSON)
- `GET /q/openapi.yaml` - OpenAPI specification (YAML)
- `GET /q/swagger-ui/` - Swagger UI

## Configuration

Configuration lives in `src/main/resources/application.properties` and can be overridden by environment variables using the Quarkus naming convention (uppercase, dots and dashes replaced by underscores — `dcs.software-identifier` becomes `DCS_SOFTWARE_IDENTIFIER`).

### Database

| Property | Default | Description |
|----------|---------|-------------|
| `quarkus.datasource.db-kind` | `mariadb` | Database type: `mariadb` or `mssql` (`postgres` requires adding the driver, see [Database Migrations](#database-migrations)) |
| `quarkus.datasource.jdbc.url` | *(required)* | JDBC URL, e.g. `jdbc:mariadb://localhost:3306/dcs` |
| `quarkus.datasource.username` | *(required)* | Database username |
| `quarkus.datasource.password` | *(required)* | Database password |
| `quarkus.flyway.migrate-at-start` | `true` | Apply migrations on startup |
| `quarkus.flyway.locations` | `classpath:db/migration/${quarkus.datasource.db-kind}` | Migration location, selected by database kind |

### Certificates & Security

| Property | Default | Description |
|----------|---------|-------------|
| `dcs.trusted-certificates.url` | `https://trustcenter-data.itsg.de/dale/gesamt-rsa4096.key` | ITSG trusted certificate directory |
| `dcs.keystore.file` | *(required)* | Keystore (`.jks`/`.p12`) with the signing key |
| `dcs.keystore.password` | *(required)* | Keystore password |
| `dcs.signing-tsp.keystore` | *(required)* | Keystore for the timestamp authority connection |
| `dcs.signing-tsp.keystore-password` | *(required)* | Password for the TSA keystore |
| `dcs.crypt.aes-key` | *(required)* | Base64-encoded AES-256 key for encrypted columns |
| `dcs.kvnr-pseudonym-secret` | *(required)* | Secret for KVNR pseudonymisation (min. 32 characters) |

### Application

| Property | Default | Description |
|----------|---------|-------------|
| `dcs.software-identifier` | *(required)* | Software identifier written into confirmations |
| `dcs.contact-mail-address` | *(required)* | Contact address published in responses |
| `dcs.kostentraeger-iks` | *(required)* | Comma-separated list of accepted cost carrier IKs |
| `dcs.request.pending-timeout-hours` | `24` | Hours before a pending request is cancelled automatically |
| `jaxrs-secon.default-verfahren` | `ELB` | SECON procedure identifier |

### Scheduled Jobs

| Property | Default | Description |
|----------|---------|-------------|
| `dcs.task.confirmation.request.handler.cron` | `*/10 * * * * ?` | Process pending confirmation requests |
| `dcs.task.billing.callback.cron` | `*/12 * * * * ?` | Deliver responses to callback URLs |
| `dcs.task.secon.certificate.download.cron` | `0 0 16 * * ?` | Download the ITSG certificate directory |
| `dcs.logging.save-db-cron` | `*/30 * * * * ?` | Flush buffered process log entries |
| `dcs.statistics.save-db-cron` | `*/30 * * * * ?` | Flush buffered statistics |
| `dcs.task.statistics.aggregation.cron` | `* */15 * * * ? 1970` | Aggregate statistics (disabled by default) |
| `dcs.task.push.notification.cron` | `*/10 * * * * ? 1970` | Send push notifications (disabled by default) |

The year `1970` in a cron expression disables the job — no execution can ever match.

### Authentication

| Property | Default | Description |
|----------|---------|-------------|
| `quarkus.http.auth.basic` | `true` | Enable HTTP Basic authentication |
| `quarkus.security.users.embedded.enabled` | `true` | Use embedded users from configuration |
| `quarkus.security.users.embedded.plain-text` | `true` | Plain text passwords (development only) |
| `quarkus.http.auth.permission.billing.paths` | `billing/*` | Paths protected by the `billing` role |
| `quarkus.http.auth.permission.testapi.paths` | `test-api/*` | Paths protected by the `testapi` role |
| `quarkus.http.auth.permission.bearer.paths` | `/app/*` | Paths requiring a JWT bearer token |
| `mp.jwt.verify.publickey.algorithm` | `RS512` | JWT signature algorithm |
| `mp.jwt.verify.publickey.location` | *(optional)* | Public key location, e.g. `file:///path/to/public.key` |
| `mp.jwt.verify.publickey` | *(optional)* | Base64-encoded public key (alternative to the location) |
| `dcs.app.jwt.claim-name.user-identifier` | `sub` | Claim holding the user identifier |
| `dcs.app.jwt.claim-name.kvnr-or-hash` | `kvnrHashSha256` | Claim holding the KVNR or its SHA-256 hash |
| `dcs.app-service.endpoint.enabled` | `false` | Enable the `/app-service` endpoint |
| `dcs.app-service.jwt.sub` | *(required if enabled)* | Expected JWT subject for `/app-service` |
| `quarkus.oidc-client.client-enabled` | `false` | Enable the OIDC client-credentials flow for outbound calls |

### Push Notifications

| Property | Default | Description |
|----------|---------|-------------|
| `dcs.push.service.implementation` | `default` | Notification implementation: `default` or `barmer` |
| `dcs.push-service.auth-header` | `UNAUTHORIZED` | Authorization header for the push service |
| `dcs.push.service.barmer.notification-title` | `Titel` | Notification title |
| `dcs.push.service.barmer.notification-text` | `Text` | Notification body |
| `dcs.push.service.barmer.deeplink-data` | `https://` | Deep link opened by the notification |

### Caching & OpenAPI

| Property | Default | Description |
|----------|---------|-------------|
| `quarkus.cache.caffeine."trusted-certificate-ik-cache".expire-after-write` | `12h` | Certificate lookup by IK |
| `quarkus.cache.caffeine."trusted-certificate-serial-cache".expire-after-write` | `12h` | Certificate lookup by serial number |
| `quarkus.cache.caffeine."practitioner".expire-after-write` | `12h` | Practitioner data |
| `quarkus.smallrye-openapi.info-title` | `Digital Confirmation Service` | OpenAPI title |
| `quarkus.smallrye-openapi.info-version` | `2.1` | OpenAPI version |
| `quarkus.swagger-ui.always-include` | `true` | Expose Swagger UI in all profiles |

## Database Migrations

Flyway applies migrations on startup from `db/migration/<db-kind>/`, following the naming convention `V{version}__{description}.sql`. Each supported dialect has its own migration set:

```
src/main/resources/db/migration/
├── mariadb/
├── mssql/
└── postgres/
```

When adding a migration, add the equivalent script to every dialect directory.

The `pom.xml` bundles the MariaDB and SQL Server drivers together with the matching Flyway modules. The PostgreSQL migrations are maintained alongside them, but running against PostgreSQL additionally requires `io.quarkus:quarkus-jdbc-postgresql` and `org.flywaydb:flyway-database-postgresql` on the classpath.

## Service Descriptions

Confirmation requests are presented to insurants with a human-readable service description resolved from resource bundles:

- `service_descriptions_hebamme_de.properties` - Midwife services
- `service_descriptions_heilmittel_de.properties` - Therapeutic services
- `service_descriptions_hkp_de.properties` - Home care services
- `service_type_de.properties`, `service_type_en.properties` - Service type labels

Descriptions are selected by the acceptable languages of the request; override the defaults by providing `ServiceDescriptionGenerator` or `ServiceTypeDescriptionGenerator` implementations.

## Testing

```bash
# Unit tests
./mvnw test

# Full verification including integration tests
./mvnw verify

# Single test
./mvnw test -Dtest=SignatureServiceTest
```

The test setup is based on `quarkus-junit5` with REST Assured for endpoint tests and the Quarkus security test extensions for JWT and Basic authentication.

## Dependencies

### Module Dependencies
- `de.yoshteq:dcs-app-api` - App REST API contract
- `de.yoshteq:dcs-test-api` - Test REST API contract
- `de.yoshteq:dcs-fhir-model` - FHIR R4 domain model
- `de.yoshteq:diga-secon-jaxrs` - SECON transport security

### Core Dependencies
- `io.quarkus:quarkus-bom:3.35.2` - Quarkus platform (REST, Hibernate ORM, Security, JWT, Scheduler, Cache, Health, Fault Tolerance)
- `ca.uhn.hapi.fhir:hapi-fhir-*:6.8.0` - HAPI FHIR R4 runtime
- `eu.europa.ec.joinup.sd-dss:dss-*:6.4` - XAdES signing, tokens, and services
- `io.quarkiverse.loggingsentry:quarkus-logging-sentry:2.3.1` - Sentry log forwarding
- `com.nimbusds:nimbus-jose-jwt` - JOSE/JWT handling
- `org.flywaydb:flyway-mysql`, `flyway-sqlserver` - Flyway dialect support

DSS artifacts are resolved from the European Commission Nexus repository configured in the `pom.xml`.

### Test Dependencies
- `io.quarkus:quarkus-junit5` - Quarkus test framework
- `io.rest-assured:rest-assured` - REST endpoint testing
- `io.quarkus:quarkus-test-security`, `quarkus-test-security-jwt` - Authentication in tests

## Troubleshooting

### Database connection issues

```properties
# MariaDB
quarkus.datasource.db-kind=mariadb
quarkus.datasource.jdbc.url=jdbc:mariadb://localhost:3306/dcs

# SQL Server
quarkus.datasource.db-kind=mssql
quarkus.datasource.jdbc.url=jdbc:sqlserver://localhost:1433;databaseName=dcs
```

`quarkus.flyway.locations` follows `db-kind`, so a mismatch between URL and `db-kind` also loads the wrong migrations.

### Certificate validation failures

Verify that `dcs.trusted-certificates.url` is reachable, that the downloaded directory is current (the download job runs daily at 16:00 by default), and that the certificate is not expired. Cached entries survive for 12 hours by default.

### JWT authentication failures

Provide the public key through either `mp.jwt.verify.publickey.location` or `mp.jwt.verify.publickey`, make sure `mp.jwt.verify.publickey.algorithm` matches the token, and check that the configured claim names for user identifier and KVNR exist in the token.

### Scheduled jobs not running

Cron expressions ending in `1970` are intentionally disabled. Enable a job by replacing the year with a valid expression.

### Migration failures

Check that the migration files exist for the configured dialect, that the database user holds DDL permissions, and that no stale Flyway lock or corrupted history table remains.

## License

This project is licensed under the Apache License, Version 2.0. See the [LICENSE](../LICENSE) file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Support

For issues, questions, or feedback, please open an issue on the GitHub repository.
