# DCS DTA Proxy

A Quarkus service that receives SECON-encrypted eLeistungsbestätigung billing containers and forwards them to a data acceptance office (Datenannahmestelle, DTA) via SFTP. It decrypts each request, splits it into the files the DTA expects, re-encrypts them for the DTA's IK, and uploads them.

## Overview

Service providers submit a billing container: a FHIR `Parameters` resource wrapped in a SECON request (procedure `ELB`). The proxy:

1. Decrypts the request and checks its signature with [diga-secon-jaxrs](../diga-secon-jaxrs).
2. Parses the FHIR `Parameters` and the embedded EDIFACT ESOL file.
3. Signs and encrypts each part for the configured DTA IK (`dcs.remote.ik`).
4. Uploads the files to the DTA's SFTP server.
5. Replies with a SECON-encrypted FHIR `OperationOutcome`.

The ITSG trusted certificate directory is downloaded once a day and cached in a local H2 database. It is used to encrypt for the DTA and to verify senders.

## Features

- **Quarkus 3.35.2** on **Java 25**
- **FHIR R4** with HAPI FHIR and [dcs-fhir-model](../dcs-fhir-model)
- **SECON transport security** with [diga-secon-jaxrs](../diga-secon-jaxrs) and the TK `secon-tool`
- **SFTP upload** with sshj and public-key authentication
- **Embedded H2 database** with Flyway migrations, so no external database is needed
- **Health checks** with SmallRye Health

## Requirements

- **Java 25** or higher
- **Maven 3.8+** (or the included Maven wrapper)
- Locally installed sibling modules [dcs-fhir-model](../dcs-fhir-model) and [diga-secon-jaxrs](../diga-secon-jaxrs) (`./mvnw install` in each)
- A **keystore** (`.p12`/`.jks`) with the proxy's SECON key and certificate. The IK is read from the certificate.
- An **SSH private key** for the DTA's SFTP server
- Access to the **ITSG trust center** for the trusted certificate directory

## Building

```bash
./mvnw clean package
```

## Running

### Development Mode

```bash
./mvnw quarkus:dev
```

### Production Mode

```bash
java \
  -Ddcs.keystore.file=/secrets/keystore.p12 \
  -Ddcs.keystore.password=secret \
  -Ddcs.remote.sftp.host=sftp.example.org \
  -Ddcs.remote.sftp.port=22 \
  -Ddcs.remote.sftp.username=upload \
  -Ddcs.remote.sftp.key-path=/secrets/id_rsa \
  -Ddcs.remote.sftp.saving-location=/upload \
  -jar target/quarkus-app/quarkus-run.jar
```

### Docker

