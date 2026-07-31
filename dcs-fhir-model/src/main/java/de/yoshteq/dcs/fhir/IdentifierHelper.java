package de.yoshteq.dcs.fhir;

import static de.yoshteq.dcs.fhir.FhirHelper.*;

import java.util.UUID;
import java.util.regex.Pattern;

import org.hl7.fhir.r4.model.Identifier;

public class IdentifierHelper
{

	private IdentifierHelper()
	{
	}

	public static final String IK_SYSTEM = "http://fhir.de/sid/arge-ik/iknr";

	public static final String KVID_SYSTEM = "http://fhir.de/sid/gkv/kvid-10";

	public static final String RFC3986_SYSTEM = "urn:ietf:rfc:3986";

	private static final Pattern IK_PATTERN = Pattern.compile("^\\d{8,9}$");

	private static final Pattern KVID_PATTERN = Pattern.compile("^[A-Z]\\d{9}$");

	public static Integer getIkValue(Identifier identifier)
	{
		assertFixed(IK_SYSTEM, identifier.getSystem());
		var value = required(() -> identifier.getValue());

		if (IK_PATTERN.matcher(value).find())
		{
			return Integer.valueOf(value);
		}

		throw new FhirValidationException("Invalid IK in identifier");
	}

	public static String getKvnrValue(Identifier identifier)
	{
		assertFixed(KVID_SYSTEM, identifier.getSystem());
		var value = required(() -> identifier.getValue(), "kvnr value missing");

		if (KVID_PATTERN.matcher(value).find())
		{
			return value;
		}

		throw new FhirValidationException("Invalid KVID in identifier");
	}

	public static UUID getUuid(Identifier identifier)
	{
		assertFixed(RFC3986_SYSTEM, identifier.getSystem());
		var uuidString = required(() -> identifier.getValue(), "uuid value missing");
		return UuidHelper.getUUIDFromString(uuidString);
	}

	public static Identifier createIkIdentifier(Integer organizationIK)
	{
		return new Identifier()
			.setSystem(IK_SYSTEM)
			.setValue(organizationIK.toString());
	}

	public static Identifier createUuidIdentifier(UUID uuid)
	{
		return new Identifier()
			.setSystem(RFC3986_SYSTEM)
			.setValue(UuidHelper.toUuidString(uuid));
	}

	public static Identifier createKvnrIdentifier(String kvnr)
	{
		if (!kvnr.matches(KVID_PATTERN.pattern()))
		{
			throw new FhirValidationException("Invalid KVID in identifier: " + kvnr);
		}

		return new Identifier()
			.setSystem(KVID_SYSTEM)
			.setValue(kvnr);
	}

}
