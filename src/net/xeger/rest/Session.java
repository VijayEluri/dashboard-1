package net.xeger.rest;

import java.net.URI;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;

public interface Session {
	/**
	 * Attempt to authenticate the session using whatever mechanism is most appropriate. Although
	 * a successful return from this method guarantees that the session has been authenticated,
	 * there is no guarantee that the session will <em>stay</em> authenticated; callers should
	 * make no assumptions regarding the success of HTTP requests executed on an HTTP client returned
	 * by createClient() after a login() call has completed, and should always be capable of handling
	 * a RestAuthException (e.g. by calling login() again).
	 * 
	 * @throws RestException
	 */
	public void login() throws RestException;
	
	public void logout();
	
	public URI getBaseURI();
	public HttpClient createClient();
	public HttpGet createGet(URI uri);
	public HttpPost createPost(URI uri);
}
