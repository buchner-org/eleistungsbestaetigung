package de.yoshteq.dcs.app.api;

import jakarta.validation.constraints.NotBlank;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(title = "Teilnahmeregistrierung")
public record ParticipationRequest(
	@Schema(title = "IK des Kostenträgers", description = "Kann entfallen, wenn das Bestätigungssystem ein Standardwert konfiguriert hat") Long kostentraegerIK,
	@Schema(title = "Sha256 Hashwert der KVNR") @NotBlank String kvnrHashSha256,
	@Schema(title = "ID der Versicherten") @NotBlank String userIdentifier)
{

}
