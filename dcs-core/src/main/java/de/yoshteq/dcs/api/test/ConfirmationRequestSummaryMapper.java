package de.yoshteq.dcs.api.test;

import de.yoshteq.dcs.confirmation.ConfirmationRequest;

public class ConfirmationRequestSummaryMapper
{
	private ConfirmationRequestSummaryMapper()
	{

	}

	public static ConfirmationRequestSummary map(ConfirmationRequest confirmationRequest)
	{
		return new ConfirmationRequestSummary(
			confirmationRequest.getId(),
			confirmationRequest.getKostentraegerIK(),
			confirmationRequest.getKvnr(),
			RequestState.valueOf(confirmationRequest.getState().toString()),
			confirmationRequest.getSenderIK(),
			confirmationRequest.getRequestDateTime(),
			ResponseType.valueOf(confirmationRequest.getResponseType().toString()),
			confirmationRequest.getSoftwareIdentifier(),
			confirmationRequest.getContactMailAddress(),
			confirmationRequest.getCallbackUrl(),
			confirmationRequest.getUuid(),
			ServiceType.valueOf(confirmationRequest.getServiceType().toString()),
			confirmationRequest.getCode(),
			confirmationRequest.getOccurrenceDateTime(),
			confirmationRequest.getOccurrenceStartDateTime(),
			confirmationRequest.getOccurrenceEndDateTime(),
			confirmationRequest.getPerformerOrganizationIK(),
			confirmationRequest.getPerformerOrganizationDisplay(),
			confirmationRequest.getPerformerPractitionerDisplay(),
			confirmationRequest.getResponseDateTime(),
			confirmationRequest.getResponseDocumentBundleUuid(),
			confirmationRequest.getResponseDocumentBundleXml());
	}

}
