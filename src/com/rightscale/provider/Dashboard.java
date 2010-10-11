package com.rightscale.provider;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import net.xeger.rest.RestException;

import org.apache.http.client.HttpClient;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.rightscale.dashboard.Settings;

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
 * Also note that this content provider is a gigantic hack inasmuch as it does
 * not keep track of RightScale dashboard resources in a local content database,
 * which it really should. Instead, it synchronously issues API calls in
 * response to SQL queries. As such, it only knows how to handle an EXTREMELY
 * LIMITED set of SQL "where" clauses and it always returns all columns of the
 * given resource.
 * 
 * Supported resources so far: - Deployment (optionally, WHERE deployment_id =
 * x) - Server (optionally, WHERE deployment_id = x) - Server settings (WHERE
 * server_id = x)
 * 
 * @author tony
 * 
 */
public class Dashboard extends ContentProvider {
	public static final Uri BASE_CONTENT_URI = Uri
			.parse("content://com.rightscale.provider.dashboard");

	public static final String ID = "_id";
	public static final String HREF = "href";

	public static final String[] ACCOUNT_COLUMNS         = AccountsResource.COLUMNS;
	public static final String[] DEPLOYMENT_COLUMNS      = DeploymentsResource.COLUMNS;
	public static final String[] SERVER_COLUMNS          = ServersResource.COLUMNS;
	public static final String[] SERVER_SETTING_COLUMNS  = ServerSettingsResource.COLUMNS;
	public static final String[] SERVER_MONITORS_COLUMNS = ServerMonitorsResource.COLUMNS;
	public static final String[] SERVER_TEMPLATE_COLUMNS = ServerTemplatesResource.COLUMNS;
	public static final String[] SERVER_TEMPLATE_EXECUTABLE_COLUMNS = ServerTemplateExecutablesResource.COLUMNS;

	public static final String ACTION_LAUNCH = "launch";
	public static final String ACTION_TERMINATE = "terminate";
	public static final String ACTION_REBOOT = "reboot";
	public static final String ACTION_RUN_SCRIPT = "runScript";

	static protected List<String> WHERE_ACCOUNT = new ArrayList<String>();
	static {
		WHERE_ACCOUNT.add("account_id");
	}
	
	static protected List<String> WHERE_ACCOUNT_AND_ID = new ArrayList<String>();
	static {
		WHERE_ACCOUNT_AND_ID.add("account_id");
		WHERE_ACCOUNT_AND_ID.add("id");
	}
	
	static protected List<String> WHERE_ACCOUNT_AND_DEPLOYMENT = new ArrayList<String>();
	static {
		WHERE_ACCOUNT_AND_DEPLOYMENT.add("account_id");
		WHERE_ACCOUNT_AND_DEPLOYMENT.add("deployment_id");
	}
	
	static protected List<String> WHERE_ACCOUNT_AND_SERVER = new ArrayList<String>();
	static {
		WHERE_ACCOUNT_AND_SERVER.add("account_id");
		WHERE_ACCOUNT_AND_SERVER.add("server_id");
	}
	
	static protected List<String> WHERE_ACCOUNT_AND_SERVER_TEMPLATE_AND_APPLY = new ArrayList<String>();
	static {
		WHERE_ACCOUNT_AND_SERVER_TEMPLATE_AND_APPLY.add("account_id");
		WHERE_ACCOUNT_AND_SERVER_TEMPLATE_AND_APPLY.add("server_template_id");
		WHERE_ACCOUNT_AND_SERVER_TEMPLATE_AND_APPLY.add("apply");
	}

	/**
	 * Session object that is shared among all callers of this class. Note that since a Context
	 * is required to create a session, the first caller "wins".
	 */
	static private DashboardSession _session         = null;
	static private String           _sessionEmail    = null;
	static private String           _sessionPassword = null;
	static private String           _sessionSystem   = null;	
	
	/*
	 * TODO figure out how to fit this better into Android's app framework, e.g.
	 * use a BroadcastReceiver that can handle these actions as Intents.
	 */
	static public void performAction(Context context, Uri uri, String accountId, String action) {
		performAction(context, uri, accountId, action, null);
	}

