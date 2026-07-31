package de.yoshteq.dcs.api.app;

import java.util.List;
import java.util.Locale;

import de.yoshteq.dcs.confirmation.ServiceType;

public interface ServiceTypeDescriptionGenerator
{
	String generateServiceTypeDescription(List<Locale> preferredLanguages, ServiceType serviceType);
}
