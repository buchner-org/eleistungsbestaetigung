package de.yoshteq.dcs.fhir;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.UuidType;

@DisplayName("ParametersHelper Tests")
class ParametersHelperTest
{

	private Parameters parameters;

	@BeforeEach
	void setUp()
	{
		parameters = new Parameters();
	}

	@Test
	@DisplayName("should retrieve all parameters by name")
	void getAllByName()
	{
		var name = "testParam";
		parameters.addParameter(name, new StringType("value1"));
		parameters.addParameter(name, new StringType("value2"));
		parameters.addParameter("otherParam", new StringType("value3"));

		var results = ParametersHelper.getAllByName(parameters, name);

		assertEquals(2, results.size());
	}

	@Test
	@DisplayName("should return empty list when name not found")
	void getAllByNameNotFound()
	{
		parameters.addParameter("existingParam", new StringType("value"));

		var results = ParametersHelper.getAllByName(parameters, "nonExistent");

		assertTrue(results.isEmpty());
	}

	@Test
	@DisplayName("should retrieve identifier parameter")
	void getIdentifier()
	{
		var identifier = new Identifier().setValue("12345");
		parameters.addParameter("patientId", identifier);

		var result = ParametersHelper.getIdentifier(parameters, "patientId");

		assertTrue(result.isPresent());
		assertEquals("12345", result.get().getValue());
	}

	@Test
	@DisplayName("should return empty optional when identifier parameter not found")
	void getIdentifierNotFound()
	{
		var result = ParametersHelper.getIdentifier(parameters, "missingId");

		assertTrue(result.isEmpty());
	}

	@Test
	@DisplayName("should throw exception when parameter has no value")
	void getIdentifierNoValue()
	{
		var component = new ParametersParameterComponent();
		component.setName("patientId");
		parameters.getParameter().add(component);

		var exception = assertThrows(
			FhirValidationException.class,
			() -> ParametersHelper.getIdentifier(parameters, "patientId")
		);

		assertTrue(exception.getMessage().contains("has no value"));
	}

	@Test
	@DisplayName("should retrieve reference parameter")
	void getReference()
	{
		var reference = new Reference("Patient/123");
		parameters.addParameter("subject", reference);

		var result = ParametersHelper.getReference(parameters, "subject");

		assertTrue(result.isPresent());
		assertEquals("Patient/123", result.get().getReference());
	}

	@Test
	@DisplayName("should return empty optional when reference parameter not found")
	void getReferenceNotFound()
	{
		var result = ParametersHelper.getReference(parameters, "missingRef");

		assertTrue(result.isEmpty());
	}

	@Test
	@DisplayName("should retrieve string parameter")
	void getString()
	{
		var value = "test value";
		parameters.addParameter("message", new StringType(value));

		var result = ParametersHelper.getString(parameters, "message");

		assertEquals(value, result);
	}

	@Test
	@DisplayName("should throw exception when string parameter not found")
	void getStringNotFound()
	{
		var exception = assertThrows(
			FhirValidationException.class,
			() -> ParametersHelper.getString(parameters, "missing")
		);

		assertTrue(exception.getMessage().contains("required parameter not found"));
	}

	@Test
	@DisplayName("should retrieve parameter value by name with type")
	void getParameterValueByName()
	{
		var identifier = new Identifier().setValue("test");
		parameters.addParameter("id", identifier);

		var result = ParametersHelper.getParameterValueByName(parameters, "id", Identifier.class);

		assertNotNull(result);
		assertEquals("test", result.getValue());
	}

	@Test
	@DisplayName("should throw exception for missing required parameter")
	void getParameterValueByNameMissing()
	{
		var exception = assertThrows(
			FhirValidationException.class,
			() -> ParametersHelper.getParameterValueByName(parameters, "missing", StringType.class)
		);

		assertTrue(exception.getMessage().contains("required parameter not found: missing"));
	}

	@Test
	@DisplayName("should throw exception when parameter has no value")
	void getParameterValueByNameNoValue()
	{
		var component = new ParametersParameterComponent();
		component.setName("empty");
		parameters.getParameter().add(component);

		var exception = assertThrows(
			FhirValidationException.class,
			() -> ParametersHelper.getParameterValueByName(parameters, "empty", StringType.class)
		);

		assertTrue(exception.getMessage().contains("has no value"));
	}

