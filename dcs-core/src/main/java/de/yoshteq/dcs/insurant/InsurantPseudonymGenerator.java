package de.yoshteq.dcs.insurant;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import de.yoshteq.dcs.DcsRuntimeException;

@ApplicationScoped
public class InsurantPseudonymGenerator
{

	@Inject
	@ConfigProperty(name = "dcs.kvnr-pseudonym-secret")
	String secret;

	private HmacUtils hmacUtil;

	@PostConstruct
	void init()
	{
		hmacUtil = new HmacUtils(HmacAlgorithms.HMAC_SHA_384, secret);
	}

	public String from(String kvnrOrHash)
	{
		if (kvnrOrHash.length() == 10)
		{
			return fromKvnr(kvnrOrHash);
		}
		else if (kvnrOrHash.length() == 64)
		{
			return fromKvnrHash(kvnrOrHash);
		}
		else
		{
			throw new DcsRuntimeException("invalid value for kvnr or sha256 kvnr Hash: " + kvnrOrHash);
		}
	}

	public String fromKvnr(String kvnr)
	{
		return fromKvnrHash(DigestUtils.sha256Hex(kvnr.toUpperCase()));
	}

	public String fromKvnrHash(String kvnrSha256Hash)
	{
		return hmacUtil.hmacHex(kvnrSha256Hash.toLowerCase());
	}

}
