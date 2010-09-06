package com.rightscale;

import android.net.Uri;

public class Helper {
	protected String _accountId;

	public Helper(String accountId) {
		_accountId = accountId;
	}
	
	public String getAccountId() {
		return _accountId;
	}
	
	public Uri getRelativeRoute(String pathSegment) {
		Uri uri = Uri.withAppendedPath(Routes.BASE_CONTENT_URI, "accounts/" + getAccountId());
		return Uri.withAppendedPath(uri, pathSegment);
	}
	
	public Uri getRelativeRoute(String pathSegment, long resourceId) {
		Uri uri = Uri.withAppendedPath(Routes.BASE_CONTENT_URI, "accounts/" + getAccountId());
		return Uri.withAppendedPath(uri, pathSegment + "/" + resourceId);
	}
	

}
