package de.yoshteq.diga.secon;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tk.opensource.secon.SECON;
import de.tk.opensource.secon.SeconException;

@Provider
@Dependent
public class SeconMessageBodyWriter<T> extends AbstractSeconProvider implements MessageBodyWriter<SeconContainer<T>> {
	private static final Logger logger = LoggerFactory.getLogger(SeconMessageBodyWriter.class);

	@Inject
	@ConfigProperty(name = "jaxrs-secon.default-verfahren")
	Instance<java.util.Optional<java.lang.String>> defaultVerfahren;

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return SeconContainer.class.isAssignableFrom(type);
	}

	@Override
	public void writeTo(
			SeconContainer<T> seconContainer,
			Class<?> type,
			Type genericType,
			Annotation[] annotations,
			MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream)
			throws IOException,
			WebApplicationException {

		try (var seconInputStream = writeWrappedEntity(seconContainer, mediaType);
				var entityPartOutputStream = new ByteArrayOutputStream()) {

			// as this library should be usable for quarkus and jakartaee app servers, this
			// is a workaround as quarkus does not allow blocking access in IO Thread, so
			// the cert cannot be obtained from the db
			if (seconContainer.getReceiverCertificate() != null) {
				SECON.copy(() -> seconInputStream, seconSubscriber.signAndEncryptTo(() -> entityPartOutputStream,
						seconContainer.getReceiverCertificate()));
			} else {
				SECON.copy(() -> seconInputStream, seconSubscriber.signAndEncryptTo(() -> entityPartOutputStream,
						seconContainer.getIkEmpfaenger()));
			}

			var boundary = UUID.randomUUID().toString();
			var verfahren = seconContainer.getVerfahren().orElseGet(defaultVerfahren.get()::orElseThrow);
			writeMultipartFormData(entityStream, boundary, httpHeaders,
					SECON_PARAMETER_IKSENDER, seconIK, MediaType.TEXT_PLAIN_TYPE,
					SECON_PARAMETER_IKEMPFAENGER, seconContainer.getIkEmpfaenger(), MediaType.TEXT_PLAIN_TYPE,
					SECON_PARAMETER_VERFAHREN, verfahren, MediaType.TEXT_PLAIN_TYPE,
					SECON_PARAMETER_NUTZDATEN, entityPartOutputStream.toByteArray(),
					MediaType.APPLICATION_OCTET_STREAM_TYPE);
		} catch (SeconException e) {
			logger.error("Could not write secon response", e);
			throw new InternalServerErrorException();
		}
	}

	private InputStream writeWrappedEntity(SeconContainer<T> seconWrappedResponse, MediaType mediaType)
			throws WebApplicationException, IOException {
		try (var wrappedEntityOutputStream = new ByteArrayOutputStream();) {
			var innerMediaType = getInnerMediaType(mediaType);

			@SuppressWarnings("unchecked")
			var wrappedEntityWriter = (MessageBodyWriter<T>) providers.getMessageBodyWriter(
					seconWrappedResponse.getEntity().getClass(),
					seconWrappedResponse.getEntity().getClass(),
					null,
					innerMediaType);

			wrappedEntityWriter.writeTo(
					seconWrappedResponse.getEntity(),
					seconWrappedResponse.getEntity().getClass(),
					seconWrappedResponse.getEntity().getClass(),
					null,
					innerMediaType,
					new MultivaluedHashMap<>(),
					wrappedEntityOutputStream);

			return new ByteArrayInputStream(wrappedEntityOutputStream.toByteArray());
		}
	}

	private void writeMultipartFormData(OutputStream out, String boundary, MultivaluedMap<String, Object> httpHeaders,
			Object... fieldParts) throws IOException {
		httpHeaders.putSingle("Content-Type", "multipart/form-data; boundary=" + boundary);

		for (int i = 0; i < fieldParts.length; i += 3) {
			String fieldName = (String) fieldParts[i];
			Object fieldValue = fieldParts[i + 1];
			MediaType fieldMediaType = (MediaType) fieldParts[i + 2];

			out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
			out.write(("Content-Disposition: form-data; name=\"" + fieldName + "\"\r\n")
					.getBytes(StandardCharsets.UTF_8));
			out.write(("Content-Type: " + fieldMediaType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));

			if (fieldValue instanceof String stringValue) {
				out.write(stringValue.getBytes(StandardCharsets.UTF_8));
			} else if (fieldValue instanceof byte[] byteValue) {
				out.write(byteValue);
			}

			out.write("\r\n".getBytes(StandardCharsets.UTF_8));
		}

		out.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
	}

}
