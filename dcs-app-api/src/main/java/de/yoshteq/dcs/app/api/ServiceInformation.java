package de.yoshteq.dcs.app.api;

import org.eclipse.microprofile.openapi.annotations.ExternalDocumentation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(title = "Leistung", description = "Leistung, für die eine Bestätigung angefragt wird")
public record ServiceInformation(
	ServiceTypeInformation type,
	@Schema(title = "Leistungsnummer der ausgeführten Leistung", description = "Leistungsnummer nach Gebührenordnung des jeweiligen Gebührenordnungskatalogs der Leistungsart") String code,
	ServiceDescription description,
	@Schema(title = "Leistungsdatum bzw. Leistungszeitpunkt", externalDocs = @ExternalDocumentation(description = "ISO8601 (partial) dateTime FHIR DateTime", url = "https://build.fhir.org/datatypes.html#dateTime")) String dateTime,
	@Schema(title = "Startzeitpunkt der Leistung", externalDocs = @ExternalDocumentation(description = "ISO8601 (partial) dateTime FHIR DateTime", url = "https://build.fhir.org/datatypes.html#dateTime")) String startDateTime,
	@Schema(title = "Endzeitpunkt der Leistung", externalDocs = @ExternalDocumentation(description = "ISO8601 (partial) dateTime FHIR DateTime", url = "https://build.fhir.org/datatypes.html#dateTime")) String endDateTime)
{

}
