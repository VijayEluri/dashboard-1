package com.rightscale.provider;

import android.database.*;
import java.io.*;
import org.json.*;

import net.xeger.rest.*;

class DeploymentsResource extends Resource {
	public DeploymentsResource(Session session, int accountId) {
		super(session, accountId);
	}
	
	public Cursor index()
		throws JSONException, IOException, RestAuthException
	{
		MatrixCursor result = new MatrixCursor(Dashboard.DEPLOYMENT_COLUMNS);
		
		JSONArray response = getJsonArray("deployments");
		
		
		for(int i = 0; i < response.length(); i++) {
			JSONObject deployment = response.getJSONObject(i);
			MatrixCursor.RowBuilder row = result.newRow();
			
			String href = deployment.getString("href");
			int id = new Integer(href.substring(href.lastIndexOf('/')+1)).intValue(); //TODO error handling
			
			String nickname = deployment.getString("nickname"); 
			row.add(id);
			row.add(nickname);
		}
		
		return result;			
	}
}
