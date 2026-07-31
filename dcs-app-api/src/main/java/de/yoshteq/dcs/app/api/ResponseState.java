package de.yoshteq.dcs.app.api;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(title = "Antwortstatus", description = "Status kann bestätigt (CONFIRMED) oder abgelehnt (DECLINED) sein")
public enum ResponseState
{
		CONFIRMED, DECLINED
}
