package de.yoshteq.dcs.push.barmer;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.yoshteq.dcs.ObjectMapperHolder;
import de.yoshteq.dcs.insurant.Insurant;
import de.yoshteq.dcs.push.NotificationService;
import io.quarkus.arc.lookup.LookupIfProperty;
import io.quarkus.oidc.client.OidcClients;
import io.quarkus.oidc.client.runtime.TokensHelper;

@ApplicationScoped
@LookupIfProperty(name = "dcs.push.service.implementation", stringValue = "barmer")
public class BarmerNotificationService implements NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(BarmerNotificationService.class);

    @Inject
    OidcClients clients;

    TokensHelper tokenHelper = new TokensHelper();

    @Inject
    @RestClient
    BarmerPushApi pushApiClient;

    @Inject
    ObjectMapperHolder objectMapperHolder;

    @Inject
    @ConfigProperty(name = "dcs.push.service.barmer.notification-title", defaultValue = "Titel")
    String notificationTitle;

    @Inject
    @ConfigProperty(name = "dcs.push.service.barmer.notification-text", defaultValue = "Text")
    String notificationText;

    @Inject
    @ConfigProperty(name = "dcs.push.service.barmer.deeplink-data", defaultValue = "https://")
    String deeplinkData;

    @Override
    public void sendPushNotification(Insurant insurant) {
        logger.info("BARMER push service");
        var tokens = clients.getClient().getTokens().await().atMost(Duration.ofSeconds(3));
        logger.info("Insurant-Partner-ID: {}", insurant.getUserIdentifier());
        logger.info("AccessToken: {}", tokens.getAccessToken());

        try {
            var request = new PushNotificationRequest(
                    ZonedDateTime
                            .now(ZoneId.of("Europe/Berlin"))
                            .plusMinutes(2)
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmm")),
                    notificationTitle,
                    notificationText,
                    deeplinkData,
                    "ELB_01",
                    "ONETIME",
                    insurant.getUserIdentifier());

            var requestString = objectMapperHolder.getObjectMapper().writeValueAsString(request);
            var response = pushApiClient.sendPushNotification("Bearer " + tokens.getAccessToken(), requestString);

            logger.info("ResponseStatus: {}", response.getStatus());
        } catch (Exception e) {
            logger.error("Could not send push request", e);
        }
    }

}
