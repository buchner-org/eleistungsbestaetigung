package de.yoshteq.dcs.api;

import org.hl7.fhir.r4.model.OperationOutcome.IssueType;

public class BillingEndpointException extends RuntimeException
{

	private static final long serialVersionUID = 6734424256314595323L;

	private final IssueType type;

	private final String fhirPathExpression;

	public BillingEndpointException(String message, IssueType type)
	{
		this(message, type, null);
	}

	public BillingEndpointException(IssueType type, String fhirPathExpression)
	{
		this(null, type, fhirPathExpression);
	}

	public BillingEndpointException(String message, IssueType type, String fhirPathExpression)
	{
		super(message);
		this.type = type;
		this.fhirPathExpression = fhirPathExpression;
	}

	public IssueType getType()
	{
		return type;
	}

	public String getFhirPathExpression()
	{
		return fhirPathExpression;
	}

}
