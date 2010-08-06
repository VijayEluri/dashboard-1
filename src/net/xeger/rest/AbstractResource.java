package net.xeger.rest;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

abstract public class AbstractResource {
    abstract protected URI getBaseURI();
    abstract protected URI getResourceURI(String resource, int id);
    abstract protected URI getCollectionURI(String collection);
    
    private Session _session = null;
    
    public AbstractResource(Session session) {
    	_session = session;
    }
    
    
	protected JSONObject getJsonObject(String resourceName)
		throws JSONException, IOException, RestException
	{
		URI uri = getCollectionURI(resourceName);

		DefaultHttpClient client    = _session.createClient();
		
		HttpGet        get          = new HttpGet(uri.toString());
		HttpResponse   response;
		
		try {
			response = client.execute(get);
		}
		catch(Exception e) {
			throw new RestNetworkException(e);
		}
		
		String responseText = readResponse(response.getEntity());			
		int statusCode      = response.getStatusLine().getStatusCode();
		
		if(statusCode >= 200 && statusCode < 300) {
			return new JSONObject(responseText);							
		}
		else if(statusCode >= 400 && statusCode < 500) {
			throw new RestAuthException("Authentication failed", statusCode);
		}
		else if(statusCode >= 500 && statusCode < 600) {
			throw new RestServerException("Internal server error", statusCode);
		}
		else {
			throw new RestException("Unrecognized HTTP status code", statusCode);			
		}
	}	
    
	protected JSONArray getJsonArray(String resourceName)
		throws JSONException, IOException, RestException
	{
		URI uri = getCollectionURI(resourceName);

		DefaultHttpClient client    = _session.createClient();
		
		HttpGet        get          = new HttpGet(uri.toString());
		HttpResponse   response;

		try {
			response = client.execute(get);
		}
		catch(Exception e) {
			throw new RestNetworkException(e);
		}

		String responseText = readResponse(response.getEntity());			
		int statusCode      = response.getStatusLine().getStatusCode();
		
		if(statusCode >= 200 && statusCode < 300) {
			return new JSONArray(responseText);							
		}
		else if(statusCode >= 400 && statusCode < 500) {
			throw new RestAuthException("Authentication failed", statusCode);
		}
		else if(statusCode >= 500 && statusCode < 600) {
			throw new RestServerException("Internal server error", statusCode);
		}
		else {
			throw new RestException("Unrecognized HTTP status code", statusCode);			
		}
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
