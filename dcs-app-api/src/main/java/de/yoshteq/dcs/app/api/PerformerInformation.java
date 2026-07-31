package de.yoshteq.dcs.app.api;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(required = true, title = "Leistungserbringer", description = "Die Leistungserbringerorganisation wird über die IK identifiziert. Das anfragende System liefert einen organizationFallbackDisplayValue für die Organisation mit der Anfrage mit. Kann die Leistungserbringerorganisation über das IK nicht einer Organisation in der Arge-IK Datenbank zugewiesen werden, ist nur der organizationFallbackDisplayValue gesetzt. Andernfalls werden die Daten der Leistungserbringerorganisation aus der Arge-IK Datenbank gesetzt. Für angestellte Leistungserbringer kann der practitionerDisplayValue vom anfragenden System mitgeschickt werden und identifiziert die durchführende Leistungserbringerin in Person.")
public record PerformerInformation(
	@Schema(title = "IK der Organiation", required = true) Integer organizationIK,
	@Schema(title = "Name der Organiation Zeile 1", required = false) String organizationNameRow1,
	@Schema(title = "Name der Organiation Zeile 2", required = false) String organizationNameRow2,
	@Schema(title = "Vorname des Ansprechpartners der Organisation", required = false) String organizationContactFirstName,
	@Schema(title = "Nachname des Ansprechpartners der Organisation", required = false) String organizationContactLastName,
	@Schema(title = "Anzeigetext für die Leistungserbringerorganisation", required = true) String organizationFallbackDisplayValue,
	@Schema(title = "Anzeigetext für die Leistungserbringerin im Angestelltenverhältnis", required = false) String practitionerDisplayValue)
{

}
