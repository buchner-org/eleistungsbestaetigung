package de.yoshteq.dcs.secon;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.yoshteq.dcs.DcsRuntimeException;
import de.yoshteq.diga.secon.X509CertificateUtil;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;

@ApplicationScoped
class ItsgCertificateDownloadService
{
	private static final Logger logger = LoggerFactory.getLogger(ItsgCertificateDownloadService.class);

	@Inject
	@ConfigProperty(name = "dcs.trusted-certificates.url")
	String trustedCertificateUrlString;

	@Inject
	SeconDirectory seconDirectory;

	private CertificateFactory certificateFactory;

	@PostConstruct
	void init()
	{
		try
		{
			certificateFactory = CertificateFactory.getInstance("X.509");
		}
		catch (CertificateException e)
		{
			throw new DcsRuntimeException("Could not initialize needed crypto", e);
		}
	}

	void checkCertificatesOnStartUp(@Observes StartupEvent startupEvent)
	{
		var latestUpdate = seconDirectory.getLatestUpdateTime();
		if (latestUpdate.isEmpty() || latestUpdate.get().isBefore(OffsetDateTime.now().minusHours(24)))
		{
			logger.info("Trusted Certificates not available or too old");
			runScheduledDownloadTask();
		}
	}

	@Scheduled(cron = "{dcs.task.secon.certificate.download.cron}", identity = "secon.certificate-download")
	public void runScheduledDownloadTask()
	{
		var timestamp = OffsetDateTime.now();
		logger.info("Starting task to refresh Trusted Certificates from ITSG TrustCenter [{}]", timestamp);
		try
		{
			var url = URI.create(trustedCertificateUrlString).toURL();
			var certificateCounter = 0;

			try (var stream = url.openStream();
				var streamReader = new InputStreamReader(stream);
				BufferedReader reader = new BufferedReader(streamReader))
			{
				var cert = readNextCertificate(reader);
				while (cert != null)
				{
					storeCertificate(cert, timestamp);
					cert = readNextCertificate(reader);

					certificateCounter++;
				}
			}

			seconDirectory.clearExpiredCertificates(timestamp);

			logger.info("Donwloaded {} certificates", certificateCounter);
		}
		catch (IOException e)
		{
			logger.error("Could not connect to ITSG TrustCenter to download certificates", e);
		}
	}

	private void storeCertificate(X509Certificate cert, OffsetDateTime updateTimestamp)
	{
		try
		{
			var ik = X509CertificateUtil.extractIkAsInteger(cert);
			if (ik.isPresent())
			{
				var trustedCertificateEntity = new TrustedCertificateEntity(
					cert.getSerialNumber(),
					ik.get(),
					cert.getSubjectX500Principal().getName(),
					cert.getNotBefore().toInstant().atOffset(ZoneOffset.UTC),
					cert.getEncoded(),
					updateTimestamp);
				seconDirectory.store(trustedCertificateEntity);
			}
		}
		catch (CertificateEncodingException e)
		{
			logger.error("Could not connect to ITSG TrustCenter to download certificates", e);
		}
	}

	private X509Certificate readNextCertificate(BufferedReader reader)
	{
		try
		{
			var buffer = new StringBuilder();
			var line = reader.readLine();
			while ((line != null) && (line.length() > 0))
			{
				buffer.append(line);
				line = reader.readLine();
			}

			if (buffer.length() == 0)
			{
				return null;
			}

			var decodedBytes = Base64.getDecoder().decode(buffer.toString());
			ByteArrayInputStream bais = new ByteArrayInputStream(decodedBytes);
			return (X509Certificate) certificateFactory.generateCertificate(bais);
		}
		catch (IOException | CertificateException e)
		{
			logger.error("Could not read certificate from base64 stream", e);
			return null;
		}
	}

}
