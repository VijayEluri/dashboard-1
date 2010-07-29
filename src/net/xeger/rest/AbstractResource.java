package net.xeger.rest;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;

abstract public class AbstractResource {
    abstract protected URI getBaseURI();
    abstract protected URI getResourceURI(String resource, int id);
    abstract protected URI getCollectionURI(String collection);
    
    private Session _session = null;
    
    public AbstractResource(Session session) {
    	_session = session;
    }
    
	protected JSONArray getJsonArray(String resourceName)
		throws JSONException, IOException, RestAuthException
	{
		URI uri = getCollectionURI(resourceName);

		DefaultHttpClient client    = _session.createClient();
		
		HttpGet        get          = new HttpGet(uri.toString());
		HttpResponse   response     = client.execute(get);
		String         responseText = readResponse(response.getEntity());			

		if(response.getStatusLine().getStatusCode() == 200) {
			Log.d("getJsonArray", responseText);
			return new JSONArray(responseText);							
		}
		else {
			throw new RestAuthException("Not logged in successfully.");
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
