package com.rightscale.provider;

import android.database.*;
import android.net.Uri;

import java.io.*;
import org.json.*;

import net.xeger.rest.*;

class ServersResource extends Resource {
	public static final Uri CONTENT_URI =
		Uri.withAppendedPath(Dashboard.CONTENT_URI, "servers");
	public static final String MIME_TYPE = "vnd.rightscale.server";
	
	public static final String ID                 = Dashboard.ID;
	public static final String DEPLOYMENT_ID      = "deployment_id";
	public static final String SERVER_TEMPLATE_ID = "server_template_id";

	public static final String HREF          = Dashboard.HREF;
	public static final String NICKNAME      = "nickname";
	public static final String STATE         = "state";
	
	public static final String[] COLUMNS = { ID, HREF, DEPLOYMENT_ID, SERVER_TEMPLATE_ID, NICKNAME, STATE };

	public ServersResource(Session session, int accountId) {
		super(session, accountId);
	}
	
	public Cursor index()
		throws JSONException, IOException, RestException
	{
		return buildCursor( getJsonArray("servers") );		
	}
	
	public Cursor indexForDeployment(int deploymentId)
		throws JSONException, IOException, RestException
	{
		JSONObject deployment = getJsonObject("deployments/" + deploymentId);
		JSONArray servers     = deployment.getJSONArray("servers");
		return buildCursor(servers);
	}
	
	public Cursor show(int id)
	throws JSONException, IOException, RestException
	{
		JSONObject deployment = getJsonObject("servers/" + id);
		return buildCursor(deployment);
	}

	private void buildRow(MatrixCursor.RowBuilder row, JSONObject object)
		throws JSONException
	{
		String href                 = object.getString("href");
		String deployment_href      = object.getString("deployment_href");
		String server_template_href = object.getString("server_template_href");
		
		int id                 = new Integer(href.substring(href.lastIndexOf('/')+1)).intValue(); //TODO error handling
		int deployment_id      = new Integer(deployment_href.substring(deployment_href.lastIndexOf('/')+1)).intValue(); //TODO error handling
		int server_template_id = new Integer(server_template_href.substring(server_template_href.lastIndexOf('/')+1)).intValue(); //TODO error handling
		
		String nickname = object.getString("nickname");
        String state    = object.getString("state");
		row.add(id);
		row.add(href);
		row.add(deployment_id);
		row.add(server_template_id);
		row.add(nickname);
		row.add(state);
	}
	
	private Cursor buildCursor(JSONObject object)
	throws JSONException
	{
		MatrixCursor result = new MatrixCursor(COLUMNS);
		MatrixCursor.RowBuilder row = result.newRow();
		buildRow(row, object);
		return result;
	}
	
	private Cursor buildCursor(JSONArray array)
		throws JSONException
	{
		MatrixCursor result = new MatrixCursor(COLUMNS);
				
		for(int i = 0; i < array.length(); i++) {
			JSONObject object = array.getJSONObject(i);
			
			/* 
			 * The API is glitchy for non-EC2 servers and returns an incomplete JSON object.
			 * If the object lacks an href property, it is one of these and we skip it...
			 */
			if(object.has("href")) {                
				MatrixCursor.RowBuilder row = result.newRow();
				buildRow(row, object);
			}
		}
		
		return result;			
	}
}
