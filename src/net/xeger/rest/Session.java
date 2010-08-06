package net.xeger.rest;

import org.apache.http.impl.client.DefaultHttpClient;

public interface Session {
	public void login() throws RestException;
	public void logout();
	
	public DefaultHttpClient createClient();
}
