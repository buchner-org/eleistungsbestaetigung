package de.yoshteq.dcs.billing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;

import de.tk.opensource.secon.SeconCallable;
import de.tk.opensource.secon.SeconException;
import de.tk.opensource.secon.Subscriber;
import de.yoshteq.dcs.api.BillingEndpointException;
import de.yoshteq.dcs.billing.ESOLParameter.InvoiceInformation;
import de.yoshteq.dcs.fhir.FhirHelper;
import de.yoshteq.dcs.sftp.SFTPConnector.SftpFileTransferInformation;

public class BillingInvoice
{
	protected interface BillingInvoiceParameter
	{
		public void writeToSeconStream(SeconCallable<OutputStream> seconStream)
			throws IOException,
			SeconException;
	}

	public static BillingInvoice parseBilingInvoiceParameters(String ikToEncryptFor, Parameters parameters)
	{
		BillingInvoice billingInvoice = new BillingInvoice(ikToEncryptFor, parameters);

		List<ParametersParameterComponent> parameterList = parameters.getParameter();
		for (var parameter : parameterList)
		{
			if (ESOLParameter.isESOLParameter(parameter))
			{
				billingInvoice.esolParameter = ESOLParameter.parseESOLParameter(parameter);
			}
			else if (InvoiceParameter.isInvoiceParameter(parameter))
			{
				billingInvoice.invoices.add(InvoiceParameter.parseInvoiceParameter(parameter));
			}
			else if ("emailContactPoint".equals(parameter.getName()))
			{
				billingInvoice.email = FhirHelper
					.assertElementType(parameter.getValue(), ContactPoint.class, "emailContactPoint must be a ContactPoint")
					.getValue();
			}
		}

		billingInvoice.edifactInvoiceNumber = getFinalThreeDigits(billingInvoice.esolParameter.getReferenceNumber());

		return billingInvoice;
	}

	private static String getFinalThreeDigits(String value)
	{
		if (value == null)
		{
			return null;
		}
		var padded = StringUtils.leftPad(value, 3, "0");
		return padded.substring(padded.length() - 3, padded.length());

	}

	private final String ikToEncryptFor;

	private final Parameters parameters;

	private String email;

	private String edifactInvoiceNumber;

	private ESOLParameter esolParameter;

	private List<InvoiceParameter> invoices = new ArrayList<>();

	private BillingInvoice(String ikToEncryptFor, Parameters parameters)
	{
		super();
		this.ikToEncryptFor = ikToEncryptFor;
		this.parameters = parameters;
	}

	public SftpFileTransferInformation generateEncryptedBackupTransferInformation(Subscriber subscriber)
		throws SeconException,
		IOException
	{
		var backupParameters = BackupParameter.parseInvoiceParameter(parameters);
		byte[] encryptedFile = generateEncryptedFileForParameter(subscriber, backupParameters);
		var savingDestination = Path
			.of("Backup", esolParameter.getAbsenderIK(), LocalDate.now().toString() + "_ESOL0" + edifactInvoiceNumber + ".elbx");

		return new SftpFileTransferInformation(savingDestination, encryptedFile);
	}

	public SftpFileTransferInformation generateEncryptedESOLTransferInformation(Subscriber subscriber)
		throws IOException,
		SeconException
	{
		byte[] encryptedFile = generateEncryptedFileForParameter(subscriber, esolParameter);
		var savingDestination = Path.of("ESOL", esolParameter.getAbsenderIK() + "_ESOL0" + edifactInvoiceNumber);

		return new SftpFileTransferInformation(savingDestination, encryptedFile);
	}

	public List<SftpFileTransferInformation> generateEncryptedInvoicesTransferInformations(Subscriber subscriber)
		throws SeconException,
		IOException
	{
		List<SftpFileTransferInformation> encryptedFiles = new ArrayList<>();
		for (InvoiceParameter invoice : invoices)
		{
			byte[] encryptedFile = generateEncryptedFileForParameter(subscriber, invoice);
			String invoiceNumber = invoice.getInvoiceReferenceName();
			InvoiceInformation invoiceInformation = esolParameter.getInvoiceInformation(invoiceNumber);
			if (invoiceInformation == null)
			{
				throw new BillingEndpointException("Invoice not present in ESOL file", IssueType.BUSINESSRULE);
			}
			Path savingDestination = Path.of("eLBx", buildInvoiceFilename(invoiceInformation));

			encryptedFiles.add(new SftpFileTransferInformation(savingDestination, encryptedFile));
		}
		return encryptedFiles;
	}

