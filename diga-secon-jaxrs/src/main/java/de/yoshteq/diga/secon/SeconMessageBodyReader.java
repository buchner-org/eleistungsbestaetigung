package de.yoshteq.diga.secon;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.validation.Validator;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.Provider;

import org.apache.commons.fileupload2.core.DiskFileItem;
import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tk.opensource.secon.SECON;

@Provider
@Dependent
public class SeconMessageBodyReader<T> extends AbstractSeconProvider implements MessageBodyReader<SeconContainer<T>> {
	private static final Logger logger = LoggerFactory.getLogger(SeconMessageBodyReader.class.getName());

	@Inject
	Validator validator;

	@Inject
	SeconRequestParameters seconRequestParameters;

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return SeconContainer.class.isAssignableFrom(type);
	}

	@SuppressWarnings("unchecked")
	@Override
	public SeconContainer<T> readFrom(
			Class<SeconContainer<T>> type,
			Type genericType,
			Annotation[] annotations,
			MediaType mediaType,
			MultivaluedMap<String, String> httpHeaders,
			InputStream entityStream)
			throws IOException,
			WebApplicationException {

		var items = parseMultipartFormData(mediaType, entityStream);
		fillSeconRequestParameter(items);

		try (var inputStream = getPartInputStream(items, SECON_PARAMETER_NUTZDATEN);
				var outputStream = new ByteArrayOutputStream();) {
			SECON.copy(
					seconSubscriber.decryptAndVerifyFrom(() -> inputStream,
							cert -> this.verifyCertificate(seconRequestParameters.getIkSender(), cert)),
					() -> outputStream);

			var innerMediaType = getInnerMediaType(mediaType);
			Type firstParameterizedType = ((ParameterizedType) genericType).getActualTypeArguments()[0];
			Class<T> entityType = (Class<T>) Class.forName(firstParameterizedType.getTypeName());
			var reader = providers.getMessageBodyReader(entityType, firstParameterizedType, annotations,
					innerMediaType);
			try (var entityInputStream = new ByteArrayInputStream(outputStream.toByteArray())) {
				var entity = reader.readFrom(entityType, firstParameterizedType, annotations, innerMediaType,
						httpHeaders, entityInputStream);

				return new SeconContainer<>(
						seconRequestParameters.getIkSender(),
						seconRequestParameters.getSenderCertificate(),
						null, // receiverCertificate — none on the inbound request
						seconRequestParameters.getIkEmpfaenger(),
						seconRequestParameters.getVerfahren(),
						entity,
						seconRequestParameters.getAdditionalParamters());
			}
		} catch (Exception e) {
			logger.error("could not read secon request", e);
			throw new BadRequestException("could not successfully decrypt and verify 'nutzdaten'");
		}
	}

	private List<DiskFileItem> parseMultipartFormData(MediaType mediaType, InputStream entityStream)
			throws IOException {
		var parser = new FileUploadMessageBodyReader<DiskFileItem, DiskFileItemFactory>(
				DiskFileItemFactory.builder().setThreshold(DiskFileItemFactory.DEFAULT_THRESHOLD)
						.setPath(System.getProperty("java.io.tmpdir")).get());
		return parser.parseRequest(new MultipartRequestContext(entityStream, mediaType));
	}

	private void fillSeconRequestParameter(List<DiskFileItem> multipartFormData) {
		var ikEmpfaenger = getStringPart(multipartFormData, SECON_PARAMETER_IKEMPFAENGER);
		if (!seconIK.equals(ikEmpfaenger)) {
			throw new BadRequestException(
					"the 'ikempfaenger' of the request does not match this systems IK [" + seconIK + "]");
		}

		var ikSender = getStringPart(multipartFormData, SECON_PARAMETER_IKSENDER);
		var verfahren = getStringPart(multipartFormData, SECON_PARAMETER_VERFAHREN);
		var additionalParameters = getAdditionalParameters(multipartFormData);

		seconRequestParameters.setIkEmpfaenger(ikEmpfaenger);
		seconRequestParameters.setIkSender(ikSender);
		seconRequestParameters.setVerfahren(verfahren);
		seconRequestParameters.setAdditionalParamters(additionalParameters);
		seconRequestParameters.validate(validator);
	}

	private Map<String, String> getAdditionalParameters(List<DiskFileItem> items) {
		return items
				.stream()
				.filter(DiskFileItem::isFormField)
				.filter(i -> !KNOWN_PARAMETERS.contains(i.getFieldName()))
				.map(i -> Map.entry(i.getFieldName(), getStringPart(items, i.getFieldName())))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}

	private String getStringPart(List<DiskFileItem> items, String key) {
		return items.stream()
				.filter(DiskFileItem::isFormField)
				.filter(i -> i.getFieldName().equals(key))
				.findFirst()
				.map(this::fileItemContentAsString)
				.orElse(null);
	}

	private String fileItemContentAsString(DiskFileItem item) {
		try {
			return item.getReader().readAllAsString();
		} catch (IOException _) {
			throw new BadRequestException("Could not read string content");
		}
	}

	private InputStream getPartInputStream(List<DiskFileItem> items, String key) {
		var item = items.stream()
				.filter(DiskFileItem::isFormField)
				.filter(i -> i.getFieldName().equals(key))
				.findFirst().orElseThrow(() -> new BadRequestException("nutzdaten part is missing"));
		try {
			return item.getInputStream();
		} catch (IOException _) {
			throw new BadRequestException("the part nutzdaten is malformed");
		}
	}

	private void verifyCertificate(String ikSender, X509Certificate certificate) {
		var certIk = X509CertificateUtil.extractIkAsString(certificate);
		if (certIk.isEmpty() || certIk.get().isBlank() || !certIk.get().equals(ikSender)) {
			throw new BadRequestException("mismatch of iksender and IK of certificate for encryption and signature");
		}

		seconRequestParameters.setSenderCertificate(certificate);
	}

}
