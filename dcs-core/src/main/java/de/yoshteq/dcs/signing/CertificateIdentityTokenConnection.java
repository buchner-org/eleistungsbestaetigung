package de.yoshteq.dcs.signing;

import java.util.Arrays;
import java.util.List;

import de.yoshteq.dcs.secon.CertificateIdentity;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.token.AbstractSignatureTokenConnection;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.KSPrivateKeyEntry;

public class CertificateIdentityTokenConnection extends AbstractSignatureTokenConnection
{

	private KSPrivateKeyEntry keyEntry;
	
	public CertificateIdentityTokenConnection(CertificateIdentity identity)
	{
		keyEntry = new KSPrivateKeyEntry("signing_key", identity.getPrivateKeyEntry());
	}

	@Override
	public void close()
	{
		keyEntry = null;
	}

	@Override
	public List<DSSPrivateKeyEntry> getKeys() throws DSSException
	{
		return Arrays.asList(keyEntry);
	}
	
	public DSSPrivateKeyEntry getKey() {
		return keyEntry;
	}

}
