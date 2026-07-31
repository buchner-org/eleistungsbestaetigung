package de.yoshteq.dcs.fhir.model.billing;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.r4.model.Base64BinaryType;

import de.yoshteq.dcs.fhir.FhirBinaryFileHelper;
import de.yoshteq.dcs.fhir.MetaHelper;

public class BillingContainerParameters
{

	private BillingContainerParameters()
	{
	}

	public static Parameters create(
		String emailContactPoint,
		Path esolFilePath,
		List<ParametersParameterComponent> invoiceContainer)
		throws IOException
	{
		var parameters = new Parameters();
		parameters.setMeta(MetaHelper.withProfileUrl("https://e-lb.de/fhir/StructureDefinition/KK_ELB_BillingContainerParameters"));

		var emailParam = new ParametersParameterComponent()
			.setName("emailContactPoint")
			.setValue(new ContactPoint()
				.setSystem(ContactPointSystem.EMAIL)
				.setValue(emailContactPoint));
		parameters.addParameter(emailParam);

		var esolFileData = FhirBinaryFileHelper.createBase64BinaryFromFile(esolFilePath);
		var esolParam = new ParametersParameterComponent()
			.setName("esolFile")
			.setValue(new Base64BinaryType(esolFileData));
		parameters.addParameter(esolParam);

		invoiceContainer.forEach(parameters::addParameter);

		return parameters;
	}

}
