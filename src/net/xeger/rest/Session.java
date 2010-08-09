package net.xeger.rest;

import java.net.URI;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

public interface Session {
	public void login() throws RestException;
	public void logout();
	
	public DefaultHttpClient createClient();
	public HttpGet createGet(URI uri);
	public HttpPost createPost(URI uri);
}
