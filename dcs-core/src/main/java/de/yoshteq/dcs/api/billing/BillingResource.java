package de.yoshteq.dcs.api.billing;

import java.util.List;
import java.util.UUID;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;

import de.yoshteq.dcs.api.BillingEndpointException;
import de.yoshteq.dcs.confirmation.ConfirmationRequest;
import de.yoshteq.dcs.confirmation.ConfirmationService;
import de.yoshteq.dcs.fhir.api.ConfirmationRequests;
import de.yoshteq.dcs.fhir.api.OperationOutcomeResponse;
import de.yoshteq.dcs.fhir.api.ParticipationStatusRequest;
import de.yoshteq.dcs.fhir.api.ParticipationStatusResponse;
import de.yoshteq.dcs.fhir.api.PollingConfirmationOfResponse;
import de.yoshteq.dcs.fhir.api.PollingRequest;
import de.yoshteq.dcs.fhir.model.ChargeItem;
import de.yoshteq.dcs.insurant.InsurantService;
import de.yoshteq.dcs.logging.ProcessLogger;
import de.yoshteq.diga.secon.SeconContainer;
import de.yoshteq.diga.secon.SeconMediaType;
import io.quarkus.security.UnauthorizedException;

@Path("/billing")
@Consumes(SeconMediaType.INNER_XML)
@Produces(SeconMediaType.INNER_XML)
@SecurityScheme(securitySchemeName = "Basic", description = "Benutzername und Passwort", type = SecuritySchemeType.HTTP, scheme = "basic")
public class BillingResource
{

	@Inject
	ProcessLogger processLogger;

	@Inject
	InsurantService insurantService;

	@Inject
	ConfirmationService confirmationService;

	@Inject
	@ConfigProperty(name = "dcs.kostentraeger-iks")
	List<String> kostentraegerIKs;

	@POST
	@Operation(hidden = true)
	@Transactional
	@Path("/participation-status-query")
	public SeconContainer<Parameters> getParticipationStatus(SeconContainer<Parameters> seconRequest)
	{
		var request = ParticipationStatusRequest.fromParameters(seconRequest.getEntity());

		validateKostentragerIk(request.getKostentraegerIK().toString());

		var isParticipating = insurantService.hasInsurantActiveRegistration(request.getKvnr());
		processLogger.participationStatusQuery(Integer.valueOf(seconRequest.getIkSender()), isParticipating);

		return SeconContainer.create(seconRequest.getIkSender(), ParticipationStatusResponse.create(isParticipating));
	}

	private void validateKostentragerIk(String kostentragerIkString)
	{
		if (!kostentraegerIKs.contains(kostentragerIkString))
		{
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity("Kostentraeger IK unknown").build());
		}
	}

	@POST
	@Operation(hidden = true)
	@Path("/confirmation-request")
	public SeconContainer<OperationOutcome> createConfirmationRequest(SeconContainer<Parameters> seconRequest)
	{
		var requestUuid = UUID.randomUUID().toString();
		var confirmationRequests = ConfirmationRequests.from(seconRequest.getEntity());

		var ikSender = Integer.valueOf(seconRequest.getIkSender());
		var requests = confirmationRequests.getChargeItems()
			.stream()
			.map(chargeItemInfo -> mapToConfirmationRequest(ikSender, confirmationRequests, chargeItemInfo))
			.toList();

		confirmationService.createConfirmationRequest(requests);

		for (var confirmationRequest : requests)
		{
			processLogger.requestFromBilling(requestUuid, confirmationRequest);
		}

		return createAllOkResponse(seconRequest.getIkSender());
	}

	private ConfirmationRequest mapToConfirmationRequest(Integer senderIk, ConfirmationRequests incomingRequest, ChargeItem chargeItem)
	{
		var insurant = insurantService.getInsurant(chargeItem.getPatientKvnr())
			.orElseThrow(() -> new BillingEndpointException("unknown KVNR", IssueType.NOTFOUND));
		var confirmationRequest = ConfirmationRequest.with(senderIk, incomingRequest, insurant, chargeItem);

		if (!confirmationRequest.getServiceType().isValidServiceCode(confirmationRequest.getCode()))
		{
			throw new BillingEndpointException("ChargeItem.code = " + confirmationRequest.getCode(), IssueType.INVALID);
		}

		return confirmationRequest;
	}

	private SeconContainer<OperationOutcome> createAllOkResponse(String ikEmpfaenger)
	{
		return SeconContainer.create(ikEmpfaenger, OperationOutcomeResponse.allOk());
	}

	@POST
	@Operation(hidden = true)
	@Transactional
	@Path("/polling/poll")
	public SeconContainer<Parameters> pollConfirmationRequests(SeconContainer<Parameters> seconRequest)
	{
		var request = PollingRequest.fromParameters(seconRequest.getEntity());
		var senderIkInteger = Integer.valueOf(seconRequest.getIkSender());
		List<ConfirmationRequest> requests;
		if (request.getRequestingSystemIK() != null)
		{
			if (!senderIkInteger.equals(request.getRequestingSystemIK()))
			{
				throw new UnauthorizedException();
			}
			requests = confirmationService.getPollingConfirmationRequestsForRequestingSystem(request.getRequestingSystemIK());
		}
		else if (request.getPerformerOrganizationIK() != null)
		{
			requests = confirmationService.getPollingConfirmationRequestsForPerformerOrganization(
				senderIkInteger,
				request.getPerformerOrganizationIK());
		}
		else if (request.getPatientKvnr() != null)
		{
			requests = confirmationService.getPollingConfirmationRequestsForPatient(senderIkInteger, request.getPatientKvnr());
		}
		else if (request.getPayorIK() != null)
		{
			requests = confirmationService.getPollingConfirmationRequestsForPayor(senderIkInteger, request.getPayorIK());
		}
		else
		{
			throw new BadRequestException();
		}

		var response = confirmationService.createResponseParameters(requests);

		var requestUuid = UUID.randomUUID().toString();
		for (var confirmationRequest : requests)
		{
			processLogger.pollingRequest(Integer.valueOf(seconRequest.getIkSender()), requestUuid, confirmationRequest);
		}

		return seconRequest.respondWith(response);
	}

	@POST
	@Operation(hidden = true)
	@Transactional
	@Path("/polling/confirm")
	public SeconContainer<OperationOutcome> confirmPollingResponse(SeconContainer<Parameters> seconRequest)
	{
		var request = PollingConfirmationOfResponse.fromParameters(seconRequest.getEntity());
		var uuidStrings = request.getConfirmationBundleUuids().stream().map(UUID::toString).toList();
		confirmationService.deleteConfirmedRequestsAfterPolling(Integer.valueOf(seconRequest.getIkSender()), uuidStrings);

		var requestUuid = UUID.randomUUID().toString();
		for (var bundleUuid : uuidStrings)
		{
			processLogger.pollingConfirm(Integer.valueOf(seconRequest.getIkSender()), requestUuid, bundleUuid);
		}

		return createAllOkResponse(seconRequest.getIkSender());
	}

}
