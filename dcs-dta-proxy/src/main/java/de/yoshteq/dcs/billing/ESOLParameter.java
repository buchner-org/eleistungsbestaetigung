package de.yoshteq.dcs.billing;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.hl7.fhir.r4.model.Base64BinaryType;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;

import de.tk.opensource.secon.SECON;
import de.tk.opensource.secon.SeconCallable;
import de.tk.opensource.secon.SeconException;
import de.yoshteq.dcs.billing.BillingInvoice.BillingInvoiceParameter;
import de.yoshteq.dcs.fhir.FhirHelper;

public class ESOLParameter implements BillingInvoiceParameter
{
	record UNBLine(String senderIK, String receiverIK, String datetime, String referenceNumber, String logischerDateiname)
	{
	}

	record FKTLine(String midwifeIK, String patientInsuranceNumber)
	{
	}

	record RECLine(String belegNumber, String date)
	{
	}

	public class InvoiceInformation
	{
		public String number;

		public String leistungserbringerIK;

		public String patientInsuranceNumber;

		public LocalDate date;

		public InvoiceInformation(FKTLine fktLine, RECLine recLine)
		{
			number = recLine.belegNumber;
			leistungserbringerIK = fktLine.midwifeIK;
			patientInsuranceNumber = fktLine.patientInsuranceNumber;
			date = LocalDate.parse(recLine.date, DateTimeFormatter.ofPattern("yyyyMMdd"));
		}
	}

	public static ESOLParameter parseESOLParameter(ParametersParameterComponent parameter)
	{
		var decodedData = FhirHelper
			.assertElementType(parameter.getValue(), Base64BinaryType.class, "esolFile must be base64Binary")
			.getValue();
		return new ESOLParameter(decodedData);
	}

	public static boolean isESOLParameter(ParametersParameterComponent parameter)
	{
		return "esolFile".equals(parameter.getName());
	}

	private byte[] decodedData;

	private String absenderIK;

	private String empfaengerIK;

	private LocalDateTime invoiceDateTime;

	private String logischerDateiname;

	private String referenceNumber;

	private Map<String, InvoiceInformation> invoiceInformation = new HashMap<>();

	public ESOLParameter(byte[] decodedData)
	{
		this.decodedData = decodedData;
		extractESOLInformation();
	}

	private void extractESOLInformation()
	{
		String esolString = new String(decodedData, StandardCharsets.ISO_8859_1);
		String[] esolLines = esolString.split("\r\n");
		for (int i = 0; i < esolLines.length; i++)
		{
			if (esolLines[i].contains("UNB"))
			{
				UNBLine unbLine = parseUNBLine(esolLines[i]);
				absenderIK = unbLine.senderIK;
				empfaengerIK = unbLine.receiverIK;
				invoiceDateTime = LocalDateTime.parse(unbLine.datetime, DateTimeFormatter.ofPattern("yyyyMMdd:HHmm"));
				referenceNumber = unbLine.referenceNumber;
				logischerDateiname = unbLine.logischerDateiname;
				i++;
			}
			if (esolLines[i].startsWith("UNH") && esolLines[i].contains("SLLA"))
			{
				FKTLine fktLine = parseFKTLine(esolLines[i + 1]);
				RECLine recLine = parseRECLine(esolLines[i + 2]);
				invoiceInformation.put(recLine.belegNumber(), new InvoiceInformation(fktLine, recLine));
				i = i + 2;
			}
		}
	}

	private UNBLine parseUNBLine(String esoLine)
	{
		String[] unbLineArray = esoLine.split("\\+");
		return new UNBLine(unbLineArray[2], unbLineArray[3], unbLineArray[4], unbLineArray[5], unbLineArray[7]);
	}

	private FKTLine parseFKTLine(String esoLine)
	{
		String[] fktLineArray = esoLine.split("\\+\\+");
		String[] fktValues = fktLineArray[1].split("\\+");
		return new FKTLine(fktValues[0], fktValues[1]);
	}

	private RECLine parseRECLine(String esoLine)
	{
		String[] recLineArray = esoLine.split("\\+");
		return new RECLine(recLineArray[1].split(":")[0], recLineArray[2]);
	}

	@Override
	public void writeToSeconStream(SeconCallable<OutputStream> seconStream) throws IOException, SeconException
	{
		try (var inputStream = new ByteArrayInputStream(decodedData))
		{
			SECON.copy(() -> inputStream, seconStream);
		}
	}

	public String getAbsenderIK()
	{
		return absenderIK;
	}

	public String getEmpfaengerIK()
	{
		return empfaengerIK;
	}

	public LocalDateTime getInvoiceDateTime()
	{
		return invoiceDateTime;
	}

	public InvoiceInformation getInvoiceInformation(String belegNummer)
	{
		return invoiceInformation.get(belegNummer);
	}

	public String getLogischerDateiname()
	{
		return logischerDateiname;
	}

	public int getLengthOfEsol()
	{
		return decodedData.length;
	}

	public String getReferenceNumber()
	{
		return referenceNumber;
	}
}
