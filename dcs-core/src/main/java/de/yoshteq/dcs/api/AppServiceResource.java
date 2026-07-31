package de.yoshteq.dcs.api;

import java.util.Objects;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import de.yoshteq.dcs.insurant.InsurantService;
import io.quarkus.security.UnauthorizedException;

@Path("/app-service")
@ApplicationScoped
public class AppServiceResource
{

	@Inject
	JsonWebToken jwt;

	@Inject
	@ConfigProperty(name = "dcs.app-service.endpoint.enabled", defaultValue = "false")
	boolean endpointEnabled;

	@Inject
	@ConfigProperty(name = "dcs.app-service.jwt.sub")
	Optional<String> expectedSubject;

	@Inject
	InsurantService insurantService;

	@Transactional
	@DELETE
	@Path("/insurant")
	@APIResponse(responseCode = "204", description = "Deregistrierung wurde erfolgreich durchgeführt")
	@APIResponse(responseCode = "401", description = "Fehlerhafte Authentifizierung")
	@Operation(summary = "Versicherte deregistrieren", description = "Deregistrierung der Versicherten zur Teilnahme am Verfahren.")
	public void deregisterInsurant(AppServiceDeregisterUserRequest request)
	{
		if (!endpointEnabled)
		{
			throw new WebApplicationException(Status.NOT_IMPLEMENTED);
		}

		assertJwtClaims();

		insurantService.revokeParticipation(request.kvnr());
	}

	private void assertJwtClaims()
	{
		var userIdentifierClaim = jwt.getClaim("sub");
		Objects.requireNonNull(userIdentifierClaim);

		if (expectedSubject.isPresent() && !userIdentifierClaim.equals(expectedSubject.get()))
		{
			throw new UnauthorizedException();
		}
	}
}
