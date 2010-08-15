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

class ServerTemplateExecutablesResource extends DashboardResource {
	public static final Uri CONTENT_URI =
		Uri.withAppendedPath(Dashboard.CONTENT_URI, "server_template_executables");
	public static final String MIME_TYPE = "vnd.rightscale.server_template_executable";
	
	public static final String ID                 = Dashboard.ID;
	public static final String SERVER_TEMPLATE_ID = "server_template_id";
	public static final String APPLY		      = "apply";
	public static final String POSITION           = "position";
	public static final String NAME               = "name";
	public static final String RIGHT_SCRIPT_ID    = "right_script_id";
	
	public static final String[] COLUMNS = { ID, SERVER_TEMPLATE_ID, APPLY, POSITION, NAME, RIGHT_SCRIPT_ID };

	public ServerTemplateExecutablesResource(Session session, int accountId) {
		super(session, accountId);
	}
	
	public Cursor indexForServerTemplate(int serverTemplateId, String apply)
		throws RestException
	{
		try {
			return buildCursor( serverTemplateId, getJsonArray("server_templates/" + serverTemplateId + "/executables.js") );
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
			int rightScriptId = new Integer(href.substring(href.lastIndexOf('/')+1)).intValue(); //TODO error handling
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
	
	private Cursor buildCursor(int serverTemplateId, JSONArray array)
		throws JSONException, ProtocolError
	{
		MatrixCursor result = new MatrixCursor(COLUMNS);
				
		for(int i = 0; i < array.length(); i++) {
			JSONObject object = array.getJSONObject(i);		
			MatrixCursor.RowBuilder row = result.newRow();
			buildRow(serverTemplateId, row, object);
		}
		
		return result;			
	}
}
