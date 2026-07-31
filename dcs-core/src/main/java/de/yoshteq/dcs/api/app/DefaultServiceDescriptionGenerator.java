package de.yoshteq.dcs.api.app;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;

import de.yoshteq.dcs.DcsRuntimeException;
import de.yoshteq.dcs.app.api.ServiceDescription;
import de.yoshteq.dcs.confirmation.ServiceType;

@Priority(1000)
@Alternative
@ApplicationScoped
public class DefaultServiceDescriptionGenerator implements ServiceDescriptionGenerator
{

	@Override
	public ServiceDescription generateServiceDescription(List<Locale> preferredLanguages, ServiceType serviceType, String code)
	{
		var bundleName = "service_descriptions_" + serviceType.getServiceDescriptionBundleSuffix();
		var resourceBundle = ResourceBundle.getBundle(bundleName, preferredLanguages.get(0), new ResourceBundle.Control()
		{
			@Override
			public Locale getFallbackLocale(String baseName, Locale locale)
			{
				return Locale.GERMAN;
			}
		});

		String serviceDescriptionLookupCode = serviceType.getServiceDescriptionLookupCode(code);
		if (resourceBundle == null || !resourceBundle.containsKey(serviceDescriptionLookupCode))
		{
			throw new DcsRuntimeException("Could not find service resource bundle for type '" + serviceType + "' and code: " + code);
		}
		
		var description = resourceBundle.getString(serviceDescriptionLookupCode);
		var splitted = description.split(";", 2);

		return new ServiceDescription(splitted[0], splitted.length > 1 ? splitted[1] : "");
	}

}
