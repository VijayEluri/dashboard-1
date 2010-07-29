package net.xeger.rest;

import org.apache.http.impl.client.DefaultHttpClient;

public interface Session {
	public void login() throws RestAuthException;
	public void logout();
	
	public DefaultHttpClient createClient();
}
