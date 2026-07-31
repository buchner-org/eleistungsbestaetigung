package de.yoshteq.dcs.fhir;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;

import org.hl7.fhir.r4.model.BaseDateTimeType;
import org.hl7.fhir.r4.model.DateTimeType;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;

public record DateTimeTypeConverter() {

    public static TemporalAccessor convertDateTimeTypePrecision(BaseDateTimeType dateTimeType) {
        if (dateTimeType == null) {
            return null;
        }

        var instant = dateTimeType.getValue().toInstant();
        var precision = dateTimeType.getPrecision();
        return switch (precision) {
            case YEAR -> Year.from(instant);
            case MONTH -> YearMonth.from(instant);
            case DAY -> {
                var zoneId = dateTimeType.getTimeZone() != null
                        ? dateTimeType.getTimeZone().toZoneId()
                        : ZoneId.systemDefault();
                yield LocalDate.from(instant.atZone(zoneId));
            }
            default -> convertDateTimeTypeWithTimePrecision(dateTimeType, instant);
        };
    }

    private static TemporalAccessor convertDateTimeTypeWithTimePrecision(BaseDateTimeType dateTimeType,
            Instant instant) {
        var valueAsString = dateTimeType.getValueAsString();

        if (valueAsString.contains("+")) {
            return OffsetDateTime.parse(valueAsString);
        } else if (dateTimeType.getTimeZone() != null) {
            return ZonedDateTime.parse(valueAsString);
        } else {
            return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
        }
    }

    public static DateTimeType temporalAccessorToDateTimeType(TemporalAccessor temporal) {
        if (temporal == null) {
            return null;
        }

        return switch (temporal) {
            case Year year -> dateTimeTypeFromLocalDate(year.atDay(1), TemporalPrecisionEnum.YEAR);
            case YearMonth yearMonth -> dateTimeTypeFromLocalDate(yearMonth.atDay(1), TemporalPrecisionEnum.MONTH);
            case LocalDate localDate -> dateTimeTypeFromLocalDate(localDate, TemporalPrecisionEnum.DAY);
            case LocalDateTime localDateTime -> dateTimeTypeFromZonedDateTime(localDateTime.atZone(ZoneId.of("UTC")));
            case OffsetDateTime offsetDateTime -> dateTimeTypeFromOffsetDateTime(offsetDateTime);
            case ZonedDateTime zonedDateTime -> dateTimeTypeFromZonedDateTime(zonedDateTime);
            case Instant instant -> dateTimeTypeFromZonedDateTime(instant.atZone(ZoneId.of("UTC")));
            default -> throw new FhirValidationException(
                    "Unsupported TemporalAccessor type: " + temporal.getClass().getName());
        };
    }

    private static DateTimeType dateTimeTypeFromLocalDate(LocalDate localDate, TemporalPrecisionEnum precision) {
        var zoneId = ZoneId.of("UTC");
        var date = Date.from(localDate.atStartOfDay(zoneId).toInstant());
        return new DateTimeType(date, precision);
    }

    private static DateTimeType dateTimeTypeFromZonedDateTime(ZonedDateTime zonedDateTime) {
        return new DateTimeType(zonedDateTime.toOffsetDateTime().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    }

    private static DateTimeType dateTimeTypeFromOffsetDateTime(OffsetDateTime offsetDateTime) {
        var stringValue = offsetDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        if (stringValue.endsWith("Z")) {
            stringValue = stringValue.substring(0, stringValue.length() - 1) + "+00:00";
        }
        return new DateTimeType(stringValue);
    }

}
