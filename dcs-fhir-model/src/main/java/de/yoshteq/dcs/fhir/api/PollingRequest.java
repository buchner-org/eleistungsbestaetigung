package de.yoshteq.dcs.fhir.api;

import static de.yoshteq.dcs.fhir.FhirHelper.*;

import org.hl7.fhir.r4.model.Parameters;

import de.yoshteq.dcs.fhir.FhirValidationException;
import de.yoshteq.dcs.fhir.IdentifierHelper;
import de.yoshteq.dcs.fhir.ParametersHelper;

public class PollingRequest
{
	private static final String PROFILE_URL = "https://e-lb.de/fhir/StructureDefinition/KK_ELB_PollingRequest";

	public static PollingRequest fromParameters(Parameters parameters)
	{
		return new PollingRequest(parameters);
	}

	private PollingRequest(Parameters parameters)
	{
		super();
		this.parameters = parameters;

		extractParameterValues();
	}

	private final Parameters parameters;

	private Integer requestingSystemIK;

	private String patientKvnr;

	private Integer performerOrganizationIK;

	private Integer payorIK;

	public Integer getRequestingSystemIK()
	{
		return requestingSystemIK;
	}

	public String getPatientKvnr()
	{
		return patientKvnr;
	}

	public Integer getPerformerOrganizationIK()
	{
		return performerOrganizationIK;
	}

	public Integer getPayorIK()
	{
		return payorIK;
	}

	private void extractParameterValues()
	{
		validateProfile();
		validateParameterCount();

		extractRequestingSystem();
		extractPerformingOrganisation();
		extractPatientKvnr();
		extractPayor();
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

	private void extractRequestingSystem()
	{
		var identifier = ParametersHelper.getIdentifier(parameters, "requestingSystem");
		if (identifier.isPresent())
		{
			requestingSystemIK = IdentifierHelper.getIkValue(identifier.get());
		}
	}

	private void extractPerformingOrganisation()
	{
		var identifier = ParametersHelper.getIdentifier(parameters, "performingOrganization");
		if (identifier.isPresent())
		{
			performerOrganizationIK = IdentifierHelper.getIkValue(identifier.get());
		}
	}

	private void extractPatientKvnr()
	{
		var identifier = ParametersHelper.getIdentifier(parameters, "patient");
		if (identifier.isPresent())
		{
			patientKvnr = IdentifierHelper.getKvnrValue(identifier.get());
		}
	}

	private void extractPayor()
	{
		var identifier = ParametersHelper.getIdentifier(parameters, "payor");
		if (identifier.isPresent())
		{
			payorIK = IdentifierHelper.getIkValue(identifier.get());
		}
	}
}
