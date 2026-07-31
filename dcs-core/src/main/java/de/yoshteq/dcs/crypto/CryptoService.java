package de.yoshteq.dcs.crypto;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;

import de.yoshteq.dcs.DcsRuntimeException;

@ApplicationScoped
public class CryptoService
{

	@Inject
	@ConfigProperty(name = "dcs.crypt.aes-key")
	String cryptoKey;

	public String encryptToJwe(String value)
	{
		try
		{
			JWEHeader header = new JWEHeader(JWEAlgorithm.DIR, EncryptionMethod.A256GCM);
			Payload payload = new Payload(value);

			JWEObject jweObject = new JWEObject(header, payload);
			jweObject.encrypt(new DirectEncrypter(cryptoKey.getBytes(StandardCharsets.UTF_8)));

			return jweObject.serialize();
		}
		catch (JOSEException e)
		{
			throw new DcsRuntimeException("Could not encrypt value", e);
		}
	}

	public String decryptFromJwe(String jweValue)
	{
		try
		{
			var jweObject = JWEObject.parse(jweValue);
			jweObject.decrypt(new DirectDecrypter(cryptoKey.getBytes(StandardCharsets.UTF_8)));

			return jweObject.getPayload().toString();
		}
		catch (JOSEException | ParseException e)
		{
			throw new DcsRuntimeException("Could not decrypt value: " + jweValue, e);
		}
	}

}
