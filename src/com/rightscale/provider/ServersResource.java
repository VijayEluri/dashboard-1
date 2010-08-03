package com.rightscale.provider;

import android.database.*;

import java.io.*;
import org.json.*;

import net.xeger.rest.*;

class ServersResource extends Resource {
	public ServersResource(Session session, int accountId) {
		super(session, accountId);
	}
	
	public Cursor index()
		throws JSONException, IOException, RestAuthException
	{
		return buildCursor( getJsonArray("servers") );		
	}
	
	public Cursor indexForDeployment(int deploymentId)
		throws JSONException, IOException, RestAuthException
	{
		JSONObject deployment = getJsonObject("deployments/" + deploymentId);
		JSONArray servers     = deployment.getJSONArray("servers");
		return buildCursor(servers);
	}
	
	private Cursor buildCursor(JSONArray array)
		throws JSONException
	{
		MatrixCursor result = new MatrixCursor(Dashboard.SERVER_COLUMNS);
				
		for(int i = 0; i < array.length(); i++) {
			JSONObject object = array.getJSONObject(i);
			
			/* 
			 * The API is glitchy for non-EC2 servers and returns an incomplete JSON object.
			 * If the object lacks an href property, it is one of these and we skip it...
			 */
			if(object.has("href")) {
				String href     = object.getString("href");
				int    id       = new Integer(href.substring(href.lastIndexOf('/')+1)).intValue(); //TODO error handling			
				String nickname = object.getString("nickname");
                String state    = object.getString("state");
                
				MatrixCursor.RowBuilder row = result.newRow();
				row.add(id);
				row.add(href);
				row.add(nickname);
				row.add(state);
			}
		}
		
		return result;			
	}
}
