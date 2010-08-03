package com.rightscale.provider;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.*;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.auth.*;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import net.xeger.rest.*;

public class DashboardSession implements Session {
	private String _username, _password;
	private DefaultHttpClient _client;
	
	public DashboardSession(String username, String password) {
		_username = username;
		_password = password;
	}
	
	public DefaultHttpClient createClient()
	{
		if(_client == null) {
			_client = new DefaultHttpClient();
			AuthScope authScope = new AuthScope(Resource.API_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM);
			Credentials creds = new UsernamePasswordCredentials(_username, _password);
			_client.getCredentialsProvider().setCredentials(authScope, creds);
			_client.addRequestInterceptor(createPreemptiveAuth(), 0);
		}
		
		return _client;
	}

	public void login()
		throws RestAuthException
	{
		//TODO: can't do API request without knowing account; can't know account list until we login.
		//Perform a normal (non-API) login here to test creds validity and scrape HTML for account list (yech!)

		//TODO: convey account list to app somehow; allow current account to be tweaked by user
	}
	
	public void logout() {
		_client.getCredentialsProvider().clear();
		_username = _password = null;		
	}
	
	private HttpRequestInterceptor createPreemptiveAuth() {
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
