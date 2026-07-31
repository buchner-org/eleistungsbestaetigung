package de.yoshteq.dcs.app.api;

import jakarta.validation.constraints.NotNull;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(title = "Antwort zur Bestätigungsanfrage")
public record ConfirmationRequestResponse(
	@NotNull ResponseState response)
{
}
