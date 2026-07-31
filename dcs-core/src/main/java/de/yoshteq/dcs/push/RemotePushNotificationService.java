package de.yoshteq.dcs.push;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@ApplicationScoped
@RegisterRestClient(configKey = "push-service")
public interface RemotePushNotificationService
{

	@POST
	void sendPushNotification(@HeaderParam("User-Identifier") String userIdentifier, @HeaderParam("Authorization") String authHeader);

}
