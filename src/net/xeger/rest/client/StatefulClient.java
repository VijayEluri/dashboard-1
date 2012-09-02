package net.xeger.rest.client;

import java.io.IOException;
import java.lang.reflect.Method;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

public class StatefulClient implements HttpClient {
	HttpClient _client     = null;
	CookieStore _cookieStore = null;
	
	public StatefulClient(HttpClient realClient) {		
		_client = realClient;
		try {
			Method method = realClient.getClass().getMethod("getCookieStore");
			_cookieStore = (CookieStore)method.invoke(realClient); 
		}
		catch(Exception e) {
			throw new IllegalArgumentException("Client must implement getCookieStore()");
		}
	}
	
	public HttpClient getRealClient() {
		return _client;
	}
	
	public HttpResponse execute(HttpUriRequest arg0) throws IOException,
			ClientProtocolException {
		return _client.execute(arg0);
	}

	public HttpResponse execute(HttpUriRequest arg0, HttpContext arg1)
			throws IOException, ClientProtocolException {
		return _client.execute(arg0, arg1);
	}

	public HttpResponse execute(HttpHost arg0, HttpRequest arg1)
			throws IOException, ClientProtocolException {
		return _client.execute(arg0, arg1);
	}

	public <T> T execute(HttpUriRequest arg0, ResponseHandler<? extends T> arg1)
			throws IOException, ClientProtocolException {
		return _client.execute(arg0, arg1);
	}

	public HttpResponse execute(HttpHost arg0, HttpRequest arg1,
			HttpContext arg2) throws IOException, ClientProtocolException {
		return _client.execute(arg0, arg1, arg2);
	}

	public <T> T execute(HttpUriRequest arg0,
			ResponseHandler<? extends T> arg1, HttpContext arg2)
			throws IOException, ClientProtocolException {
		return _client.execute(arg0, arg1, arg2);
	}

	public <T> T execute(HttpHost arg0, HttpRequest arg1,
			ResponseHandler<? extends T> arg2) throws IOException,
			ClientProtocolException {
		return _client.execute(arg0, arg1, arg2);
	}

	public <T> T execute(HttpHost arg0, HttpRequest arg1,
			ResponseHandler<? extends T> arg2, HttpContext arg3)
			throws IOException, ClientProtocolException {
		return _client.execute(arg0, arg1, arg2, arg3);
	}

	public ClientConnectionManager getConnectionManager() {
		return _client.getConnectionManager();
	}

	public HttpParams getParams() {
		return _client.getParams();
	}

	public CookieStore getCookieStore() {
		return _cookieStore;
	}

	protected boolean checkSuccess(HttpResponse response) {
		int code = response.getStatusLine().getStatusCode();
		return (code >= 200 && code < 300);
	}
}
