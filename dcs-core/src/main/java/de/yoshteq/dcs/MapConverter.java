package de.yoshteq.dcs;

import java.util.HashMap;
import java.util.Map;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

@Converter
public class MapConverter implements AttributeConverter<Map<String, Object>, String>
{

	private static final Logger logger = LoggerFactory.getLogger(MapConverter.class);

	@Override
	public String convertToDatabaseColumn(Map<String, Object> attribute)
	{
		if (attribute == null)
		{
			return null;
		}

		try
		{
			var objectMapper = CDI.current().select(ObjectMapperHolder.class).get().getObjectMapper();
			return objectMapper.writeValueAsString(attribute);
		}
		catch (JsonProcessingException e)
		{
			logger.error("Cannot Serialize Map attribute", e);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> convertToEntityAttribute(String dbData)
	{
		if (dbData == null)
		{
			return new HashMap<>();
		}
		try
		{
			var objectMapper = CDI.current().select(ObjectMapperHolder.class).get().getObjectMapper();
			return objectMapper.readValue(dbData, Map.class);
		}
		catch (JsonProcessingException e)
		{
			logger.error("Cannot Serialize Map attribute", e);
			return new HashMap<>();
		}
	}

}
