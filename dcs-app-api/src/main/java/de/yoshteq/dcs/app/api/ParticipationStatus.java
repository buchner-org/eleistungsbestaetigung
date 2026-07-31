package de.yoshteq.dcs.app.api;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(title = "Teilnahmestatus")
public enum ParticipationStatus
{
		PARTICIPATING, NOT_PARTICIPATING
}
