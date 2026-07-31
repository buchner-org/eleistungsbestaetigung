package de.yoshteq.dcs.logging;

import java.time.OffsetDateTime;
import java.util.Map;

public record LogEntry(OffsetDateTime dateTime, Integer senderIk, String sourceSoftwareIdentifier, String chargeItemUuid, LogEntryState state, Map<String, Object> detail)
{

}
