package de.yoshteq.dcs.signing;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.hl7.fhir.r4.model.Bundle;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import de.yoshteq.dcs.secon.CertificateIdentity;

class SignatureServiceTest
{

	@Test
	void testBundleSigning() throws Exception
	{
		var signatureService = setupSignatureService();
		FhirContext fhirContext = FhirContext.forR4();
		IParser xmlParser = fhirContext.newXmlParser();
		Bundle bundle = xmlParser.parseResource(Bundle.class, Files.newInputStream(Paths.get("src/test/resources/signing/bundle.xml")));

		Bundle signedBundle = signatureService.sign(bundle);

		assertNotNull(signedBundle.getSignature().getSigFormat());
		assertEquals("application/signature+xml", signedBundle.getSignature().getSigFormat());
		assertNotNull(signedBundle.getSignature().getData());
		assertTrue(signedBundle.getSignature().getData().length > 0);

		System.out.println();
		xmlParser.setPrettyPrint(false);
		xmlParser.encodeResourceToWriter(signedBundle, new java.io.OutputStreamWriter(System.out));
	}

	private SignatureService setupSignatureService()
	{
		var identity = prepareIdentity();

		var timeStampingService = new TimeStampingService();
		timeStampingService.tspKeyStoreFile = Optional.empty();
		timeStampingService.tspKeyStorePassword = Optional.empty();
		timeStampingService.certificateIdentity = identity;
		timeStampingService.init();

		var signatureService = new SignatureService(identity, timeStampingService);
		signatureService.init();

		return signatureService;
	}

	private CertificateIdentity prepareIdentity()
	{
		var identity = new CertificateIdentity("src/test/resources/signing/keystore.jks", "1234");
		identity.init();
		return identity;
	}
}
