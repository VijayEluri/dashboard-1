package com.rightscale.provider;

import java.security.InvalidParameterException;
import java.util.List;

import org.apache.http.client.HttpClient;

import com.rightscale.Settings;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

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
    private static int    HARDCODED_ACCOUNT_ID = 2951;
    
	public static final Uri CONTENT_URI = 
		Uri.parse("content://com.rightscale.provider.dashboard");
	
	public static final Uri DEPLOYMENTS_URI     = DeploymentsResource.CONTENT_URI;
	public static final Uri SERVERS_URI         = ServersResource.CONTENT_URI;
	public static final Uri SERVER_SETTINGS_URI = ServerSettingsResource.CONTENT_URI;
	
	public static final String ID   = "_id";
	public static final String HREF = "href";

	public static final String[] DEPLOYMENT_COLUMNS = DeploymentsResource.COLUMNS;
	public static final String[] SERVER_COLUMNS     = ServersResource.COLUMNS;
	public static final String[] SERVER_SETTING_COLUMNS = ServerSettingsResource.COLUMNS;
	
	@Override
	public String getType(Uri uri) {
		List<String> path = uri.getPathSegments();

		
		String model = path.get(0);
		String mimePrefix, mimeType;

		if (path.size() % 2 == 1) {
			// Odd-sized paths (/deployments, /deployments/1/servers, ...) 
			// represent a collection of resources.
			mimePrefix = "vnd.android.cursor.dir/";
		} else {
			// Even-sized paths represent an individual item.
			mimePrefix = "vnd.android.cursor.item/";
		}

		if (model.equals("deployments")) {
			mimeType = DeploymentsResource.MIME_TYPE;
		}
		else if (model.equals("servers")) {
			mimeType = ServersResource.MIME_TYPE;
		}
		else if (model.equals("server_settings")) {
			mimeType = ServerSettingsResource.MIME_TYPE;
		}
		else {
			throw new InvalidParameterException("Unknown URI: " + model);
		}
		
		return mimePrefix + mimeType;
	}

	@Override
	public boolean onCreate() {
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] columns, String where,
			String[] whereArgs, String sortBy) {
		try {
			//TODO cache the session if it becomes stateful? use a pool?
			DashboardSession session = new DashboardSession(Settings.getEmail(getContext()), Settings.getPassword(getContext()));
			session.login();
			
			if (uri.equals(DeploymentsResource.CONTENT_URI)) {
				DeploymentsResource deployments = new DeploymentsResource(session, HARDCODED_ACCOUNT_ID);
				
				if (where != null && where.equals("deployment_id = ?")
						&& whereArgs != null && whereArgs.length >= 1) {
					//SELECT ... FROM deployments WHERE deployment_id = ?
					int deploymentId = new Integer(whereArgs[0]).intValue();
					return deployments.show(deploymentId);
				} else {
					//SELECT ... FROM deployments
					return deployments.index();
				}
			} else if (uri.equals(ServersResource.CONTENT_URI)) {
				ServersResource servers = new ServersResource(session, HARDCODED_ACCOUNT_ID);

				if (where != null && where.equals("deployment_id = ?")
						&& whereArgs != null && whereArgs.length >= 1) {
					//SELECT ... FROM servers WHERE deployment_id = ?
					int deploymentId = new Integer(whereArgs[0]).intValue();
					return servers.indexForDeployment(deploymentId);
				}
				else if(where != null && where.equals("server_id = ?")) {
					//SELECT ... FROM servers WHERE server_id = ?
					int serverId = new Integer(whereArgs[0]).intValue();
					return servers.show(serverId);
				}
				else {
					//SELECT ... FROM servers
					return servers.index();
				}
			}
			else if(uri.equals(ServerSettingsResource.CONTENT_URI)) {
				ServerSettingsResource serverSettings = new ServerSettingsResource(session, HARDCODED_ACCOUNT_ID);

				if(where != null && where.equals("server_id = ?")) {
					//SELECT ... FROM server_settings WHERE server_id = ?
					int serverId = new Integer(whereArgs[0]).intValue();
					return serverSettings.showForServer(serverId);
				}
				else {
					throw new DashboardError("Cannot query server_settings without specifying server_id in where-clause");
				}
				
			}
			else {
				throw new DashboardError("Unknown content URI " + uri);
			}
		}
		catch(RuntimeException e) {
			throw e;
		}
		catch (Exception e) {
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
	
	static public HttpClient createHttpClient(Context context) {
		//TODO cache the session if it becomes stateful? use a pool?
		DashboardSession session = new DashboardSession(Settings.getEmail(context), Settings.getPassword(context));
		return session.createClient();
	}
}
