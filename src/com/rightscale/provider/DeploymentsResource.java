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

import net.xeger.rest.ProtocolError;
import net.xeger.rest.RestException;
import net.xeger.rest.Session;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;
import android.database.MatrixCursor;

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
		
		for( Integer i : sortJsonArray(response, "nickname") ) {
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
