package de.yoshteq.dcs.api.test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import de.yoshteq.dcs.FhirDateTimeFormatter;
import de.yoshteq.dcs.confirmation.ConfirmationRequest;
import de.yoshteq.dcs.confirmation.ConfirmationRequestState;
import de.yoshteq.dcs.confirmation.ConfirmationResponseType;
import de.yoshteq.dcs.confirmation.ConfirmationService;
import de.yoshteq.dcs.confirmation.ServiceType;
import de.yoshteq.dcs.insurant.InsurantPseudonymGenerator;
import de.yoshteq.dcs.insurant.InsurantService;

@Path("/test-api")
@Tag(name = "Test API", description = "Schnittstellen zum generieren von Testdaten mittels Authentifizierung über Benutzer und Passwort")
@SecurityRequirement(name = "Basic")
public class TestResource implements TestApi
{

	@Inject
	EntityManager entityManager;

	@Inject
	InsurantService insurantService;

	@Inject
	ConfirmationService confirmationService;

	@Inject
	InsurantPseudonymGenerator pseudonymGenerator;

	@Override
	@Transactional
	public Long createConfirmationRequest(TestConfirmationRequest request)
	{
		if (!request.kvnr().matches("[A-Z]\\d{9}"))
		{
			throw new NotFoundException("invalid kvnr ( must match: [A-Z]{1}[0-9]{9} )");
		}

		if (!insurantService.hasInsurantActiveRegistration(request.kvnr()))
		{
			throw new NotFoundException("insurant not found or has no active registration");
		}

		var insurant = insurantService.getInsurant(request.kvnr())
			.orElseThrow(() -> new NotFoundException("insurant not found"));

		var type = request.serviceTypeCode() == null
			? ServiceType.HEBAMME
			: ServiceType.from(request.serviceTypeCode());
		if (!type.isValidServiceCode(request.serviceCode()))
		{
			throw new BadRequestException("invalid service code or no need for confirming services with this code");
		}

		var confirmationRequest = new ConfirmationRequest();
		confirmationRequest.setInsurant(insurant);
		confirmationRequest.setKvnr(request.kvnr().toUpperCase());
		confirmationRequest.setKostentraegerIK(request.kostentraegerIk());
		confirmationRequest.setState(ConfirmationRequestState.PENDING);
		confirmationRequest.setSenderIK(123456780);
		confirmationRequest.setRequestDateTime(OffsetDateTime.now());
		confirmationRequest.setResponseType(ConfirmationResponseType.POLLING);
		confirmationRequest.setSoftwareIdentifier("dcs-test-api");
		confirmationRequest.setContactMailAddress("info@yoshteq.de");
		confirmationRequest.setUuid(UUID.randomUUID().toString());
		confirmationRequest.setServiceType(type);
		confirmationRequest.setPolicyVersion(1);
		confirmationRequest.setCode(request.serviceCode());
		confirmationRequest.setOccurrenceDateTime(fromString(request.occurrenceDateTime()));
		confirmationRequest.setOccurrenceStartDateTime(fromString(request.occurrenceStartDateTime()));
		confirmationRequest.setOccurrenceEndDateTime(fromString(request.occurrenceEndDateTime()));
		confirmationRequest.setPerformerOrganizationIK(request.performerOrganizationIK().intValue());
		confirmationRequest.setPerformerOrganizationDisplay(request.performerOrganizationDisplay());
		confirmationRequest.setPerformerPractitionerIdentifier(request.performerPractitionerIdentifier());
		confirmationRequest.setPerformerPractitionerDisplay(request.performerPractitionerDisplay());

		confirmationService.createConfirmationRequest(Arrays.asList(confirmationRequest));
		return confirmationRequest.getId();
	}

	private TemporalAccessor fromString(String stringValue)
	{
		if (stringValue == null)
		{
			return null;
		}

		return FhirDateTimeFormatter.PARSER_FORMATTER.parseBest(stringValue, ZonedDateTime::from, LocalDate::from, YearMonth::from, Year::from);
	}

	@Override
	public List<ConfirmationRequestSummary> getConfirmationRequests()
	{
		return entityManager.createQuery("SELECT C FROM ConfirmationRequest C", ConfirmationRequest.class)
			.setMaxResults(100)
			.getResultStream()
			.map(ConfirmationRequestSummaryMapper::map)
			.toList();
	}

	@Override
	public ConfirmationRequestSummary getConfirmationRequest(@PathParam("id") Long id)
	{
		return ConfirmationRequestSummaryMapper.map(entityManager.find(ConfirmationRequest.class, id));
	}

	@Override
	@Transactional
	public void deleteConfirmationRequest(@PathParam("id") Long id)
	{
		var entity = entityManager.find(ConfirmationRequest.class, id);
		entityManager.remove(entity);
	}

	@Override
	public List<TestConfirmationRequest> createConfirmationRequest(TestDataServiceType testDataServiceType, TestDataGenerationRequest request)
	{
		var serviceType = ServiceType.valueOf(testDataServiceType.toString());
		return new TestDataGenerator(serviceType, request).createRequests();
	}
}
