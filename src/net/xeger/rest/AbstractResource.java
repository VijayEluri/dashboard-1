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

package net.xeger.rest;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

abstract public class AbstractResource {
    abstract protected URI      getResourceURI(String relativePath, String query);
    
    private Session _session = null;
    
    public AbstractResource(Session session) {
    	_session = session;
    }
    
    protected URI getBaseURI() {
    	return _session.getBaseURI();
    }

    protected Session getSession() {
    	return _session;
    }
    
    protected JSONObject getJsonObject(String relativePath)
    	throws RestException
    {
    	return getJsonObject(relativePath, null);
    }
    
	protected JSONObject getJsonObject(String relativePath, String query)
		throws RestException
	{
		try {
			return new JSONObject(get(relativePath, query));
		}
		catch(JSONException e) {
			throw new ProtocolError(e);
		}
	}	
    
    protected JSONArray getJsonArray(String relativePath)
	throws RestException
{
	return getJsonArray(relativePath, null);
}

	protected JSONArray getJsonArray(String relativePath, String query)
		throws RestException
	{
			try {
				return new JSONArray(get(relativePath, query));
			}
			catch(JSONException e) {
				throw new ProtocolError(e);
			}
	}	

	protected HttpEntity getEntity(String relativePath, String query)
		throws RestException
	{
		URI uri = getResourceURI(relativePath, query);

		_session.login();
		
		HttpClient client        = createClient();		
		HttpGet        get       = createGet(uri);
		HttpResponse   response;		
		int            statusCode;

		try {
			response = client.execute(get);
			statusCode   = response.getStatusLine().getStatusCode();
		}
		catch (Exception e) {
			throw new RestNetworkException(e);
		}
		
		if(statusCode >= 200 && statusCode < 300) {
			return response.getEntity();
		}
		
		try {
			response.getEntity().consumeContent(); //An error occurred; we won't be using this...
		}
		catch(IOException e) {}

		if(statusCode >= 400 && statusCode < 500) {
			throw new RestAuthException("Authentication failed", statusCode);
		}
		else if(statusCode >= 500 && statusCode < 600) {
			throw new RestServerException("Internal server error", statusCode);
		}
		else {
			throw new RestException("Unrecognized HTTP status code", statusCode);			
		}		
	}
	
	protected String get(String relativePath, String query)
		throws RestException
	{
		try {
			return readResponse(getEntity(relativePath, query));
		}
		catch(IOException e) {
			throw new RestNetworkException(e);			
		}
	}
	protected String post(String relativePath)
		throws RestException
	{
		return post(relativePath, null);
	}
	
	protected String post(String relativePath, List<? extends NameValuePair> params)
		throws RestException
	{
		URI uri = getResourceURI(relativePath, null);

		HttpClient client = createClient();		
		HttpPost   post   = createPost(uri);
		
		if(params != null) {
			UrlEncodedFormEntity entity;
			try {
				entity = new UrlEncodedFormEntity(params);
			} catch (UnsupportedEncodingException e) {
				throw new ProtocolError(e);
			}
			post.setEntity(entity);			
		}
		
		HttpResponse   response;
		String         responseText;
		int 		   statusCode;
		
		try {
			response        = client.execute(post);
			responseText    = readResponse(response.getEntity());
			statusCode      = response.getStatusLine().getStatusCode();
			response.getEntity().consumeContent(); //tell the response we're finished with its data
		}
		catch(Exception e) {
			throw new RestNetworkException(e);
		}
		
		if(statusCode >= 200 && statusCode < 300) {
			return responseText;							
		}
		else if(statusCode >= 400 && statusCode < 500) {
			_session.logout();
			throw new RestAuthException("Authentication failed", statusCode);
		}
		else if(statusCode >= 500 && statusCode < 600) {
			throw new RestServerException("Internal server error", statusCode);
		}
		else {
			throw new RestException("Unrecognized HTTP status code", statusCode);			
		}		
	}

	protected HttpClient createClient() {
		return _session.createClient();		
	}
	
	protected HttpGet createGet(URI uri) {
		return _session.createGet(uri);		
	}
	
	protected HttpPost createPost(URI uri) {
		return _session.createPost(uri);
	}

	static public String readResponse(HttpEntity entity) throws IOException
	{
	    String response = "";

		int length = ( int ) entity.getContentLength();

		if(length <= 0) {
			length = (64*1024);
		}
		
		StringBuffer sb = new StringBuffer( length );
		InputStreamReader isr = new InputStreamReader( entity.getContent(), "UTF-8" );
		char buff[] = new char[length];
		int cnt;
		while ( ( cnt = isr.read( buff, 0, length - 1 ) ) > 0 )
		{
			sb.append( buff, 0, cnt );
		}
		
		response = sb.toString();
		isr.close();
	    return response;
	}
}
