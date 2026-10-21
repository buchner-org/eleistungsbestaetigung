package de.yoshteq.dcs.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;
import org.hl7.fhir.r4.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.yoshteq.dcs.fhir.FhirValidationException;
import de.yoshteq.diga.secon.SeconContainer;
import de.yoshteq.diga.secon.SeconRequestParameters;

@Provider
public class FhirValidationExceptionMapper implements ExceptionMapper<FhirValidationException>
{

	private static final Logger logger = LoggerFactory.getLogger(FhirValidationExceptionMapper.class);

	@Inject
	SeconRequestParameters parameters;

	@Override
	public Response toResponse(FhirValidationException exception)
	{
		if (logger.isWarnEnabled())
		{
			logger.warn("Exception", exception);
		}

		if (StringUtils.isBlank(parameters.getIkSender()))
		{
			return Response.status(Status.NOT_ACCEPTABLE).build();
		}

		var issue = new OperationOutcomeIssueComponent();
		issue.setSeverity(IssueSeverity.ERROR);
		issue.setCode(IssueType.INVALID);
		issue.setDiagnostics(exception.getMessage());

		var operationOutcome = new OperationOutcome();
		operationOutcome.addIssue(issue);

		return Response.status(Status.BAD_REQUEST)
			.entity(SeconContainer.create(parameters.getIkSender(), "ELB", operationOutcome))
			.build();
	}

}
