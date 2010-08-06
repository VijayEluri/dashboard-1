package net.xeger.rest;

public class RestServerException extends RestException {
	private static final long serialVersionUID = -5603043392074673662L;

	public RestServerException(String message, int statusCode) {
		super(message, statusCode);
	}
}
