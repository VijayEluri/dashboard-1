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

class ServerTemplatesResource extends DashboardResource {
	public static final String MIME_TYPE = "vnd.rightscale.server_template";
	
	public static final String ID              = Dashboard.ID;
	public static final String HREF            = Dashboard.HREF;
	public static final String NICKNAME        = "nickname";
	public static final String DESCRIPTION     = "description";
	public static final String VERSION         = "version";
	public static final String IS_HEAD_VERSION = "is_head_version";
	
	public static final String[] COLUMNS = { ID, HREF, NICKNAME, DESCRIPTION, VERSION, IS_HEAD_VERSION };

	public ServerTemplatesResource(Session session, String accountId) {
		super(session, accountId);
	}
	
	public Cursor index()
		throws RestException
	{
		try {
			return buildCursor( getJsonArray("server_templates.js") );
		}
		catch(JSONException e) {
			throw new ProtocolError(e);
		}
	}
	
	public Cursor show(String id)
		throws RestException
	{
		try {
			JSONObject st = getJsonObject("server_templates/" + id + ".js");
			return buildCursor(st);
		}
		catch(JSONException e) {
			throw new ProtocolError(e);
		}
	}

	private void buildRow(MatrixCursor.RowBuilder row, JSONObject object)
		throws JSONException
	{
		String href        = object.getString("href");		
		int id             = new Integer(href.substring(href.lastIndexOf('/')+1)).intValue(); //TODO error handling
		String nickname    = object.getString("nickname");
		String description = object.getString("description");
		int version        = object.getInt("version");
		boolean isHead     = object.getBoolean("is_head_version");
		
		row.add(id);
		row.add(href);
		row.add(nickname);
		row.add(description);
		row.add(version);
		row.add(isHead);
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
			MatrixCursor.RowBuilder row = result.newRow();
			buildRow(row, object);
		}
		
		return result;			
	}
}
