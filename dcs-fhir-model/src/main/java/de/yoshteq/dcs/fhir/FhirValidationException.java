package de.yoshteq.dcs.fhir;

public class FhirValidationException extends RuntimeException
{

	private static final long serialVersionUID = -9028909785575398289L;

	public FhirValidationException(String message)
	{
		super(message);
	}

	public FhirValidationException(String message, Throwable e)
	{
		super(message, e);
	}
}
