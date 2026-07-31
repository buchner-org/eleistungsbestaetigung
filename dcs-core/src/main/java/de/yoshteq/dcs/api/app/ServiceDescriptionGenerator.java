package de.yoshteq.dcs.api.app;

import java.util.List;
import java.util.Locale;

import de.yoshteq.dcs.app.api.ServiceDescription;
import de.yoshteq.dcs.confirmation.ServiceType;

public interface ServiceDescriptionGenerator
{
	ServiceDescription generateServiceDescription(List<Locale> preferredLanguages, ServiceType serviceType, String code);
}
