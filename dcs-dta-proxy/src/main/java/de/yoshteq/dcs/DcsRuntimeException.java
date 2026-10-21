package de.yoshteq.dcs;

public class DcsRuntimeException extends RuntimeException
{

	private static final long serialVersionUID = 4086108883312731159L;

	public DcsRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public DcsRuntimeException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public DcsRuntimeException(String message)
	{
		super(message);
	}

	public DcsRuntimeException(Throwable cause)
	{
		super(cause);
	}

}
