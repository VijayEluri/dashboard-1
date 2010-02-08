package com.rightscale.provider;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import net.xeger.rest.*;

public class DashboardSession implements Session {
	public static String LOGIN_URI = "https://my.rightscale.com/sessions";

	private String _username, _password;
	private DefaultHttpClient _client;
	
	public DashboardSession(String username, String password) {
		_username = username;
		_password = password;
		_client = new DefaultHttpClient();
	}
	
	public DefaultHttpClient login()
		throws AuthenticationException
	{
		try {
			HttpPost post = new HttpPost(new URI(LOGIN_URI));
			
			List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
			params.add(new BasicNameValuePair("email", _username));
			params.add(new BasicNameValuePair("password", _password));	    		
			post.setEntity(new UrlEncodedFormEntity(params));
			
			HttpResponse response = _client.execute(post);
			
			if(response.getStatusLine().getStatusCode() == 200) {
				//Success!
				return _client;
			}
			else {
				//Failure... so sad :(
				String responseText = AbstractResource.readResponse(response.getEntity());
				throw new AuthenticationException(responseText);
			}
		}
		catch(IOException e) {
			throw new Error(e);
		}
		catch(URISyntaxException e) {
			throw new ProtocolError(e);
		}
	}

	public void logout(DefaultHttpClient client) {
		// TODO Auto-generated method stub
		
	}
}
