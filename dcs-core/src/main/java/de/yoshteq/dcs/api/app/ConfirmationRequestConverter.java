package de.yoshteq.dcs.api.app;

import java.time.temporal.TemporalAccessor;

import org.eclipse.microprofile.config.ConfigProvider;

import de.yoshteq.dcs.FhirDateTimeFormatter;

import de.yoshteq.dcs.app.api.ConfirmationRequestEntry;
import de.yoshteq.dcs.app.api.ServiceDescription;
import de.yoshteq.dcs.app.api.ServiceInformation;
import de.yoshteq.dcs.app.api.ServiceTypeInformation;
import de.yoshteq.dcs.confirmation.ConfirmationRequest;

public class ConfirmationRequestConverter
{
	private static Integer requestPendingTimeoutInHours;

	static
	{
		requestPendingTimeoutInHours = ConfigProvider.getConfig().getValue("dcs.request.pending-timeout-hours", Integer.class);
	}

	private ConfirmationRequestConverter()
	{
	}

	public static ConfirmationRequestEntry from(ConfirmationRequest request, ServiceDescription serviceDescription, ServiceTypeInformation serviceType)
	{

		return new ConfirmationRequestEntry(
			request.getId(),
			PerformerOrganizationConverter.with(
				request.getPerformerOrganizationIK(),
				request.getPerformerOrganization(),
				request.getPerformerOrganizationDisplay(),
				request.getPerformerPractitionerDisplay()),
			new ServiceInformation(
				serviceType,
				request.getCode(),
				serviceDescription,
				toDateString(request.getOccurrenceDateTime()),
				toDateString(request.getOccurrenceStartDateTime()),
				toDateString(request.getOccurrenceEndDateTime())),
			request.getRequestDateTime(),
			request.getRequestDateTime().plusHours(requestPendingTimeoutInHours));
	}

	private static String toDateString(TemporalAccessor temporal)
	{
		if (temporal == null)
		{
			return null;
		}

		return FhirDateTimeFormatter.PARSER_FORMATTER.format(temporal);
	}

}