```bash
docker build -f src/main/docker/Dockerfile.jvm -t dcs-dta-proxy:latest .

docker run -p 8080:8080 \
  -v /path/to/secrets:/secrets:ro \
  -v dta-proxy-data:/deployments/data \
  -e QUARKUS_DATASOURCE_JDBC_URL=jdbc:h2:file:/deployments/data/dta_proxy \
  -e DCS_KEYSTORE_FILE=/secrets/keystore.p12 \
  -e DCS_KEYSTORE_PASSWORD=secret \
  -e DCS_REMOTE_SFTP_HOST=sftp.example.org \
  -e DCS_REMOTE_SFTP_PORT=22 \
  -e DCS_REMOTE_SFTP_USERNAME=upload \
  -e DCS_REMOTE_SFTP_KEY_PATH=/secrets/id_rsa \
  -e DCS_REMOTE_SFTP_SAVING_LOCATION=/upload \
  dcs-dta-proxy:latest
```

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/billing/submit-invoice` | Submit a SECON-encrypted billing container (`multipart/form-data` with `iksender`, `ikempfaenger`, `verfahren`, `nutzdaten`) |
| `GET`  | `/q/health` | Liveness and readiness (includes the database check) |

### Billing container

The decrypted `nutzdaten` hold a FHIR `Parameters` resource (profile `KK_ELB_BillingContainerParameters`) with these parameters:

| Parameter | Type | Description |
|-----------|------|-------------|
| `esolFile` | `base64Binary` | EDIFACT ESOL file (ISO-8859-1) |
| `invoice` | parts | One per invoice. Its `invoiceReference` part must match a receipt number in the ESOL file. |
| `emailContactPoint` | `ContactPoint` | Sender e-mail address, written into the Auftragsdatei |
| `referenceNumber`, `container`, `termsOfUseAccepted` | | Passed through in the backup file only |

### Responses

| Status | Meaning |
|--------|---------|
| `200` | All files uploaded. The body is a SECON-encrypted `OperationOutcome` with `All OK`. |
| `400` | Decryption or signature check failed, the FHIR content is invalid, or an invoice is missing from the ESOL file. The body is a SECON-encrypted `OperationOutcome` whenever the sender IK is known. |
| `409` | A file with the same name already exists on the SFTP server, i.e. the ESOL reference number was already submitted. |
| `500` | Encryption or SFTP upload failed |

### Uploaded files

All paths are relative to `dcs.remote.sftp.saving-location`. `<nnn>` is the last three digits of the ESOL reference number.

| Path | Content | Encrypted |
|------|---------|-----------|
| `Backup/<senderIK>/<yyyy-MM-dd>_ESOL0<nnn>.elbx` | Complete billing container as FHIR XML | yes |
| `ESOL/<senderIK>_ESOL0<nnn>` | ESOL file | yes |
| `ESOL/<senderIK>_ESOL0<nnn>.AUF` | Auftragsdatei (348-byte order record) | no |
| `eLBx/<providerIK>_<invoiceNumber>_<yyyy-MM-dd>.elbx` | One file per invoice, as FHIR XML | yes |

## Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `dcs.keystore.file` | *(required)* | Keystore with the proxy's SECON key and certificate |
| `dcs.keystore.password` | *(required)* | Keystore password |
| `dcs.remote.ik` | `661430035` | IK of the DTA that the files are encrypted for |
| `dcs.remote.sftp.host` | *(required)* | SFTP host |
| `dcs.remote.sftp.port` | *(required)* | SFTP port |
| `dcs.remote.sftp.username` | *(required)* | SFTP user |
| `dcs.remote.sftp.key-path` | *(required)* | Path to the SSH private key |
| `dcs.remote.sftp.saving-location` | *(required)* | Base directory on the SFTP server |
| `dcs.trusted-certificates.url` | `https://trustcenter-data.itsg.de/dale/gesamt-rsa4096.key` | ITSG trusted certificate directory |
| `dcs.task.secon.certificate.download.cron` | `0 0 16 * * ?` | Certificate directory refresh. It also runs at startup if the cache is empty or older than 24 hours. |
| `quarkus.datasource.jdbc.url` | `jdbc:h2:file:./dta_proxy` | H2 database file for the certificate cache |
| `jaxrs-secon.default-verfahren` | `ELB` | SECON procedure identifier |

Every property can also be set as an environment variable, e.g. `DCS_REMOTE_SFTP_HOST`.

## Project Structure

```
src/main/java/de/yoshteq/dcs/
├── api/
│   ├── billing/BillingResource.java  # /billing endpoint
│   ├── fhir/FhirProvider.java        # FHIR body reader/writer (HAPI)
│   └── *ExceptionMapper.java         # Errors as SECON OperationOutcome
├── billing/                          # Splitting the container into ESOL, invoices, backup, Auftragsdatei
├── secon/                            # Keystore identity, certificate directory, ITSG download
└── sftp/                             # SFTP client and upload

src/main/resources/
├── application.properties
└── db/migration/                     # Flyway migrations (H2)
```

## Upgrading from 2.x

Version 3.0 moves to Quarkus 3, Java 25 and HAPI FHIR, and ships a newer H2 that cannot open the old database files. The `V002` migration was changed for H2 2.x as well. **Delete the existing `dta_proxy*.db` files before starting 3.0.** The certificate cache is downloaded again automatically at startup.

## Known Limitations

- `/billing/*` has no HTTP authentication. Access is protected only by the SECON signature check.
- The SFTP host key is not verified (`PromiscuousVerifier`).
