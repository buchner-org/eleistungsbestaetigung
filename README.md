# Digital Service Confirmation (DCS) System
## eLeistungsbestätigung Bestätigungssystem

A Quarkus-based microservices application for managing digital service confirmations in the German healthcare system. This system handles confirmation requests, verification of service providers, signing of confirmations, and push notifications.

## Project Overview

The DCS system is built as a modular, microservices-oriented application using Quarkus 3.35.2 and Java 25. It provides REST APIs for:
- Digital service confirmations (eLeistungsbestätigung)
- Billing integration
- FHIR-based health data exchange
- Certificate management and validation
- Notification services

## Project Structure

```
├── dcs-core/                 # Core service with REST APIs and business logic
├── dcs-app-api/              # Application-level API definitions
├── dcs-test-api/             # Testing API endpoint
├── dcs-dta-proxy/            # Data Transfer Agent proxy service
├── dcs-fhir-model/           # FHIR data model definitions
└── diga-secon-jaxrs/         # SeconAPI integration for certificate handling
```

## Core Modules

### dcs-core
The main service containing:
- **API Endpoints**
  - `/app/*` - Application API (JWT authenticated)
  - `/billing/*` - Billing API (Basic auth)
  - `/test-api/*` - Test API (Basic auth)
  - `/health/*` - Health check endpoints

- **Key Features**
  - Confirmation request processing
  - Digital signatures (XAdES/DSS signing)
  - Certificate validation and caching
  - Scheduled jobs for request handling and statistics
  - Push notification support
  - Database migrations (MariaDB/SQL Server)

### dcs-dta-proxy
A standalone proxy service for the Data Transfer Agent (DTA) with:
- Certificate caching
- H2 database for local storage
- Separate configuration for proxy-specific needs

### dcs-fhir-model
FHIR R4 data model extensions and definitions for the DCS system.

### diga-secon-jaxrs
Integration with SeconAPI (Secon is the German certificate service) for:
- Certificate downloads
- Certificate validation
- Trusted certificate management

## Technology Stack

- **Framework**: Quarkus 3.35.2
- **Language**: Java 25
- **Databases**: MariaDB, SQL Server, H2
- **Security**: JWT, Basic Auth, OIDC
- **APIs**: FHIR R4, SeconAPI
- **Signing**: Digital Signature Service (DSS) 6.4
- **Build**: Maven

## Configuration Properties

Configuration is managed through `application.properties` files in `src/main/resources/` or environment variables.

### Database Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `quarkus.datasource.db-kind` | `mariadb` | Database type (mariadb, mssql, postgres). Determines which driver and dialect to use. |
| `quarkus.datasource.jdbc.url` | *(required)* | JDBC connection URL to the database. Example: `jdbc:mariadb://localhost:3306/dcs` |
| `quarkus.datasource.username` | *(required)* | Database username for authentication |
| `quarkus.datasource.password` | *(required)* | Database password for authentication |

### Cache Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `quarkus.cache.caffeine."trusted-certificate-ik-cache".expire-after-write` | `12h` | Cache expiration for trusted institution certificates |
| `quarkus.cache.caffeine."trusted-certificate-serial-cache".expire-after-write` | `12h` | Cache expiration for certificate serial numbers |
| `quarkus.cache.caffeine."practitioner".expire-after-write` | `12h` | Cache expiration for practitioner data |

### Scheduled Jobs

| Property | Default | Description |
|----------|---------|-------------|
| `dcs.logging.save-db-cron` | `*/30 * * * * ?` | Cron schedule for saving logging data (every 30 seconds) |
| `dcs.statistics.save-db-cron` | `*/30 * * * * ?` | Cron schedule for saving statistics (every 30 seconds) |
| `dcs.task.statistics.aggregation.cron` | `* */15 * * * ? 1970` | Cron schedule for aggregating statistics (every 15 minutes) |
| `dcs.task.secon.certificate.download.cron` | `0 0 16 * * ?` | Cron schedule for downloading certificates from Secon (daily at 4 PM) |
| `dcs.task.confirmation.request.handler.cron` | `*/10 * * * * ?` | Cron schedule for handling confirmation requests (every 10 seconds) |
| `dcs.task.billing.callback.cron` | `*/12 * * * * ?` | Cron schedule for billing callbacks (every 12 seconds) |
| `dcs.task.push.notification.cron` | `*/10 * * * * ? 1970` | Cron schedule for push notifications (disabled by default, see 1970 year) |

