package de.yoshteq.dcs.fhir.model;

import java.time.temporal.TemporalAccessor;

import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Element;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Type;

import de.yoshteq.dcs.fhir.DateTimeTypeConverter;
import de.yoshteq.dcs.fhir.FhirValidationException;

public class Occurrence {

	public static Occurrence create(TemporalAccessor occurenceTemporalAccessor) {
		return new Occurrence(occurenceTemporalAccessor);
	}

	public static Occurrence create(TemporalAccessor occurrenceStartDateTime, TemporalAccessor occurenceEndDateTime) {
		return new Occurrence(occurrenceStartDateTime, occurenceEndDateTime);
	}

	public static Occurrence from(Element occurance) {
		return new Occurrence(occurance);
	}

	private TemporalAccessor dateTime;

	private TemporalAccessor startDateTime;

	private TemporalAccessor endDateTime;

	private Occurrence(TemporalAccessor occurenceDateTime) {
		this.dateTime = occurenceDateTime;
	}

	private Occurrence(TemporalAccessor occurrenceStartDateTime, TemporalAccessor occurenceEndDateTime) {
		this.startDateTime = occurrenceStartDateTime;
		this.endDateTime = occurenceEndDateTime;
	}

	private Occurrence(Element occurance) {
		switch (occurance) {
			case DateTimeType dateTimeType -> dateTime = DateTimeTypeConverter.convertDateTimeTypePrecision(dateTimeType);
			case Period period -> {
				startDateTime = DateTimeTypeConverter.convertDateTimeTypePrecision(period.getStartElement());
				endDateTime = DateTimeTypeConverter.convertDateTimeTypePrecision(period.getEndElement());
			}
			default -> throw new FhirValidationException("unknown occurance format");
		}
	}

	public Type toOccurance() {
		if (dateTime != null) {
			return DateTimeTypeConverter.temporalAccessorToDateTimeType(dateTime);
		} else if (startDateTime != null && endDateTime != null) {
			var startDateTimeType = DateTimeTypeConverter.temporalAccessorToDateTimeType(startDateTime);
			var endDateTimeType = DateTimeTypeConverter.temporalAccessorToDateTimeType(endDateTime);
			return new Period()
					.setStartElement(startDateTimeType)
					.setEndElement(endDateTimeType);
		} else {
			throw new FhirValidationException("");
		}
	}

	
	public TemporalAccessor getDateTime() {
		return dateTime;
	}

	public TemporalAccessor getStartDateTime() {
		return startDateTime;
	}

	public TemporalAccessor getEndDateTime() {
		return endDateTime;
	}

}
