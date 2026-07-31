package de.yoshteq.dcs.performer;

import java.io.Serializable;
import java.util.Objects;

public class PerformerOrganizationParticipationId implements Serializable
{
	private static final long serialVersionUID = 8020912123662223723L;

	private String ikHash;

	private String practitionerDisplayHash;

	PerformerOrganizationParticipationId()
	{
		super();
	}

	public PerformerOrganizationParticipationId(String ikHash, String practitionerDisplayHash)
	{
		this.ikHash = ikHash;
		this.practitionerDisplayHash = practitionerDisplayHash;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(ikHash, practitionerDisplayHash);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PerformerOrganizationParticipationId other = (PerformerOrganizationParticipationId) obj;
		return Objects.equals(ikHash, other.ikHash) && Objects.equals(practitionerDisplayHash, other.practitionerDisplayHash);
	}

}