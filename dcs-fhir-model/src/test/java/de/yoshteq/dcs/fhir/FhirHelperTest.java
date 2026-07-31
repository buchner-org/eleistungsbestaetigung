package de.yoshteq.dcs.fhir;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Reference;

@DisplayName("FhirHelper Tests")
class FhirHelperTest
{

	@Test
	@DisplayName("should verify fixed value matches")
	void assertFixedValue()
	{
		var fixedValue = "expected";
		var actualValue = "expected";

		assertDoesNotThrow(() -> FhirHelper.assertFixed(fixedValue, actualValue));
	}

	@Test
	@DisplayName("should throw exception when fixed value does not match")
	void assertFixedValueMismatch()
	{
		var fixedValue = "expected";
		var actualValue = "actual";

		assertThrows(
			FhirValidationException.class,
			() -> FhirHelper.assertFixed(fixedValue, actualValue)
		);
	}

	@Test
	@DisplayName("should verify fixed value from callable")
	void assertFixedCallable()
	{
		var fixedValue = "expected";

		assertDoesNotThrow(() -> FhirHelper.assertFixed(fixedValue, () -> "expected"));
	}

	@Test
	@DisplayName("should throw exception when callable fixed value does not match")
	void assertFixedCallableMismatch()
	{
		var fixedValue = "expected";

		assertThrows(
			FhirValidationException.class,
			() -> FhirHelper.assertFixed(fixedValue, () -> "actual")
		);
	}

	@Test
	@DisplayName("should throw exception when callable throws exception")
	void assertFixedCallableException()
	{
		var fixedValue = "expected";

		assertThrows(
			FhirValidationException.class,
			() -> FhirHelper.assertFixed(fixedValue, () -> {
				throw new RuntimeException("error");
			})
		);
	}

	@Test
	@DisplayName("should pass assertion when callable returns true")
	void assertThatTrue()
	{
		assertDoesNotThrow(() -> FhirHelper.assertThat(() -> true, "assertion failed"));
	}

	@Test
	@DisplayName("should throw exception when assertion returns false")
	void assertThatFalse()
	{
		assertThrows(
			FhirValidationException.class,
			() -> FhirHelper.assertThat(() -> false, "assertion failed")
		);
	}

	@Test
	@DisplayName("should throw exception when assertion callable throws exception")
	void assertThatException()
	{
		assertThrows(
			FhirValidationException.class,
			() -> FhirHelper.assertThat(() -> {
				throw new RuntimeException("error");
			}, "assertion failed")
		);
	}

	@Test
	@DisplayName("should return non-null required object")
	void requiredNonNull()
	{
		var value = "required value";

		var result = FhirHelper.required(value);

		assertEquals(value, result);
	}

	@Test
	@DisplayName("should throw exception for null required object")
	void requiredNull()
	{
		assertThrows(
			FhirValidationException.class,
			() -> FhirHelper.required(null)
		);
	}

	@Test
	@DisplayName("should throw exception with custom message for null required object")
	void requiredNullWithMessage()
	{
		var exception = assertThrows(
			FhirValidationException.class,
			() -> FhirHelper.required(null, "custom", "message")
		);

		assertTrue(exception.getMessage().contains("custom"));
		assertTrue(exception.getMessage().contains("message"));
	}

	@Test
	@DisplayName("should return object from callable")
	void requiredCallable()
	{
		var value = "required value";

		var result = FhirHelper.required(() -> value);

		assertEquals(value, result);
	}

	@Test
	@DisplayName("should throw exception when callable returns null")
	void requiredCallableNull()
	{
		assertThrows(
			FhirValidationException.class,
			() -> FhirHelper.required(() -> null)
		);
	}

	@Test
	@DisplayName("should throw exception when callable throws exception")
	void requiredCallableException()
	{
		assertThrows(
			FhirValidationException.class,
			() -> FhirHelper.required(() -> {
				throw new RuntimeException("error");
			})
		);
	}

	@Test
	@DisplayName("should return optional with value from callable")
	void optionalValue()
	{
		var value = "optional value";

		var result = FhirHelper.optional(() -> value);

		assertTrue(result.isPresent());
		assertEquals(value, result.get());
	}

	@Test
	@DisplayName("should return empty optional when callable returns null")
	void optionalNull()
	{
		var result = FhirHelper.optional(() -> null);

		assertTrue(result.isEmpty());
	}

	@Test
	@DisplayName("should return empty optional when callable throws exception")
	void optionalException()
	{
		var result = FhirHelper.optional(() -> {
			throw new RuntimeException("error");
		});

		assertTrue(result.isEmpty());
	}

	@Test
	@DisplayName("should cast resource to expected type")
	void assertResourceTypeValid()
	{
		var patient = new Patient();
		patient.setId("123");

		var result = FhirHelper.assertResourceType(patient, Patient.class);

		assertEquals("123", result.getId());
	}

	@Test
	@DisplayName("should throw exception for resource type mismatch")
	void assertResourceTypeWrongType()
	{
		var observation = new Observation();

		assertThrows(
			FhirValidationException.class,
			() -> FhirHelper.assertResourceType(observation, Patient.class)
		);
	}

	@Test
	@DisplayName("should throw exception when resource is null")
	void assertResourceTypeNull()
	{
		assertThrows(
			FhirValidationException.class,
			() -> FhirHelper.assertResourceType(null, Patient.class)
		);
	}

	@Test
	@DisplayName("should throw exception with custom message for wrong resource type")
	void assertResourceTypeWrongTypeWithMessage()
	{
		var observation = new Observation();

		var exception = assertThrows(
			FhirValidationException.class,
			() -> FhirHelper.assertResourceType(observation, Patient.class, "custom", "message")
		);

		assertTrue(exception.getMessage().contains("custom"));
		assertTrue(exception.getMessage().contains("message"));
	}

	@Test
	@DisplayName("should cast element to expected type")
	void assertElementTypeValid()
	{
		var stringElement = new StringType("test");

		var result = FhirHelper.assertElementType(stringElement, StringType.class);

		assertEquals("test", result.getValue());
	}

	@Test
	@DisplayName("should throw exception for element type mismatch")
	void assertElementTypeWrongType()
	{
		var stringElement = new StringType("test");

		assertThrows(
			FhirValidationException.class,
			() -> FhirHelper.assertElementType(stringElement, Reference.class)
		);
	}

	@Test
	@DisplayName("should throw exception when element is null")
	void assertElementTypeNull()
	{
		assertThrows(
			FhirValidationException.class,
			() -> FhirHelper.assertElementType(null, StringType.class)
		);
	}

	@Test
	@DisplayName("should throw exception with custom message for wrong element type")
	void assertElementTypeWrongTypeWithMessage()
	{
		var stringElement = new StringType("test");

		var exception = assertThrows(
			FhirValidationException.class,
			() -> FhirHelper.assertElementType(stringElement, Reference.class, "custom", "message")
		);

		assertTrue(exception.getMessage().contains("custom"));
		assertTrue(exception.getMessage().contains("message"));
	}

	@Test
	@DisplayName("should convert resource to XML string")
	void toXmlString()
	{
		var patient = new Patient();
		patient.setId("123");

		var result = FhirHelper.toXmlString(patient);

		assertNotNull(result);
		assertTrue(result.contains("Patient"));
		assertTrue(result.contains("123"));
	}

	@Test
	@DisplayName("should throw exception when resource cannot be converted to XML")
	void toXmlStringException()
	{
		var exception = assertThrows(
			FhirValidationException.class,
			() -> FhirHelper.toXmlString(null)
		);

		assertTrue(exception.getMessage().contains("Could not generate XML String"));
	}

}
