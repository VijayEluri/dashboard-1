package com.rightscale.provider;

import java.net.URI;
import java.net.URISyntaxException;
import net.xeger.rest.*;

public class Resource extends net.xeger.rest.AbstractResource {
	public static String API_PREFIX = "https://my.rightscale.com/api";
	public static String URI_SUFFIX = ".js?api_version=1.0";
	
	private int _accountId           = 0;
	private URI _baseURI             = null;
	private Session _session = null;
	
	public Resource(Session session, int accountId)
	{
		super(session);
		_accountId = accountId;
	}
	
	protected URI getBaseURI() {
		try {
			if(_baseURI == null) {
				_baseURI = new URI(API_PREFIX + "/acct/" + new Integer(_accountId).toString());
			}
		} catch(URISyntaxException e) {
			throw new ProtocolError(e);
		}
		
		return _baseURI;
	}	

    protected URI getResourceURI(String resource, int id) {
		try {
			return new URI(getBaseURI().toString() + "/" + resource + "/" + new Integer(id).toString() + URI_SUFFIX);
		}
		catch(URISyntaxException e) {
			throw new Error(e);
		}
    }
    
    protected URI getCollectionURI(String collection) {
		try {
			return new URI(getBaseURI().toString() + "/" + collection + URI_SUFFIX);
		}
		catch(URISyntaxException e) {
			throw new ProtocolError(e);
		}    	    	
    }	
}
