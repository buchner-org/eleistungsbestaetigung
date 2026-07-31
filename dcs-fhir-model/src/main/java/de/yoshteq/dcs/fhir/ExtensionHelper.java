package de.yoshteq.dcs.fhir;

import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Element;

public class ExtensionHelper
{
	private ExtensionHelper()
	{

	}

	public static <T extends Element> T getExtensionWithUrl(DomainResource element, String extensionUrl, Class<T> classOfElement)
	{
		for (var extension : element.getExtension())
		{
			if (extension.getUrl().equals(extensionUrl))
			{
				var value = extension.getValue();
				if (!classOfElement.isInstance(value))
				{
					throw new FhirValidationException(
						"Extension with URL '" + extensionUrl + "' has wrong type: " + value.getClass().getSimpleName() + ", expected: "
							+ classOfElement.getSimpleName());
				}

				return classOfElement.cast(value);
			}
		}

		throw new FhirValidationException("Extension with URL '" + extensionUrl + "' not found");
	}

}
