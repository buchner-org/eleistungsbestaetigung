package de.yoshteq.dcs.api.test;

import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import de.yoshteq.dcs.confirmation.ServiceType;

public class TestDataGenerator
{

	private record ServiceTestParameters(String serviceTypeCode, String serviceNumber, boolean needsStartDateTime, boolean needsEndDateTime)
	{

	}

	private static final List<ServiceTestParameters> MIDWIFE_TEST_SERVICES = Arrays.asList(
		new ServiceTestParameters("F", "0200", true, true),
		new ServiceTestParameters("F", "0230", true, true),
		new ServiceTestParameters("F", "0240", true, true),
		new ServiceTestParameters("F", "0270", true, true),
		new ServiceTestParameters("F", "0280", true, true),
		new ServiceTestParameters("F", "0290", true, true),
		new ServiceTestParameters("F", "0300", true, true),
		new ServiceTestParameters("F", "0400", true, false),
		new ServiceTestParameters("F", "0500", true, true),
		new ServiceTestParameters("F", "0501", true, true),
		new ServiceTestParameters("F", "0502", true, true),
		new ServiceTestParameters("F", "0510", true, true),
		new ServiceTestParameters("F", "0511", true, true),
		new ServiceTestParameters("F", "0512", true, true),
		new ServiceTestParameters("F", "0570", true, true),
		new ServiceTestParameters("F", "0580", true, true),
		new ServiceTestParameters("F", "0600", true, false),
		new ServiceTestParameters("F", "0601", true, false),
		new ServiceTestParameters("F", "0602", true, false),
		new ServiceTestParameters("F", "0700", true, true),
		new ServiceTestParameters("F", "0770", true, true),
		new ServiceTestParameters("F", "0800", true, true),
		new ServiceTestParameters("F", "0830", true, true),
		new ServiceTestParameters("F", "0901", true, true),
		new ServiceTestParameters("F", "0902", true, true),
		new ServiceTestParameters("F", "0911", true, false),
		new ServiceTestParameters("F", "0912", true, false),
		new ServiceTestParameters("F", "1000", true, true),
		new ServiceTestParameters("F", "1010", true, false),
		new ServiceTestParameters("F", "1100", true, true),
		new ServiceTestParameters("F", "1110", true, false),
		new ServiceTestParameters("F", "1200", true, true),
		new ServiceTestParameters("F", "1210", true, false),
		new ServiceTestParameters("F", "1300", true, true),
		new ServiceTestParameters("F", "1301", true, true),
		new ServiceTestParameters("F", "1302", true, true),
		new ServiceTestParameters("F", "1310", true, false),
		new ServiceTestParameters("F", "1311", true, false),
		new ServiceTestParameters("F", "1312", true, false),
		new ServiceTestParameters("F", "1600", true, true),
		new ServiceTestParameters("F", "1601", true, true),
		new ServiceTestParameters("F", "1602", true, true),
		new ServiceTestParameters("F", "1610", true, true),
		new ServiceTestParameters("F", "1611", true, true),
		new ServiceTestParameters("F", "1612", true, true),
		new ServiceTestParameters("F", "1700", true, true),
		new ServiceTestParameters("F", "1701", true, true),
		new ServiceTestParameters("F", "1702", true, true),
		new ServiceTestParameters("F", "1710", true, true),
		new ServiceTestParameters("F", "1711", true, true),
		new ServiceTestParameters("F", "1712", true, true),
		new ServiceTestParameters("F", "1800", true, true),
		new ServiceTestParameters("F", "1810", true, true),
		new ServiceTestParameters("F", "1830", true, true),
		new ServiceTestParameters("F", "1850", true, true),
		new ServiceTestParameters("F", "2001", true, true),
		new ServiceTestParameters("F", "2002", true, true),
		new ServiceTestParameters("F", "2011", true, true),
		new ServiceTestParameters("F", "2012", true, true),
		new ServiceTestParameters("F", "2100", true, true),
		new ServiceTestParameters("F", "2110", true, true),
		new ServiceTestParameters("F", "2370", true, true),
		new ServiceTestParameters("F", "2380", true, true),
		new ServiceTestParameters("F", "2400", false, false),
		new ServiceTestParameters("F", "2401", false, false),
		new ServiceTestParameters("F", "2402", false, false),
		new ServiceTestParameters("F", "2500", false, false),
		new ServiceTestParameters("F", "2501", false, false),
		new ServiceTestParameters("F", "2502", false, false),
		new ServiceTestParameters("F", "2600", true, true),
		new ServiceTestParameters("F", "2601", true, true),
		new ServiceTestParameters("F", "2602", true, true),
		new ServiceTestParameters("F", "2610", true, true),
		new ServiceTestParameters("F", "2611", true, true),
		new ServiceTestParameters("F", "2612", true, true),
		new ServiceTestParameters("F", "2630", true, true),
		new ServiceTestParameters("F", "2650", true, true),
		new ServiceTestParameters("F", "2670", true, false),
		new ServiceTestParameters("F", "2671", true, false),
		new ServiceTestParameters("F", "2672", true, false),
		new ServiceTestParameters("F", "2700", true, true),
		new ServiceTestParameters("F", "2730", true, true),
		new ServiceTestParameters("F", "2770", true, true),
		new ServiceTestParameters("F", "2800", true, true),
		new ServiceTestParameters("F", "2810", true, true),
		new ServiceTestParameters("F", "2870", true, true),
		new ServiceTestParameters("F", "2880", true, true),
		new ServiceTestParameters("F", "3910", false, false),
		new ServiceTestParameters("F", "3920", false, false));

