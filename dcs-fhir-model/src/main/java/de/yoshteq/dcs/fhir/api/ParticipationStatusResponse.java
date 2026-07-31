package de.yoshteq.dcs.fhir.api;

import static de.yoshteq.dcs.fhir.FhirHelper.required;

import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Meta;

import de.yoshteq.dcs.fhir.FhirValidationException;
import de.yoshteq.dcs.fhir.ParametersHelper;

public class ParticipationStatusResponse
{
	private static final String PROFILE_URL = "https://e-lb.de/fhir/StructureDefinition/KK_ELB_ParticipationStatusResponseParameters";

	public static ParticipationStatusResponse fromParameters(Parameters parameters)
	{
		return new ParticipationStatusResponse(parameters);
	}

	public static Parameters create(boolean isParticipating)
	{
		var result = new Parameters();

		var meta = new Meta()
			.addProfile(PROFILE_URL);
		result.setMeta(meta);

		result.addParameter(new ParametersParameterComponent()
			.setName("isParticipating")
			.setValue(new BooleanType(isParticipating)));

		return result;
	}

	private ParticipationStatusResponse(Parameters parameters)
	{
		this.parameters = parameters;
		extractParameterValues();
	}

	private final Parameters parameters;

	private boolean isParticipating;

	public boolean isParticipating()
	{
		return isParticipating;

	}

	private void extractParameterValues()
	{
		validateProfile();
		validateParameterCount();

		extractIsParticipating();
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
		ParametersHelper.requireParameterCount(parameters, 1);
	}

	private void extractIsParticipating()
	{
		var fhirIsParticipating = ParametersHelper.getParameterValueByName(parameters, "isParticipating", BooleanType.class);
		isParticipating = fhirIsParticipating.getValue();
	}

}
