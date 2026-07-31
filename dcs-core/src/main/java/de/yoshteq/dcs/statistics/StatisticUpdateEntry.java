package de.yoshteq.dcs.statistics;

import de.yoshteq.dcs.confirmation.ServiceType;

public record StatisticUpdateEntry(
	Integer softwareIK,
	String softwareIdentifier,
	ServiceType serviceType,
	String performingOrganizationIkHash,
	String performingPractitionerDisplayHash)
{

}
