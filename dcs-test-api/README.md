# DCS Test API

A Jakarta EE REST API specification for the test and test data interfaces of the **eLeistungsbestätigung Bestätigungssystem** (Digital Service Confirmation System). This library provides the standardized API contracts used to create, inspect, and remove confirmation requests as well as to generate test data in non-production environments.

## Overview

DCS Test API defines the REST interface for exercising the digital service confirmation process without a real service provider system. It enables test clients and integration tests to:

- **Confirmation Request Management**: Create, query, and delete confirmation requests directly
- **Request Inspection**: Retrieve the full state of a confirmation request including the signed response document
- **Test Data Generation**: Generate plausible confirmation requests per service type (Heilmittel, HKP, Hebamme)

Built on modern Java standards (Jakarta EE 10+) with OpenAPI documentation support, this package serves as the contract layer for the test interfaces of the eLeistungsbestätigung ecosystem.

> **Note:** These interfaces bypass the regular billing flow and expose insurant data. They are intended for test and staging environments only and must be protected by authentication and authorization (in `dcs-core` the `test-api` role with HTTP Basic authentication).

## Features

- **Jakarta REST (JAX-RS 3.1)**: Industry-standard REST API implementation
- **Jakarta Validation**: Automatic request validation with bean validation
- **MicroProfile OpenAPI**: Built-in OpenAPI/Swagger documentation support
- **Java 25+**: Modern Java language features (records, sealed classes)
- **Type-Safe DTOs**: Record-based immutable data transfer objects
- **Service Code Validation**: Resource-bundle-backed validation of service codes per service type

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
src/main/java/de/yoshteq/dcs/api/test/
├── TestApi.java                      # Main REST API interface
├── TestConfirmationRequest.java      # Confirmation request payload
├── ConfirmationRequestSummary.java   # Full state of a confirmation request
├── TestDataGenerationRequest.java    # Test data generation payload
├── TestDataServiceType.java          # Service types for test data generation
├── ServiceType.java                  # Service type with code and description lookup
├── RequestState.java                 # Confirmation request state enumeration
└── ResponseType.java                 # Response delivery type (callback/polling)
```

## API Endpoints

### Confirmation Requests

- `POST /confirmation-requests` - Create a confirmation request and return its generated id
- `GET /confirmation-requests` - List all confirmation requests with their current state
- `GET /confirmation-requests/{id}` - Retrieve a single confirmation request including the response document
- `DELETE /confirmation-requests/{id}` - Delete a confirmation request

### Test Data

- `POST /generate-data?serviceType={HEILMITTEL|HKP|HEBAMME}` - Generate confirmation requests for the given service type

## Domain Model

### Service Types

`ServiceType` maps the FHIR performer function code to a service domain and resolves the resource bundle used for service descriptions:

| Enum | Code | Description bundle suffix |
|------|------|---------------------------|
| `HILFSMITTEL` | `A` | `hilfsmittel` |
| `HEILMITTEL` | `B` | `heilmittel` |
| `HEBAMME` | `F` | `hebamme` |

`ServiceType.isValidServiceCode(String)` validates a service code against `service_descriptions_<suffix>_de.properties` on the classpath. For `HEBAMME` the lookup uses the first three characters of the code.

### Request State

`RequestState` describes the lifecycle of a confirmation request: `PENDING`, `CONFIRMED`, `DECLINED`, `CANCELLED`.

### Response Type

`ResponseType` describes how the response is delivered back to the requesting system: `CALLBACK` (the system pushes the response) or `POLLING` (the requesting system fetches it).

## Implementation

This package defines the API contract. Implement the `TestApi` interface in a Jakarta REST resource to create the backend service:

```java
@Path("/test-api")
public class TestApiImplementation implements TestApi {
    @Override
    public Long createConfirmationRequest(TestConfirmationRequest request) {
        // Persist a new confirmation request and return its id
    }

    @Override
    public List<ConfirmationRequestSummary> getConfirmationRequests() {
        // Return all confirmation requests
    }

    @Override
    public ConfirmationRequestSummary getConfirmationRequest(Long id) {
        // Return a single confirmation request including its response document
    }

    @Override
    public void deleteConfirmationRequest(Long id) {
        // Remove the confirmation request
    }

    @Override
    public List<TestConfirmationRequest> createConfirmationRequest(
        TestDataServiceType serviceType,
        TestDataGenerationRequest request) {
        // Generate and persist test confirmation requests
    }
}
```

## Dependencies

### Core Dependencies
- `jakarta.ws.rs-api:3.1.0` - Jakarta REST specification
- `jakarta.validation-api:3.0.2` - Jakarta Bean Validation specification
- `org.eclipse.microprofile.openapi:microprofile-openapi-api:4.1.1` - MicroProfile OpenAPI annotations

All dependencies use provided scope (expected to be supplied by the Jakarta EE runtime).

### Build Plugins
- `io.smallrye:jandex-maven-plugin:3.5.3` - Generates a Jandex index so CDI runtimes can discover the classes of this jar

## Maven Dependency

Add this to your project's `pom.xml`:

```xml
<dependency>
    <groupId>de.yoshteq</groupId>
    <artifactId>dcs-test-api</artifactId>
    <version>3.0.0</version>
</dependency>
```

## License

This project is licensed under the Apache License, Version 2.0. See the [LICENSE](../LICENSE) file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Support

For issues, questions, or feedback, please open an issue on the GitHub repository.
