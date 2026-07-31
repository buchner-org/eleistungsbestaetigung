package de.yoshteq.dcs.fhir;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.hl7.fhir.r4.model.Identifier;

@DisplayName("IdentifierHelper Tests")
class IdentifierHelperTest
{

	@Test
	@DisplayName("should extract valid IK value from identifier")
	void getIkValueValid()
	{
		var identifier = new Identifier()
			.setSystem(IdentifierHelper.IK_SYSTEM)
			.setValue("103441809");

		var result = IdentifierHelper.getIkValue(identifier);

		assertEquals(103441809, result);
	}

	@Test
	@DisplayName("should extract IK value with 8 digits")
	void getIkValueEightDigits()
	{
		var identifier = new Identifier()
			.setSystem(IdentifierHelper.IK_SYSTEM)
			.setValue("10344180");

		var result = IdentifierHelper.getIkValue(identifier);

		assertEquals(10344180, result);
	}

	@Test
	@DisplayName("should throw exception for invalid IK system")
	void getIkValueWrongSystem()
	{
		var identifier = new Identifier()
			.setSystem("http://wrong.system")
			.setValue("103441809");

		assertThrows(
			FhirValidationException.class,
			() -> IdentifierHelper.getIkValue(identifier)
		);
	}

	@Test
	@DisplayName("should throw exception for invalid IK format")
	void getIkValueInvalidFormat()
	{
		var identifier = new Identifier()
			.setSystem(IdentifierHelper.IK_SYSTEM)
			.setValue("invalid");

		assertThrows(
			FhirValidationException.class,
			() -> IdentifierHelper.getIkValue(identifier)
		);
	}

	@Test
	@DisplayName("should throw exception for IK with less than 8 digits")
	void getIkValueTooShort()
	{
		var identifier = new Identifier()
			.setSystem(IdentifierHelper.IK_SYSTEM)
			.setValue("1234567");

		assertThrows(
			FhirValidationException.class,
			() -> IdentifierHelper.getIkValue(identifier)
		);
	}

	@Test
	@DisplayName("should extract valid KVNR value from identifier")
	void getKvnrValueValid()
	{
		var identifier = new Identifier()
			.setSystem(IdentifierHelper.KVID_SYSTEM)
			.setValue("A123456789");

		var result = IdentifierHelper.getKvnrValue(identifier);

		assertEquals("A123456789", result);
	}

	@Test
	@DisplayName("should throw exception for invalid KVNR system")
	void getKvnrValueWrongSystem()
	{
		var identifier = new Identifier()
			.setSystem("http://wrong.system")
			.setValue("A1234567890");

		assertThrows(
			FhirValidationException.class,
			() -> IdentifierHelper.getKvnrValue(identifier)
		);
	}

	@Test
	@DisplayName("should throw exception for invalid KVNR format")
	void getKvnrValueInvalidFormat()
	{
		var identifier = new Identifier()
			.setSystem(IdentifierHelper.KVID_SYSTEM)
			.setValue("123456789");

		assertThrows(
			FhirValidationException.class,
			() -> IdentifierHelper.getKvnrValue(identifier)
		);
	}

	@Test
	@DisplayName("should throw exception when KVNR value is missing")
	void getKvnrValueMissing()
	{
		var identifier = new Identifier()
			.setSystem(IdentifierHelper.KVID_SYSTEM);

		assertThrows(
			FhirValidationException.class,
			() -> IdentifierHelper.getKvnrValue(identifier)
		);
	}

	@Test
	@DisplayName("should extract UUID from identifier")
	void getUuid()
	{
		var uuid = UUID.randomUUID();
		var uuidString = UuidHelper.toUuidString(uuid);
		var identifier = new Identifier()
			.setSystem(IdentifierHelper.RFC3986_SYSTEM)
			.setValue(uuidString);

		var result = IdentifierHelper.getUuid(identifier);

		assertEquals(uuid, result);
	}

	@Test
	@DisplayName("should throw exception for invalid UUID system")
	void getUuidWrongSystem()
	{
		var identifier = new Identifier()
			.setSystem("http://wrong.system")
			.setValue("urn:uuid:550e8400-e29b-41d4-a716-446655440000");

		assertThrows(
			FhirValidationException.class,
			() -> IdentifierHelper.getUuid(identifier)
		);
	}

	@Test
	@DisplayName("should throw exception when UUID value is missing")
	void getUuidMissing()
	{
		var identifier = new Identifier()
			.setSystem(IdentifierHelper.RFC3986_SYSTEM);

		assertThrows(
			FhirValidationException.class,
			() -> IdentifierHelper.getUuid(identifier)
		);
	}

	@Test
	@DisplayName("should create IK identifier with correct system and value")
	void createIkIdentifier()
	{
		var ikValue = 103441809;

		var result = IdentifierHelper.createIkIdentifier(ikValue);

		assertEquals(IdentifierHelper.IK_SYSTEM, result.getSystem());
		assertEquals("103441809", result.getValue());
	}

	@Test
	@DisplayName("should create UUID identifier with correct system and value")
	void createUuidIdentifier()
	{
		var uuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

		var result = IdentifierHelper.createUuidIdentifier(uuid);

		assertEquals(IdentifierHelper.RFC3986_SYSTEM, result.getSystem());
		assertEquals("urn:uuid:550e8400-e29b-41d4-a716-446655440000", result.getValue());
	}

	@Test
	@DisplayName("should create KVNR identifier with correct system and value")
	void createKvnrIdentifier()
	{
		var kvnr = "A123456789";

		var result = IdentifierHelper.createKvnrIdentifier(kvnr);

		assertEquals(IdentifierHelper.KVID_SYSTEM, result.getSystem());
		assertEquals(kvnr, result.getValue());
	}

	@Test
	@DisplayName("should throw exception when creating KVNR identifier with invalid format")
	void createKvnrIdentifierInvalid()
	{
		var invalidKvnr = "invalid";

		assertThrows(
			FhirValidationException.class,
			() -> IdentifierHelper.createKvnrIdentifier(invalidKvnr)
		);
	}

	@Test
	@DisplayName("should create KVNR identifier with various valid formats")
	void createKvnrIdentifierVariousFormats()
	{
		var validFormats = new String[] { "A123456789", "Z999999999", "B000000000" };

		for (var kvnr : validFormats)
		{
			var result = IdentifierHelper.createKvnrIdentifier(kvnr);
			assertEquals(kvnr, result.getValue());
		}
	}

	@Test
	@DisplayName("should reject KVNR with lowercase letter")
	void createKvnrIdentifierLowercaseLetter()
	{
		assertThrows(
			FhirValidationException.class,
			() -> IdentifierHelper.createKvnrIdentifier("a123456789")
		);
	}

	@Test
	@DisplayName("should reject KVNR with less than 9 digits")
	void createKvnrIdentifierTooShort()
	{
		assertThrows(
			FhirValidationException.class,
			() -> IdentifierHelper.createKvnrIdentifier("A12345678")
		);
	}

	@Test
	@DisplayName("should reject KVNR with more than 9 digits")
	void createKvnrIdentifierTooLong()
	{
		assertThrows(
			FhirValidationException.class,
			() -> IdentifierHelper.createKvnrIdentifier("A1234567890")
		);
	}

}
