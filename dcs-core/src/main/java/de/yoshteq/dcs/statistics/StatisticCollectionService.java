package de.yoshteq.dcs.statistics;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import de.yoshteq.dcs.confirmation.ConfirmationRequest;
import de.yoshteq.dcs.performer.PerformerOrganizationParticipation;
import de.yoshteq.dcs.performer.PerformerOrganizationParticipationId;
import io.quarkus.scheduler.Scheduled;

@ApplicationScoped
public class StatisticCollectionService
{
	@Inject
	EntityManager entityManager;

	private Queue<StatisticUpdateEntry> confirmationRequests = new ConcurrentLinkedQueue<>();

	public void onConfirmationRequestChanged(@Observes(during = TransactionPhase.AFTER_SUCCESS) ConfirmationRequest request)
	{
		var performingOrganizationIkHash = DigestUtils.sha256Hex(request.getPerformerOrganizationIK().toString());
		var performingOrganizationDisplayHash = DigestUtils.sha256Hex(StringUtils.defaultString(request.getPerformerPractitionerDisplay()));

		var entry = new StatisticUpdateEntry(
			request.getSenderIK(),
			request.getSoftwareIdentifier(),
			request.getServiceType(),
			performingOrganizationIkHash,
			performingOrganizationDisplayHash);

		confirmationRequests.add(entry);
	}

	@Scheduled(cron = "{dcs.statistics.save-db-cron}")
	@Transactional(value = TxType.REQUIRES_NEW)
	void updateStatisticsAsync()
	{
		StatisticUpdateEntry entry;
		while ((entry = confirmationRequests.poll()) != null)
		{
			updatePerformingOrganizationStatistics(entry);
			updateSoftwareStatistics(entry);
		}
	}

	private void updatePerformingOrganizationStatistics(StatisticUpdateEntry entry)
	{
		var performerOrganizationId = new PerformerOrganizationParticipationId(
			entry.performingOrganizationIkHash(),
			entry.performingPractitionerDisplayHash());

		var practitionerParticipation = entityManager.find(PerformerOrganizationParticipation.class, performerOrganizationId);
		if (practitionerParticipation == null)
		{
			practitionerParticipation = new PerformerOrganizationParticipation(
				entry.performingOrganizationIkHash(), 
				entry.performingPractitionerDisplayHash(),
				entry.serviceType());
		}
		practitionerParticipation.updateLatestRequestTime();

		entityManager.merge(practitionerParticipation);
	}

	private void updateSoftwareStatistics(StatisticUpdateEntry entry)
	{
		var softwareEntry = entityManager.find(
			Software.class,
			new SoftwareId(entry.softwareIK(), entry.softwareIdentifier()));

		if (softwareEntry == null)
		{
			softwareEntry = new Software(entry.softwareIK(), entry.softwareIdentifier());
		}
		softwareEntry.updateLatestRequestTime();

		entityManager.merge(softwareEntry);
	}

}