	@Test
	@DisplayName("should throw exception when parameter has wrong type")
	void getParameterValueByNameWrongType()
	{
		parameters.addParameter("param", new StringType("value"));

		var exception = assertThrows(
			FhirValidationException.class,
			() -> ParametersHelper.getParameterValueByName(parameters, "param", Identifier.class)
		);

		assertTrue(exception.getMessage().contains("has wrong type"));
		assertTrue(exception.getMessage().contains("Identifier"));
	}

	@Test
	@DisplayName("should optionally retrieve parameter value by name")
	void tryGetParameterValueByName()
	{
		var uuid = new UuidType("urn:uuid:550e8400-e29b-41d4-a716-446655440000");
		parameters.addParameter("id", uuid);

		var result = ParametersHelper.tryGetParameterValueByName(parameters, "id", UuidType.class);

		assertTrue(result.isPresent());
	}

	@Test
	@DisplayName("should return empty optional when trying to get missing parameter")
	void tryGetParameterValueByNameMissing()
	{
		var result = ParametersHelper.tryGetParameterValueByName(parameters, "missing", StringType.class);

		assertTrue(result.isEmpty());
	}

	@Test
	@DisplayName("should throw exception when trying to get parameter with no value")
	void tryGetParameterValueByNameNoValue()
	{
		var component = new ParametersParameterComponent();
		component.setName("empty");
		parameters.getParameter().add(component);

		var exception = assertThrows(
			FhirValidationException.class,
			() -> ParametersHelper.tryGetParameterValueByName(parameters, "empty", StringType.class)
		);

		assertTrue(exception.getMessage().contains("has no value"));
	}

	@Test
	@DisplayName("should throw exception when trying to get parameter with wrong type")
	void tryGetParameterValueByNameWrongType()
	{
		parameters.addParameter("param", new StringType("value"));

		var exception = assertThrows(
			FhirValidationException.class,
			() -> ParametersHelper.tryGetParameterValueByName(parameters, "param", Identifier.class)
		);

		assertTrue(exception.getMessage().contains("has wrong type"));
	}

	@Test
	@DisplayName("should retrieve parameter by name")
	void tryGetByName()
	{
		parameters.addParameter("test", new StringType("value"));

		var result = ParametersHelper.tryGetByName(parameters, "test");

		assertTrue(result.isPresent());
		assertEquals("test", result.get().getName());
	}

	@Test
	@DisplayName("should return empty optional when parameter not found")
	void tryGetByNameNotFound()
	{
		var result = ParametersHelper.tryGetByName(parameters, "missing");

		assertTrue(result.isEmpty());
	}

	@Test
	@DisplayName("should handle null parameter name gracefully")
	void tryGetByNameNullName()
	{
		var component = new ParametersParameterComponent();
		component.setValue(new StringType("value"));
		parameters.getParameter().add(component);

		var result = ParametersHelper.tryGetByName(parameters, "test");

		assertTrue(result.isEmpty());
	}

	@Test
	@DisplayName("should require exact parameter count")
	void requireParameterCount()
	{
		parameters.addParameter("param1", new StringType("value1"));
		parameters.addParameter("param2", new StringType("value2"));

		assertDoesNotThrow(() -> ParametersHelper.requireParameterCount(parameters, 2));
	}

	@Test
	@DisplayName("should throw exception when parameter count is less than required")
	void requireParameterCountTooFew()
	{
		parameters.addParameter("param", new StringType("value"));

		var exception = assertThrows(
			FhirValidationException.class,
			() -> ParametersHelper.requireParameterCount(parameters, 2)
		);

		assertTrue(exception.getMessage().contains("Expected 2 parameters but got 1"));
	}

	@Test
	@DisplayName("should throw exception when parameter count is more than required")
	void requireParameterCountTooMany()
	{
		parameters.addParameter("param1", new StringType("value1"));
		parameters.addParameter("param2", new StringType("value2"));
		parameters.addParameter("param3", new StringType("value3"));

		var exception = assertThrows(
			FhirValidationException.class,
			() -> ParametersHelper.requireParameterCount(parameters, 2)
		);

		assertTrue(exception.getMessage().contains("Expected 2 parameters but got 3"));
	}