	private String buildInvoiceFilename(InvoiceInformation invoiceInformation)
	{
		return invoiceInformation.leistungserbringerIK + "_" + invoiceInformation.number.replace('/', '-') + "_"
			+ invoiceInformation.date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".elbx";
	}

	private byte[] generateEncryptedFileForParameter(Subscriber subscriber, BillingInvoiceParameter parameter)
		throws SeconException,
		IOException
	{
		try (ByteArrayOutputStream out = new ByteArrayOutputStream())
		{
			SeconCallable<OutputStream> seconStream = subscriber.signAndEncryptTo(() -> out, ikToEncryptFor);
			parameter.writeToSeconStream(seconStream);
			return out.toByteArray();
		}
	}

	public SftpFileTransferInformation generateAuftragsdateiTransferInformation()
	{
		byte[] file = buildAuftragsdatei().getBytes(StandardCharsets.ISO_8859_1);
		Path savingDestination = Path.of("ESOL", esolParameter.getAbsenderIK() + "_ESOL0" + edifactInvoiceNumber + ".AUF");

		return new SftpFileTransferInformation(savingDestination, file);
	}

	private String buildAuftragsdatei()
	{
		var ausgabe = new StringBuilder();
		ausgabe.append("500000"); // Identifikator
		ausgabe.append("01"); // Version
		ausgabe.append("00000348"); // Laenge Auftrag
		ausgabe.append("000"); // SequenzNummer
		ausgabe.append("ESOL0"); // Verfahrenkennung
		ausgabe.append(edifactInvoiceNumber);
		ausgabe.append("     "); // VerfahrenkennungSpezifikation
		ausgabe.append(StringUtils.rightPad(esolParameter.getAbsenderIK(), 15, ' '));
		ausgabe.append(StringUtils.rightPad(esolParameter.getAbsenderIK(), 15, ' '));
		ausgabe.append(StringUtils.rightPad(esolParameter.getEmpfaengerIK(), 15, ' '));
		ausgabe.append(StringUtils.rightPad(esolParameter.getEmpfaengerIK(), 15, ' '));
		ausgabe.append("000000"); // FehlerNummer
		ausgabe.append("000000"); // FehlerMassnahme
		ausgabe.append(esolParameter.getLogischerDateiname()); // Dateiname
		ausgabe.append(esolParameter.getInvoiceDateTime().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
		ausgabe.append("00000000000000"); // Datum_Gesendet
		ausgabe.append("00000000000000"); // Datum_EmpfangenStart
		ausgabe.append("00000000000000"); // Datum_EmpfangenEnde
		ausgabe.append("000000"); // Dateiversion
		ausgabe.append("0"); // Korrektur

		ausgabe.append(String.format("%012d", esolParameter.getLengthOfEsol())); // 12 digits of esol Parameter
		ausgabe.append("000000000000");
		ausgabe.append("I1"); // Zeichensatz
		ausgabe.append("00"); // Komprimierung
		ausgabe.append("0303"); // Verschluesselungsart
		ausgabe.append("   "); // Satzformat
		ausgabe.append("00000"); // Satzlaenge
		ausgabe.append("00000000"); // Blocklaenge
		ausgabe.append("0"); // Status
		ausgabe.append("00"); // Wiederholung
		ausgabe.append("0"); // Uebertragungsweg
		ausgabe.append("0000000000"); // Verzoegerter Versand
		ausgabe.append("000000"); // Info
		ausgabe.append("                            "); // Infofeld
		ausgabe.append(email); // email absender
		while (ausgabe.length() < 348)
		{
			ausgabe.append(" ");
		}
		return ausgabe.toString();
	}
}
