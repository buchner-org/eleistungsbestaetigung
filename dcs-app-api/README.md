# DCS App API

A Jakarta EE REST API specification for the **eLeistungsbestätigung Bestätigungssystem** (Digital Service Confirmation System). This library provides the standardized API contracts for mobile applications to interact with digital service confirmation services.

## Overview

DCS App API defines the REST interface for managing insurant participation in the digital service confirmation system. It enables mobile applications to:

- **Insurant Registration & Management**: Register, deregister, and query participation status in the digital service confirmation process
- **Confirmation Requests**: Retrieve pending confirmation requests and submit responses
- **Service Information**: Access service descriptions and performer information

Built on modern Java standards (Jakarta EE 10+) with OpenAPI documentation support, this package serves as the contract layer for the eLeistungsbestätigung ecosystem.

## Features

- **Jakarta REST (JAX-RS 3.1)**: Industry-standard REST API implementation
- **Jakarta Validation**: Automatic request validation with bean validation
- **MicroProfile OpenAPI**: Built-in OpenAPI/Swagger documentation support
- **Java 25+**: Modern Java language features (records, sealed classes)
- **Type-Safe DTOs**: Record-based immutable data transfer objects

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
src/main/java/de/yoshteq/dcs/app/api/
├── AppApi.java                      # Main REST API interface
├── ParticipationStatusResponse.java  # Insurant participation status
├── ConfirmationRequestEntry.java    # Pending confirmation entry
├── ConfirmationRequestResponse.java  # Confirmation response submission
├── ServiceDescription.java          # Service description (title + text)
├── ServiceInformation.java          # Extended service information
├── PerformerInformation.java        # Performer/provider details
├── ParticipationRequest.java        # Participation request payload
├── ParticipationStatus.java         # Participation status enumeration
└── ResponseState.java               # Confirmation response state
```

## API Endpoints

### Insurant Management

- `GET /insurant` - Query insurant participation status in the confirmation system
- `POST /insurant` - Register insurant for participation in the digital service confirmation process
- `DELETE /insurant` - Deregister insurant from the confirmation system

### Confirmation Requests

- `GET /confirmation-requests` - List all pending confirmation requests for the insurant
- `POST /confirmation-requests/{id}/response` - Submit confirmation or rejection response for a specific request

## Implementation

This package defines the API contract. Implement the `AppApi` interface in a Jakarta REST resource to create the backend service:

```java
@Path("/api")
public class AppApiImplementation implements AppApi {
    @Override
    public ParticipationStatusResponse getParticipationStatus() {
        // Fetch current insurant participation status
    }

    @Override
    public void registerInsurant() {
        // Register insurant in the confirmation system
    }

    @Override
    public void deregisterInsurant() {
        // Remove insurant from the confirmation system
    }

    @Override
    public List<ConfirmationRequestEntry> listPendingConfirmationRequests() {
        // Return all pending confirmation requests for this insurant
    }

    @Override
    public void handleConfirmationResponse(Long id, ConfirmationRequestResponse response) {
        // Process confirmation or rejection response
    }
}
```

## Dependencies

### Core Dependencies
- `jakarta.ws.rs-api:3.1.0` - Jakarta REST specification
- `jakarta.validation-api:3.1.1` - Jakarta Bean Validation specification
- `org.eclipse.microprofile.openapi:microprofile-openapi-api:4.1.1` - MicroProfile OpenAPI annotations

All dependencies use provided scope (expected to be supplied by the Jakarta EE runtime).

## Maven Dependency

Add this to your project's `pom.xml`:

```xml
<dependency>
    <groupId>de.yoshteq</groupId>
    <artifactId>dcs-app-api</artifactId>
    <version>3.0.0</version>
</dependency>
```

## License

This project is licensed under the Apache License, Version 2.0. See the [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Support

For issues, questions, or feedback, please open an issue on the GitHub repository.
