package com.rightscale.provider;

public class DashboardError extends Error {
	private static final long serialVersionUID = -167607775758952817L;
	
	public DashboardError(Throwable cause) {
		super(cause);
		setStackTrace(cause.getStackTrace());
	}
	
	public DashboardError(String message) {
		super(message);
	}
}