	private TestDataGenerationRequest request;

	private ServiceType type;

	public TestDataGenerator(ServiceType type, TestDataGenerationRequest request)
	{
		super();
		this.request = request;
		this.type = type;
	}

	public List<TestConfirmationRequest> createRequests()
	{
		switch (type)
		{
			case HEBAMME:
				return MIDWIFE_TEST_SERVICES.stream().map(this::map).flatMap(Collection::stream).toList();
			case HEILMITTEL:
				return iterateHeilmittel().map(this::map).flatMap(Collection::stream).toList();
			case HILFSMITTEL:
				return Collections.emptyList();
			case HKP:
				return iterateHkp().map(this::map).flatMap(Collection::stream).toList();
			default:
				throw new IllegalArgumentException("Unexpected value: " + type);
		}
	}

	private Stream<ServiceTestParameters> iterateHeilmittel()
	{
		return ServiceType.HEILMITTEL.serviceCodes().stream().map(c -> new ServiceTestParameters(ServiceType.HEILMITTEL.getCode(), c, false, false));
	}

	private Stream<ServiceTestParameters> iterateHkp()
	{
		return ServiceType.HKP.serviceCodes().stream().map(c -> new ServiceTestParameters(ServiceType.HKP.getCode(), "011" + c, false, false));
	}

	private List<TestConfirmationRequest> map(ServiceTestParameters parameters)
	{
		TemporalAccessor dateTime = null;
		var startDateTime = OffsetDateTime.now().minusMinutes(30);
		var endDateTime = OffsetDateTime.now().minusMinutes(10);

		if (parameters.needsStartDateTime && !parameters.needsEndDateTime)
		{
			dateTime = startDateTime;
			startDateTime = null;
			endDateTime = null;
		}
		else if (!parameters.needsStartDateTime && !parameters.needsEndDateTime)
		{
			dateTime = startDateTime.toLocalDate();
			startDateTime = null;
			endDateTime = null;
		}

		return Arrays
			.asList(
				create(parameters, dateTime, startDateTime, endDateTime, true),
				create(parameters, dateTime, startDateTime, endDateTime, false));
	}

	private TestConfirmationRequest create(
		ServiceTestParameters parameters,
		TemporalAccessor dateTime,
		OffsetDateTime startDateTime,
		OffsetDateTime endDateTime,
		boolean isEmployed)
	{
		return new TestConfirmationRequest(
			request.kostentraegerIk(),
			request.kvnr(),
			parameters.serviceTypeCode,
			parameters.serviceNumber,
			dateTime != null
				? dateTime.toString()
				: null,
			startDateTime != null
				? startDateTime.toString()
				: null,
			endDateTime != null
				? endDateTime.toString()
				: null,
			request.performerOrganizationIK(),
			request.performerOrganizationDisplay(),
			isEmployed
				? request.performerPractitionerIdentifier()
				: null,
			isEmployed
				? request.performerPractitionerDisplay()
				: null);
	}

}
