package com.rightscale.provider;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.database.Cursor;
import android.database.MatrixCursor;
import net.xeger.rest.AbstractResource;
import net.xeger.rest.ProtocolError;
import net.xeger.rest.RestAuthException;
import net.xeger.rest.RestException;
import net.xeger.rest.Session;

class AccountsResource extends AbstractResource {
	public static final String MIME_TYPE = "vnd.rightscale.account";
	
	static final Pattern ACCOUNT_REGEX   = Pattern.compile("<option value=\"([0-9]+)\".*>(.*)</option>");

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

    public Cursor index()
		throws RestException
	{
		MatrixCursor result = new MatrixCursor(COLUMNS);
		
		String response = this.get("servers", null);		

		if(response.contains(DashboardSession.LOGIN_PAGE_CANARY)) {
			throw new RestAuthException("Authentication failed", 401);
		}
		
		int nStart = response.indexOf("<select id=\"account\"");
		if(nStart < 0) {
			return null;
		}
		int nStop  = response.indexOf("</select>", nStart);
		
		Matcher match = ACCOUNT_REGEX.matcher(response);

		boolean found = false;
		
		//Advance beyond accounts <select>
		while( (found = match.find()) && match.start(0) < nStart) { }

		//Eat the <option> tags
		while(found && match.end(0) < nStop) {
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