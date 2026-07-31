package de.yoshteq.dcs.confirmation;

import java.security.SignatureException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

import de.yoshteq.dcs.fhir.FhirHelper;
import de.yoshteq.dcs.fhir.api.ConfirmationRequestResponses;
import de.yoshteq.dcs.fhir.model.ResponseDocumentBundle;
import de.yoshteq.dcs.insurant.Insurant;
import de.yoshteq.dcs.insurant.InsurantPseudonymGenerator;
import de.yoshteq.dcs.logging.ProcessLogger;
import de.yoshteq.dcs.push.InsurantPushTask;
import de.yoshteq.dcs.secon.CertificateIdentity;
import de.yoshteq.dcs.signing.SignatureService;
import de.yoshteq.diga.secon.X509CertificateUtil;

@ApplicationScoped
public class ConfirmationService
{
	private static final Logger logger = LoggerFactory.getLogger(ConfirmationService.class);

	@Inject
	EntityManager entityManager;

	@Inject
	ProcessLogger processLogger;

	@Inject
	SignatureService signatureService;

	@Inject
	ManagedExecutor executor;

	@Inject
	CertificateIdentity identity;

	@Inject
	InsurantPseudonymGenerator pseudonymGenerator;

	@Inject
	Event<ConfirmationRequest> confirmationRequestEvent;

	@Inject
	@ConfigProperty(name = "dcs.software-identifier")
	String softwareIdentifier;

	@Inject
	@ConfigProperty(name = "dcs.contact-mail-address")
	String contactMailAddress;

	private Integer dcsInstitutionskennzeichen;

	@PostConstruct
	void init() throws Exception
	{
		dcsInstitutionskennzeichen = X509CertificateUtil.extractIkAsInteger(identity.certificate()).orElseThrow();
	}

	public void createConfirmationRequest(List<ConfirmationRequest> requests)
	{
		persistConfirmationRequest(requests);

		executor.execute(() -> schedulePushNotification(requests));
	}

	@Transactional(value = TxType.REQUIRES_NEW)
	void persistConfirmationRequest(List<ConfirmationRequest> requests)
	{
		requests.forEach(request -> {
			entityManager.persist(request);
			confirmationRequestEvent.fire(request);
		});
	}

	@Transactional(value = TxType.REQUIRES_NEW)
	void schedulePushNotification(List<ConfirmationRequest> requests)
	{
		var requestsGroupByInsurant = requests.stream().collect(Collectors.groupingBy(ConfirmationRequest::getInsurant));

		for (var entry : requestsGroupByInsurant.entrySet())
		{
			var insurant = entry.getKey();

			var pushTask = entityManager.find(InsurantPushTask.class, insurant.getId());
			if (pushTask == null)
			{
				var latestRequest = entry.getValue().stream().max((r1, r2) -> r1.getRequestDateTime().compareTo(r2.getRequestDateTime())).orElseThrow();
				pushTask = InsurantPushTask.create(insurant.getId(), latestRequest.getRequestDateTime());
				entityManager.persist(pushTask);
			}
			else if (pushTask.getPushDateTime() != null && pushTask.getPushDateTime().isBefore(OffsetDateTime.now().minusHours(1)))
			{
				pushTask.setPushDateTime(null);
			}
		}
	}

	public List<ConfirmationRequest> getPendingConfirmationRequestsForInsurant(String kvnrOrKvnrSha256Hash)
	{
		var pseudonym = pseudonymGenerator.from(kvnrOrKvnrSha256Hash);
		var query = entityManager
			.createQuery(
				"SELECT C FROM ConfirmationRequest C WHERE C.insurant.pseudonym = :pseudonym AND C.state = 'PENDING' ORDER BY C.occurrenceDateTime, C.occurrenceStartDateTime, C.occurrenceEndDateTime",
				ConfirmationRequest.class);
		query.setParameter("pseudonym", pseudonym);

		var resultList = query.getResultList();

		if (!resultList.isEmpty())
		{
			clearPushNotificationTaskState(resultList.get(0).getInsurant().getId());
		}

		return resultList;
	}

