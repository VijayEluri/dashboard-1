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

import net.xeger.rest.Session;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HttpContext;

public class SessionClient extends StatefulClient {
	private Session    _session = null;
	
	public SessionClient(HttpClient realClient, Session session) {
		super(realClient);
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

	public HttpResponse execute(HttpHost arg0, HttpRequest arg1,
			HttpContext arg2) throws IOException, ClientProtocolException {
		HttpResponse response = _client.execute(arg0, arg1, arg2);
		checkSessionStateChange(response);
		return response;
	}

	protected void checkSessionStateChange(HttpResponse response) {
		int code = response.getStatusLine().getStatusCode();

		if(code >= 400 && code < 500) {
			_session.logout();
		}
	}	
}
