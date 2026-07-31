package de.yoshteq.dcs;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

public class FhirDateTimeFormatter {
	public static final DateTimeFormatter PARSER_FORMATTER = new DateTimeFormatterBuilder().appendPattern("yyyy")
			.optionalStart().appendPattern("-MM").optionalStart().appendPattern("-dd").optionalStart()
			.appendPattern("'T'HH:mm:ss").optionalStart().appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
			.optionalEnd().appendPattern("XXX").optionalEnd().optionalEnd().optionalEnd().toFormatter();

	private FhirDateTimeFormatter() {
	}
}
