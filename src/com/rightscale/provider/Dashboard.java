package com.rightscale.provider;

import java.security.InvalidParameterException;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

/**
 * The Android content provider used to retrieve REST resources from the RightScale API.
 * The UI widgets know how to deal with content, so we wrap the API in a layer that makes
 * it act like content.
 * 
 * Note that "URIs" mentioned in this class do not necessarily map 1:1 to API paths;
 * the Android OS uses a fancy URI-based scheme to identify data types; as such, each
 * Java content-data type exposed by this class (deployments, servers, ...) has its
 * own content:// URI.
 * 
 * Supported resources so far:
 *   - Deployment
 * 
 * @author tony
 *
 */
public class Dashboard extends ContentProvider {
	private static String HARDCODED_USER     = "someone@rightscale.com";
	private static String HARDCODED_PASSWORD = "xxx";
	
	public static final Uri CONTENT_URI             = Uri.parse("content://com.rightscale.provider.dashboard");	
	public static final Uri DEPLOYMENTS_URI         = Uri.withAppendedPath(CONTENT_URI, "deployments");
	
	public static final String   DEPLOYMENT_MIME            = "vnd.rightscale.deployment";
	public static final String   DEPLOYMENT_COLUMN_NICKNAME = "Nickname";
	public static final String[] DEPLOYMENT_COLUMNS         = {"_ID", DEPLOYMENT_COLUMN_NICKNAME};

	protected DashboardSession    _session  = null;
	protected DeploymentsResource _resource = null;
	
	@Override
	public String getType(Uri uri) {
		List<String> path = uri.getPathSegments();

		String model = path.get(0); 

		if(path.size() % 2 == 1) {
			//This is an index page (/deployments, /deployments/1/servers, ...)
			if(model.equals("deployments"))
				return  "vnd.android.cursor.dir/" + DEPLOYMENT_MIME;
			else
				throw new InvalidParameterException("Unknown URI: " + model);
		}		
		else if(path.size() % 2 == 0) {
			//This is a specific object (/deployments/1, /deployments/1/servers/42, ...)
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
