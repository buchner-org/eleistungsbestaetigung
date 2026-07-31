# DiGA SECON JAX-RS

A Jakarta REST provider library implementing the **SECON** transport security layer for the DiGA interface of the German healthcare system. This library provides a `MessageBodyReader` and `MessageBodyWriter` that transparently encrypt, decrypt, sign, and verify request and response payloads according to *Anlage 16 (Security-Schnittstelle)* and *Anlage 19 (DiGA-Schnittstelle)*.

## Overview

DiGA SECON JAX-RS keeps the SECON envelope out of REST resources: a resource method declares `SeconContainer<T>` as its parameter and return type and works with plain entities, while the providers handle the `multipart/form-data` envelope and the cryptography. It enables applications to:

- **Receive SECON Requests**: Parse the multipart envelope, decrypt and verify the `nutzdaten` part, and deserialize the inner entity
- **Send SECON Responses**: Serialize the inner entity, sign and encrypt it, and write the multipart envelope
- **Authenticate Senders**: Verify that the IK in the signing certificate matches the declared `iksender` and that `ikempfaenger` matches this system's own IK
- **Access Envelope Metadata**: Read `iksender`, `ikempfaenger`, `verfahren`, and any additional form fields from the container

Built on the open-source [secon-tool](https://github.com/DieTechniker/secon-tool) of Techniker Krankenkasse, this package serves as the transport security layer for the eLeistungsbestätigung ecosystem.

## Features

- **Jakarta REST (JAX-RS 3.1)**: Provider-based integration, no resource-level boilerplate
- **Transparent Crypto**: Sign, encrypt, decrypt, and verify handled by the message body providers
- **Pluggable Inner Media Type**: XML or JSON inner payloads via `SeconMediaType.INNER_XML` / `INNER_JSON`
- **Certificate-Based Sender Verification**: IK extraction from the X.509 subject and comparison with the declared sender
- **Jakarta Validation**: `iksender`, `ikempfaenger`, and `verfahren` are validated on every inbound request
- **Runtime Agnostic**: Usable from Quarkus and Jakarta EE application servers; the receiver certificate may be supplied explicitly to avoid blocking lookups on IO threads
- **Java 25+**: Modern Java language features

## Requirements

- **Java 25** or higher
- **Maven 3.8+**
- A Jakarta REST runtime with CDI and MicroProfile Config
- A SECON `Identity` (own key and certificate) and `Directory` (peer certificates) available as CDI beans

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
src/main/java/de/yoshteq/diga/secon/
├── SeconContainer.java               # Envelope wrapper around the inner entity
├── SeconMediaType.java               # Media types with innerType parameter
├── SeconMessageBodyReader.java       # Inbound: parse, decrypt, verify, deserialize
├── SeconMessageBodyWriter.java       # Outbound: serialize, sign, encrypt, write
├── AbstractSeconProvider.java        # Shared provider setup and parameter names
├── SeconRequestParameters.java       # Request-scoped envelope parameters
├── SeconMultipartRequest.java        # Multipart input stream and media type
├── MultipartRequestContext.java      # RequestContext adapter for commons-fileupload
├── FileUploadMessageBodyReader.java  # Multipart parser for JAX-RS streams
└── X509CertificateUtil.java          # IK extraction from certificate subjects
```

## Envelope Format

SECON requests and responses are `multipart/form-data` bodies with the following parts:

| Part | Content type | Description |
|------|--------------|-------------|
| `iksender` | `text/plain` | Institution ID (IK) of the sending system |
| `ikempfaenger` | `text/plain` | Institution ID (IK) of the receiving system |
| `verfahren` | `text/plain` | Procedure identifier (e.g. `ELB`) |
| `nutzdaten` | `application/octet-stream` | The signed and encrypted inner payload |

Any additional form fields are made available through `SeconContainer.getAdditionalParameters()`.

The inner media type is selected via the `innerType` media type parameter, which `SeconMediaType` provides as constants:

```java
SeconMediaType.INNER_XML   // multipart/form-data;innerType="application/xml"
SeconMediaType.INNER_JSON  // multipart/form-data;innerType="application/json"
```

## Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `jaxrs-secon.default-verfahren` | *(required for writing)* | Procedure written to the `verfahren` part when the container does not specify one (e.g. `ELB`) |
| `jaxrs-secon.default-inner-mime-type` | `application/xml` | Inner media type used when the request or response media type has no `innerType` parameter |

The providers additionally require two CDI beans to be produced by the application:

- `de.tk.opensource.secon.Identity` - the own signing and decryption identity; its certificate subject must contain the own IK as `IK<9 digits>`
- `de.tk.opensource.secon.Directory` - the directory used to resolve peer certificates for encryption and signature verification

## Usage

### Receiving and answering a SECON request

Declare `SeconContainer<T>` as parameter and return type — the providers do the rest:

```java
@Path("/billing")
@Consumes(SeconMediaType.INNER_XML)
@Produces(SeconMediaType.INNER_XML)
public class BillingResource {

    @POST
    @Path("/confirmation-request")
    public SeconContainer<Parameters> postConfirmationRequest(SeconContainer<Parameters> request) {
        String senderIk = request.getIkSender();
        Parameters payload = request.getEntity();

        Parameters response = process(senderIk, payload);

        // Responds to the original sender, reusing its certificate and verfahren
        return request.respondWith(response);
    }
}
```

### Sending a SECON request via a REST client

```java
SeconContainer<Parameters> container = SeconContainer.create(receiverIk, "ELB", parameters);
Response response = client.postConfirmationResponse(container);
```

If the receiver certificate has already been resolved — for example from a database, which must not happen on a Quarkus IO thread — pass it explicitly so the writer does not have to look it up:

```java
SeconContainer<Parameters> container =
    SeconContainer.create(receiverIk, receiverCertificate, parameters);
```

### Extracting the IK from a certificate

```java
Optional<Integer> ik = X509CertificateUtil.extractIkAsInteger(certificate);
```

## Error Handling

| Situation | Result |
|-----------|--------|
| `ikempfaenger` does not match this system's IK | `400 Bad Request` |
| IK of the signing certificate does not match `iksender` | `400 Bad Request` |
| `nutzdaten` part missing or malformed | `400 Bad Request` |
| Decryption or signature verification fails | `400 Bad Request` |
| `iksender`, `ikempfaenger`, or `verfahren` blank | `ConstraintViolationException` |
| Signing or encryption of a response fails | `500 Internal Server Error` |

## Dependencies

### Core Dependencies
- `de.tk.opensource:secon-tool:1.2.1` - SECON signing, encryption, and verification
- `org.apache.commons:commons-fileupload2-core:2.0.0-M5` - Multipart form data parsing

### Provided Dependencies
- `jakarta.ws.rs-api:3.1.0` - Jakarta REST specification
- `jakarta.validation-api:3.1.1` - Jakarta Bean Validation specification
- `jakarta.enterprise.cdi-api:4.1.0` - Jakarta CDI specification
- `microprofile-config-api:3.1.1` - MicroProfile Config annotations
- `slf4j-api:2.0.18` - Logging facade

Provided dependencies are expected to be supplied by the Jakarta EE runtime.

### Build Plugins
- `org.jboss.jandex:jandex-maven-plugin:1.2.2` - Generates a Jandex index so CDI runtimes can discover the providers of this jar

## Maven Dependency

Add this to your project's `pom.xml`:

```xml
<dependency>
    <groupId>de.yoshteq</groupId>
    <artifactId>diga-secon-jaxrs</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Specification

The implemented interface is defined by the following annexes of the DiGA framework agreement:

- **Anlage 16** - Security-Schnittstelle (encryption and signature)
- **Anlage 19** - DiGA-Schnittstelle (transport and envelope)

## License

This project is licensed under the Apache License, Version 2.0. See the [LICENSE](../LICENSE) file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Support

For issues, questions, or feedback, please open an issue on the GitHub repository.
