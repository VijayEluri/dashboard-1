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

import net.xeger.rest.ProtocolError;
import net.xeger.rest.Session;

public class DashboardResource extends net.xeger.rest.AbstractResource {
	private URI _accountBaseURI = null;
	
	public DashboardResource(Session session, String accountId)
	{
		super(session);
		
		try {
			_accountBaseURI = new URI(super.getBaseURI().toString() + "/api/acct/" + accountId);			
		}
		catch(URISyntaxException e) {
			throw new DashboardError(e);
		}
	}

	public URI getBaseURI()
	{
		return _accountBaseURI;
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
}
