package de.yoshteq.dcs.api.billing;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tk.opensource.secon.SECON;
import de.tk.opensource.secon.SeconException;
import de.tk.opensource.secon.Subscriber;
import de.yoshteq.dcs.billing.BillingInvoice;
import de.yoshteq.dcs.fhir.api.OperationOutcomeResponse;
import de.yoshteq.dcs.secon.CertificateIdentity;
import de.yoshteq.dcs.secon.SeconDirectory;
import de.yoshteq.dcs.sftp.SFTPConnector;
import de.yoshteq.dcs.sftp.SFTPConnector.SftpFileTransferInformation;
import de.yoshteq.dcs.sftp.SftpClientCreator;
import de.yoshteq.diga.secon.SeconContainer;
import de.yoshteq.diga.secon.SeconMediaType;

@Path("/billing")
@Consumes(SeconMediaType.INNER_XML)
@Produces(SeconMediaType.INNER_XML)
public class BillingResource
{
	private static final Logger logger = LoggerFactory.getLogger(BillingResource.class);

	@Inject
	@ConfigProperty(name = "dcs.remote.sftp.saving-location")
	String savingLocation;

	@Inject
	@ConfigProperty(name = "dcs.remote.ik")
	String remoteIK;

	@Inject
	CertificateIdentity certificateIdentity;

	@Inject
	SeconDirectory certificateDirectory;

	@Inject
	SftpClientCreator sftpClientCreator;

	@POST
	@Transactional
	@Path("/submit-invoice")
	public SeconContainer<OperationOutcome> submitInvoice(SeconContainer<Parameters> seconRequest)
	{
		splitBillingInvoiceEncryptAndTransferToServer(seconRequest.getEntity());
		return createAllOkResponse(seconRequest.getIkSender());

	}

	void splitBillingInvoiceEncryptAndTransferToServer(Parameters parameters)
	{
		BillingInvoice billingInvoice = BillingInvoice.parseBilingInvoiceParameters(remoteIK, parameters);

		Subscriber subscriber = SECON.subscriber(certificateIdentity, certificateDirectory);

		try (var sftpConnector = sftpClientCreator.create())
		{
			SftpFileTransferInformation backupSftpFileInformation = billingInvoice.generateEncryptedBackupTransferInformation(subscriber);
			SftpFileTransferInformation esolSftpFileInformation = billingInvoice.generateEncryptedESOLTransferInformation(subscriber);
			SftpFileTransferInformation auftragsDateiSftpFileInformation = billingInvoice.generateAuftragsdateiTransferInformation();
			List<SftpFileTransferInformation> invoiceSftpFileInformation = billingInvoice.generateEncryptedInvoicesTransferInformations(subscriber);

			transferFileToSftpServer(sftpConnector, backupSftpFileInformation);
			transferFileToSftpServer(sftpConnector, esolSftpFileInformation);
			transferFileToSftpServer(sftpConnector, auftragsDateiSftpFileInformation);
			for (var sftpFileTransferInformation : invoiceSftpFileInformation)
			{
				transferFileToSftpServer(sftpConnector, sftpFileTransferInformation);
			}
		}
		catch (FileAlreadyExistsException e)
		{
			throw new WebApplicationException(Status.CONFLICT);
		}
		catch (IOException e)
		{
			logger.error("Failed to open file ", e);
			throw new InternalServerErrorException();
		}
		catch (SeconException e)
		{
			logger.error("Failed to encrypt the file using SECON ", e);
			throw new InternalServerErrorException();
		}
	}

	private void transferFileToSftpServer(SFTPConnector sftpConnector, SftpFileTransferInformation sftpFileTransferInformation) throws IOException
	{
		sftpConnector.transferFile(
			java.nio.file.Path.of(savingLocation, sftpFileTransferInformation.savingDestination().toString()),
			sftpFileTransferInformation.file());
	}

	private SeconContainer<OperationOutcome> createAllOkResponse(String ikEmpfaenger)
	{
		return SeconContainer.create(ikEmpfaenger, OperationOutcomeResponse.allOk());
	}
}
