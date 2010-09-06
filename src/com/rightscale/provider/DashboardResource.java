package com.rightscale.provider;

import java.net.URI;
import java.net.URISyntaxException;

import net.xeger.rest.ProtocolError;
import net.xeger.rest.Session;

public class DashboardResource extends net.xeger.rest.AbstractResource {
	public static String API_HOST   = "moo1.rightscale.com"; //TODO make this configurable
	public static String API_PREFIX = "https://" + API_HOST + "/api";
	
	private String _accountId = null;
	private URI _baseURI      = null;
	
	public DashboardResource(Session session, String accountId)
	{
		super(session);
		_accountId = accountId;
	}
	
	protected URI getBaseURI() {
		try {
			if(_baseURI == null) {
				_baseURI = new URI(API_PREFIX + "/acct/" + _accountId);
			}
		} catch(URISyntaxException e) {
			throw new ProtocolError(e);
		}
		
		return _baseURI;
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