	/*
	 * TODO figure out how to fit this better into Android's app framework, e.g.
	 * use a BroadcastReceiver that can handle these actions as Intents.
	 */
	static public void performAction(Context context, Uri uri, String accountId, String action, String param) throws DashboardError {
		String mimeType = _getType(uri);

		if (!mimeType.startsWith("vnd.android.cursor.item/")) {
			throw new DashboardError(
					"Cannot perform actions on collection resource "
							+ uri.toString());
		}

		List<String> pathSegments = uri.getPathSegments();
		String id = pathSegments.get(pathSegments.size() - 1);

		try {
			DashboardSession session = createSession(context);
			session.login();

			if (mimeType.endsWith(ServersResource.MIME_TYPE)) {
				ServersResource servers = new ServersResource(session, accountId);

				if (ACTION_LAUNCH.equals(action)) {
					servers.launch(id);

				} else if (ACTION_TERMINATE.equals(action)) {
					servers.terminate(id);

				} else if (ACTION_REBOOT.equals(action)) {
					servers.reboot(id);
				} else if (ACTION_RUN_SCRIPT.equals(action)) {
					servers.runScript(id, (String)param);
				}
			} else {
				throw new DashboardError(
						"Cannot perform actions on unknown URI "
								+ uri.toString());
			}
		} catch (RestException e) {
			forgetSession();
			Error err = new DashboardError(e);
			throw err;
		}
	}

	@Override
	public String getType(Uri uri) {
		return _getType(uri);
	}

