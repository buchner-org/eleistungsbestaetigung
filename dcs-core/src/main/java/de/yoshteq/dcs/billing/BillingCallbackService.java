package de.yoshteq.dcs.billing;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

import org.eclipse.microprofile.context.ManagedExecutor;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.yoshteq.dcs.api.billing.BillingCallbackResource;
import de.yoshteq.dcs.confirmation.ConfirmationRequest;
import de.yoshteq.dcs.confirmation.ConfirmationService;
import de.yoshteq.dcs.logging.ProcessLogger;
import de.yoshteq.dcs.secon.CertificateIdentity;
import de.yoshteq.dcs.secon.SeconDirectory;
import de.yoshteq.dcs.task.TaskSkipPredicate;
import de.yoshteq.diga.secon.SeconContainer;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.Scheduled.ConcurrentExecution;
import io.smallrye.common.annotation.Blocking;

@ApplicationScoped
public class BillingCallbackService {
	private static final Logger logger = LoggerFactory.getLogger(BillingCallbackService.class);

	@Inject
	ConfirmationService confirmationService;

	@Inject
	ProcessLogger processLogger;

	@Inject
	EntityManager entityManager;

	@Inject
	ManagedExecutor executor;

	@Inject
	CertificateIdentity certificateIdentity;

	@Inject
	SeconDirectory seconDirectory;

	@Blocking
	@Scheduled(cron = "{dcs.task.billing.callback.cron}", concurrentExecution = ConcurrentExecution.SKIP, skipExecutionIf = TaskSkipPredicate.class, identity = "billing.callback")
	public void callbackPeriodically() {
		var confirmationRequestsPerCallbackUrl = entityManager.createQuery(
				"SELECT C FROM ConfirmationRequest C WHERE C.state != 'PENDING' AND C.responseDocumentBundleXml IS NOT NULL AND C.responseType = 'CALLBACK' AND C.callbackFailCount < 10 ORDER BY C.callbackUrl",
				ConfirmationRequest.class)
				.getResultStream()
				.collect(Collectors.groupingBy(ConfirmationRequest::getSenderIK));

		for (var ikGrouped : confirmationRequestsPerCallbackUrl.entrySet()) {
			var receivingIk = ikGrouped.getKey();
			var callbackUrls = ikGrouped.getValue().stream()
					.collect(Collectors.groupingBy(ConfirmationRequest::getCallbackUrl));
			for (var urlGrouped : callbackUrls.entrySet()) {
				executor.execute(() -> callback(receivingIk, urlGrouped.getKey(), urlGrouped.getValue()));
			}
		}
	}

	@Transactional(value = TxType.REQUIRES_NEW)
	void callback(Integer receivingIK, String callbackUrl, List<ConfirmationRequest> requests) {
		try {
			var response = confirmationService.createResponseParameters(requests);

			URI apiUri = new URI(callbackUrl);
			var restClientBuilder = RestClientBuilder.newBuilder().baseUri(apiUri);
			certificateIdentity.setRestClientCertificate(restClientBuilder);

			var x = seconDirectory.certificate(requests.get(0).getSenderIK().toString());
			try (BillingCallbackResource callbackResource = restClientBuilder.build(BillingCallbackResource.class);) {
				callbackResource.postConfirmationResponse(
						SeconContainer.create(requests.get(0).getSenderIK().toString(), x.orElseThrow(), response));
			}

			var ids = requests.stream().map(ConfirmationRequest::getId).toList();
			entityManager.createQuery("DELETE FROM ConfirmationRequest WHERE id IN (:ids)")
					.setParameter("ids", ids)
					.executeUpdate();

			requests.forEach(this::logDelivery);
		} catch (Exception e) {
			logger.error("Error on response callback", e);
			for (var confirmationRequest : requests) {
				confirmationRequest.incrementCallbackFailCount();
				entityManager.merge(confirmationRequest);
			}
		}
	}

	private void logDelivery(ConfirmationRequest confirmationRequest) {
		processLogger.callback(confirmationRequest.getUuid());
	}

}
