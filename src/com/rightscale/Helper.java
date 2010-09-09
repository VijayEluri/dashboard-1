package com.rightscale;

import com.rightscale.provider.Dashboard;
import com.rightscale.service.DashboardFeed;

import android.app.ProgressDialog;
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

	//All instances of this class share a single spinner. The lock is needed for thread-safe
	//access to the static instance variable.
	static private final Object     _spinnerLock = new Object();
	static private ProgressDialog 	_spinner     = null;
	
	public Helper(Context context, String accountId) {
		_context    = context;
		_accountId  = accountId;
		_accountUri = Uri.withAppendedPath(Dashboard.BASE_CONTENT_URI, "accounts/" + getAccountId());
		_receiver	= null;
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
	
	public void onCreate(){
		showSpinner();
	}
	
    public void onStart() {
    	if(BROADCAST_RECEIVERS_SUCK) {
    		Log.w("Helper", "Not starting DashboardFeed since broadcast receivers are stupid and buggy :(");
    		return;
    	}
        _context.startService(new Intent(_context, DashboardFeed.class));    	
    }

    public void onPause() {
    	if(BROADCAST_RECEIVERS_SUCK) {
    		return;
    	}
    	_context.unregisterReceiver(_receiver);
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

    public void onStop() {
    	hideSpinner();
    	
    	if(BROADCAST_RECEIVERS_SUCK) {
    		return;
    	}
    	_context.stopService(new Intent(_context, DashboardFeed.class));
    }

    public void onConsumeContent(){
    	hideSpinner();
    }
    
    public void onConsumeContentError(Throwable t) {
		Settings.handleError(t, _context);    	
    }

	protected void showSpinner() {
		synchronized(_spinnerLock) {
			if (_spinner == null) {
				_spinner = ProgressDialog.show(_context, "", "Loading...", true);
			}
		}
	}
	
	protected void hideSpinner() {
		synchronized(_spinnerLock) {
			if(_spinner != null) {
				_spinner.dismiss();
				_spinner = null;
			}
		}
	}	
}
