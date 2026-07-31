package de.yoshteq.dcs.push.barmer;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/")
@RegisterRestClient(configKey = "push-service-barmer")
public interface BarmerPushApi
{

	@POST
	@Consumes("application/vnd.healthplatform.thirdparty.reminder.v2+json;charset=UTF-8")
	@Path("/api/v2/reminders")
	Response sendPushNotification(
		@HeaderParam("Authorization") String authorization,
		String request);

}
