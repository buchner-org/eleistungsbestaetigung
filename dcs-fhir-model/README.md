# DCS FHIR Model

A FHIR R4 domain model library for the **eLeistungsbestätigung Bestätigungssystem** (Digital Service Confirmation System). This library provides type-safe wrappers, builders, and validators for the KK_ELB FHIR profiles exchanged between service providers, confirmation systems, and cost carriers.

## Overview

DCS FHIR Model turns the profiled FHIR resources of the eLeistungsbestätigung specification into a small, validating Java API. Instead of navigating raw HAPI FHIR structures, callers work with domain types that validate the profile on read and produce profile-conformant resources on write. It enables applications to:

- **Parse & Validate**: Read `Parameters`, `ChargeItem`, and `Bundle` resources and fail fast with `FhirValidationException` when a profile constraint is violated
- **Build Profile-Conformant Resources**: Create requests, responses, and signed response documents through static factory methods
- **Handle Identifiers & Extensions**: Read and write IK, KVNR, and UUID identifiers plus the KK_ELB extensions
- **Preserve Date Precision**: Convert FHIR `dateTime` values to `TemporalAccessor` without losing the original precision

Built on HAPI FHIR R4 and Java 25, this package serves as the shared data model layer for the eLeistungsbestätigung ecosystem.

## Features

- **HAPI FHIR R4**: Built on the reference Java implementation of FHIR R4
- **Profile Validation**: Meta profile, fixed value, and cardinality assertions on every read
- **Java 25+**: Modern Java language features (records, sealed classes, switch expressions)
- **Round-Trip Safe**: Every model type can be created from and converted back to its FHIR resource
- **Precision-Preserving Date Handling**: `DateTimeTypeConverter` maps FHIR precision to `Year`, `YearMonth`, `LocalDate`, or `OffsetDateTime`
- **No Runtime Framework Required**: Plain Java library, usable from Quarkus, Jakarta EE, or standalone

## Requirements

- **Java 25** or higher
- **Maven 3.8+**

## Building

```bash
./mvnw clean package
```

Or on Windows:

```bash
mvnw.cmd clean package
```

## Project Structure

```
src/main/java/de/yoshteq/dcs/fhir/
├── FhirHelper.java                       # Assertion and conversion helpers
├── FhirValidationException.java          # Thrown on profile violations
├── FhirBinaryFileHelper.java             # Binary resources from files
├── DateTimeTypeConverter.java            # FHIR dateTime <-> TemporalAccessor
├── ExtensionHelper.java                  # Typed extension access
├── IdentifierHelper.java                 # IK / KVNR / UUID identifiers
├── MetaHelper.java                       # Profile meta handling
├── ParametersHelper.java                 # Typed Parameters access
├── UuidHelper.java                       # UUID and urn:uuid conversion
├── api/                                  # Operation request/response Parameters
│   ├── ParticipationStatusRequest.java   # Participation query request
│   ├── ParticipationStatusResponse.java  # Participation query response
│   ├── ConfirmationRequests.java         # Confirmation request (callback/polling)
│   ├── ConfirmationRequestResponses.java # Confirmation response documents
│   ├── PollingRequest.java               # Polling request
│   ├── PollingConfirmationOfResponse.java# Acknowledgement of polled responses
│   └── OperationOutcomeResponse.java     # OperationOutcome helper
└── model/                                # Profiled resources
    ├── ChargeItem.java                   # KK_ELB_ChargeItem
    ├── Occurrence.java                   # Point in time or period
    ├── Performer.java                    # Organization / practitioner performer
    ├── ResponseDocumentBundle.java       # KK_ELB_ResponseDocumentBundle
    ├── ResponseDocumentComposition.java  # KK_ELB_ResponseComposition
    ├── Signature.java                    # Detached XAdES signature
    └── billing/                          # Billing container structures
        ├── BillingContainerParameters.java
        ├── BillingCointainerInvoiceMultiPart.java
        ├── InvoiceContainerBundle.java
        └── MidwifeServiceRequest.java
```

## Supported Profiles

The library implements the following `KK_ELB` structure definitions and code systems:

