package de.yoshteq.dcs.performer;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import de.yoshteq.dcs.confirmation.ServiceType;

@Entity
@IdClass(PerformerOrganizationParticipationId.class)
@Table(name = "performer_organization_participation")
public class PerformerOrganizationParticipation
{
	@Id
	@Column(name = "ik_hash")
	private String ikHash;

	@Id
	@Column(name = "practitioner_display_hash")
	private String practitionerDisplayHash;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "service_type")
	private ServiceType serviceType;

	@Column(name = "first_request_date_time")
	private OffsetDateTime firstRequestDateTime;

	@Column(name = "latest_request_date_time")
	private OffsetDateTime latestRequestDateTime;

	PerformerOrganizationParticipation()
	{
		super();
	}

	public PerformerOrganizationParticipation(String ikHash, String practitionerDisplayHash, ServiceType serviceType)
	{
		super();
		this.ikHash = ikHash;
		this.practitionerDisplayHash = practitionerDisplayHash;
		this.serviceType =serviceType;
		this.firstRequestDateTime = OffsetDateTime.now();
	}

	public void updateLatestRequestTime()
	{
		latestRequestDateTime = OffsetDateTime.now();
	}
}
