package de.yoshteq.dcs.billing;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import jakarta.ws.rs.InternalServerErrorException;

import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.r4.model.StringType;

import de.tk.opensource.secon.SECON;
import de.tk.opensource.secon.SeconCallable;
import de.tk.opensource.secon.SeconException;
import de.yoshteq.dcs.billing.BillingInvoice.BillingInvoiceParameter;
import de.yoshteq.dcs.fhir.FhirHelper;

public class InvoiceParameter implements BillingInvoiceParameter
{
	public static InvoiceParameter parseInvoiceParameter(ParametersParameterComponent parameter)
	{
		return new InvoiceParameter(parameter);
	}

	public static boolean isInvoiceParameter(ParametersParameterComponent parameter)
	{
		return "invoice".equals(parameter.getName());
	}

	private ParametersParameterComponent parameter;

	private String invoiceReferenceName;

	public InvoiceParameter(ParametersParameterComponent parameter)
	{
		this.parameter = parameter;
		invoiceReferenceName = parseInvoiceReferenceName(parameter);
	}

	private String parseInvoiceReferenceName(ParametersParameterComponent parameter)
	{
		Optional<ParametersParameterComponent> invoiceReference = parameter.getPart()
			.stream()
			.filter(parm -> "invoiceReference".equals(parm.getName()))
			.findFirst();
		if (invoiceReference.isPresent())
		{
			return FhirHelper
				.assertElementType(invoiceReference.get().getValue(), StringType.class, "invoiceReference must be a string")
				.getValue();
		}
		throw new InternalServerErrorException();
	}

	public void writeToSeconStream(SeconCallable<OutputStream> seconStream) throws SeconException, IOException
	{
		Parameters parameterContainer = new Parameters().addParameter(parameter);
		byte[] buffer = FhirHelper.toXmlString(parameterContainer).getBytes(StandardCharsets.UTF_8);

		try(var inputStream = new ByteArrayInputStream(buffer))
		{
			SECON.copy(() -> inputStream, seconStream);
		}
		
	}

	public String getInvoiceReferenceName()
	{
		return invoiceReferenceName;
	}
}
