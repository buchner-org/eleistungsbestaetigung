package de.yoshteq.dcs.fhir.model.billing;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.r4.model.StringType;

public class BillingCointainerInvoiceMultiPart
{

	private BillingCointainerInvoiceMultiPart()
	{
	}

	public static ParametersParameterComponent create(String invoiceReference, Bundle container)
	{
		var invoiceParam = new ParametersParameterComponent()
			.setName("invoice");

		invoiceParam.addPart(new ParametersParameterComponent()
			.setName("invoiceReference")
			.setValue(new StringType(invoiceReference)));

		invoiceParam.addPart(new ParametersParameterComponent()
			.setName("container")
			.setResource(container));

		return invoiceParam;
	}

}
