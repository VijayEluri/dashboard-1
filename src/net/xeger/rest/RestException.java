package net.xeger.rest;

public class RestException
	extends java.lang.Exception
{
	private static final long serialVersionUID = -5691717258605209067L;

	protected int _statusCode;
	
	public RestException(String message) {
		super(message);
		_statusCode = 0;
	}

	public RestException(Throwable cause) {
		super(cause);
		_statusCode = 0;
	}

	public RestException(String message, int statusCode) {
		super(message);
		_statusCode = statusCode;
	}
}
