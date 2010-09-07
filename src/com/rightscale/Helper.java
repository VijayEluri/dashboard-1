package com.rightscale;

import com.rightscale.provider.Dashboard;
import com.rightscale.service.DashboardFeed;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.util.Log;

public class Helper {
	static public final boolean BROADCAST_RECEIVERS_SUCK = true;
	
	protected Context           _context;
	protected String            _accountId;
	protected Uri               _accountUri;
	protected BroadcastReceiver _receiver;
	
	public Helper(Context context, String accountId) {
		_context    = context;
		_accountId  = accountId;
		_accountUri = Uri.withAppendedPath(Dashboard.BASE_CONTENT_URI, "accounts/" + getAccountId());
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
    	if(BROADCAST_RECEIVERS_SUCK) {
    		Log.w("Helper", "Not starting DashboardFeed since broadcast receivers are stupid and buggy :(");
    		return;
    	}
        _context.startService(new Intent(_context, DashboardFeed.class));    	
    }

    public void onResume() {
    	if(BROADCAST_RECEIVERS_SUCK) {
    		return;
    	}
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
    	if(BROADCAST_RECEIVERS_SUCK) {
    		return;
    	}
    	_context.unregisterReceiver(_receiver);
    }

    public void onStop() {
    	if(BROADCAST_RECEIVERS_SUCK) {
    		return;
    	}
    	_context.stopService(new Intent(_context, DashboardFeed.class));
    }
}
