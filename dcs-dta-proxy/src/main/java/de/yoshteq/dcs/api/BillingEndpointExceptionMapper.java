package de.yoshteq.dcs.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r4.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.yoshteq.diga.secon.SeconContainer;
import de.yoshteq.diga.secon.SeconRequestParameters;

@Provider
public class BillingEndpointExceptionMapper implements ExceptionMapper<BillingEndpointException>
{
	private static final Logger logger = LoggerFactory.getLogger(BillingEndpointExceptionMapper.class);

	@Inject
	SeconRequestParameters parameters;

	@Override
	public Response toResponse(BillingEndpointException exception)
	{
		var operationOutcome = new OperationOutcome();
		operationOutcome.addIssue(createIssue(exception));

		if (logger.isWarnEnabled())
		{
			logger.warn("Billing request rejected [{}]: {}", exception.getType(), exception.getMessage(), exception);
		}

		return Response.status(Status.BAD_REQUEST)
			.entity(SeconContainer.create(parameters.getIkSender(), operationOutcome))
			.build();
	}

	private OperationOutcomeIssueComponent createIssue(BillingEndpointException exception)
	{
		var issue = new OperationOutcomeIssueComponent();
		issue.setSeverity(IssueSeverity.ERROR);
		issue.setCode(exception.getType());

		if (exception.getMessage() != null)
		{
			issue.setDiagnostics(exception.getMessage());
		}

		if (exception.getFhirPathExpression() != null)
		{
			issue.addExpression(exception.getFhirPathExpression());
		}

		return issue;
	}

}
