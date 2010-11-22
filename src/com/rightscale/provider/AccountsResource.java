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

package com.rightscale.provider;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.HttpClient;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.util.Log;
import net.xeger.rest.AbstractResource;
import net.xeger.rest.ProtocolError;
import net.xeger.rest.RestAuthException;
import net.xeger.rest.RestException;
import net.xeger.rest.Session;

class AccountsResource extends AbstractResource {
	public static final String MIME_TYPE = "vnd.rightscale.account";
	
	static final Pattern ACCOUNT_REGEX   = Pattern.compile("<option.+value=\"([0-9]+)\".*>(.*)</option>");
	//<select class="one_account" id="account" name="account" onchange="javascript:submit()">
	static final Pattern ACCOUNT_SELECT_REGEX = Pattern.compile("<select");
	
	public static final String ID       = Dashboard.ID;
	public static final String NICKNAME = "nickname";
	
	public static final String[] COLUMNS = { ID, NICKNAME };

	public AccountsResource(Session session) {
		super(session);
	}
	
    protected URI getResourceURI(String relativePath, String query) {
		try {
			if(query != null) {
				return new URI(getBaseURI().toString() + "/" + relativePath + "?" + query);				
			}
			else {
				return new URI(getBaseURI().toString() + "/" + relativePath);
			}
		}
		catch(URISyntaxException e) {
			throw new ProtocolError(e);
		}    	    	
    }    

    /**
     * Create a cookie-authenticated client, overriding the default behavior of DashboardSession
     * which is to create a basic-auth client.
     */
	protected HttpClient createClient() {
		return ((DashboardSession)getSession()).createCookieClient();		
	}

	public Cursor index()
		throws RestException
	{
		MatrixCursor result = new MatrixCursor(COLUMNS);
		
		String response = get("servers", null);		

		if(response.contains(DashboardSession.LOGIN_PAGE_CANARY)) {
			throw new RestAuthException("Authentication failed", 401);
		}

		
		Matcher selectMatch = ACCOUNT_SELECT_REGEX.matcher(response);
		if(!selectMatch.find()) {
			throw new ProtocolError("Couldn't find accounts dropdown in dashboard UI!");
		}		
		int nStart = selectMatch.start();
		int nStop  = response.indexOf("</select>", nStart);
		
		Matcher match = ACCOUNT_REGEX.matcher(response);

		boolean found = false;
		
		//Advance beyond accounts <select>
		while( (found = match.find()) && match.start(0) < nStart) { }

		//Eat the <option> tags
		while(found && match.end(0) <= nStop) {
			String id   = match.group(1);
			String name = match.group(2);
			MatrixCursor.RowBuilder row = result.newRow();
			row.add(new Long(id).longValue());
			row.add(name);
			found = match.find();
		}
		
		return result;
	}
}