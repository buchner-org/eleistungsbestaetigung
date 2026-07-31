package de.yoshteq.dcs.api.test;

import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;

public record ConfirmationRequestSummary(
	Long id,
	Integer kostentraegerIK,
	String kvnr,
	RequestState state,
	Integer senderIK,
	OffsetDateTime requestDateTime,
	ResponseType responseType,
	String softwareIdentifier,
	String contactMailAddress,
	String callbackUrl,
	String uuid,
	ServiceType serviceType,
	String code,
	TemporalAccessor occurrenceDateTime,
	TemporalAccessor occurrenceStartDateTime,
	TemporalAccessor occurrenceEndDateTime,
	Integer performerOrganizationIK,
	String performerOrganizationDisplay,
	String performerPractitionerDisplay,
	OffsetDateTime responseDateTime,
	String responseDocumentBundleUuid,
	String responseDocumentBundleXml)
{
}
