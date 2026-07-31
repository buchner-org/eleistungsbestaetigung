package de.yoshteq.dcs.fhir.api;

import static de.yoshteq.dcs.fhir.FhirHelper.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hl7.fhir.r4.model.Parameters;

import de.yoshteq.dcs.fhir.FhirValidationException;
import de.yoshteq.dcs.fhir.ParametersHelper;

public class PollingConfirmationOfResponse
{
	private static final String PROFILE_URL = "https://e-lb.de/fhir/StructureDefinition/KK_ELB_ConfirmationOfResponse";

	public static PollingConfirmationOfResponse fromParameters(Parameters parameters)
	{
		return new PollingConfirmationOfResponse(parameters);
	}

	private PollingConfirmationOfResponse(Parameters parameters)
	{
		super();
		this.parameters = parameters;

		extractParameterValues();
	}

	private final Parameters parameters;

	private List<UUID> confirmationBundleUuids = new ArrayList<>();

	public List<UUID> getConfirmationBundleUuids()
	{
		return confirmationBundleUuids;
	}

	private void extractParameterValues()
	{
		validateProfile();
		validateParameterCount();

		extractConfirmationBundleUuids();
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
		ParametersHelper.requireParameterCountMin(parameters, 1);
	}

	private void extractConfirmationBundleUuids()
	{
		for (var parameter : parameters.getParameter())
		{
			if (!"confirmationBundleUuid".equals(parameter.getName()))
			{
				throw new FhirValidationException("unexpected parameter name: " + parameter.getName());
			}

			confirmationBundleUuids.add(ParametersHelper.extractUuid(parameter));
		}
	}

}
