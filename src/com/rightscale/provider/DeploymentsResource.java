package com.rightscale.provider;

import net.xeger.rest.ProtocolError;
import net.xeger.rest.RestException;
import net.xeger.rest.Session;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

class DeploymentsResource extends DashboardResource {
	public static final String MIME_TYPE = "vnd.rightscale.deployment";
	
	public static final String ID       = Dashboard.ID;
	public static final String HREF     = Dashboard.HREF;
	public static final String NICKNAME = "nickname";
	
	public static final String[] COLUMNS = { ID, HREF, NICKNAME };

	public DeploymentsResource(Session session, String accountId) {
		super(session, accountId);
	}
	
	public Cursor index()
		throws RestException
	{		
		MatrixCursor result = new MatrixCursor(COLUMNS);
		JSONArray response = getJsonArray("deployments.js");		
		
		for(int i = 0; i < response.length(); i++) {
			try {
				JSONObject deployment = response.getJSONObject(i);
				
				String href = deployment.getString("href");
				int id = new Integer(href.substring(href.lastIndexOf('/')+1)).intValue(); //TODO error handling			
				String nickname = deployment.getString("nickname"); 
	
				MatrixCursor.RowBuilder row = result.newRow();
				row.add(id);
				row.add(href);
				row.add(nickname);
			}
			catch(JSONException e) {
				throw new ProtocolError(e);
			}
		}
		
		return result;			
	}
	
	public Cursor show(String id)
		throws RestException
	{
		try {
			MatrixCursor result = new MatrixCursor(COLUMNS);		
			JSONObject deployment = getJsonObject("deployments/" + id + ".js");				
			String href = deployment.getString("href");		
			String nickname = deployment.getString("nickname"); 
	
			MatrixCursor.RowBuilder row = result.newRow();
			row.add(id);
			row.add(href);
			row.add(nickname);
			
			return result;
		}
		catch(JSONException e) {
			throw new ProtocolError(e);
		}
	}
}
