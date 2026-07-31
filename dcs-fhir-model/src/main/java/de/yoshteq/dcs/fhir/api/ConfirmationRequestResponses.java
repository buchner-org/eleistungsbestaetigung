package de.yoshteq.dcs.fhir.api;

import static de.yoshteq.dcs.fhir.FhirHelper.*;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.r4.model.StringType;

import de.yoshteq.dcs.fhir.ParametersHelper;
import de.yoshteq.dcs.fhir.model.ResponseDocumentBundle;

public class ConfirmationRequestResponses
{
	public enum RequestType
	{
			REQUEST_WITH_CALLBACK, REQUEST_WITH_POLLING
	}

	private static final String PARAMETER_RESPONSE_BUNDLE = "responseBundle";

	private static final String PARAMETER_MAIL_ADDRESS = "emailContactPoint";

	private static final String PARAMETER_SOFTWARE_IDENTIFIER = "softwareIdentifier";

	public static ConfirmationRequestResponses from(Parameters parameters)
	{
		return new ConfirmationRequestResponses(parameters);
	}

	public static Parameters create(String softwareIdentifier, String contactMailAddress, List<Bundle> responseBundles)
	{
		var result = new Parameters();

		result.addParameter(new ParametersParameterComponent()
			.setName(PARAMETER_SOFTWARE_IDENTIFIER)
			.setValue(new StringType(softwareIdentifier)));

		var contactPoint = new ContactPoint()
			.setSystem(ContactPointSystem.EMAIL)
			.setValue(contactMailAddress);
		result.addParameter(new ParametersParameterComponent()
			.setName(PARAMETER_MAIL_ADDRESS)
			.setValue(contactPoint));

		for (var responseBundle : responseBundles)
		{
			result.addParameter(new ParametersParameterComponent()
				.setName(PARAMETER_RESPONSE_BUNDLE)
				.setResource(responseBundle));
		}

		return result;
	}

	private final Parameters parameters;

	private String softwareIdentifier;

	private String contactMailAddress;

	private List<ResponseDocumentBundle> responseBundles;

	private ConfirmationRequestResponses(Parameters parameters)
	{
		this.parameters = parameters;
		validate();

		extractParameter();
	}

	private void validate()
	{
		ParametersHelper.requireParameterCountMin(parameters, 2);
	}

	private void extractParameter()
	{
		extractTransportParameters();
		extractDocumentBundles();
	}

	private void extractTransportParameters()
	{
		softwareIdentifier = ParametersHelper.getString(parameters, PARAMETER_SOFTWARE_IDENTIFIER);

		extractContactMailAddress();
	}

	protected void extractContactMailAddress()
	{
		var contactPoint = ParametersHelper.getParameterValueByName(parameters, PARAMETER_MAIL_ADDRESS, ContactPoint.class);
		assertFixed(ContactPointSystem.EMAIL, contactPoint.getSystem());
		contactMailAddress = required(() -> contactPoint.getValue(), "contactMailAddress is required");
	}

	private void extractDocumentBundles()
	{
		this.responseBundles = new ArrayList<>();
		var responseBundleParameters = ParametersHelper.getAllByName(parameters, PARAMETER_RESPONSE_BUNDLE);

		for (var bundleParameter : responseBundleParameters)
		{
			var param = (ParametersParameterComponent) bundleParameter;
			var bundle = (Bundle) param.getResource();
			responseBundles.add(ResponseDocumentBundle.from(bundle));
		}
	}

	public String getSoftwareIdentifier()
	{
		return softwareIdentifier;
	}

	public String getContactMailAddress()
	{
		return contactMailAddress;
	}

	public Parameters toParameters()
	{
		var result = new Parameters();

		result.addParameter(new ParametersParameterComponent()
			.setName(PARAMETER_SOFTWARE_IDENTIFIER)
			.setValue(new StringType(softwareIdentifier)));

		var contactPoint = new ContactPoint()
			.setSystem(ContactPointSystem.EMAIL)
			.setValue(contactMailAddress);
		result.addParameter(new ParametersParameterComponent()
			.setName(PARAMETER_MAIL_ADDRESS)
			.setValue(contactPoint));

		for (var responseBundle : responseBundles)
		{
			result.addParameter(new ParametersParameterComponent()
				.setName(PARAMETER_RESPONSE_BUNDLE)
				.setResource(responseBundle.toBundle()));
		}

		return result;
	}

	public List<ResponseDocumentBundle> getResponseBundles()
	{
		return responseBundles;
	}

}
