package de.yoshteq.dcs.fhir;

import static de.yoshteq.dcs.fhir.FhirHelper.*;

import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Meta;

public class MetaHelper
{

	private MetaHelper()
	{
	}

	public static Meta withProfileUrl(String url)
	{
		var meta = new Meta();
		meta.addProfile(url);
		return meta;
	}


	public static void assertProfile(Resource resource, String profileUrl)
	{
		assertFixed(profileUrl, () -> resource.getMeta().getProfile().get(0).getValue());
	}


}