	@Test
	@DisplayName("should throw exception when parameter count is zero")
	void requireParameterCountNull()
	{
		var emptyParams = new Parameters();

		assertThrows(
			FhirValidationException.class,
			() -> ParametersHelper.requireParameterCount(emptyParams, 1)
		);
	}

	@Test
	@DisplayName("should require parameter count within range")
	void requireParameterCountRange()
	{
		parameters.addParameter("param1", new StringType("value1"));
		parameters.addParameter("param2", new StringType("value2"));

		assertDoesNotThrow(() -> ParametersHelper.requireParameterCount(parameters, 1, 3));
	}

	@Test
	@DisplayName("should throw exception when parameter count is below range")
	void requireParameterCountRangeTooFew()
	{
		var exception = assertThrows(
			FhirValidationException.class,
			() -> ParametersHelper.requireParameterCount(parameters, 1, 3)
		);

		assertTrue(exception.getMessage().contains("min 1 and max 3"));
	}

	@Test
	@DisplayName("should throw exception when parameter count is above range")
	void requireParameterCountRangeTooMany()
	{
		parameters.addParameter("param1", new StringType("value1"));
		parameters.addParameter("param2", new StringType("value2"));
		parameters.addParameter("param3", new StringType("value3"));
		parameters.addParameter("param4", new StringType("value4"));

		var exception = assertThrows(
			FhirValidationException.class,
			() -> ParametersHelper.requireParameterCount(parameters, 1, 3)
		);

		assertTrue(exception.getMessage().contains("min 1 and max 3"));
	}

	@Test
	@DisplayName("should require minimum parameter count")
	void requireParameterCountMin()
	{
		parameters.addParameter("param1", new StringType("value1"));
		parameters.addParameter("param2", new StringType("value2"));

		assertDoesNotThrow(() -> ParametersHelper.requireParameterCountMin(parameters, 2));
	}

	@Test
	@DisplayName("should allow more than minimum parameter count")
	void requireParameterCountMinAllow()
	{
		parameters.addParameter("param1", new StringType("value1"));
		parameters.addParameter("param2", new StringType("value2"));
		parameters.addParameter("param3", new StringType("value3"));

		assertDoesNotThrow(() -> ParametersHelper.requireParameterCountMin(parameters, 2));
	}

	@Test
	@DisplayName("should throw exception when parameter count is below minimum")
	void requireParameterCountMinTooFew()
	{
		parameters.addParameter("param", new StringType("value"));

		var exception = assertThrows(
			FhirValidationException.class,
			() -> ParametersHelper.requireParameterCountMin(parameters, 2)
		);

		assertTrue(exception.getMessage().contains("Expected min 2 parameters but got 1"));
	}

	@Test
	@DisplayName("should throw exception when no parameters for minimum requirement")
	void requireParameterCountMinNull()
	{
		var emptyParams = new Parameters();

		assertThrows(
			FhirValidationException.class,
			() -> ParametersHelper.requireParameterCountMin(emptyParams, 1)
		);
	}

	@Test
	@DisplayName("should extract UUID from parameter component")
	void extractUuid()
	{
		var uuidString = "urn:uuid:550e8400-e29b-41d4-a716-446655440000";
		var uuidType = new UuidType(uuidString);
		var component = new ParametersParameterComponent();
		component.setName("id");
		component.setValue(uuidType);

		var result = ParametersHelper.extractUuid(component);

		assertNotNull(result);
		assertEquals(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"), result);
	}

	@Test
	@DisplayName("should throw exception when extracting non-UUID value")
	void extractUuidWrongType()
	{
		var component = new ParametersParameterComponent();
		component.setName("id");
		component.setValue(new StringType("not-a-uuid"));

		var exception = assertThrows(
			FhirValidationException.class,
			() -> ParametersHelper.extractUuid(component)
		);

		assertTrue(exception.getMessage().contains("has wrong type"));
		assertTrue(exception.getMessage().contains("Expected 'uuid'"));
	}

}
