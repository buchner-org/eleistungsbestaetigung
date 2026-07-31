package de.yoshteq.dcs.fhir;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.hl7.fhir.r4.model.Element;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.UuidType;

public class ParametersHelper
{

	private ParametersHelper()
	{
	}

	public static List<?> getAllByName(Parameters parameters, String name)
	{
		return parameters.getParameter().stream().filter(p -> p.getName().equals(name)).toList();
	}

	public static Optional<Identifier> getIdentifier(Parameters parameters, String parameterName)
	{
		return tryGetParameterValueByName(parameters, parameterName, Identifier.class);
	}

	public static Optional<Reference> getReference(Parameters parameters, String parameterName)
	{
		return tryGetParameterValueByName(parameters, parameterName, Reference.class);
	}

	public static String getString(Parameters parameters, String name)
	{
		return getParameterValueByName(parameters, name, StringType.class).getValue();
	}

	public static <T extends Element> T getParameterValueByName(Parameters parameters, String name, Class<T> classOfValue)
	{
		return tryGetParameterValueByName(parameters, name, classOfValue)
			.orElseThrow(() -> new FhirValidationException("required parameter not found: " + name));
	}

	public static <T extends Element> Optional<T> tryGetParameterValueByName(Parameters parameters, String name, Class<T> classOfValue)
	{
		var optionalParameter = tryGetByName(parameters, name);
		if (optionalParameter.isEmpty())
		{
			return Optional.empty();
		}

		var parameter = optionalParameter.get();
		if (parameter.getValue() == null)
		{
			throw new FhirValidationException("parameter '" + name + "' has no value.");
		}

		var value = parameter.getValue();
		if (!classOfValue.isInstance(value))
		{
			throw new FhirValidationException(
				"value of parameter '" + name + "' has wrong type. Expected '" + classOfValue.getSimpleName() + "' but was '" + value.getClass().getSimpleName() + "'");
		}

		return Optional.of(classOfValue.cast(value));
	}

	public static Optional<ParametersParameterComponent> tryGetByName(Parameters parameters, String name)
	{
		for (var parameter : parameters.getParameter())
		{
			var paramName = parameter.getName();
			if (paramName != null && paramName.equals(name))
			{
				return Optional.of(parameter);
			}
		}

		return Optional.empty();
	}

	public static void requireParameterCount(Parameters parameters, int count)
	{
		requireParameterCount(parameters, count, count);
	}

	public static void requireParameterCount(Parameters parameters, int min, int max)
	{
		if (parameters.getParameter() == null)
		{
			throw new FhirValidationException("Expected min " + min + " parameters but got none");
		}

		if (parameters.getParameter().size() < min || parameters.getParameter().size() > max)
		{
			var message = min == max
				? min + ""
				: "min " + min + " and max " + max;
			throw new FhirValidationException("Expected " + message + " parameters but got " + parameters.getParameter().size());
		}
	}

	public static void requireParameterCountMin(Parameters parameters, int min)
	{
		if (parameters.getParameter() == null)
		{
			throw new FhirValidationException("Expected min " + min + " parameters but got none");
		}

		if (parameters.getParameter().size() < min)
		{
			throw new FhirValidationException("Expected min " + min + " parameters but got " + parameters.getParameter().size());
		}
	}

	public static UUID extractUuid(ParametersParameterComponent parameter)
	{
		var value = parameter.getValue();
		if (!(value instanceof UuidType))
		{
			throw new FhirValidationException(
				"value of parameter '" + parameter.getName() + "' has wrong type. Expected 'uuid' but was '" + value.getClass().getSimpleName() + "'");
		}

		return UuidHelper.getUUIDFromUuid((UuidType) value);
	}

}
