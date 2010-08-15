package net.xeger.rest;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

abstract public class AbstractResource {
    abstract protected URI      getBaseURI();
    abstract protected URI      getResourceURI(String relativePath, String query);
    
    private Session _session = null;
    
    public AbstractResource(Session session) {
    	_session = session;
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

	public String get(String relativePath, String query)
		throws RestException
	{
		URI uri = getResourceURI(relativePath, query);

		DefaultHttpClient client = _session.createClient();		
		HttpGet        get       = _session.createGet(uri);
		HttpResponse   response;		
		String         responseText;
		int            statusCode;
		
		try {
			response = client.execute(get);
			responseText = readResponse(response.getEntity());
			statusCode   = response.getStatusLine().getStatusCode();
			response.getEntity().consumeContent(); //tell the response we're finished with its data
			
			if(statusCode >= 400 && statusCode < 500) {
				throw new RestAuthException("Authentication failed", statusCode);
			}
			else if(statusCode >= 500 && statusCode < 600) {
				throw new RestServerException("Internal server error", statusCode);
			}
		}
		catch(IOException e) {
			throw new RestNetworkException(e);
		}
		
		if(statusCode >= 200 && statusCode < 300) {
			return responseText;
		}
		else {
			throw new RestException("Unrecognized HTTP status code", statusCode);			
		}		
	}
	
	public String post(String relativePath)
		throws RestException
	{
		URI uri = getResourceURI(relativePath, null);

		DefaultHttpClient client = _session.createClient();		
		HttpPost       post      = _session.createPost(uri);
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