	private void clearPushNotificationTaskState(Long insurantId)
	{
		entityManager.createQuery("DELETE FROM InsurantPushTask WHERE insurantId = :insurantId")
			.setParameter("insurantId", insurantId)
			.executeUpdate();
	}

	public void handleResponse(String kvnrOrKvnrSha256Hash, Long confirmationRequestId, boolean confirmed)
	{
		var pseudonym = pseudonymGenerator.from(kvnrOrKvnrSha256Hash);
		checkAndPersistResponse(pseudonym, confirmationRequestId, confirmed);
		executor.execute(() -> processConfirmationResponse(confirmationRequestId));
	}

	@Transactional(value = TxType.REQUIRES_NEW)
	void checkAndPersistResponse(String pseudonym, Long confirmationRequestId, boolean confirmed)
	{
		var confirmationRequest = entityManager.find(ConfirmationRequest.class, confirmationRequestId);
		if ((confirmationRequest == null))
		{
			throw new NotFoundException();
		}
		else if (!confirmationRequest.isPending())
		{
			throw new WebApplicationException(Status.CONFLICT);
		}

		if (!pseudonym.equals(confirmationRequest.getInsurant().getPseudonym()))
		{
			throw new WebApplicationException(Status.UNAUTHORIZED);
		}

		confirmationRequest.setResponse(confirmed);
		processLogger.responseFromApp(confirmationRequest.getUuid(), confirmed);
	}

	@Transactional(value = TxType.REQUIRES_NEW)
	void processConfirmationResponse(Long confirmationRequestId)
	{
		var confirmationRequest = entityManager.find(ConfirmationRequest.class, confirmationRequestId);
		try
		{
			ResponseDocumentBundle responseDocumentBundle = ResponseDocumentBundle.create(
				dcsInstitutionskennzeichen,
				confirmationRequest.toChargeItem());

			var documentBundle = responseDocumentBundle.toBundle();
			if (confirmationRequest.isConfirmed())
			{
				documentBundle = createConfirmationSignature(confirmationRequest, documentBundle);
			}

			confirmationRequest.setResponseDocumentBundleUuid(responseDocumentBundle.getUuid().toString());
			confirmationRequest.setResponseDocumentBundleXml(FhirHelper.toXmlString(documentBundle));
			
			processLogger.processed(confirmationRequest);
		}
		catch (SignatureException e)
		{
			logger.error("Could not create signature", e);
			confirmationRequest.incrementProcessFailCount();
		}
		catch (Exception e)
		{
			logger.error("Error processing confirmatation response", e);
			confirmationRequest.incrementProcessFailCount();
		}
	}

	private Bundle createConfirmationSignature(ConfirmationRequest confirmationRequest, Bundle documentBundle)
		throws SignatureException
	{
		var signedFhirBundle = signatureService.sign(documentBundle);
		confirmationRequest.setResponseDocumentBundleXml(FhirHelper.toXmlString(signedFhirBundle));
		return signedFhirBundle;
	}

	public void cancelConfirmationRequestsForInsurant(Insurant insurant)
	{
		var query = entityManager
			.createQuery("UPDATE ConfirmationRequest R SET R.state = 'CANCELLED' WHERE R.state = 'PENDING' AND R.insurant.id = :insurantId")
			.setParameter("insurantId", insurant.getId());

		query.executeUpdate();
	}

	public List<ConfirmationRequest> getPollingConfirmationRequestsForRequestingSystem(Integer requestingSystemIk)
	{
		var query = entityManager
			.createQuery(
				"SELECT C FROM ConfirmationRequest C WHERE C.senderIK = :requestingSystemIk AND C.state != 'PENDING' AND C.responseType = 'POLLING' ORDER BY C.occurrenceDateTime, C.occurrenceStartDateTime, C.occurrenceEndDateTime",
				ConfirmationRequest.class);
		query.setParameter("requestingSystemIk", requestingSystemIk);

		return query.getResultList();
	}

