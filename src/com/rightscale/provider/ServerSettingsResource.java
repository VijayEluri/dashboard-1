package com.rightscale.provider;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.database.*;
import android.net.Uri;

import net.xeger.rest.*;

public class ServerSettingsResource extends Resource {
	public static final Uri CONTENT_URI =
		Uri.withAppendedPath(Dashboard.CONTENT_URI, "server_settings");
	public static final String MIME_TYPE = "vnd.rightscale.server_setting";
	
	public static final String ID                 = Dashboard.ID;
	public static final String HREF               = Dashboard.HREF;
	public static final String SERVER_ID          = "server_id";
	public static final String CLOUD_ID           = "cloud_id";
	public static final String IP_ADDRESS         = "ip_address";
	public static final String PRIVATE_IP_ADDRESS = "private_ip_address";
	public static final String LOCKED             = "locked";
	public static final String PRICING            = "pricing";
	public static final String DATACENTER         = "datacenter";

	public static final String[] COLUMNS = { ID, HREF, SERVER_ID, CLOUD_ID, IP_ADDRESS, PRIVATE_IP_ADDRESS, LOCKED, PRICING, DATACENTER };
	
	public ServerSettingsResource(Session session, int accountId) {
		super(session, accountId);
	}

	public Cursor showForServer(int server_id)
	throws JSONException, IOException, RestException
	{
		JSONObject settings = getJsonObject("servers/" + server_id + "/settings");
		return buildCursor(server_id, settings);
	}


	private void buildRow(int server_id, MatrixCursor.RowBuilder row, JSONObject object)
		throws JSONException
	{

		//HACK: Settings have no ID exposed through the API, but since they map 1:1 to servers,
		//we can reuse the Server ID. Kind of shitty!
		int id                    = server_id;
		String href               = Uri.withAppendedPath(ServersResource.CONTENT_URI, new Integer(server_id).toString() + "/settings").toString();
		int cloud_id;
		if(object.has("cloud_id")) {
			cloud_id = new Integer(object.getString("cloud_id")).intValue(); 
		}
		else {
			cloud_id = 0;
		}		
		String ip_address = null;
		if(object.has("ip-address")) {
			ip_address = object.getString("ip-address");
		}
		String private_ip_address = null;
		if(object.has("private-ip-address")) {
			private_ip_address = object.getString("private-ip-address");
		}
		
		boolean locked            = object.getBoolean("locked");
		String pricing            = object.getString("pricing");
		String datacenter         = object.getString("ec2-availability-zone");
		
        row.add(id);
		row.add(href);
		row.add(server_id);
		row.add(cloud_id);
		row.add(ip_address);
		row.add(private_ip_address);
		row.add(locked);
		row.add(pricing);
		row.add(datacenter);
	}
	
	private Cursor buildCursor(int server_id, JSONObject object)
	throws JSONException
	{
		MatrixCursor result = new MatrixCursor(COLUMNS);
		MatrixCursor.RowBuilder row = result.newRow();
		buildRow(server_id, row, object);
		return result;
	}
}
