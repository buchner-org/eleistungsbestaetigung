package de.yoshteq.dcs.api.test;

public record TestConfirmationRequest(
	Integer kostentraegerIk,
	String kvnr,
	String serviceTypeCode,
	String serviceCode,
	String occurrenceDateTime,
	String occurrenceStartDateTime,
	String occurrenceEndDateTime,
	Long performerOrganizationIK,
	String performerOrganizationDisplay,
	String performerPractitionerIdentifier,
	String performerPractitionerDisplay)
{

}
