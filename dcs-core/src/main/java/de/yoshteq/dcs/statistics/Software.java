package de.yoshteq.dcs.statistics;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "software")
@IdClass(SoftwareId.class)
public class Software
{
	@Id
	@Column(name = "ik")
	private Integer ik;

	@Id
	@Column(name = "software_identifier")
	private String softwareIdentifier;

	@Column(name = "first_request_date_time")
	private OffsetDateTime firstRequestDateTime;

	@Column(name = "latest_request_date_time")
	private OffsetDateTime latestRequestDateTime;

	Software()
	{
		super();
	}

	public Software(Integer ik, String softwareIdentifier)
	{
		super();
		this.ik = ik;
		this.softwareIdentifier = softwareIdentifier;
		this.firstRequestDateTime = OffsetDateTime.now();
	}

	public void updateLatestRequestTime()
	{
		latestRequestDateTime = OffsetDateTime.now();
	}
}
