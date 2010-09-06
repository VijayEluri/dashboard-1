package com.rightscale;

import com.rightscale.service.DashboardFeed;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.util.Log;

public class Helper {
	protected Context           _context;
	protected String            _accountId;
	protected Uri               _accountUri;
	protected BroadcastReceiver _receiver;
	
	public Helper(Context context, String accountId) {
		_context    = context;
		_accountId  = accountId;
		_accountUri = Uri.withAppendedPath(Routes.BASE_CONTENT_URI, "accounts/" + getAccountId());
	}
	
	public String getAccountId() {
		return _accountId;
	}
	
	public Uri getContentRoute(String pathSegment) {
		return Uri.withAppendedPath(_accountUri, pathSegment);
	}
	
	public Uri getContentRoute(String pathSegment, String resourceId) {
		return Uri.withAppendedPath(_accountUri, pathSegment + "/" + resourceId);
	}
	
    public void onStart() {
        _context.startService(new Intent(_context, DashboardFeed.class));    	
    }

    public void onResume() {
    	_receiver = new BroadcastReceiver() {
    		@Override
    		public void onReceive(Context context, Intent intent) {
    			Uri uri = intent.getData();
    			Log.i("Receiver", uri.toString());
    		}
    	};
    	
    	IntentFilter filter = new IntentFilter();
    	filter.addCategory(DashboardFeed.CATEGORY_EVENT);
    	
    	_context.registerReceiver(_receiver, filter);
    }

    public void onPause() {
    	_context.unregisterReceiver(_receiver);
    }

    public void onStop() {
    	_context.stopService(new Intent(_context, DashboardFeed.class));
    }
}