### Certificate & Security

| Property | Default | Description |
|----------|---------|-------------|
| `dcs.trusted-certificates.url` | `https://trustcenter-data.itsg.de/dale/gesamt-rsa4096.key` | URL to trusted root certificates from ITSG (German eHealth trust center) |
| `dcs.keystore.file` | *(required)* | Path to the keystore file (.jks/.p12) for signing operations |
| `dcs.keystore.password` | *(required)* | Password for the keystore file |
| `dcs.signing-tsp.keystore` | *(required)* | Path to Time Stamp Authority (TSA) keystore for timestamp tokens |
| `dcs.signing-tsp.keystore-password` | *(required)* | Password for TSA keystore |
| `dcs.crypt.aes-key` | *(required)* | AES-256 key for encryption (base64 encoded, 44 chars for 256-bit) |
| `dcs.kvnr-pseudonym-secret` | *(required)* | Secret key for generating KVNR pseudonyms (minimum 32 characters) |

### Application Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `dcs.software-identifier` | *(required)* | Software identifier for confirmations (e.g., "CompanyName/Product v1.0") |
| `dcs.contact-mail-address` | *(required)* | Contact email address for support inquiries |
| `dcs.request.pending-timeout-hours` | `24` | Timeout for pending confirmation requests in hours before automatic cancellation |

### Billing Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `dcs.kostentraeger-iks` | *(required)* | Comma-separated list of cost carrier institution ID codes (Kostenträger-IKs). Example: `108111118,108111119` |

### JWT/Authentication Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `mp.jwt.verify.publickey.algorithm` | `RS512` | JWT public key algorithm (RS256, RS384, RS512, ES256, etc.) |
| `mp.jwt.verify.publickey.location` | *(optional)* | Path to public key file (e.g., `file:///path/to/public.key`) or URL |
| `mp.jwt.verify.publickey` | *(optional)* | Base64-encoded public key string (alternative to location) |
| `dcs.app.jwt.claim-name.user-identifier` | `sub` | JWT claim name for user identifier (typically subject claim) |
| `dcs.app.jwt.claim-name.kvnr-or-hash` | `kvnrHashSha256` | JWT claim name for KVNR or KVNR SHA256 hash |
| `dcs.app-service.endpoint.enabled` | `false` | Enable app service endpoint for inter-service communication |
| `dcs.app-service.jwt.sub` | *(required if enabled)* | JWT subject value for app service authentication |

### Push Notification Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `dcs.push-service.auth-header` | `UNAUTHORIZED` | Authorization header value for push notification service (when disabled) |
| `dcs.push.service.implementation` | `default` | Push notification service implementation: `default` or `barmer` |
| `dcs.push.service.barmer.notification-title` | `Titel` | Title for Barmer (health insurance) push notifications |
| `dcs.push.service.barmer.notification-text` | `Text` | Text/body content for Barmer push notifications |
| `dcs.push.service.barmer.deeplink-data` | `https://` | Deep link URL for Barmer push notifications to redirect users |

### SeconAPI Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `jaxrs-secon.default-verfahren` | `ELB` | Default procedure for Secon API (ELB = eLeistungsbestätigung / Digital Service Confirmation) |
| `dcs.remote.ik` | (DTA proxy only) `661430035` | Institution ID (IK) for DTA proxy configuration |

### API Documentation

