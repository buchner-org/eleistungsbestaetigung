package de.yoshteq.dcs.insurant;

import java.time.OffsetDateTime;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;

import de.yoshteq.dcs.confirmation.ConfirmationService;

@Transactional
@ApplicationScoped
public class InsurantService
{
	@Inject
	EntityManager entityManager;
	
	@Inject
	ConfirmationService confirmationService;
	
	@Inject
	InsurantPseudonymGenerator pseudonymGenerator;

	public Optional<InsurantRegistration> getActiveInsurantRegistration(String kvnrOrKvnrSha256Hash)
	{
		var pseudonym = pseudonymGenerator.from(kvnrOrKvnrSha256Hash);
		return entityManager
			.createNamedQuery(InsurantRegistration.FIND_ACTIVE_BY_PSEUDONYM, InsurantRegistration.class)
			.setParameter("pseudonym", pseudonym)
			.getResultStream()
			.findFirst();
	}

	public InsurantRegistration registerInsurant(@NotBlank String kvnrOrKvnrSha256Hash, @NotBlank String userIdentifier)
	{
		revokeParticipation(kvnrOrKvnrSha256Hash);

		var pseudonym = pseudonymGenerator.from(kvnrOrKvnrSha256Hash);
		Insurant insurant = getInsurantByPseudonym(pseudonym).orElse(null);
		if (insurant != null)
		{
			deactivateLatestRegistration(insurant.getId());
			insurant.setUserIdentifier(userIdentifier);
		}
		else
		{
			insurant = createInsurant(pseudonym, userIdentifier);
		}

		return insurant.createRegistration();
	}

	private Optional<Insurant> getInsurantByPseudonym(String pseudonym)
	{
		return entityManager
			.createNamedQuery(Insurant.QUERY_BY_PSEUDONYM, Insurant.class)
			.setParameter("pseudonym", pseudonym)
			.getResultStream()
			.findFirst();
	}

	private void deactivateLatestRegistration(Long insurantId)
	{
		var query = entityManager
			.createQuery("UPDATE InsurantRegistration R SET R.deactivationDateTime = :nowDateTime WHERE R.insurant.id = :insurantId AND R.deactivationDateTime IS NULL AND R.deregistrationDateTime IS NULL")
			.setParameter("nowDateTime", OffsetDateTime.now())
			.setParameter("insurantId", insurantId);

		query.executeUpdate();
	}

	private Insurant createInsurant(String pseudonym, String userIdentifier)
	{
		var insurant = Insurant.with(pseudonym, userIdentifier);
		return entityManager.merge(insurant);
	}
	
	public Optional<Insurant> getInsurant(String kvnrOrKvnrSha256Hash)
	{
		var pseudonym = pseudonymGenerator.from(kvnrOrKvnrSha256Hash);
		return getInsurantByPseudonym(pseudonym);
	}

	public void revokeParticipation(String kvnrOrKvnrSha256Hash)
	{
		var registrationOptional = getActiveInsurantRegistration(kvnrOrKvnrSha256Hash);
		if (registrationOptional.isEmpty())
		{
			return;
		}

		var insurantRegistration = registrationOptional.get();
		insurantRegistration.unregister();
		
		confirmationService.cancelConfirmationRequestsForInsurant(insurantRegistration.getInsurant());
	}

	public boolean hasInsurantActiveRegistration(String kvnrOrKvnrSha256Hash)
	{
		var pseudonym = pseudonymGenerator.from(kvnrOrKvnrSha256Hash);
		return entityManager
			.createNamedQuery(InsurantRegistration.HAS_ACTIVE_REGISTRATIONS_PSEUDONYM, InsurantRegistration.class)
			.setParameter("pseudonym", pseudonym)
			.getResultStream()
			.count() > 0;
	}

}
