package de.yoshteq.dcs.fhir;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.IntegerType;

@DisplayName("ExtensionHelper Tests")
class ExtensionHelperTest
{

	@Test
	@DisplayName("should retrieve extension with correct type")
	void getExtensionWithUrl()
	{
		var extensionUrl = "http://example.com/extension/custom";
		var extensionValue = new StringType("test value");
		var patient = new Patient();
		patient.addExtension(extensionUrl, extensionValue);

		var result = ExtensionHelper.getExtensionWithUrl(patient, extensionUrl, StringType.class);

		assertNotNull(result);
		assertEquals("test value", result.getValue());
	}

	@Test
	@DisplayName("should throw exception when extension not found")
	void getExtensionWithUrlNotFound()
	{
		var patient = new Patient();
		patient.addExtension("http://example.com/extension/other", new StringType("value"));

		assertThrows(
			FhirValidationException.class,
			() -> ExtensionHelper.getExtensionWithUrl(patient, "http://example.com/extension/missing", StringType.class)
		);
	}

	@Test
	@DisplayName("should throw exception when extension type is wrong")
	void getExtensionWithUrlWrongType()
	{
		var extensionUrl = "http://example.com/extension/custom";
		var patient = new Patient();
		patient.addExtension(extensionUrl, new StringType("test value"));

		var exception = assertThrows(
			FhirValidationException.class,
			() -> ExtensionHelper.getExtensionWithUrl(patient, extensionUrl, IntegerType.class)
		);

		assertTrue(exception.getMessage().contains("wrong type"));
		assertTrue(exception.getMessage().contains("StringType"));
		assertTrue(exception.getMessage().contains("IntegerType"));
	}

	@Test
	@DisplayName("should retrieve extension from resource with multiple extensions")
	void getExtensionWithUrlMultiple()
	{
		var url1 = "http://example.com/extension/ext1";
		var url2 = "http://example.com/extension/ext2";
		var value1 = new StringType("value1");
		var value2 = new StringType("value2");

		var patient = new Patient();
		patient.addExtension(url1, value1);
		patient.addExtension(url2, value2);

		var result = ExtensionHelper.getExtensionWithUrl(patient, url2, StringType.class);

		assertEquals("value2", result.getValue());
	}

	@Test
	@DisplayName("should throw exception when resource has no extensions")
	void getExtensionWithUrlNoExtensions()
	{
		var patient = new Patient();

		assertThrows(
			FhirValidationException.class,
			() -> ExtensionHelper.getExtensionWithUrl(patient, "http://example.com/extension/missing", StringType.class)
		);
	}

	@Test
	@DisplayName("should retrieve different extension types")
	void getExtensionWithUrlDifferentTypes()
	{
		var stringUrl = "http://example.com/extension/string";
		var intUrl = "http://example.com/extension/int";

		var patient = new Patient();
		patient.addExtension(stringUrl, new StringType("text"));
		patient.addExtension(intUrl, new IntegerType(42));

		var stringResult = ExtensionHelper.getExtensionWithUrl(patient, stringUrl, StringType.class);
		var intResult = ExtensionHelper.getExtensionWithUrl(patient, intUrl, IntegerType.class);

		assertEquals("text", stringResult.getValue());
		assertEquals(42, intResult.getValue());
	}

}
