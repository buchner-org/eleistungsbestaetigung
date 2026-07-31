package de.yoshteq.dcs;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;

import jakarta.persistence.AttributeConverter;

public class FhirTemporalAccessorConverter implements AttributeConverter<TemporalAccessor, String>
{

	@Override
	public String convertToDatabaseColumn(TemporalAccessor attribute)
	{
		if (attribute == null)
		{
			return null;
		}

		return FhirDateTimeFormatter.PARSER_FORMATTER.format(attribute);
	}

	@Override
	public TemporalAccessor convertToEntityAttribute(String dbData)
	{
		if (dbData == null)
		{
			return null;
		}

		try
		{
			return FhirDateTimeFormatter.PARSER_FORMATTER.parseBest(dbData, ZonedDateTime::from, LocalDate::from, YearMonth::from, Year::from);
		}
		catch (Exception e)
		{
			return null;
		}
	}

}
