package de.yoshteq.dcs.statistics;

import java.io.Serializable;
import java.util.Objects;

public class SoftwareId implements Serializable
{

	private static final long serialVersionUID = -7328305789265193605L;

	private Integer ik;

	private String softwareIdentifier;

	SoftwareId()
	{
		super();
	}

	public SoftwareId(Integer ik, String softwareIdentifier)
	{
		this.ik = ik;
		this.softwareIdentifier = softwareIdentifier;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(ik, softwareIdentifier);
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
		SoftwareId other = (SoftwareId) obj;
		return Objects.equals(ik, other.ik) && Objects.equals(softwareIdentifier, other.softwareIdentifier);
	}

}
