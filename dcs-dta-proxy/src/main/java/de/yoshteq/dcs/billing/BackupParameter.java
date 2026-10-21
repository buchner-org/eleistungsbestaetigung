package de.yoshteq.dcs.billing;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.hl7.fhir.r4.model.Parameters;

import de.tk.opensource.secon.SECON;
import de.tk.opensource.secon.SeconCallable;
import de.tk.opensource.secon.SeconException;
import de.yoshteq.dcs.billing.BillingInvoice.BillingInvoiceParameter;
import de.yoshteq.dcs.fhir.FhirHelper;

public class BackupParameter implements BillingInvoiceParameter
{
	public static BackupParameter parseInvoiceParameter(Parameters parameters)
	{
		return new BackupParameter(parameters);
	}

	private Parameters parameters;

	public BackupParameter(Parameters parameters)
	{
		this.parameters = parameters;
	}
	
	public void writeToSeconStream(SeconCallable<OutputStream> seconStream) throws SeconException, IOException
	{
		byte[] buffer = FhirHelper.toXmlString(parameters).getBytes(StandardCharsets.UTF_8);

		try(var inputStream = new ByteArrayInputStream(buffer))
		{
			SECON.copy(() -> inputStream, seconStream);
		}
		
	}
}
