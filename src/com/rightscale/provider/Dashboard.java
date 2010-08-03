package com.rightscale.provider;

import java.security.InvalidParameterException;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

/**
 * The Android content provider used to retrieve REST resources from the
 * RightScale API. The UI widgets know how to deal with content, so we wrap the
 * API in a layer that makes it act like content.
 * 
 * Note that "URIs" mentioned in this class do not necessarily map 1:1 to API
 * paths; the Android OS uses a fancy URI-based scheme to identify data types;
 * as such, each Java content-data type exposed by this class (deployments,
 * servers, ...) has its own content:// URI.
 *
 * Also note that this content provider is a gigantic hack inasmuch as it does not
 * keep track of RightScale dashboard resources in a local content database, which it
 * really should. Instead, it synchronously issues API calls in response to SQL
 * queries. As such, it only knows how to handle an EXTREMELY LIMITED set of SQL
 * "where" clauses and it always returns all columns of the given resource.
 * 
 * Supported resources so far:
 *   - Deployment (optionally, WHERE deployment_id = x)
 *   - Server (optionally, WHERE deployment_id = x)
 * 
 * @author tony
 * 
 */
public class Dashboard extends ContentProvider {
	private static String HARDCODED_USER       = "someone@rightscale.com";
	private static String HARDCODED_PASSWORD   = "xxx";
    private static int    HARDCODED_ACCOUNT_ID = 2951;
    
	public static final Uri CONTENT_URI = Uri
			.parse("content://com.rightscale.provider.dashboard");

	public static final String COLUMN_ID = "_ID";
	public static final String COLUMN_HREF = "href";

	public static final Uri DEPLOYMENTS_URI = Uri.withAppendedPath(CONTENT_URI,
			"deployments");
	public static final String DEPLOYMENT_MIME = "vnd.rightscale.deployment";
	public static final String DEPLOYMENT_COLUMN_NICKNAME = "Nickname";
	public static final String[] DEPLOYMENT_COLUMNS = { COLUMN_ID, COLUMN_HREF,
			DEPLOYMENT_COLUMN_NICKNAME };

	public static final Uri SERVERS_URI = Uri.withAppendedPath(CONTENT_URI,
			"servers");
	public static final String SERVER_MIME = "vnd.rightscale.server";
	public static final String SERVER_COLUMN_NICKNAME = "Nickname";
	public static final String SERVER_COLUMN_STATE = "State";
	public static final String[] SERVER_COLUMNS = { COLUMN_ID, COLUMN_HREF,
			SERVER_COLUMN_NICKNAME, SERVER_COLUMN_STATE };

	protected DashboardSession _session = null;
	protected DeploymentsResource _deployments = null;
	protected ServersResource _servers = null;

	@Override
	public String getType(Uri uri) {
		List<String> path = uri.getPathSegments();

		String model = path.get(0);

		String mimePrefix, mimeType;

		if (path.size() % 2 == 1) {
			// Odd-sized paths (/deployments, /deployments/1/servers, ...) are
			// an index page
			// representing a collection of resources
			mimePrefix = "vnd.android.cursor.dir/";
		} else {
			// Even-sized paths are the page for an individual item
			mimePrefix = "vnd.android.cursor.item/";
		}

		if (model.equals("deployments"))
			mimeType = DEPLOYMENT_MIME;
		if (model.equals("servers"))
			mimeType = SERVER_MIME;
		else
			throw new InvalidParameterException("Unknown URI: " + model);

		return mimePrefix + mimeType;
	}

	@Override
	public boolean onCreate() {
		try {
			_session = new DashboardSession(HARDCODED_USER, HARDCODED_PASSWORD);
			_deployments = new DeploymentsResource(_session, HARDCODED_ACCOUNT_ID);
			_servers = new ServersResource(_session, HARDCODED_ACCOUNT_ID);
		} catch (Exception e) {
			Log.d("Dashboard", "Failed to login", e);
		}
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] columns, String where,
			String[] whereArgs, String sortBy) {
		try {
			if (uri.equals(DEPLOYMENTS_URI)) {
				if (where != null && where.equals("deployment_id = ?")
						&& whereArgs != null && whereArgs.length >= 1) {
					int deploymentId = new Integer(whereArgs[0]).intValue();
					return _deployments.show(deploymentId);
				} else {
					return _deployments.index();
				}
			} else if (uri.equals(SERVERS_URI)) {
				if (where != null && where.equals("deployment_id = ?")
						&& whereArgs != null && whereArgs.length >= 1) {
					int deploymentId = new Integer(whereArgs[0]).intValue();
					return _servers.indexForDeployment(deploymentId);
				} else {
					return _servers.index();
				}
			} else {
				throw new DashboardError("Unknown content URI " + uri);
			}
		} catch (Exception e) {
			Error err = new DashboardError(e);
			err.setStackTrace(e.getStackTrace());
			throw err;
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
