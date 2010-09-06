package com.rightscale;

import com.rightscale.provider.Dashboard;

import android.net.Uri;

public class Routes {
	static public Uri indexDeployments() {
		return Dashboard.DEPLOYMENTS_URI;
	}

	static public Uri showDeployment(long id) {
		return Uri.withAppendedPath(Dashboard.DEPLOYMENTS_URI, new Long(id).toString());
	}

	static public Uri showServer(long id) {
		return Uri.withAppendedPath(Dashboard.SERVERS_URI, new Long(id).toString());
	}
}
