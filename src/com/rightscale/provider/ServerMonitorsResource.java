package com.rightscale.provider;

import net.xeger.rest.ProtocolError;
import net.xeger.rest.RestException;
import net.xeger.rest.Session;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;
import android.database.MatrixCursor;

public class ServerMonitorsResource extends DashboardResource {
	public static final String MIME_TYPE   = "vnd.rightscale.server_monitor";
	
	public static final String ID          = Dashboard.ID;
	public static final String HREF        = Dashboard.HREF;
	public static final String SERVER_ID   = "server_id";
	public static final String GRAPH_NAME  = "graph_name";

	public static final String[] COLUMNS = { ID, HREF, SERVER_ID, GRAPH_NAME };
	
	public ServerMonitorsResource(Session session, String accountId) {
		super(session, accountId);
	}

	public Cursor showForServer(String serverId)
	throws RestException
{
	try {
		JSONObject deployment = getJsonObject("servers/" + serverId + "/monitoring.js");
		JSONArray monitors     = deployment.getJSONArray("monitors");
		return buildCursor(serverId, monitors);
	}
	catch(JSONException e) {
		throw new ProtocolError(e);
	}
}

	
	private Cursor buildCursor(String serverId, JSONArray array)
		throws JSONException
	{
		MatrixCursor result = new MatrixCursor(COLUMNS);

		int nServerId = Integer.parseInt(serverId);
		
		for(int i = 0; i < array.length(); i++) {
			JSONObject object = array.getJSONObject(i);
			MatrixCursor.RowBuilder row = result.newRow();
			buildRow(nServerId, row, object);
		}
		
		return result;
	}

	private void buildRow(int serverId, MatrixCursor.RowBuilder row, JSONObject object)
		throws JSONException
	{
		object = object.getJSONObject("monitor");
		
		int id      = 0; // fake ID column to keep view adapters happy
		String href = object.getString("href");
		String graphName = object.getString("graph-name");

		row.add(id++);
		row.add(href);
		row.add(serverId);
		row.add(graphName);
	}
}
