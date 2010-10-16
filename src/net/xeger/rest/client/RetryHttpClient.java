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

package net.xeger.rest.client;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

public class RetryHttpClient implements HttpClient {
	HttpClient _client      = null;
	int        _maxGetTries = 3;
	
	public RetryHttpClient(HttpClient realClient, int maxGetTries) {
		_client      = realClient;
		_maxGetTries = maxGetTries;
	}
	
	public HttpResponse execute(HttpUriRequest arg0) throws IOException,
			ClientProtocolException {
		int tries             = 0;
		int maxTries          = arg0 instanceof HttpGet ? _maxGetTries : 1;
		HttpResponse response = null;
		
		while(tries++ < maxTries) {
			
			response   = _client.execute(arg0);

			if(checkSuccess(response)) {
				return response;
			}
		}
		
		return response;
	}

	public HttpResponse execute(HttpUriRequest arg0, HttpContext arg1)
			throws IOException, ClientProtocolException {
		int tries             = 0;
		int maxTries          = arg0 instanceof HttpGet ? _maxGetTries : 1;
		HttpResponse response = null;
		
		while(tries++ < maxTries) {
			
			response   = _client.execute(arg0, arg1);

			if(checkSuccess(response)) {
				return response;
			}
		}
		
		return response;
	}

	public HttpResponse execute(HttpHost arg0, HttpRequest arg1)
			throws IOException, ClientProtocolException {
		int tries             = 0;
		int maxTries          = arg1 instanceof HttpGet ? _maxGetTries : 1;
		HttpResponse response = null;
		
		while(tries++ < maxTries) {
			
			response   = _client.execute(arg0, arg1);

			if(checkSuccess(response)) {
				return response;
			}
		}
		
		return response;
	}

	public <T> T execute(HttpUriRequest arg0, ResponseHandler<? extends T> arg1)
			throws IOException, ClientProtocolException {
		return _client.execute(arg0, arg1);
	}

	public HttpResponse execute(HttpHost arg0, HttpRequest arg1,
			HttpContext arg2) throws IOException, ClientProtocolException {
		int tries             = 0;
		int maxTries          = arg1 instanceof HttpGet ? _maxGetTries : 1;
		HttpResponse response = null;
		
		while(tries++ < maxTries) {
			
			response   = _client.execute(arg0, arg1, arg2);

			if(checkSuccess(response)) {
				return response;
			}
		}
		
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

	protected boolean checkSuccess(HttpResponse response) {
		int code = response.getStatusLine().getStatusCode();
		return (code >= 200 && code < 300);
	}
}
