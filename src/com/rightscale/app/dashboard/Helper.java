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

	private int                 _throbberCount = 0;
	private ProgressDialog 	    _throbber      = null;
	
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
	}
	
    public void onStart() {
    	if(BROADCAST_RECEIVERS_SUCK) {
    		//Log.
    		return;
    	}
        _context.startService(new Intent(_context, DashboardFeed.class));    	
    }

    public void onPause() {
    	hideThrobber(true);
    	
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
    			//Log.
    		}
    	};
    	
    	IntentFilter filter = new IntentFilter();
    	filter.addCategory(DashboardFeed.CATEGORY_EVENT);
    	
    	_context.registerReceiver(_receiver, filter);
    }

    public void onStop() {
    	if(BROADCAST_RECEIVERS_SUCK) {
    		return;
    	}
    	_context.stopService(new Intent(_context, DashboardFeed.class));
    }

    public void onLoadContent() {
    	showThrobber();
    }
    
    public void onConsumeContent() {
    	hideThrobber(false);
    }
    
    public void onConsumeContentError(Throwable t) {
    	hideThrobber(true);
    	
		Throwable cause = t.getCause() != null ? t.getCause() : t;
		//Log.
		cause.printStackTrace();
		
		Login.handleError(t, _context);    	
    }

	public synchronized void showThrobber() {
		_throbberCount++;
		
		if (_throbberCount > 0 && _throbber == null) {
			_throbber = ProgressDialog.show(_context, "", "Loading...", true);
		}
	}
	
	public synchronized void hideThrobber(boolean absolutely) {
		if(absolutely) {
			_throbberCount = 0;
		}
		else {
			_throbberCount = (_throbberCount > 0) ? (_throbberCount - 1) : 0;
		}

		if(_throbberCount <= 0 && _throbber != null) {
			_throbber.dismiss();
			_throbber = null;
		}
	}	
}
