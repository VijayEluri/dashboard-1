package com.rightscale.provider;

import java.security.InvalidParameterException;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class Dashboard extends ContentProvider {
	private static String HARDCODED_USER     = "tony@rightscale.com";
	private static String HARDCODED_PASSWORD = "xxx";
	
	public static final Uri CONTENT_URI             = Uri.parse("content://com.rightscale.provider.dashboard");	
	public static final Uri DEPLOYMENTS_URI         = Uri.withAppendedPath(CONTENT_URI, "deployments");
	
	public static final String   DEPLOYMENT_MIME            = "vnd.xeger.deployment";
	public static final String   DEPLOYMENT_COLUMN_NICKNAME = "Nickname";
	public static final String[] DEPLOYMENT_COLUMNS         = {"_ID", DEPLOYMENT_COLUMN_NICKNAME};

	protected DashboardSession    _session  = null;
	protected DeploymentsResource _resource = null;
	
	@Override
	public String getType(Uri uri) {
		List<String> path = uri.getPathSegments();

		String model = path.get(0); 

		//This is an index page (/deployments, /deployments/1/servers, ...)
		if(path.size() % 2 == 1) {
			if(model.equals("deployments"))
				return  "vnd.android.cursor.dir/" + DEPLOYMENT_MIME;
			else
				throw new InvalidParameterException("Unknown URI: " + model);
		}		
		//This is a specific object (/deployments/1, /deployments/1/servers/42, ...)
		else if(path.size() % 2 == 0) {
			if(model.equals("deployments"))
				return  "vnd.android.cursor.item/" + DEPLOYMENT_MIME;
			else
				throw new InvalidParameterException("Unknown URI: " + model);
			
		}
		else {
			throw new InvalidParameterException("Unknown URI: " + model);			
		}
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		try {
			_session  = new DashboardSession(HARDCODED_USER, HARDCODED_PASSWORD);
			_resource = new DeploymentsResource(_session, 71);
		}
		catch(Exception e) {
			Log.d("Dashboard", "Failed to login", e);
		}
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] columns, String where, String[] whereArgs, String sortBy) {		
		try {
			return _resource.index();
		} catch (Exception e) {
			throw new Error(e);
		}
	}

	@Override
	public Uri insert(Uri arg0, ContentValues arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// TODO Auto-generated method stub
		return 0;
	}
}
