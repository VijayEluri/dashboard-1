package net.xeger.rest;

public class ProtocolError extends Error {
	private static final long serialVersionUID = -4960342196718367565L;
	
	public ProtocolError(Throwable cause) {
		super(cause);
	}
	
	public ProtocolError(String message) {
		super(message);
	}
}
