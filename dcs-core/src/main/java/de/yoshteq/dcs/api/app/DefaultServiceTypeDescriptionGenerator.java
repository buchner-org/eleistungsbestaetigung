package de.yoshteq.dcs.api.app;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;

import de.yoshteq.dcs.DcsRuntimeException;
import de.yoshteq.dcs.confirmation.ServiceType;

@Priority(1000)
@Alternative
@ApplicationScoped
public class DefaultServiceTypeDescriptionGenerator implements ServiceTypeDescriptionGenerator
{

	@Override
	public String generateServiceTypeDescription(List<Locale> preferredLanguages, ServiceType serviceType)
	{
		var preferredLanguage = preferredLanguages.get(0);
		var resourceBundle = ResourceBundle.getBundle("service_type", preferredLanguage, new ResourceBundle.Control()
		{
			@Override
			public Locale getFallbackLocale(String baseName, Locale locale)
			{
				return Locale.GERMAN;
			}
		});

		if (resourceBundle == null || !resourceBundle.containsKey(serviceType.getCode()))
		{
			throw new DcsRuntimeException("Could not find type resource bundle for: " + serviceType);
		}

		return resourceBundle.getString(serviceType.getCode());
	}
}
