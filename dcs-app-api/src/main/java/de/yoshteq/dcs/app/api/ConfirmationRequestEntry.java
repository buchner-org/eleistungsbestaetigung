package de.yoshteq.dcs.app.api;

import java.time.OffsetDateTime;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(title = "Bestätigungsanfrage")
public record ConfirmationRequestEntry(
	@Schema(title = "Identifier", description = "Eindeutige ID der Anfrage im Bestätigungssystem") Long id,
	PerformerInformation performer,
	ServiceInformation service,
	@Schema(title = "Erstellungzeitpunkt", description = "Zeitpunkt zu dem die Anfrage erstellt wurde") OffsetDateTime creationDateTime,
	@Schema(title = "Ablaufzeitpunkt", description = "Zeitpunkt bis zu dem die Anfrage noch bestätigt werden kann, bevor sie abgebrochen wird") OffsetDateTime expiryDateTime
	)
{


}
