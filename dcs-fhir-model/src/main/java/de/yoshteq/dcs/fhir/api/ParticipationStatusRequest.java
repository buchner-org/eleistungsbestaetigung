package de.yoshteq.dcs.fhir.api;

import static de.yoshteq.dcs.fhir.FhirHelper.*;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Meta;

import de.yoshteq.dcs.fhir.FhirValidationException;
import de.yoshteq.dcs.fhir.IdentifierHelper;
import de.yoshteq.dcs.fhir.ParametersHelper;

public class ParticipationStatusRequest
{
	private static final String PROFILE_URL = "https://e-lb.de/fhir/StructureDefinition/KK_ELB_ParticipationStatusRequestParameters";
	private static final String SYSTEM_PATIENT_URL = "http://fhir.de/sid/gkv/kvid-10";
	private static final String SYSTEM_PAYOR_URL = "http://fhir.de/sid/arge-ik/iknr";
	
	public static ParticipationStatusRequest fromParameters(Parameters parameters)
	{
		return new ParticipationStatusRequest(parameters);
	}
	
	public static Parameters create(Integer kostentragerIK, String kvnr)
	{
		var result = new Parameters();

		var meta = new Meta()
			.addProfile(PROFILE_URL);
		result.setMeta(meta);

		var patientIdentifier = new Identifier()
			.setSystem(SYSTEM_PATIENT_URL)
			.setValue(kvnr);
		result.addParameter(new ParametersParameterComponent()
			.setName("patient")
			.setValue(patientIdentifier));

		var payorIdentifier = new Identifier()
			.setSystem(SYSTEM_PAYOR_URL)
			.setValue(Long.toString(kostentragerIK));
		result.addParameter(new ParametersParameterComponent()
			.setName("payor")
			.setValue(payorIdentifier));

		return result;
	}

	private ParticipationStatusRequest(Parameters parameters)
	{
		super();
		this.parameters = parameters;

		extractParameterValues();
	}

	private final Parameters parameters;

	private String kvnr;

	private Integer kostentraegerIK;

	public String getKvnr()
	{
		return kvnr;
	}

	public Integer getKostentraegerIK()
	{
		return kostentraegerIK;
	}

	private void extractParameterValues()
	{
		validateProfile();
		validateParameterCount();

		extractPayor();
		extractPatient();
	}

	private void validateProfile()
	{
		var profile = required(() -> parameters.getMeta().getProfile().get(0).getValue(), "meta profile missing");
		if (!PROFILE_URL.equals(profile))
		{
			throw new FhirValidationException("Wrong Parameters profile. Expected [" + PROFILE_URL + "] but got [" + profile + "]");
		}
	}

	private void validateParameterCount()
	{
		ParametersHelper.requireParameterCount(parameters, 2);
	}

	private void extractPayor()
	{
		var identifier = ParametersHelper.getIdentifier(parameters, "payor")
			.orElseThrow(() -> new FhirValidationException("Parameter 'payor' missing"));

		kostentraegerIK = IdentifierHelper.getIkValue(identifier);
	}

	private void extractPatient()
	{
		var identifier = ParametersHelper.getIdentifier(parameters, "patient")
			.orElseThrow(() -> new FhirValidationException("Parameter 'patient' missing"));

		kvnr = IdentifierHelper.getKvnrValue(identifier);
	}
}