	public List<ConfirmationRequest> getPollingConfirmationRequestsForPerformerOrganization(Integer senderIK, Integer performerOrganizationIK)
	{
		var query = entityManager
			.createQuery(
				"SELECT C FROM ConfirmationRequest C WHERE C.senderIK = :senderIK AND C.performerOrganizationIK = :performerOrganizationIK  AND C.state != 'PENDING' AND C.responseType = 'POLLING' ORDER BY C.occurrenceDateTime, C.occurrenceStartDateTime, C.occurrenceEndDateTime",
				ConfirmationRequest.class);
		query.setParameter("senderIK", senderIK);
		query.setParameter("performerOrganizationIK", performerOrganizationIK);

		return query.getResultList();
	}

	public List<ConfirmationRequest> getPollingConfirmationRequestsForPatient(Integer senderIK, String kvnrOrKvnrSha256Hash)
	{
		var pseudonym = pseudonymGenerator.from(kvnrOrKvnrSha256Hash);
		var query = entityManager
			.createQuery(
				"SELECT C FROM ConfirmationRequest C WHERE C.senderIK = :senderIK AND C.insurant.pseudonym = :pseudonym  AND C.state != 'PENDING' AND C.responseType = 'POLLING' ORDER BY C.occurrenceDateTime, C.occurrenceStartDateTime, C.occurrenceEndDateTime",
				ConfirmationRequest.class);
		query.setParameter("senderIK", senderIK);
		query.setParameter("pseudonym", pseudonym);

		return query.getResultList();
	}

	public List<ConfirmationRequest> getPollingConfirmationRequestsForPayor(Integer senderIK, Integer kostentraegerIK)
	{
		var query = entityManager
			.createQuery(
				"SELECT C FROM ConfirmationRequest C WHERE C.senderIK = :senderIK AND C.kostentraegerIK = :kostentraegerIK  AND C.state != 'PENDING' AND C.responseType = 'POLLING' ORDER BY C.occurrenceDateTime, C.occurrenceStartDateTime, C.occurrenceEndDateTime",
				ConfirmationRequest.class);
		query.setParameter("senderIK", senderIK);
		query.setParameter("kostentraegerIK", kostentraegerIK);

		return query.getResultList();
	}

	public void deleteConfirmedRequestsAfterPolling(Integer senderIK, List<String> uuids)
	{
		var query = entityManager
			.createQuery(
				"DELETE FROM ConfirmationRequest C WHERE C.senderIK = :senderIK AND C.responseDocumentBundleUuid IN :responseDocumentBundleUuids AND C.state != 'PENDING' AND C.responseType = 'POLLING'");
		query.setParameter("senderIK", senderIK);
		query.setParameter("responseDocumentBundleUuids", uuids);
		var deletedRows = query.executeUpdate();
		if (uuids.size() != deletedRows)
		{
			logger.warn("Expected to delete {} rows, but deleted {}", uuids.size(), deletedRows);
		}
	}

	public Parameters createResponseParameters(List<ConfirmationRequest> confirmationRequests)
	{
		return ConfirmationRequestResponses.create(softwareIdentifier, contactMailAddress, deserializeResponseBundles(confirmationRequests));
	}

	private List<Bundle> deserializeResponseBundles(List<ConfirmationRequest> confirmationRequests)
	{
		FhirContext fhirContext = FhirContext.forR4();
		IParser xmlParser = fhirContext.newXmlParser();
		var bundles = new ArrayList<Bundle>();
		for (var confirmationRequest : confirmationRequests)
		{
			var bundle = parseConfirmationRequestSerializedBundle(xmlParser, confirmationRequest);
			bundles.add(bundle);
		}

		return bundles;
	}

	private Bundle parseConfirmationRequestSerializedBundle(IParser parser, ConfirmationRequest request)
	{
		try
		{
			return parser.parseResource(Bundle.class, request.getResponseDocumentBundleXml());
		}
		catch (Exception e)
		{
			throw new InternalServerErrorException();
		}
	}
}
