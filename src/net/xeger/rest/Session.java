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

import java.net.URI;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;

public interface Session {
	/**
	 * Attempt to authenticate the session using whatever mechanism is most appropriate. Although
	 * a successful return from this method guarantees that the session has been authenticated,
	 * there is no guarantee that the session will <em>stay</em> authenticated; callers should
	 * make no assumptions regarding the success of HTTP requests executed on an HTTP client returned
	 * by createClient() after a login() call has completed, and should always be capable of handling
	 * a RestAuthException (e.g. by calling login() again).
	 * 
	 * @throws RestException
	 */
	public void login() throws RestException;
	
	public void logout();
	
	public URI getBaseURI();
	public HttpClient createClient();
	public HttpGet createGet(URI uri);
	public HttpPost createPost(URI uri);
}
