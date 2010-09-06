package com.rightscale;

import com.rightscale.provider.Dashboard;

import android.net.Uri;

public class Routes {
	/**
	 * The application routes share a base URI with the content provider. This allows Activities to be triggered
	 * on the MIME content-type of the URI attached to an Intent.
	 */
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
