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

package com.rightscale.provider.rest;

import net.xeger.rest.ProtocolError;
import net.xeger.rest.RestException;
import net.xeger.rest.Session;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.rightscale.provider.*;

import android.database.Cursor;
import android.database.MatrixCursor;

public class ServerTemplateExecutablesResource extends DashboardResource {
	public static final String MIME_TYPE = "vnd.rightscale.server_template_executable";
	
	public static final String ID                 = Dashboard.ID;
	public static final String SERVER_TEMPLATE_ID = "server_template_id";
	public static final String APPLY		      = "apply";
	public static final String POSITION           = "position";
	public static final String NAME               = "name";
	public static final String RIGHT_SCRIPT_ID    = "right_script_id";
	
	public static final String[] COLUMNS = { ID, SERVER_TEMPLATE_ID, APPLY, POSITION, NAME, RIGHT_SCRIPT_ID };

	public ServerTemplateExecutablesResource(Session session, String accountId) {
		super(session, accountId);
	}
	
	public Cursor indexForServerTemplate(String serverTemplateId, String apply)
		throws RestException
	{
		try {
			return buildCursor( serverTemplateId, getJsonArray("server_templates/" + serverTemplateId + "/executables.js", "phase=" + apply) );
		}
		catch(JSONException e) {
			throw new ProtocolError(e);
		}
	}
	
//	public static final String[] COLUMNS = { ID, APPLY, POSITION, NAME, RIGHT_SCRIPT_ID };
	private void buildRow(int serverTemplateId, MatrixCursor.RowBuilder row, JSONObject object)
		throws JSONException, ProtocolError
	{
		String apply    = object.getString("apply");
		int    position = object.getInt("position");

		int applyOffset;
		if(apply.equals("boot")) {
			applyOffset = 0;
		}
		else if(apply.equals("operational")) {
			applyOffset = 250;
		}
		else if(apply.equals("decommission")) {
			applyOffset = 500;
		}
		else {
			throw new ProtocolError("Unexpected attribute value for server_template_executable: apply=" + apply);
		}

		//Synthesize a globally unique ID for this executable by composing its ST ID, apply phase, and position 
		int id = serverTemplateId * 1000 + applyOffset + position;		
		
		row.add(id);
		row.add(serverTemplateId);
		row.add(apply);
		row.add(position);

		if(object.has("right_script")) {
			JSONObject right_script = object.getJSONObject("right_script");
			String name       = right_script.getString("name");
			String href       = right_script.getString("href");
			int rightScriptId = Integer.valueOf(href.substring(href.lastIndexOf('/')+1)).intValue(); //TODO error handling
			row.add(name);
			row.add(rightScriptId);
		}
		else if(object.has("recipe")) {
			String name = object.getString("recipe");
			row.add(name);
		}
		else {
			throw new ProtocolError("server_template_executable has neither right_script nor recipe sub-element");
		}
	}
	
	private Cursor buildCursor(String serverTemplateId, JSONArray array)
		throws JSONException, ProtocolError
	{
		int nServerTemplateId = Integer.parseInt(serverTemplateId);

		MatrixCursor result = new MatrixCursor(COLUMNS);	
		for(int i = 0; i < array.length(); i++) {
			JSONObject object = array.getJSONObject(i);		
			MatrixCursor.RowBuilder row = result.newRow();
			buildRow(nServerTemplateId, row, object);
		}
		
		return result;			
	}
}
