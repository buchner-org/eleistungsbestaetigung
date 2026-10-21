package de.yoshteq.dcs.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
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

import de.yoshteq.diga.secon.SeconContainer;
import de.yoshteq.diga.secon.SeconRequestParameters;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable>
{
	private static final Logger logger = LoggerFactory.getLogger(GenericExceptionMapper.class);

	@Inject
	SeconRequestParameters parameters;

	@Override
	public Response toResponse(Throwable exception)
	{
		if (logger.isWarnEnabled())
		{
			logger.warn("Exception", exception);
		}

		if (StringUtils.isNotBlank(parameters.getIkSender()))
		{
			return createFhirExceptionResponse();
		}
		else
		{
			return createResponseWithoutBody(exception);
		}
	}

	private Response createFhirExceptionResponse()
	{
		var issue = new OperationOutcomeIssueComponent();
		issue.setSeverity(IssueSeverity.FATAL);
		issue.setCode(IssueType.EXCEPTION);

		var operationOutcome = new OperationOutcome();
		operationOutcome.addIssue(issue);

		return Response.status(Status.BAD_REQUEST)
			.entity(SeconContainer.create(parameters.getIkSender(), operationOutcome))
			.build();
	}

	private Response createResponseWithoutBody(Throwable exception)
	{
		if (exception instanceof WebApplicationException webApplicationException
			&& webApplicationException.getResponse() != null)
		{
			return Response.status(webApplicationException.getResponse().getStatus()).build();
		}

		return Response.status(Status.INTERNAL_SERVER_ERROR).build();
	}
}
