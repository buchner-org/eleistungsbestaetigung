package de.yoshteq.dcs.app.api;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

public interface AppApi
{
	@GET
	@Path("/insurant")
	@APIResponse(responseCode = "200", description = "Erfolgreiche Anfrage mit Ergebnis")
	@APIResponse(responseCode = "401", description = "Fehlerhafte Authentifizierung")
	@Operation(summary = "Teilnahmestatus der Versicherten abfragen", description = "Abfrage des Teilnahmestatus der Versicherten am Verfahren.")
	ParticipationStatusResponse getParticipationStatus();

	@POST
	@Path("/insurant")
	@APIResponse(responseCode = "204", description = "Registrierung wurde ergfolgreich durchgeführt")
	@APIResponse(responseCode = "400", description = "Fehlerhafte Anfrage. Format der Antwort siehe: https://www.rfc-editor.org/rfc/rfc7807", content = @Content(mediaType = "application/problem+json", example = """
		{
		  "type": "urn:problem-type:bad-request",
		  "title": "Bad Request",
		  "status": 400,
		  "detail": "detailed description",
		  "instance": "urn:uuid:8e7a3825-4091-4cd3-92e2-1a9be4b910ab"
		}"""))
	@APIResponse(responseCode = "401", description = "Fehlerhafte Authentifizierung")
	@Operation(summary = "Versicherte registrieren", description = "Registrierung der Versicherten zur Teilnahme am Verfahren.")
	void registerInsurant();

	@DELETE
	@Path("/insurant")
	@APIResponse(responseCode = "204", description = "Deregistrierung wurde erfolgreich durchgeführt")
	@APIResponse(responseCode = "401", description = "Fehlerhafte Authentifizierung")
	@Operation(summary = "Versicherte deregistrieren", description = "Deregistrierung der Versicherten zur Teilnahme am Verfahren.")
	void deregisterInsurant();

	@GET
	@Path("/confirmation-requests")
	@APIResponse(responseCode = "200", description = "Liste offener Bestätigungsanfragen")
	@APIResponse(responseCode = "401", description = "Fehlerhafte Authentifizierung")
	@Operation(summary = "Abfrage der offenen Bestätigungsanfragen", description = "Offene Bestätigungsanfragen der Versicherten abfragen.")
	List<ConfirmationRequestEntry> listPendingConfirmationRequests();

	@POST
	@Path("/confirmation-requests/{id}/response")
	@APIResponse(responseCode = "204", description = "Die Antwort auf die Bestätigungsanfrage wurde erfolgreich verarbeitet")
	@APIResponse(responseCode = "401", description = "Fehlerhafte Authentifizierung")
	@APIResponse(responseCode = "404", description = "Es wurde keine Bestätigungsanfrage mit der gegebenen id gefunden")
	@Operation(summary = "Bestätigungsanfrage beantworten", description = "Bestätigen oder Ablehnen einer Bestätigungsanfrage mit gegebener id.")
	void handleConfirmationResponse(@PathParam("id") Long id, @Valid ConfirmationRequestResponse response);

}