package de.yoshteq.dcs.fhir;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.hl7.fhir.r4.model.UuidType;

@DisplayName("UuidHelper Tests")
class UuidHelperTest
{

	private static final String VALID_UUID_STRING = "550e8400-e29b-41d4-a716-446655440000";
	private static final String VALID_URN_UUID = "urn:uuid:550e8400-e29b-41d4-a716-446655440000";
	private static final UUID VALID_UUID = UUID.fromString(VALID_UUID_STRING);

	@Test
	@DisplayName("should convert valid UuidType to UUID")
	void testGetUUIDFromUuidType()
	{
		UuidType uuidType = new UuidType(VALID_URN_UUID);

		UUID result = UuidHelper.getUUIDFromUuid(uuidType);

		assertEquals(VALID_UUID, result);
	}

	@Test
	@DisplayName("should convert valid URN string to UUID")
	void testGetUUIDFromValidString()
	{
		UUID result = UuidHelper.getUUIDFromString(VALID_URN_UUID);

		assertEquals(VALID_UUID, result);
	}

	@Test
	@DisplayName("should throw FhirValidationException for string without urn:uuid: prefix")
	void testGetUUIDFromStringWithoutPrefix()
	{
		String invalidUuid = VALID_UUID_STRING;

		FhirValidationException exception = assertThrows(
			FhirValidationException.class,
			() -> UuidHelper.getUUIDFromString(invalidUuid)
		);

		assertTrue(exception.getMessage().contains("Invalid uuid identifier"));
		assertTrue(exception.getMessage().contains("urn:uuid:"));
	}

	@Test
	@DisplayName("should throw FhirValidationException for malformed UUID after prefix")
	void testGetUUIDFromStringWithMalformedUuid()
	{
		String malformedUuid = "urn:uuid:invalid-uuid-format";

		assertThrows(
			IllegalArgumentException.class,
			() -> UuidHelper.getUUIDFromString(malformedUuid)
		);
	}

	@Test
	@DisplayName("should convert UUID to URN string format")
	void testToUuidString()
	{
		String result = UuidHelper.toUuidString(VALID_UUID);

		assertEquals(VALID_URN_UUID, result);
		assertTrue(result.startsWith("urn:uuid:"));
	}

	@Test
	@DisplayName("should handle different UUID values correctly")
	void testWithDifferentUuid()
	{
		UUID otherUuid = UUID.randomUUID();
		String expectedUrn = "urn:uuid:" + otherUuid.toString();

		String result = UuidHelper.toUuidString(otherUuid);

		assertEquals(expectedUrn, result);
	}

	@Test
	@DisplayName("should support round-trip conversion: UUID -> String -> UUID")
	void testRoundTripConversion()
	{
		UUID originalUuid = UUID.randomUUID();

		String urnString = UuidHelper.toUuidString(originalUuid);
		UUID convertedUuid = UuidHelper.getUUIDFromString(urnString);

		assertEquals(originalUuid, convertedUuid);
	}

	@Test
	@DisplayName("should support round-trip conversion: UuidType -> UUID -> String -> UUID")
	void testRoundTripWithUuidType()
	{
		UUID originalUuid = UUID.randomUUID();
		String urnString = UuidHelper.toUuidString(originalUuid);
		UuidType uuidType = new UuidType(urnString);

		UUID resultFromType = UuidHelper.getUUIDFromUuid(uuidType);
		String resultString = UuidHelper.toUuidString(resultFromType);
		UUID finalUuid = UuidHelper.getUUIDFromString(resultString);

		assertEquals(originalUuid, resultFromType);
		assertEquals(urnString, resultString);
		assertEquals(originalUuid, finalUuid);
	}

	@Test
	@DisplayName("should preserve UUID value through multiple conversions")
	void testMultipleConversions()
	{
		UUID uuid1 = UUID.randomUUID();
		UUID uuid2 = UUID.randomUUID();

		String urn1 = UuidHelper.toUuidString(uuid1);
		String urn2 = UuidHelper.toUuidString(uuid2);
		UUID result1 = UuidHelper.getUUIDFromString(urn1);
		UUID result2 = UuidHelper.getUUIDFromString(urn2);

		assertEquals(uuid1, result1);
		assertEquals(uuid2, result2);
		assertNotEquals(result1, result2);
	}

	@Test
	@DisplayName("should throw exception for empty string")
	void testGetUUIDFromEmptyString()
	{
		String emptyString = "";

		FhirValidationException exception = assertThrows(
			FhirValidationException.class,
			() -> UuidHelper.getUUIDFromString(emptyString)
		);

		assertTrue(exception.getMessage().contains("Invalid uuid identifier"));
	}

	@Test
	@DisplayName("should throw exception for string with only prefix")
	void testGetUUIDFromOnlyPrefix()
	{
		String onlyPrefix = "urn:uuid:";

		assertThrows(
			IllegalArgumentException.class,
			() -> UuidHelper.getUUIDFromString(onlyPrefix)
		);
	}

	@Test
	@DisplayName("should handle nil UUIDs correctly")
	void testNilUuid()
	{
		String nilUuid = "urn:uuid:00000000-0000-0000-0000-000000000000";

		UUID result = UuidHelper.getUUIDFromString(nilUuid);

		assertEquals(new UUID(0, 0), result);
	}

}
