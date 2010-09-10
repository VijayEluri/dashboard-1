package net.xeger.rest.client;

import java.io.IOException;

import net.xeger.rest.Session;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

public class SessionHttpClient implements HttpClient {
	private HttpClient _client  = null;
	private Session    _session = null;
	
	public SessionHttpClient(HttpClient realClient, Session session) {
		_client  = realClient;
		_session = session;
	}
	
	public HttpResponse execute(HttpUriRequest arg0) throws IOException,
			ClientProtocolException {
		HttpResponse response = _client.execute(arg0);
		checkSessionStateChange(response);
		return response;
	}

	public HttpResponse execute(HttpUriRequest arg0, HttpContext arg1)
			throws IOException, ClientProtocolException {
		HttpResponse response = _client.execute(arg0, arg1);
		checkSessionStateChange(response);
		return response;
	}

	public HttpResponse execute(HttpHost arg0, HttpRequest arg1)
			throws IOException, ClientProtocolException {
		HttpResponse response = _client.execute(arg0, arg1);
		checkSessionStateChange(response);
		return response;
	}

	public <T> T execute(HttpUriRequest arg0, ResponseHandler<? extends T> arg1)
			throws IOException, ClientProtocolException {
		return _client.execute(arg0, arg1);
	}

	public HttpResponse execute(HttpHost arg0, HttpRequest arg1,
			HttpContext arg2) throws IOException, ClientProtocolException {
		HttpResponse response = _client.execute(arg0, arg1, arg2);
		checkSessionStateChange(response);
		return response;
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

	protected void checkSessionStateChange(HttpResponse response) {
		int code = response.getStatusLine().getStatusCode();

		if(code >= 400 && code < 500) {
			_session.logout();
		}
	}
}
