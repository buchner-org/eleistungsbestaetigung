package de.yoshteq.dcs.fhir;

import java.util.UUID;

import org.hl7.fhir.r4.model.UuidType;

public class UuidHelper
{

	private static final String URN_UUID_PREFIX = "urn:uuid:";

	private UuidHelper()
	{

	}

	public static UUID getUUIDFromUuid(UuidType uuid)
	{
		var uuidString = uuid.getValue();
		return getUUIDFromString(uuidString);
	}

	public static UUID getUUIDFromString(String uuidString)
	{
		if (!uuidString.startsWith(URN_UUID_PREFIX))
		{
			throw new FhirValidationException("Invalid uuid identifier. Missing prefix " + URN_UUID_PREFIX);
		}

		return UUID.fromString(uuidString.substring(URN_UUID_PREFIX.length()));
	}
	
	public static String toUuidString(UUID uuid)
	{
		return URN_UUID_PREFIX + uuid.toString();
	}
	
}
