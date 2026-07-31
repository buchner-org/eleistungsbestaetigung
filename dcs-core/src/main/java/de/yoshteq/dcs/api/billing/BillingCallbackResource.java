package de.yoshteq.dcs.api.billing;

import java.io.Closeable;
import java.time.temporal.ChronoUnit;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.faulttolerance.Retry;
import org.hl7.fhir.r4.model.Parameters;

import de.yoshteq.diga.secon.SeconContainer;
import de.yoshteq.diga.secon.SeconMediaType;

public interface BillingCallbackResource extends Closeable
{

	@POST
	@Consumes(SeconMediaType.INNER_XML)
	@Produces(SeconMediaType.INNER_XML)
	@Retry(delay = 3, delayUnit = ChronoUnit.SECONDS, maxRetries = 3)
	Response postConfirmationResponse(SeconContainer<Parameters> response);

}
