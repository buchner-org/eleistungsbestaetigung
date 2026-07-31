package de.yoshteq.dcs.push;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.yoshteq.dcs.insurant.Insurant;
import io.quarkus.arc.lookup.LookupIfProperty;

@ApplicationScoped
@LookupIfProperty(name = "dcs.push.service.implementation", stringValue = "default")
public class DefaultNotificationService implements NotificationService
{

	private static Logger logger = LoggerFactory.getLogger(DefaultNotificationService.class);

	@Inject
	@RestClient
	RemotePushNotificationService pushNotificationService;

	@Inject
	@ConfigProperty(name = "dcs.push-service.auth-header")
	String pushApiAuthHeader;

	public void sendPushNotification(Insurant insurant)
	{
		logger.debug("Pushing notification to insurant with dcsUserId: {}", insurant.getUserIdentifier());
		pushNotificationService.sendPushNotification(insurant.getUserIdentifier(), pushApiAuthHeader);
	}

}
