package net.xeger.rest;

import org.apache.http.impl.client.DefaultHttpClient;

public interface Session {
	public DefaultHttpClient login() throws AuthenticationException;
}
