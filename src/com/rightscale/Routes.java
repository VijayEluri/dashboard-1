package com.rightscale;

import com.rightscale.provider.Dashboard;

import android.net.Uri;

/**
 * Helper class for generating URIs that launch various Activities.
 * 
 * The application's activities use the same URI authority and path scheme as the content provider;
 * this allows application routes to be associated with a MIME type (which is resolved by the
 * content provider) and activities' intent filters therefore key on MIME type instead of path
 * components.
 */
public class Routes {
	public static final Uri BASE_CONTENT_URI = Dashboard.BASE_CONTENT_URI;

	static public String getAccountId(Uri uri) {
		String str = uri.toString(); 
		if(str.startsWith("content://com.rightscale.provider.dashboard/accounts/")) {
			return uri.getPathSegments().get(1);
		}
		else {
			throw new Error("Could not parse URI: " + str); 
		}
	}
	
	static public Uri indexDeployments(String accountId) {
		return BASE_CONTENT_URI.buildUpon()
			.appendPath("accounts")
			.appendPath(accountId)
			.build();
	}

	static public Uri showDeployment(String accountId, String id) {
		return BASE_CONTENT_URI.buildUpon()
			.appendPath("accounts")
			.appendPath(accountId)
			.appendPath("deployments")
			.appendPath(id)
			.build();
	}

	static public Uri showServer(String accountId, String id) {
		return BASE_CONTENT_URI.buildUpon()
		.appendPath("accounts")
		.appendPath(accountId)
		.appendPath("servers")
		.appendPath(id)
		.build();
	}
}
