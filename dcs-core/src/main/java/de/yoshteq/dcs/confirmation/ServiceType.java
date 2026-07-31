package de.yoshteq.dcs.confirmation;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

public enum ServiceType
{

		HILFSMITTEL("A", "hilfsmittel"),
		HEILMITTEL("B", "heilmittel"),
		HKP("C", "hkp"),
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
			case HKP:
				return getHkpLookupCode(serviceCode);
			default:
				return serviceCode;
		}
	}

	private String getHebammenLookupCode(String serviceCode)
	{
		if(serviceCode.length() == 5)
		{
			return serviceCode;
		} else if(serviceCode.length() == 4)
		{
			return serviceCode.substring(0, 3);
		} else
		{
			return "unknown";
		}
	}
	
	private String getHkpLookupCode(String serviceCode)
	{
		if (serviceCode.length() < 6)
		{
			return "unknown";
		}
		return serviceCode.substring(3, 6);
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
