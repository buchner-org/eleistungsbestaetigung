package de.yoshteq.dcs.insurant;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "insurant_registration")
@NamedQuery(name = InsurantRegistration.FIND_ACTIVE_BY_INSURANT_ID, query = "SELECT R FROM InsurantRegistration R WHERE R.insurant.id = :insurantId AND R.deregistrationDateTime IS NULL AND R.deactivationDateTime IS NULL")
@NamedQuery(name = InsurantRegistration.FIND_ACTIVE_BY_PSEUDONYM, query = "SELECT R FROM InsurantRegistration R WHERE R.insurant.pseudonym = :pseudonym AND R.deregistrationDateTime IS NULL AND R.deactivationDateTime IS NULL")
@NamedQuery(name = InsurantRegistration.FIND_ACTIVE_REGISTRATIONS, query = "SELECT R FROM InsurantRegistration R WHERE R.insurant.id = :insurantId AND R.deregistrationDateTime IS NULL AND R.deactivationDateTime IS NULL")
@NamedQuery(name = InsurantRegistration.HAS_ACTIVE_REGISTRATIONS_PSEUDONYM, query = "SELECT R FROM InsurantRegistration R WHERE R.insurant.pseudonym = :pseudonym AND R.deregistrationDateTime IS NULL AND R.deactivationDateTime IS NULL")
public class InsurantRegistration
{

	static final String FIND_ACTIVE_BY_INSURANT_ID = "byInsurantId";

	static final String FIND_ACTIVE_BY_PSEUDONYM = "byPseudonym";

	static final String FIND_ACTIVE_REGISTRATIONS = "activeRegistrations";
	
	static final String HAS_ACTIVE_REGISTRATIONS_PSEUDONYM = "hasActiveRegistrations";

	public static InsurantRegistration with(Insurant insurant)
	{
		var registration = new InsurantRegistration();
		registration.insurant = insurant;
		registration.registrationDateTime = OffsetDateTime.now();

		return registration;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "insurant_id")
	private Insurant insurant;

	@NotNull
	@Column(name = "registration_date_time")
	private OffsetDateTime registrationDateTime;

	@Column(name = "deactivation_date_time")
	private OffsetDateTime deactivationDateTime;

	@Column(name = "deregistration_date_time")
	private OffsetDateTime deregistrationDateTime;

	public Long getId()
	{
		return id;
	}

	void setId(Long id)
	{
		this.id = id;
	}

	public Insurant getInsurant()
	{
		return insurant;
	}

	void setInsurant(Insurant insurant)
	{
		this.insurant = insurant;
	}

	public OffsetDateTime getRegistrationDateTime()
	{
		return registrationDateTime;
	}

	void setRegistrationDateTime(OffsetDateTime participationConfirmationDateTime)
	{
		this.registrationDateTime = participationConfirmationDateTime;
	}

	public OffsetDateTime getDeactivationDateTime()
	{
		return deactivationDateTime;
	}

	void setDeactivationDateTime(OffsetDateTime participationInactivationDateTime)
	{
		this.deactivationDateTime = participationInactivationDateTime;
	}

	public OffsetDateTime getDeregistrationDateTime()
	{
		return deregistrationDateTime;
	}

	void setDeregistrationDateTime(OffsetDateTime participationRevocationDateTime)
	{
		this.deregistrationDateTime = participationRevocationDateTime;
	}

	public boolean isActive()
	{
		return (registrationDateTime != null) && (deactivationDateTime == null) && (deregistrationDateTime == null);
	}

	public boolean isRevoked()
	{
		return deregistrationDateTime != null;
	}

	public void unregister()
	{
		if (this.isActive())
		{
			this.deregistrationDateTime = OffsetDateTime.now();
		}
	}

}
