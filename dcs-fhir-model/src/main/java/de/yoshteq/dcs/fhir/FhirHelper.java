package de.yoshteq.dcs.fhir;

import java.util.Optional;
import java.util.concurrent.Callable;

import org.hl7.fhir.r4.model.Element;
import org.hl7.fhir.r4.model.Resource;

import ca.uhn.fhir.context.FhirContext;

public class FhirHelper {
	private static final FhirContext ctx = FhirContext.forR4();

	private FhirHelper() {
	}

	public static <T> void assertFixed(T fixedValue, T value) {
		if (!fixedValue.equals(value)) {
			throw new FhirValidationException(
					buildMessage("fixed value constraint is not satisfied.", "Expected", fixedValue.toString(),
							"but got", value + ""));
		}
	}

	public static <T> void assertFixed(T fixedValue, Callable<T> valueFunction) {
		T value;

		try {
			value = valueFunction.call();
		} catch (Exception e) {
			throw new FhirValidationException(buildMessage("fixed value constraint is not satisfied"));
		}

		assertFixed(fixedValue, value);
	}

	public static void assertThat(Callable<Boolean> assertionFunction, String message) {
		try {
			var result = assertionFunction.call();
			if (Boolean.FALSE.equals(result)) {
				throw new FhirValidationException(buildMessage(message));
			}
		} catch (Exception e) {
			throw new FhirValidationException(buildMessage(message));
		}
	}

	public static <T> T required(T object, String... message) {
		if (object == null) {
			throw new FhirValidationException(buildMessage("required value is missing", message));
		}

		return object;
	}

	public static <T> T required(Callable<T> objectFunction, String... message) {
		try {
			var object = objectFunction.call();
			return required(object, message);
		} catch (Exception e) {
			throw new FhirValidationException(buildMessage("fixed value constraint is not satisfied", message));
		}
	}

	public static <T> Optional<T> optional(Callable<T> objectFunction) {
		try {
			var object = objectFunction.call();
			return Optional.of(object);
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	public static <T extends Resource> T assertResourceType(Resource resource, Class<T> expectedClass,
			String... message) {
		if (resource == null || !expectedClass.isInstance(resource)) {
			throw new FhirValidationException(buildMessage("unexpected resource type", message));
		}

		return expectedClass.cast(resource);
	}

	public static <T extends Element> T assertElementType(Element element, Class<T> expectedClass, String... message) {
		if (element == null || !expectedClass.isInstance(element)) {
			throw new FhirValidationException(buildMessage("unexpected element type", message));
		}

		return expectedClass.cast(element);
	}

	private static String buildMessage(String fallback, String... strings) {
		if (strings == null || strings.length == 0) {
			return fallback;
		}

		return String.join(" ", strings);
	}

	public static String toXmlString(Resource resource) {
		try {
			return ctx.newXmlParser().encodeResourceToString(resource);
		} catch (Exception e) {
			throw new FhirValidationException("Could not generate XML String from resource", e);
		}
	}
}
