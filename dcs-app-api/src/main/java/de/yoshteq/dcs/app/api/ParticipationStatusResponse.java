package de.yoshteq.dcs.app.api;

import java.time.OffsetDateTime;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(title = "Teilnahmestatusantwort")
public record ParticipationStatusResponse(
	ParticipationStatus status,
	@Schema(title = "Zeitpunkt der aktuellen Registrierung") OffsetDateTime registrationDateTime)
{

	public static final ParticipationStatusResponse NOT_PARTICIPATING = new ParticipationStatusResponse(ParticipationStatus.NOT_PARTICIPATING, null);

	public static ParticipationStatusResponse participating(OffsetDateTime registrationDateTime)
	{
		return new ParticipationStatusResponse(ParticipationStatus.PARTICIPATING, registrationDateTime);
	}

}
