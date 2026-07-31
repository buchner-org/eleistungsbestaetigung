package de.yoshteq.dcs.fhir;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.Resource;

public class TestFileReader
{

	public static <T extends Resource> T readFile(String path)
	{
		var ctx = FhirContext.forR4();
		var parser = ctx.newXmlParser();

		try (var inputStream = TestFileReader.class.getClassLoader().getResourceAsStream(path))
		{
			@SuppressWarnings("unchecked")
			var resource = (T) parser.parseResource(inputStream);
			return resource;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

}
