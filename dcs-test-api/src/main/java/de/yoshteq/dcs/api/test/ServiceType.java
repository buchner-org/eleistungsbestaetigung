package de.yoshteq.dcs.api.test;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

public enum ServiceType
{

		HILFSMITTEL("A", "hilfsmittel"),
		HEILMITTEL("B", "heilmittel"),
		HEBAMME("F", "hebamme");

	public static ServiceType from(String functionCode)
	{
		for (var serviceType : ServiceType.values())
		{
			if (serviceType.code.equals(functionCode))
				return serviceType;
		}
		return null;
	}

	private String code;

	private String serviceDescriptionBundleSuffix;

	private ServiceType(String code, String serviceDescriptionBundleSuffix)
	{
		this.code = code;
		this.serviceDescriptionBundleSuffix = serviceDescriptionBundleSuffix;
	}

	public String getCode()
	{
		return code;
	}

	public String getServiceDescriptionBundleSuffix()
	{
		return serviceDescriptionBundleSuffix;
	}

	public String getServiceDescriptionLookupCode(String serviceCode)
	{
		switch (this)
		{
			case HEBAMME:
				return getHebammenLookupCode(serviceCode);
			default:
				return serviceCode;
		}
	}

	private String getHebammenLookupCode(String serviceCode)
	{
		if (serviceCode.length() < 4)
		{
			return "unknown";
		}
		return serviceCode.substring(0, 3);
	}

	public Set<String> serviceCodes()
	{
		var serviceBundle = ResourceBundle.getBundle("service_descriptions_" + getServiceDescriptionBundleSuffix(), Locale.GERMANY);
		return serviceBundle.keySet();
	}

	public boolean isValidServiceCode(String code)
	{
		var serviceBundle = serviceCodes();
		var serviceNumber = getServiceDescriptionLookupCode(code);

		return serviceBundle.contains(serviceNumber);
	}

}
