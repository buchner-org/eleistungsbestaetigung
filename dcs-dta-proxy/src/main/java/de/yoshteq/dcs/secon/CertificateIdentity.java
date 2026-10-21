package de.yoshteq.dcs.secon;

import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import de.tk.opensource.secon.Identity;
import de.yoshteq.dcs.DcsRuntimeException;

@ApplicationScoped
public class CertificateIdentity implements Identity
{

	private final String dscKeyStoreFileString;

	private final String dscKeyStorePassword;
	
	private KeyStore keyStore;

	private X509Certificate dcsCertificate;

	private PrivateKeyEntry privateKeyEntry;

	public CertificateIdentity(
		@ConfigProperty(name = "dcs.keystore.file") String dscKeyStoreFileString,
		@ConfigProperty(name = "dcs.keystore.password") String dscKeyStorePassword)
	{
		this.dscKeyStoreFileString = dscKeyStoreFileString;
		this.dscKeyStorePassword = dscKeyStorePassword;
	}

	@PostConstruct
	public void init()
	{
		try
		{
			keyStore = KeyStore.getInstance(new File(dscKeyStoreFileString), dscKeyStorePassword.toCharArray());
			dcsCertificate = (X509Certificate) keyStore.getCertificate(keyStore.aliases().nextElement());
			privateKeyEntry = (PrivateKeyEntry) keyStore
				.getEntry(keyStore.aliases().nextElement(), new PasswordProtection(dscKeyStorePassword.toCharArray()));
		}
		catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException | UnrecoverableEntryException e)
		{
			throw new DcsRuntimeException("Could not init crypto", e);
		}
	}

	@Override
	public X509Certificate certificate() throws Exception
	{
		return dcsCertificate;
	}

	@Override
	public PrivateKey privateKey() throws Exception
	{
		return privateKeyEntry.getPrivateKey();
	}

	public PrivateKeyEntry getPrivateKeyEntry()
	{
		return privateKeyEntry;
	}
}
