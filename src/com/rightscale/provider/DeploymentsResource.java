package com.rightscale.provider;

import android.database.*;
import android.net.Uri;

import java.io.*;
import org.json.*;

import net.xeger.rest.*;

class DeploymentsResource extends Resource {
	public static final Uri CONTENT_URI = 
		Uri.withAppendedPath(Dashboard.CONTENT_URI, "deployments");	
	public static final String MIME_TYPE = "vnd.rightscale.deployment";
	
	public static final String ID       = Dashboard.ID;
	public static final String HREF     = Dashboard.HREF;
	public static final String NICKNAME = "nickname";
	
	public static final String[] COLUMNS = { ID, HREF, NICKNAME };

	public DeploymentsResource(Session session, int accountId) {
		super(session, accountId);
	}
	
	public Cursor index()
		throws JSONException, IOException, RestException
	{		
		MatrixCursor result = new MatrixCursor(COLUMNS);
		JSONArray response = getJsonArray("deployments");		
		
		for(int i = 0; i < response.length(); i++) {
			JSONObject deployment = response.getJSONObject(i);
			
			String href = deployment.getString("href");
			int id = new Integer(href.substring(href.lastIndexOf('/')+1)).intValue(); //TODO error handling			
			String nickname = deployment.getString("nickname"); 

			MatrixCursor.RowBuilder row = result.newRow();
			row.add(id);
			row.add(href);
			row.add(nickname);
		}
		
		return result;			
	}
	
	public Cursor show(int id)
		throws JSONException, IOException, RestException
	{
		MatrixCursor result = new MatrixCursor(COLUMNS);		
		JSONObject deployment = getJsonObject("deployments/" + id);				
		String href = deployment.getString("href");		
		String nickname = deployment.getString("nickname"); 

		MatrixCursor.RowBuilder row = result.newRow();
		row.add(id);
		row.add(href);
		row.add(nickname);
		
		return result;					
	}
}