| Property | Default | Description |
|----------|---------|-------------|
| `quarkus.smallrye-openapi.info-title` | `Digital Confirmation Service` | Title shown in OpenAPI documentation |
| `quarkus.smallrye-openapi.info-version` | `2.1` | API version shown in OpenAPI documentation |
| `quarkus.swagger-ui.always-include` | `true` | Always include Swagger UI endpoint in all environments |

### Authentication & Authorization

| Property | Default | Description |
|----------|---------|-------------|
| `quarkus.http.auth.basic` | `true` | Enable HTTP Basic authentication for endpoints |
| `quarkus.security.users.embedded.enabled` | `true` | Enable embedded user authentication (from properties file) |
| `quarkus.security.users.embedded.plain-text` | `true` | Allow plain text passwords in configuration (development only) |
| `quarkus.security.users.embedded.roles.billing` | *(configured)* | Billing API role name for role-based access control |
| `quarkus.security.users.embedded.roles.testapi` | *(configured)* | Test API role name for role-based access control |
| `quarkus.http.auth.policy.role-billing.roles-allowed` | `billing` | Roles allowed for billing endpoints |
| `quarkus.http.auth.policy.role-testapi.roles-allowed` | `testapi` | Roles allowed for test API endpoints |
| `quarkus.http.auth.permission.billing.paths` | `billing/*` | URL paths protected by billing role policy |
| `quarkus.http.auth.permission.testapi.paths` | `test-api/*` | URL paths protected by test API role policy |
| `quarkus.http.auth.permission.bearer.paths` | `/app/*` | URL paths requiring JWT Bearer token authentication |
| `quarkus.oidc-client.client-enabled` | `false` | OIDC client-credentials flow authentication (disabled by default) |

## Building

### Prerequisites
- Java 25+
- Maven 3.8.1+

### Build Commands

```bash
# Build JAR
mvn clean package

# Build native image (requires GraalVM)
mvn clean package -Pnative

# Run tests
mvn clean verify

# Run specific module
cd dcs-core && mvn clean package
```

## Running

### Development Mode

```bash
# Run dcs-core in development mode
cd dcs-core && mvn quarkus:dev
```

The application will start on `http://localhost:8080`

### Production Mode

```bash
# Build and run JAR
cd dcs-core && mvn clean package -DskipTests
java -jar target/quarkus-app/quarkus-run.jar
```

### Docker Deployment

```bash
# Build Docker image
docker build -f src/main/docker/Dockerfile.jvm -t dcs-core:latest .

# Run container with MariaDB
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

## API Endpoints

### Health Check
- `GET /q/health` - Liveness probe
- `GET /q/health/ready` - Readiness probe
- `GET /q/health/live` - Live status

### OpenAPI/Swagger
- `GET /q/openapi` - OpenAPI specification (JSON)
- `GET /q/openapi.yaml` - OpenAPI specification (YAML)
- `GET /q/swagger-ui/` - Swagger UI (development mode)

### Application API (JWT Auth)
- `POST /app/` - Create new confirmation request
- `GET /app/{requestId}` - Get confirmation request status
- All endpoints require Bearer token in `Authorization` header

### Billing API (Basic Auth)
- `POST /billing/` - Billing-related operations
- Requires `billing` role with Basic authentication

### Test API (Basic Auth)
- `POST /test-api/` - Test operations
- Requires `testapi` role with Basic authentication

## Database Migrations

Database migrations are automatically applied on startup using Flyway. Migration scripts are located in:
- `dcs-core/src/main/resources/db/migration/mariadb/` - MariaDB migrations
- `dcs-core/src/main/resources/db/migration/mssql/` - SQL Server migrations

Migration scripts follow Flyway naming convention: `V{version}__{description}.sql`

## Environment Variables

Configuration can be set via environment variables using the Quarkus naming convention. Replace dots with underscores and convert to UPPERCASE:

```bash
# Example: dcs.software-identifier → DCS_SOFTWARE_IDENTIFIER
export DCS_SOFTWARE_IDENTIFIER=MyCompany/DCS

