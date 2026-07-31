package de.yoshteq.dcs.confirmation;

import java.time.OffsetDateTime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.yoshteq.dcs.logging.ProcessLogger;
import de.yoshteq.dcs.task.TaskSkipPredicate;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.Scheduled.ConcurrentExecution;

@ApplicationScoped
public class ConfirmationRequestScheduledHandler
{

	private static final Logger logger = LoggerFactory.getLogger(ConfirmationRequestScheduledHandler.class);

	@Inject
	EntityManager entityManager;

	@Inject
	ConfirmationService confirmationService;

	@Inject
	ProcessLogger processLogger;

	@Inject
	@ConfigProperty(name = "dcs.request.pending-timeout-hours")
	Integer requestPendingTimeoutInHours;

	
	@Scheduled(cron = "{dcs.task.confirmation.request.handler.cron}", skipExecutionIf = TaskSkipPredicate.class, concurrentExecution = ConcurrentExecution.SKIP, identity = "confirmation-request.scheduled-handler")
	void handleFloatingConfirmationRequests()
	{
		cancelExpiredPendingRequests();
		processFinishedRequests();
	}
	
	@Transactional(value = TxType.REQUIRES_NEW)
	void cancelExpiredPendingRequests()
	{
		var updateQuery = entityManager.createQuery(
			"UPDATE ConfirmationRequest SET state = 'CANCELLED' WHERE state = 'PENDING' AND requestDateTime < :expiredDateTime");
		var expiredDateTime = OffsetDateTime.now().minusHours(requestPendingTimeoutInHours);
		updateQuery.setParameter("expiredDateTime", expiredDateTime);
		var expiredRequestCount = updateQuery.executeUpdate();

		if (expiredRequestCount > 0)
		{
			logger.info("Cancelled {} expired requests", expiredRequestCount);
		}
	}	

	@Transactional(value = TxType.REQUIRES_NEW)
	void processFinishedRequests()
	{
		var query = entityManager.createQuery("SELECT C.id FROM ConfirmationRequest C WHERE C.state != 'PENDING' AND C.responseDocumentBundleXml IS NULL AND C.processingFailCount < 10", Long.class);
		var finishedRequestIds = query.getResultList();

		if (!finishedRequestIds.isEmpty())
		{
			logger.info("Processing finished requests");
		}

		for (var finishedRequestId : finishedRequestIds)
		{
			confirmationService.processConfirmationResponse(finishedRequestId);
		}
	}

}
