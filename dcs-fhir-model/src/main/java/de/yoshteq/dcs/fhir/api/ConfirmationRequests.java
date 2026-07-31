package de.yoshteq.dcs.fhir.api;

import static de.yoshteq.dcs.fhir.FhirHelper.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.UriType;

import de.yoshteq.dcs.fhir.FhirValidationException;
import de.yoshteq.dcs.fhir.MetaHelper;
import de.yoshteq.dcs.fhir.ParametersHelper;
import de.yoshteq.dcs.fhir.model.ChargeItem;
import de.yoshteq.dcs.fhir.model.ChargeItem.Status;

public class ConfirmationRequests
{
	public enum RequestType
	{
			REQUEST_WITH_CALLBACK, REQUEST_WITH_POLLING
	}

	private static final String PARAMETER_CHARGE_ITEM = "chargeItem";

	private static final String PARAMETER_MAIL_ADDRESS = "emailContactPoint";

	private static final String PARAMETER_CALLBACK_URL = "callbackUrl";

	private static final String PARAMETER_SOFTWARE_IDENTIFIER = "softwareIdentifier";

	private static final String PARAMETER_REQUEST_TYPE = "requestType";

	private static final String REQUEST_TYPE_SYSTEM = "https://e-lb.de/fhir/CodeSystem/KK_ELB_RequestType";

	private static final String PROFILE_URL = "https://e-lb.de/fhir/StructureDefinition/KK_ELB_ConfirmationRequestParameters";

	public static ConfirmationRequests from(Parameters parameters)
	{
		return new ConfirmationRequests(parameters);
	}

	public static ConfirmationRequests withCallback(
		String softwareIdentifier,
		String contactMailAddress,
		URL callbackUrl,
		List<ChargeItem> chargeItems)
	{
		return new ConfirmationRequests(RequestType.REQUEST_WITH_CALLBACK, softwareIdentifier, contactMailAddress, callbackUrl, chargeItems);
	}

	public static ConfirmationRequests withPolling(String softwareIdentifier, String contactMailAddress, List<ChargeItem> chargeItems)
	{
		return new ConfirmationRequests(RequestType.REQUEST_WITH_POLLING, softwareIdentifier, contactMailAddress, null, chargeItems);
	}

	private final Parameters parameters;

	private RequestType requestType;

	private String softwareIdentifier;

	private String contactMailAddress;

	private URL callbackUrl;

	private List<ChargeItem> chargeItems;

	private ConfirmationRequests(
		RequestType requestType,
		String softwareIdentifier,
		String contactMailAddress,
		URL callbackUrl,
		List<ChargeItem> chargeItems)
	{
		super();
		this.requestType = requestType;
		this.softwareIdentifier = softwareIdentifier;
		this.contactMailAddress = contactMailAddress;
		this.callbackUrl = callbackUrl;
		this.chargeItems = chargeItems;

		this.parameters = null;
	}

	private ConfirmationRequests(Parameters parameters)
	{
		this.parameters = parameters;
		extractParameters();

		validate();
	}

	private void extractParameters()
	{
		ParametersHelper.requireParameterCountMin(parameters, 4);

		extractTransportParameters();
		extractChargeItems();
	}

	private void extractTransportParameters()
	{
		var requestTypeCoding = ParametersHelper.getParameterValueByName(parameters, PARAMETER_REQUEST_TYPE, Coding.class);
		assertFixed(REQUEST_TYPE_SYSTEM, () -> requestTypeCoding.getSystem());
		requestType = RequestType.valueOf(required(() -> requestTypeCoding.getCode(), ""));
		softwareIdentifier = ParametersHelper.getString(parameters, PARAMETER_SOFTWARE_IDENTIFIER);

		extractCallbackUrl();
		extractContactMailAddress();
	}

	protected void extractContactMailAddress()
	{
		var contactPoint = ParametersHelper.getParameterValueByName(parameters, PARAMETER_MAIL_ADDRESS, ContactPoint.class);
		assertFixed(ContactPointSystem.EMAIL, contactPoint.getSystem());
		contactMailAddress = required(() -> contactPoint.getValue(), "contactMailAddress is required");
	}

	protected void extractCallbackUrl()
	{
		if (requestType == RequestType.REQUEST_WITH_POLLING)
		{
			return;
		}

		try
		{
			callbackUrl = new URL(ParametersHelper.getParameterValueByName(parameters, PARAMETER_CALLBACK_URL, UriType.class).getValue());
		}
		catch (MalformedURLException e)
		{
			throw new FhirValidationException("malformed callbackUrl [" + e.getMessage() + "]");
		}
	}

	private void extractChargeItems()
	{
		this.chargeItems = new ArrayList<>();
		var chargeItemParamters = ParametersHelper.getAllByName(parameters, PARAMETER_CHARGE_ITEM);

		for (var chargeItemParameter : chargeItemParamters)
		{
			var param = (ParametersParameterComponent) chargeItemParameter;
			var chargeItem = (org.hl7.fhir.r4.model.ChargeItem) param.getResource();
			chargeItems.add(ChargeItem.from(chargeItem));
		}
	}

	private void validate()
	{
		MetaHelper.assertProfile(parameters, PROFILE_URL);
		if (chargeItems.isEmpty())
		{
			throw new FhirValidationException("no chargeItem included");
		}

		for (var chargeItemInformation : chargeItems)
		{
			if (chargeItemInformation.getStatus() != Status.UNKNOWN)
			{
				throw new FhirValidationException("charge item status must be 'unknown'");
			}
		}
	}

	public RequestType getRequestType()
	{
		return requestType;
	}

	public String getSoftwareIdentifier()
	{
		return softwareIdentifier;
	}

	public String getContactMailAddress()
	{
		return contactMailAddress;
	}

	public URL getCallbackUrl()
	{
		return callbackUrl;
	}

	public List<ChargeItem> getChargeItems()
	{
		return Collections.unmodifiableList(chargeItems);
	}

	public Parameters toParameters()
	{
		var result = new Parameters();
		result.setMeta(MetaHelper.withProfileUrl(PROFILE_URL));

		var requestTypeCoding = new Coding()
			.setSystem(REQUEST_TYPE_SYSTEM)
			.setCode(requestType.name());
		result.addParameter(new ParametersParameterComponent()
			.setName(PARAMETER_REQUEST_TYPE)
			.setValue(requestTypeCoding));

		result.addParameter(new ParametersParameterComponent()
			.setName(PARAMETER_SOFTWARE_IDENTIFIER)
			.setValue(new StringType(softwareIdentifier)));

		var contactPoint = new ContactPoint()
			.setSystem(ContactPointSystem.EMAIL)
			.setValue(contactMailAddress);
		result.addParameter(new ParametersParameterComponent()
			.setName(PARAMETER_MAIL_ADDRESS)
			.setValue(contactPoint));

		if (requestType == RequestType.REQUEST_WITH_CALLBACK)
		{
			result.addParameter(new ParametersParameterComponent()
				.setName(PARAMETER_CALLBACK_URL)
				.setValue(new UriType(callbackUrl.toExternalForm())));
		}

		for (var item : chargeItems)
		{
			result.addParameter(new ParametersParameterComponent()
				.setName(PARAMETER_CHARGE_ITEM)
				.setResource(item.toChargeItem()));
		}

		return result;
	}
}
