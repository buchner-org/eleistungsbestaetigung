package de.yoshteq.dcs;

import jakarta.inject.Inject;
import jakarta.persistence.AttributeConverter;
import jakarta.ws.rs.ext.Provider;

import de.yoshteq.dcs.crypto.CryptoService;

@Provider
public class EncryptedValueConverter implements AttributeConverter<String, String>
{
	
	@Inject
	CryptoService cryptoService;

	@Override
	public String convertToDatabaseColumn(String attribute)
	{
		if (attribute == null)
		{
			return null;
		}

		return cryptoService.encryptToJwe(attribute);
	}

	@Override
	public String convertToEntityAttribute(String dbData)
	{
		if (dbData == null)
		{
			return null;
		}

		return cryptoService.decryptFromJwe(dbData);
	}

}
