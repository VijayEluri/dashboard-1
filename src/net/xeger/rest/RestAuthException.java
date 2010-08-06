package net.xeger.rest;

public class RestAuthException extends RestException {
	private static final long serialVersionUID = -3685355726218752463L;

	public RestAuthException(String message, int statusCode) {
		super(message, statusCode);
	}
}
