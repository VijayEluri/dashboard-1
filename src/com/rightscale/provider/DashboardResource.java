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