# Example: quarkus.datasource.jdbc.url → QUARKUS_DATASOURCE_JDBC_URL
export QUARKUS_DATASOURCE_JDBC_URL=jdbc:mariadb://localhost:3306/dcs

# Example: quarkus.datasource.username → QUARKUS_DATASOURCE_USERNAME
export QUARKUS_DATASOURCE_USERNAME=dcs

# Example: quarkus.datasource.password → QUARKUS_DATASOURCE_PASSWORD
export QUARKUS_DATASOURCE_PASSWORD=secret
```

## Development

### Project Structure Conventions
- REST resources are in `api/` package
- Business logic services in domain-specific packages (confirmation, billing, etc.)
- Database entities in each domain package
- Configuration properties injected via `@ConfigProperty` and `@Inject`

### Testing

```bash
# Run unit tests
mvn test

# Run integration tests
mvn verify

# Run specific test
mvn test -Dtest=ConfirmationRequestTest

# Run tests for specific module
cd dcs-core && mvn test
```

### IDE Setup

**IntelliJ IDEA:**
- Open project as Maven project
- Java 25 SDK required
- Enable annotation processing for Quarkus

**VS Code:**
- Install Quarkus Tools extension
- Install Red Hat Extension Pack for Apache Camel

## Dependencies & Versions

Key dependencies:
- **Quarkus**: 3.35.2
- **HAPI FHIR**: 6.8.0
- **Digital Signature Service (DSS)**: 6.4
- **Nimbus JOSE JWT**: (managed by Quarkus BOM)
- **MariaDB Driver**: (managed by Quarkus BOM)
- **SQL Server Driver**: (managed by Quarkus BOM)

See `pom.xml` for complete dependency list.

## Troubleshooting

### Database Connection Issues

Check that the correct connection parameters are set:

```properties
# MariaDB
quarkus.datasource.db-kind=mariadb
quarkus.datasource.jdbc.url=jdbc:mariadb://localhost:3306/dcs
quarkus.datasource.username=dcs
quarkus.datasource.password=secret

# SQL Server
quarkus.datasource.db-kind=mssql
quarkus.datasource.jdbc.url=jdbc:sqlserver://localhost:1433;databaseName=dcs
quarkus.datasource.username=sa
quarkus.datasource.password=secret
```

Verify the database is running and accessible from the application.

### Certificate Validation Failures
Check that:
1. `dcs.trusted-certificates.url` is accessible and returns valid certificate data
2. Certificates are not expired
3. Cache expiration settings are appropriate: `quarkus.cache.caffeine."trusted-certificate-ik-cache".expire-after-write`

### JWT Authentication Issues
Ensure JWT public key is configured:
```properties
# Option 1: Point to public key file
mp.jwt.verify.publickey.location=file:///path/to/public.key

# Option 2: Provide base64-encoded key
mp.jwt.verify.publickey=<base64-encoded-key>
```

### Scheduled Jobs Not Running
Verify cron expressions are valid and scheduler is enabled:
```properties
dcs.task.secon.certificate.download.cron=0 0 16 * * ?  # Valid: runs daily at 4 PM
dcs.task.push.notification.cron=*/10 * * * * ? 1970    # Disabled: year 1970 is invalid
```

### Migration Issues
If migrations fail, check:
1. Migration files exist in correct location
2. Database user has DDL permissions
3. No migration locks exist in the database
4. Flyway history table is not corrupted

## Licensing & Contributing

This is part of the German eHealth infrastructure (elektronische Leistungsbestätigung).

## Support & Contact

For issues and support, refer to:
- Documentation: See project documents in repository
- Issues: GitLab repository issue tracker
- Contact: Email address configured via `dcs.contact-mail-address`

---

**Project**: Digital Service Confirmation System (DCS)  
**German Name**: eLeistungsbestätigung Bestätigungssystem  
**Version**: 2.2.0-SNAPSHOT  
**Java**: 25+  
**Quarkus**: 3.35.2  
**Last Updated**: 2026-06-01
