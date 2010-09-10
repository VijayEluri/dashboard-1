package com.rightscale.provider;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import net.xeger.rest.AbstractResource;
import net.xeger.rest.ProtocolError;
import net.xeger.rest.RestAuthException;
import net.xeger.rest.RestException;
import net.xeger.rest.Session;
import net.xeger.rest.client.RetryHttpClient;
import net.xeger.rest.client.SessionHttpClient;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

public class DashboardSession implements Session {
	static public final String LOGIN_PAGE_CANARY = "New RightScale Session";
	
	private String _username, _password;
	private URI _baseURI;
	
	private Cookie _sessionCookie = null;

	public DashboardSession(String username, String password, String system)
	{
		_username = username;
		_password = password;

		try {
			_baseURI = new URI("https://" + system);
		} catch(URISyntaxException e) {
			throw new ProtocolError(e);
		}
	}

	public URI getBaseURI()
	{
		return _baseURI;
	}
	
	public void login()
		throws RestException
	{
		if(_sessionCookie != null) {
			//TODO also check that it has a valid user ID!!!!
			return;
		}
		
		if(_username == null || _password == null || _username.length() == 0 || _password.length() == 0) {
			throw new RestAuthException("Username or password not supplied", 401);
		}

		try {
			// Can't use accounts-API login because we don't necessarily know an account ID.
			// HACK: use UI login instead.
			URI loginUri = new URI(_baseURI.toString() + "/sessions");
			HttpPost post = createPost(loginUri);
	
			List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
			params.add(new BasicNameValuePair("email", _username));
			params.add(new BasicNameValuePair("password", _password));                
			post.setEntity(new UrlEncodedFormEntity(params));
	
			DefaultHttpClient client = new DefaultHttpClient();
			HttpResponse response = client.execute(post);
			String body = AbstractResource.readResponse(response.getEntity());
			
			if(response.getStatusLine().getStatusCode() != 200 || body.contains(LOGIN_PAGE_CANARY)) {
				throw new RestAuthException("UI login request failed", response.getStatusLine().getStatusCode());
			}
			
			List<Cookie> cookies = client.getCookieStore().getCookies();
			for(Cookie c : cookies) {
				if(c.getName().equals("rs_gbl")) {
					_sessionCookie = c;
					break;
				}
			}
			if(_sessionCookie == null) {
				throw new RestAuthException("UI login response did not contain session cookie", 401);
			}
		}
		catch(Exception e) {
			//Nothing should go wrong here
			throw new DashboardError(e);
		}
	}
	
	public void logout() {
		_username = _password = null;
		_sessionCookie = null;
	}

	public HttpClient createClient()
	{
		DefaultHttpClient client = new DefaultHttpClient();
		
		if(_sessionCookie != null) {
			client.getCookieStore().addCookie(_sessionCookie);
		}

		//Add some useful behaviors to the stock HTTP client
		return new RetryHttpClient(new SessionHttpClient(client, this), 3);
	}

    public HttpGet createGet(URI uri) {
    	HttpGet get = new HttpGet(uri.toString());
		get.addHeader("Host", uri.getAuthority());
		get.addHeader("Accept", "application/json,text/json,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
		get.addHeader("User-Agent", "com.rightscale.provider");
    	get.addHeader("X-API-Version", "1.0");
    	return get;
    }
    
    public HttpPost createPost(URI uri) {
    	HttpPost post = new HttpPost(uri);
    	post.addHeader("X-API-Version", "1.0");
    	return post;
    }
}
