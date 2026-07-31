package de.yoshteq.dcs.logging;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

import org.eclipse.microprofile.context.ManagedExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.yoshteq.dcs.confirmation.ConfirmationRequest;
import io.quarkus.scheduler.Scheduled;

@ApplicationScoped
public class ProcessLogger
{
	private static final Logger logger = LoggerFactory.getLogger(ProcessLogger.class);

	@Inject
	EntityManager entityManager;

	@Inject
	ManagedExecutor executor;

	private Queue<LogEntry> logEntries = new ConcurrentLinkedQueue<>();

	public void participationStatusQuery(Integer senderIK, boolean result)
	{
		log(senderIK, null, LogEntryState.PARTICIPATION_STATUS_QUERY, Map.of("result", result));
	}

	public void requestFromBilling(String batchRequestUuid, ConfirmationRequest request)
	{
		log(
			request.getSenderIK(),
			request.getSoftwareIdentifier(),
			request.getUuid(),
			LogEntryState.RECEIVED_FROM_BILLING,
			Map.of(
				"batchRequestUuid",
				batchRequestUuid,
				"serviceType",
				request.getServiceType(),
				"serviceCode",
				request.getCode()));
	}

	public void pushedToApp(Long insurantId)
	{
		log(null, null, LogEntryState.PUSHED_TO_APP, Map.of("insurantId", insurantId));
	}

	public void responseFromApp(String chargeItemUuid, boolean confirmed)
	{
		log(null, null, chargeItemUuid, LogEntryState.RECEIVED_RESPONSE_FROM_APP, Map.of("confirmed", confirmed));
	}

	public void processed(ConfirmationRequest request)
	{
		log(null, null, request.getUuid(), LogEntryState.PROCESSED, Map.of("state", request.getState()));
	}

	public void callback(String chargeItemUuid)
	{
		log(null, null, chargeItemUuid, LogEntryState.DELIVERED_TO_BILLING, null);
	}

	public void pollingRequest(Integer senderIk, String pollingUuid, ConfirmationRequest request)
	{
		log(
			senderIk,
			request.getSoftwareIdentifier(),
			request.getUuid(),
			LogEntryState.DELIVERED_TO_BILLING,
			Map.of("pollingUuid", pollingUuid, "responseBundleUuid", request.getResponseDocumentBundleUuid()));
	}

	public void pollingConfirm(Integer senderIk, String pollingConfirmUuid, String bundleUuid)
	{
		log(
			senderIk,
			null,
			LogEntryState.POLLING_CONFIRMATION,
			Map.of("pollingConfirmUuid", pollingConfirmUuid, "responseBundleUuid", bundleUuid));
	}

	private void log(Integer senderIk, String sourceSoftwareIdentifier, LogEntryState state, Map<String, Object> detail)
	{
		log(new LogEntry(OffsetDateTime.now(), senderIk, sourceSoftwareIdentifier, null, state, detail));
	}

	private void log(Integer senderIk, String sourceSoftwareIdentifier, String chargeItemUuid, LogEntryState state, Map<String, Object> detail)
	{
		log(new LogEntry(OffsetDateTime.now(), senderIk, sourceSoftwareIdentifier, chargeItemUuid, state, detail));
	}

	void log(LogEntry entry)
	{
		logEntries.add(entry);
	}

	@Inject
	ObjectMapper objectMapper;

	@Scheduled(cron = "{dcs.logging.save-db-cron}")
	@Transactional(value = TxType.REQUIRES_NEW)
	void logAsync()
	{
		LogEntry entry;
		while ((entry = logEntries.poll()) != null)
		{
			try
			{
				var query = entityManager.createNativeQuery(
					"INSERT INTO process_log (date_time, sender_ik, source_software_identifier, charge_item_uuid, state, detail) "
						+ "VALUES(:dateTime, :senderIk, :sourceSoftwareIdentifier, :chargeItemUuid, :state, :detail)");
				query.setParameter("dateTime", entry.dateTime());
				query.setParameter("senderIk", entry.senderIk());
				query.setParameter("sourceSoftwareIdentifier", entry.sourceSoftwareIdentifier());
				query.setParameter("chargeItemUuid", entry.chargeItemUuid());
				query.setParameter("state", entry.state().toString());
				query.setParameter(
					"detail",
					entry.detail() == null
						? null
						: objectMapper.writeValueAsString(entry.detail()));

				query.executeUpdate();
			}
			catch (Exception e)
			{
				logger.error("could not persist log entry: " + entry.toString(), e);
			}
		}
	}

}