	@Override
	public boolean onCreate() {
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] columns, String where, String[] whereArgs, String sortBy) {
		try {
			List<String> segments = uri.getPathSegments();
			DashboardSession session = createSession(getContext());
			String[] args = null;

			session.login();
			
			if(segments.size() == 1 && segments.get(0).equals("accounts")) {
				//Special case: asking for index of accounts
				AccountsResource accounts = new AccountsResource(session);
				return accounts.index();
			}
			
			if(segments.size() < 3 || !segments.get(0).equals("accounts")) {
				throw new DashboardError("Unknown content URI: " + uri);				
			}

			if (segments.get(2).equals("deployments")) {
				if ((args = parseWhereArgs(where, whereArgs, WHERE_ACCOUNT_AND_ID)) != null) {
					// SELECT ... FROM deployments WHERE id = ?
					DeploymentsResource deployments = new DeploymentsResource(session, args[0]);
					return deployments.show(args[1]);
				}
				else if ((args = parseWhereArgs(where, whereArgs, WHERE_ACCOUNT)) != null) {
					// SELECT ... FROM deployments
					DeploymentsResource deployments = new DeploymentsResource(session, args[0]);
					return deployments.index();
				}
				else {
					throw new DashboardError("Unknown where-clause: " + where);									
				}
			} else if (segments.get(2).equals("servers")) {
				if ((args = parseWhereArgs(where, whereArgs, WHERE_ACCOUNT_AND_DEPLOYMENT)) != null) {
					// SELECT ... FROM servers WHERE deployment_id = ?
					ServersResource servers = new ServersResource(session, args[0]);
					return servers.indexForDeployment(args[1]);
				}
				else if ((args = parseWhereArgs(where, whereArgs, WHERE_ACCOUNT_AND_ID)) != null) {
					// SELECT ... FROM servers WHERE id = ?
					ServersResource servers = new ServersResource(session, args[0]);
					return servers.show(args[1]);
				}
				else if ((args = parseWhereArgs(where, whereArgs, WHERE_ACCOUNT)) != null) {
					// SELECT ... FROM servers
					ServersResource servers = new ServersResource(session, args[0]);
					return servers.index();
				}
				else {
					throw new DashboardError("Unknown where-clause: " + where);									
				}
			} else if (segments.get(2).equals("server_settings")) {
				if ((args = parseWhereArgs(where, whereArgs, WHERE_ACCOUNT_AND_SERVER)) != null) {
					// SELECT ... FROM server_settings WHERE server_id = ?
					ServerSettingsResource serverSettings = new ServerSettingsResource(session, args[0]);
					return serverSettings.showForServer(args[1]);
				}
				else {
					throw new DashboardError("Unknown where-clause: " + where);									
				}
			} else if (segments.get(2).equals("server_monitors")) {
				if ((args = parseWhereArgs(where, whereArgs, WHERE_ACCOUNT_AND_SERVER)) != null) {
					// SELECT ... FROM server_monitors WHERE server_id = ?
					ServerMonitorsResource serverMonitors = new ServerMonitorsResource(session, args[0]);
					return serverMonitors.showForServer(args[1]);
				}
				else {
					throw new DashboardError("Unknown where-clause: " + where);									
				}
			} else if (segments.get(2).equals("server_templates")) {
				if ((args = parseWhereArgs(where, whereArgs, WHERE_ACCOUNT_AND_ID)) != null) {
					// SELECT ... FROM server_templates WHERE id = ?
					ServerTemplatesResource serverTemplates = new ServerTemplatesResource(session, args[0]);
					return serverTemplates.show(args[1]);
				}
				else if ((args = parseWhereArgs(where, whereArgs, WHERE_ACCOUNT)) != null) {
					ServerTemplatesResource serverTemplates = new ServerTemplatesResource(session, args[0]);
					return serverTemplates.index();
				}
				else {
					throw new DashboardError("Unknown where-clause: " + where);									
				}
			} else if (segments.get(2).equals("server_template_executables")) {
				if ((args = parseWhereArgs(where, whereArgs, WHERE_ACCOUNT_AND_SERVER_TEMPLATE_AND_APPLY)) != null) {
					ServerTemplateExecutablesResource serverTemplates = new ServerTemplateExecutablesResource(session, args[0]);
					return serverTemplates.indexForServerTemplate(args[1], args[2]);
				}
				else {
					throw new DashboardError("Unknown where-clause: " + where);									
				}
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (RestException e) {
			forgetSession();
			Error err = new DashboardError(e);
			throw err;
		}
		
		throw new DashboardError("Unknown content URI " + uri);
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

	protected String[] parseWhereArgs(String where, String[] whereArgs, List<String> requiredParams) {
		if(where == null) {
			return null;
		}
		
		String[] tokens = where.split("\\s+");		

		String[] args          = new String[requiredParams.size()];
		String columnName      = null;
		int    n               = 0;		
		
		int state = 0;
		for(String token : tokens) {
			switch(state) {
			case 0:
				if(requiredParams.contains(token)) {
					columnName = token;
					state = 1;
				}
				else {
					//if the query string contains something other than we expect, the parse fails 
					return null;
				}
				break;
			case 1:
				if(token.equals("=")) {
					state = 2;
				}
				else {
					throw new DashboardError("Parse error: expected '=' but got " + token);					
				}
				break;
			case 2:
				if(token.equals("?")) {
					args[requiredParams.indexOf(columnName)] = whereArgs[n++];
					state = 3;
				}
				else {
					throw new DashboardError("Parse error: expected '?' but got " + token);										
				}
				break;
			case 3:
				if(token.equalsIgnoreCase("and")) {
					state = 0;
				}
				else {
					throw new DashboardError("Parse error: expected 'AND' but got " + token);										
				}
				break;
			}
		}
		
		if(n == requiredParams.size()) {
			return args;
		}
		else {
			return null;
		}
	}
	
	static public HttpClient createClient(Context context) {
		// notice that we don't login the session (on purpose)
		return createSession(context).createClient();
	}

	static public synchronized DashboardSession createSession(Context context)
	{
		String email    = Settings.getEmail(context),
		       password = Settings.getPassword(context),
		       system   = Settings.getSystem(context);
		
		if( (_sessionEmail != null && !_sessionEmail.equals(email)) || 
			(_sessionPassword != null && !_sessionPassword.equals(password)) || 
			(_sessionSystem != null && !_sessionSystem.equals(system)))
		{
			//Reset session if any relevant preferences have changed
			_session = null;
		}
		
		if(_session == null) {
			_sessionEmail    = email;
			_sessionPassword = password;
			_sessionSystem   = system;		
			_session         = new DashboardSession(email, password, system);
		}

		return _session;
	}

	static public synchronized void forgetSession()
	{
		_session = null;
	}
	
	static protected String _getType(Uri uri) {
		List<String> path = uri.getPathSegments();

		String model;
		String mimePrefix, mimeType;

		if (path.size() % 2 == 1) {
			// Odd-sized paths (/deployments, /deployments/1/servers, ...)
			// represent a collection of resources.
			model =  path.get(path.size() - 1);
			mimePrefix = "vnd.android.cursor.dir/";
		} else {
			// Even-sized paths (/deployments/1, /server_templates/1/executables/5)
			// represent an individual item.
			model =  path.get(path.size() - 2);
			mimePrefix = "vnd.android.cursor.item/";
		}

		if (model.equals("accounts")) {
			mimeType = AccountsResource.MIME_TYPE;
		}
		else if (model.equals("deployments")) {
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
}
