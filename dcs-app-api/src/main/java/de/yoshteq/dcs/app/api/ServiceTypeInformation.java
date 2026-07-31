package de.yoshteq.dcs.app.api;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(title = "Leistungsart", description = "Kombination aus Code und textueller Beschreibung der Leistungsart (Hebamme, Heilmittel, Hilfsmittel, ...)")
public record ServiceTypeInformation(
	@Schema(title = "Code des Sammegruppenschlüssels nach GKV für die Leistungsart") String code,
	@Schema(title = "Kurze textuelle Beschreibung der Leistungsart") String description)
{

}
