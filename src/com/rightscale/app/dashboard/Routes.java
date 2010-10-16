// Dashboard: an Android front-end to the RightScale dashboard
// Copyright (C) 2009 Tony Spataro <code@tracker.xeger.net>
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package com.rightscale.app.dashboard;

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
	
	static public Uri indexAccounts() {
		return BASE_CONTENT_URI.buildUpon()
			.appendPath("accounts")
			.build();
	}

	static public Uri indexDeployments(String accountId) {
		return BASE_CONTENT_URI.buildUpon()
			.appendPath("accounts")
			.appendPath(accountId)
			.appendPath("deployments")
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
