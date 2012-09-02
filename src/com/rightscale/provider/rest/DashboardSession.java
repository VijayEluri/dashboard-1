// Dashboard: an Android front-end to the RightScale dashboard
// Copyright (C) 2009 Tony Spataro <code@tracker.xeger.net>
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package com.rightscale.provider;

import java.io.IOException;
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

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

public class DashboardSession implements Session {
	static public final String LOGIN_PAGE_CANARY = "New RightScale Session";
	
	private String _username, _password;
	private URI _baseURI;
	
	/**
	 * Session cookie; only gets stored when a login request succeeds. Thus, the presence
	 * of a session cookie in this variable doubles as an indicator that we are logged in.
	 */
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
			return;
		}
		
		if(_username == null || _password == null || _username.length() == 0 || _password.length() == 0) {
			throw new RestAuthException("Username or password not supplied", 401);
		}

		try {
			// Can't use accounts-API login because we don't necessarily know an account ID.
			// HACK: use UI login instead.
			URI loginUri = new URI(_baseURI.toString() + "/session");
			HttpPost post = createPost(loginUri);
	
			List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
			params.add(new BasicNameValuePair("email", _username));
			params.add(new BasicNameValuePair("password", _password));                
			params.add(new BasicNameValuePair("login_type", "rs"));                
			post.setEntity(new UrlEncodedFormEntity(params));
	
			DefaultHttpClient client = new DefaultHttpClient();
			HttpResponse response = client.execute(post);
			String body = AbstractResource.readResponse(response.getEntity());
			
			if(response.getStatusLine().getStatusCode() != 200 || body.contains(LOGIN_PAGE_CANARY)) {
				throw new RestAuthException("UI login request failed", response.getStatusLine().getStatusCode());
			}
			
			List<Cookie> cookies = client.getCookieStore().getCookies();
			for(Cookie c : cookies) {
				if(c.getName().startsWith("rs_gbl")) {
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
		_sessionCookie = null;
	}

	/**
	 * Create a client that uses HTTP Basic authentication. Appropriate for most RightScale API requests.
	 */
	public HttpClient createClient(boolean basicAuth)
	{
		DefaultHttpClient client = new DefaultHttpClient();
		
		if(basicAuth) {
			//Wire up the client to perform HTTP basic authentication
			setupBasicAuth(client);
		}
		
		//Add some useful behaviors to the stock HTTP client
		return new RetryHttpClient(new SessionHttpClient(client, this), 3);
	}

	/**
	 * Create a client that uses cookie-based authentication. Appropriate for RightScale UI requests.
	 */
	public HttpClient createCookieClient() {
		DefaultHttpClient client = new DefaultHttpClient();
		
		if(_sessionCookie != null) {
			client.getCookieStore().addCookie(_sessionCookie);
		}

		//Add some useful behaviors to the stock HTTP client
		return new RetryHttpClient(new SessionHttpClient(client, this), 3);		
	}
	
	/**
	 * Create an anonymous, unauthenticated client.
	 */
	public HttpClient createAnonymousClient() {
		DefaultHttpClient client = new DefaultHttpClient();
		
		//Add some useful behaviors to the stock HTTP client
		return new RetryHttpClient(client, 3);		
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

    private void setupBasicAuth(DefaultHttpClient client) {
		AuthScope authScope = new AuthScope(_baseURI.getHost(), AuthScope.ANY_PORT, AuthScope.ANY_REALM);
		UsernamePasswordCredentials creds = new UsernamePasswordCredentials(_username, _password);
		client.getCredentialsProvider().setCredentials(authScope, creds);
		client.addRequestInterceptor(createBasicAuthInterceptor(), 0);    	
    }
    
	private HttpRequestInterceptor createBasicAuthInterceptor() {
		return new HttpRequestInterceptor() {
		    public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
		        AuthState authState = (AuthState) context.getAttribute(ClientContext.TARGET_AUTH_STATE);
		        CredentialsProvider credsProvider = (CredentialsProvider) context.getAttribute(
		                ClientContext.CREDS_PROVIDER);
		        HttpHost targetHost = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
		        
		        if (authState.getAuthScheme() == null) {
		            AuthScope authScope = new AuthScope(targetHost.getHostName(), targetHost.getPort());
		            Credentials creds = credsProvider.getCredentials(authScope);
		            if (creds != null) {
		                authState.setAuthScheme(new BasicScheme());
		                authState.setCredentials(creds);
		            }
		        }
		    }    
		};		
	}    
}
