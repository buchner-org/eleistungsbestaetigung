package de.yoshteq.dcs.signing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SignatureException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.xml.crypto.dsig.CanonicalizationMethod;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Signature;

import ca.uhn.fhir.context.FhirContext;
import de.yoshteq.dcs.secon.CertificateIdentity;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.spi.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.xades.XAdESSignatureParameters;
import eu.europa.esig.dss.xades.reference.CanonicalizationTransform;
import eu.europa.esig.dss.xades.reference.DSSReference;
import eu.europa.esig.dss.xades.reference.DSSTransform;
import eu.europa.esig.dss.xades.reference.XPath2FilterTransform;
import eu.europa.esig.dss.xades.signature.XAdESService;
import io.quarkus.runtime.Startup;

@Startup
@ApplicationScoped
public class SignatureService
{
	private final TimeStampingService timeStampingService;

	private final CertificateIdentity identity;

	private CertificateIdentityTokenConnection token;

	private DSSPrivateKeyEntry signatureKey;

	private XAdESService xadesService;

	public SignatureService(CertificateIdentity identity, TimeStampingService timeStampingService)
	{
		this.identity = identity;
		this.timeStampingService = timeStampingService;
	}

	@PostConstruct
	public void init()
	{
		loadSigningKey();
		initXAdESService();
	}

	private void loadSigningKey()
	{
		token = new CertificateIdentityTokenConnection(identity);
		signatureKey = token.getKey();
	}

	private void initXAdESService()
	{
		xadesService = new XAdESService(new CommonCertificateVerifier());
		xadesService.setTspSource(timeStampingService);
	}

	public Bundle sign(Bundle documentBundle) throws SignatureException
	{
		try
		{
			var bundleWithoutSignature = documentBundle.copy();
			bundleWithoutSignature.setSignature(null);
			var documentBytes = bundleToBytes(bundleWithoutSignature);
			var detachedSignatureBytes = createDetachedSignature(documentBytes);

			return createSignedBundle(documentBundle, detachedSignatureBytes);
		}
		catch (Exception e)
		{
			throw new SignatureException("Could not create Signature", e);
		}
	}

	private byte[] bundleToBytes(Bundle bundle) throws IOException
	{
		try (var boas = new ByteArrayOutputStream())
		{
			var xmlParser = FhirContext.forR4().newXmlParser();
			xmlParser.encodeResourceToWriter(bundle, new java.io.OutputStreamWriter(boas));
			return boas.toByteArray();
		}
	}

	private byte[] createDetachedSignature(byte[] documentBytes) throws IOException
	{
		var documentToSign = new InMemoryDocument(documentBytes, null, MimeTypeEnum.XML);
		var signatureParameters = createParametersForNodeId(documentToSign);

		var dataToSign = xadesService.getDataToSign(documentToSign, signatureParameters);
		var digestAlgorithm = signatureParameters.getDigestAlgorithm();
		var signatureValue = token.sign(dataToSign, digestAlgorithm, signatureKey);
		var detachtedSignatureDocument = xadesService.signDocument(documentToSign, signatureParameters, signatureValue);
		return toBytes(detachtedSignatureDocument);
	}

	private XAdESSignatureParameters createParametersForNodeId(DSSDocument documentToSign)
	{
		List<DSSReference> references = createReferences(documentToSign);

		XAdESSignatureParameters signatureParameters = new XAdESSignatureParameters();
		signatureParameters.setSigningCertificate(signatureKey.getCertificate());
		signatureParameters.setCertificateChain(signatureKey.getCertificateChain());
		signatureParameters.setSignaturePackaging(SignaturePackaging.DETACHED);
		signatureParameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_T);
		signatureParameters.setReferences(references);

		return signatureParameters;
	}

	private List<DSSReference> createReferences(DSSDocument documentToSign)
	{
		List<DSSTransform> transforms = createTransformations();

		List<DSSReference> references = new ArrayList<>();
		DSSReference dssReference = new DSSReference();
		dssReference.setContents(documentToSign);
		dssReference.setId("r-" + UUID.randomUUID().toString());
		dssReference.setTransforms(transforms);
		dssReference.setUri(documentToSign.getName());
		dssReference.setDigestMethodAlgorithm(DigestAlgorithm.SHA256);
		references.add(dssReference);
		return references;
	}

	private List<DSSTransform> createTransformations()
	{
		List<DSSTransform> transforms = new ArrayList<>();
		DSSTransform signatureSubstractTransform = new XPath2FilterTransform("//*[name()='ChargeItem']", "intersect");
		transforms.add(signatureSubstractTransform);
		DSSTransform canonicalization = new CanonicalizationTransform(CanonicalizationMethod.EXCLUSIVE);
		transforms.add(canonicalization);
		return transforms;
	}

	private byte[] toBytes(DSSDocument signedDocument) throws IOException
	{
		try (var inputStream = signedDocument.openStream())
		{
			return inputStream.readAllBytes();
		}
	}

	private Bundle createSignedBundle(Bundle documentBundle, byte[] detachedSignatureBytes)
	{
		Bundle signedBundle = (Bundle) documentBundle.copy();

		Signature signature = new Signature();
		signature.setWhen(java.util.Date.from(ZonedDateTime.now().toInstant()));

		Coding coding = new Coding();
		coding.setSystem("urn:iso-astm:E1762-95:2013");
		coding.setCode("1.2.840.10065.1.12.1.5");
		signature.addType(coding);

		Reference reference = new Reference();
		Identifier identifier = new Identifier();
		identifier.setSystem("http://fhir.de/sid/arge-ik/iknr");
		identifier.setValue(identity.getSignerIk().toString());
		reference.setIdentifier(identifier);
		signature.setWho(reference);

		signature.setData(detachedSignatureBytes);
		signature.setSigFormat("application/signature+xml");

		signedBundle.setSignature(signature);
		return signedBundle;
	}

}
