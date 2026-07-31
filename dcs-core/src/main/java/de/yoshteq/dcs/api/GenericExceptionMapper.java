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

import de.yoshteq.diga.secon.SeconContainer;
import de.yoshteq.diga.secon.SeconRequestParameters;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable>
{

	@Inject
	SeconRequestParameters parameters;

	@Override
	public Response toResponse(Throwable exception)
	{
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
		var operationOutcome = new OperationOutcome();
		var issue = new OperationOutcome.OperationOutcomeIssueComponent();
		issue.setSeverity(IssueSeverity.FATAL);
		issue.setCode(IssueType.EXCEPTION);
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