| Java type | Profile / System |
|-----------|------------------|
| `ChargeItem` | `https://e-lb.de/fhir/StructureDefinition/KK_ELB_ChargeItem` |
| `ResponseDocumentBundle` | `https://e-lb.de/fhir/StructureDefinition/KK_ELB_ResponseDocumentBundle` |
| `ResponseDocumentComposition` | `https://e-lb.de/fhir/StructureDefinition/KK_ELB_ResponseComposition` |
| `ConfirmationRequests` | `https://e-lb.de/fhir/StructureDefinition/KK_ELB_ConfirmationRequestParameters` |
| `PollingRequest` | `https://e-lb.de/fhir/StructureDefinition/KK_ELB_PollingRequest` |
| `PollingConfirmationOfResponse` | `https://e-lb.de/fhir/StructureDefinition/KK_ELB_ConfirmationOfResponse` |
| `ParticipationStatusRequest` | `https://e-lb.de/fhir/StructureDefinition/KK_ELB_ParticipationStatusRequestParameters` |
| `ParticipationStatusResponse` | `https://e-lb.de/fhir/StructureDefinition/KK_ELB_ParticipationStatusResponseParameters` |
| `BillingContainerParameters` | `https://e-lb.de/fhir/StructureDefinition/KK_ELB_BillingContainerParameters` |
| `InvoiceContainerBundle` | `https://e-lb.de/fhir/StructureDefinition/KK_ELB_InvoiceContainerBundle` |

Standard German identifier systems are used for IK (`http://fhir.de/sid/arge-ik/iknr`), KVNR (`http://fhir.de/sid/gkv/kvid-10`), and Betriebsstättennummer (`http://fhir.de/sid/bfarm/lbnr`).

## Usage

Every model type follows the same pattern: a static `from(...)`/`fromParameters(...)` method parses and validates an existing resource, a static `create(...)` method builds a new one, and a `to...()` method converts back to the HAPI FHIR resource.

### Reading a confirmation request

```java
Parameters parameters = fhirContext.newXmlParser()
    .parseResource(Parameters.class, requestXml);

// Throws FhirValidationException if the profile is violated
ConfirmationRequests request = ConfirmationRequests.from(parameters);

String software = request.getSoftwareIdentifier();
for (ChargeItem chargeItem : request.getChargeItems()) {
    Integer payorIk = chargeItem.getKostentraegerIK();
    String kvnr = chargeItem.getPatientKvnr();
    Performer performer = chargeItem.getPerformer();
}
```

### Building a signed response document

```java
ResponseDocumentBundle bundle = ResponseDocumentBundle.create(
    confirmationSystemIk,
    UUID.randomUUID(),
    chargeItem);

Bundle fhirBundle = bundle.toBundle();
String xml = FhirHelper.toXmlString(fhirBundle);
```

### Creating a participation status response

```java
Parameters response = ParticipationStatusResponse.create(true);
```

## Testing

Validation is covered by tests over profile-conformant and deliberately invalid resources under `src/test/resources/`:

```bash
./mvnw test
```

```
src/test/resources/
├── chargeitem/             # Valid and invalid KK_ELB_ChargeItem examples
├── confirmation_request/   # Confirmation request Parameters examples
├── participation_status/   # Participation status Parameters examples
├── response_document/      # Response document Bundle examples
└── signature/              # Signed response document example
```

## Dependencies

### Core Dependencies
- `ca.uhn.hapi.fhir:hapi-fhir-base:8.8.1` - HAPI FHIR core library
- `ca.uhn.hapi.fhir:hapi-fhir-structures-r4:8.8.1` - FHIR R4 resource structures

### Test Dependencies
- `org.junit.jupiter:junit-jupiter:5.8.2` - JUnit 5 test framework

## Maven Dependency

Add this to your project's `pom.xml`:

```xml
<dependency>
    <groupId>de.yoshteq</groupId>
    <artifactId>dcs-fhir-model</artifactId>
    <version>0.2.0</version>
</dependency>
```

## License

This project is licensed under the Apache License, Version 2.0. See the [LICENSE](../LICENSE) file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Support

For issues, questions, or feedback, please open an issue on the GitHub repository.
