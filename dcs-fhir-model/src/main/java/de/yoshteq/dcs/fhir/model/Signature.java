package de.yoshteq.dcs.fhir.model;

import static de.yoshteq.dcs.fhir.FhirHelper.assertFixed;
import static de.yoshteq.dcs.fhir.FhirHelper.required;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.InstantType;
import org.hl7.fhir.r4.model.Reference;

import de.yoshteq.dcs.fhir.IdentifierHelper;

public class Signature
{

	private static final String SIGNATURE_FORMAT = "application/signature+xml";

	private static final String TYPE_CODE = "1.2.840.10065.1.12.1.5";

	private static final String TYPE_SYSTEM = "urn:iso-astm:E1762-95:2013";

	public static Signature from(org.hl7.fhir.r4.model.Signature signature)
	{
		return new Signature(signature);
	}

	public static Signature create(OffsetDateTime signatureDateTime, Integer bestaetigungssystemIk, byte[] detachedSignatureBytes)
	{
		return new Signature(signatureDateTime, bestaetigungssystemIk, detachedSignatureBytes);
	}

	private final org.hl7.fhir.r4.model.Signature signatureResource;

	private byte[] detachedSignatureBytes;

	private Integer bestaetigungssystemIk;

	private OffsetDateTime signatureDateTime;

	private Signature(OffsetDateTime signatureDateTime, Integer bestaetigungssystemIk, byte[] detachedSignatureBytes)
	{
		super();
		this.signatureDateTime = signatureDateTime;
		this.bestaetigungssystemIk = bestaetigungssystemIk;
		this.detachedSignatureBytes = detachedSignatureBytes;

		signatureResource = null;
	}

	private Signature(org.hl7.fhir.r4.model.Signature signature)
	{
		this.signatureResource = signature;

		validate();
		extractParameters();
	}

	private void validate()
	{
		assertFixed(SIGNATURE_FORMAT, signatureResource.getSigFormat());
		assertFixed(TYPE_SYSTEM, () -> signatureResource.getType().get(0).getSystem());
		assertFixed(TYPE_CODE, () -> signatureResource.getType().get(0).getCode());
	}

	private void extractParameters()
	{
		var when = required(() -> signatureResource.getWhenElement(), "signature date time is missing");
		signatureDateTime = OffsetDateTime.parse(when.asStringValue());
		bestaetigungssystemIk = IdentifierHelper.getIkValue(required(() -> signatureResource.getWho().getIdentifier(), "signature identifier missing"));
		detachedSignatureBytes = required(() -> signatureResource.getData(), "detached signature bytes missing");
	}

	public byte[] getDetachedSignatureBytes()
	{
		return detachedSignatureBytes;
	}

	public Integer getBestaetigungssystemIk()
	{
		return bestaetigungssystemIk;
	}

	public OffsetDateTime getSignatureDateTime()
	{
		return signatureDateTime;
	}

	public org.hl7.fhir.r4.model.Signature toSignature()
	{
		var signature = new org.hl7.fhir.r4.model.Signature();
		signature.setWhenElement(new InstantType(signatureDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)));

		var typeCoding = new Coding()
			.setSystem(TYPE_SYSTEM)
			.setCode(TYPE_CODE);
		signature.addType(typeCoding);

		signature.setWho(new Reference()
			.setIdentifier(IdentifierHelper.createIkIdentifier(bestaetigungssystemIk)));

		signature.setData(detachedSignatureBytes);
		signature.setSigFormat(SIGNATURE_FORMAT);

		return signature;
	}

}
