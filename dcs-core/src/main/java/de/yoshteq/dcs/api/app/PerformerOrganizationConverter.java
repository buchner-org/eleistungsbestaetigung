package de.yoshteq.dcs.api.app;

import de.yoshteq.dcs.app.api.PerformerInformation;
import de.yoshteq.dcs.performer.PerformerOrganization;

public class PerformerOrganizationConverter
{
	private PerformerOrganizationConverter()
	{
	}

	public static PerformerInformation with(
		Integer ik,
		PerformerOrganization practitioner,
		String organizationFallbackDisplayValue,
		String practitionerDisplayValue)
	{
		if (practitioner == null)
		{
			return new PerformerInformation(ik, null, null, null, null, organizationFallbackDisplayValue, practitionerDisplayValue);
		}

		return new PerformerInformation(
			practitioner.getIk(),
			practitioner.getInstitutionNameRow1(),
			practitioner.getInstitutionNameRow2(),
			practitioner.getFirstName(),
			practitioner.getLastName(),
			organizationFallbackDisplayValue,
			practitionerDisplayValue);
	}
}
