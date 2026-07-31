package de.yoshteq.dcs.app.api;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(title = "Leistungsbeschreibung", description = "Kombination aus Titel und Beschreibung der Leistung")
public record ServiceDescription(String title, String informationalText)
{

}
