package de.yoshteq.dcs.signing;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.DefaultCMSSignatureAlgorithmNameGenerator;
import org.bouncycastle.cms.SignerInfoGenerator;
import org.bouncycastle.cms.SignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.tsp.TSPAlgorithms;
import org.bouncycastle.tsp.TimeStampRequestGenerator;
import org.bouncycastle.tsp.TimeStampResponse;
import org.bouncycastle.tsp.TimeStampResponseGenerator;
import org.bouncycastle.tsp.TimeStampToken;
import org.bouncycastle.tsp.TimeStampTokenGenerator;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import de.yoshteq.dcs.DcsRuntimeException;
import de.yoshteq.dcs.secon.CertificateIdentity;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.model.TimestampBinary;
import eu.europa.esig.dss.spi.DSSASN1Utils;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;

@ApplicationScoped
public class TimeStampingService implements TSPSource
{
	private static final long serialVersionUID = 4939265774941498157L;

	public static TimeStampingService init(
		Optional<String> tspKeyStoreFile,
		Optional<String> tspKeyStorePassword,
		CertificateIdentity certificateIdentity)
	{
		var ts = new TimeStampingService();
		ts.tspKeyStoreFile = tspKeyStoreFile;
		ts.tspKeyStorePassword = tspKeyStorePassword;
		ts.certificateIdentity = certificateIdentity;

		ts.init();

		return ts;
	}

	@ConfigProperty(name = "dcs.signing-tsp.keystore")
	transient Optional<String> tspKeyStoreFile;

	@ConfigProperty(name = "dcs.signing-tsp.keystore-password")
	transient Optional<String> tspKeyStorePassword;

	@Inject
	CertificateIdentity certificateIdentity;

	private static Set<ASN1ObjectIdentifier> accepted = new HashSet<>();
	static
	{
		accepted.add(TSPAlgorithms.SHA1);
		accepted.add(TSPAlgorithms.SHA256);
		accepted.add(TSPAlgorithms.SHA384);
		accepted.add(TSPAlgorithms.SHA512);
	}

	private transient JcaCertStore certstore;

	private X509CertificateHolder certificate;

	private PrivateKey tspKey;

	@PostConstruct
	void init()
	{
		try
		{
			Security.addProvider(new BouncyCastleProvider());

			PrivateKeyEntry entry;
			if (tspKeyStoreFile.isPresent() && tspKeyStorePassword.isPresent())
			{
				KeyStore keyStore = KeyStore.getInstance(new File(tspKeyStoreFile.get()), tspKeyStorePassword.get().toCharArray());
				entry = (PrivateKeyEntry) keyStore
					.getEntry(keyStore.aliases().nextElement(), new PasswordProtection(tspKeyStorePassword.get().toCharArray()));
			}
			else
			{
				entry = certificateIdentity.getPrivateKeyEntry();
			}

			certificate = new X509CertificateHolder(entry.getCertificate().getEncoded());
			tspKey = entry.getPrivateKey();
			certstore = new JcaCertStore(Arrays.asList(entry.getCertificateChain()));
		}
		catch (KeyStoreException
			| NoSuchAlgorithmException
			| CertificateException
			| IOException
			| UnrecoverableEntryException
			| IllegalArgumentException e)
		{
			throw new DcsRuntimeException("Could not init crypto", e);
		}

	}

	@Override
	public TimestampBinary getTimeStampResponse(DigestAlgorithm digestAlgorithm, byte[] digest) throws DSSException
	{
		try
		{
			var requestGenerator = new TimeStampRequestGenerator();
			requestGenerator.setCertReq(true);

			var asn1ObjectIdentifier = new ASN1ObjectIdentifier(digestAlgorithm.getOid());
			var request = requestGenerator.generate(asn1ObjectIdentifier, digest);

			AlgorithmIdentifier digestAlgorithmIdentifier = new AlgorithmIdentifier(new ASN1ObjectIdentifier(digestAlgorithm.getOid()));
			AlgorithmIdentifier encryptionAlg = new AlgorithmIdentifier(PKCSObjectIdentifiers.rsaEncryption);

			DefaultCMSSignatureAlgorithmNameGenerator sigAlgoGenerator = new DefaultCMSSignatureAlgorithmNameGenerator();
			String sigAlgoName = sigAlgoGenerator.getSignatureName(digestAlgorithmIdentifier, encryptionAlg);

			ContentSigner signer = new JcaContentSignerBuilder(sigAlgoName).build(tspKey);

			SignerInfoGenerator infoGenerator = new SignerInfoGeneratorBuilder(new BcDigestCalculatorProvider()).build(signer, certificate);
			DigestCalculator digestCalculator = new JcaDigestCalculatorProviderBuilder().build().get(digestAlgorithmIdentifier);

			TimeStampTokenGenerator tokenGenerator = new TimeStampTokenGenerator(infoGenerator, digestCalculator, new ASN1ObjectIdentifier("1.2.3.4"));
			tokenGenerator.addCertificates(certstore);

			TimeStampResponseGenerator responseGenerator = new TimeStampResponseGenerator(tokenGenerator, accepted);
			TimeStampResponse response = responseGenerator
				.generate(request, new BigInteger(UUID.randomUUID().toString().replace("-", ""), 16), new Date());
			TimeStampToken timeStampToken = response.getTimeStampToken();

			return new TimestampBinary(DSSASN1Utils.getDEREncoded(timeStampToken));
		}
		catch (Exception e)
		{
			throw new DSSException("could not generate timestamp", e);
		}
	}

}
