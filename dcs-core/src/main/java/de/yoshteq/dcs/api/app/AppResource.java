package de.yoshteq.dcs.api.app;

import java.util.List;
import java.util.Objects;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response.Status;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import de.yoshteq.dcs.app.api.AppApi;
import de.yoshteq.dcs.app.api.ConfirmationRequestEntry;
import de.yoshteq.dcs.app.api.ConfirmationRequestResponse;
import de.yoshteq.dcs.app.api.ParticipationStatusResponse;
import de.yoshteq.dcs.app.api.ResponseState;
import de.yoshteq.dcs.app.api.ServiceTypeInformation;
import de.yoshteq.dcs.confirmation.ConfirmationRequest;
import de.yoshteq.dcs.confirmation.ConfirmationService;
import de.yoshteq.dcs.insurant.InsurantService;
import io.quarkus.security.UnauthorizedException;

@Path("/app")
@Tag(name = "App", description = "Schnittstellen für das App-Frontend mittels Authentifiziuerung eines JWT aus dem App-Backend")
@SecurityScheme(securitySchemeName = "AppApi", description = "Signed JWT Token from IAM", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "Bearer [token]")
@SecurityRequirement(name = "AppApi")
public class AppResource implements AppApi
{

	@Inject
	JsonWebToken jwt;

	@Inject
	@ConfigProperty(name = "dcs.app.jwt.claim-name.user-identifier", defaultValue = "sub")
	String userIdentifierClaimName;

	@Inject
	@ConfigProperty(name = "dcs.app.jwt.claim-name.kvnr-or-hash", defaultValue = "kvnrHashSha256")
	String kvnrOrHashSha256Claimname;

	@Inject
	InsurantService insurantService;

	@Inject
	ConfirmationService confirmationService;

	@Context
	HttpHeaders httpHeaders;

	@Inject
	ServiceTypeDescriptionGenerator serviceTypeDescriptionGenerator;

	@Inject
	ServiceDescriptionGenerator serviceDescriptionGenerator;

	@Override
	public ParticipationStatusResponse getParticipationStatus()
	{
		assertJwtClaims();

		var insurantRegistration = insurantService.getActiveInsurantRegistration(getKvnrOrKvnrSha256Hash());
		if (insurantRegistration.isEmpty())
		{
			return ParticipationStatusResponse.NOT_PARTICIPATING;
		}

		return ParticipationStatusResponse.participating(insurantRegistration.get().getRegistrationDateTime());
	}

	@Override
	@Transactional
	public void registerInsurant()
	{
		assertJwtClaims();

		insurantService.registerInsurant(getKvnrOrKvnrSha256Hash(), getUserIdentifier());
	}

	@Override
	@Transactional
	public void deregisterInsurant()
	{
		assertJwtClaims();

		insurantService.revokeParticipation(getKvnrOrKvnrSha256Hash());
	}

	@Override
	@Transactional
	public List<ConfirmationRequestEntry> listPendingConfirmationRequests()
	{
		assertJwtClaims();
		assertActiveParticipation();

		var pendingRequests = confirmationService.getPendingConfirmationRequestsForInsurant(getKvnrOrKvnrSha256Hash());
		return pendingRequests.stream().map(this::mapConfirmationRequestToEntry).toList();
	}

	private ConfirmationRequestEntry mapConfirmationRequestToEntry(ConfirmationRequest request)
	{
		var languages = httpHeaders.getAcceptableLanguages();
		var serviceTypeDescription = serviceTypeDescriptionGenerator.generateServiceTypeDescription(
			languages,
			request.getServiceType());
		var serviceTypeInfo = new ServiceTypeInformation(request.getServiceType().getCode(), serviceTypeDescription);

		var serviceDescription = serviceDescriptionGenerator.generateServiceDescription(
			languages,
			request.getServiceType(),
			request.getCode());

		return ConfirmationRequestConverter.from(request, serviceDescription, serviceTypeInfo);
	}

	@Override
	public void handleConfirmationResponse(Long id, ConfirmationRequestResponse response)
	{
		assertJwtClaims();
		assertActiveParticipation();

		confirmationService.handleResponse(getKvnrOrKvnrSha256Hash(), id, response.response() == ResponseState.CONFIRMED);
	}

	private void assertJwtClaims()
	{
		Objects.requireNonNull(getUserIdentifier());
		Objects.requireNonNull(getKvnrOrKvnrSha256Hash());
	}

	private String getUserIdentifier()
	{
		String userIdentifierClaim = jwt.getClaim(userIdentifierClaimName);
		return assertNotBlank(userIdentifierClaim);
	}

	private String getKvnrOrKvnrSha256Hash()
	{
		String kvnrOrHashSha256Claim = jwt.getClaim(kvnrOrHashSha256Claimname);
		return assertNotBlank(kvnrOrHashSha256Claim);
	}

	private <T> T assertNotBlank(T claimValue)
	{
		if (claimValue == null
			|| (claimValue instanceof String stringValue && stringValue.isBlank()))
		{
			throw new UnauthorizedException();
		}

		return claimValue;
	}

	private void assertActiveParticipation()
	{
		var insurantRegistration = insurantService.getActiveInsurantRegistration(getKvnrOrKvnrSha256Hash());
		if (insurantRegistration.isEmpty())
		{
			throw new WebApplicationException(Status.UNAUTHORIZED);
		}

		var insurant = insurantRegistration.get().getInsurant();
		if (!insurant.getUserIdentifier().equals(getUserIdentifier()))
		{
			insurant.setUserIdentifier(getUserIdentifier());
		}
	}

}
