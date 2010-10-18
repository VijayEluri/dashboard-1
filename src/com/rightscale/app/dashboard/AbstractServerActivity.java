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

import java.util.List;

import net.xeger.rest.RestException;
import net.xeger.rest.ui.ContentConsumer;
import net.xeger.rest.ui.ContentProducer;
import net.xeger.rest.ui.ContentTransfer;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.rightscale.provider.Dashboard;

public class AbstractServerActivity extends Activity implements ContentConsumer, ContentProducer {
	abstract class ServerAction implements Runnable {
		Uri _server;
		
		ServerAction(Uri server) {
			_server = server;
		}
	}
	
	static public final String SERVER          = "server";
	static public final String SERVER_SETTINGS = "server_settings";

	protected Helper         _helper = null;  
    protected boolean        _loaded = false;
    
	protected Cursor _currentServer         = null;
	protected Cursor _currentServerSettings = null;
	protected BroadcastReceiver _receiver   = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _helper = new Helper(this, Routes.getAccountId(getIntent().getData()));
        _helper.onCreate();    	
    }

    @Override
    public void onStart() {
    	super.onStart();
    	_helper.onStart();
        loadContent();
    }

    @Override
    public void onPause() {
    	super.onPause();
    	_helper.onPause();
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	_helper.onResume();
    }

    @Override
    public void onStop() {
    	super.onStop();
    	_helper.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);

    	if(_currentServer == null) {
    		//If server state isn't known yet, don't bother to create menu!
    		return false;
    	}

    	int colState = _currentServer.getColumnIndex("state");
    	String state = _currentServer.getString(colState);
    	
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.show_server, menu);

		if(state.matches("inactive|stopped")) {
    		//Can launch server if it isn't currently running
    		menu.findItem(R.id.menu_launch_server).setVisible(true);
    	}
    	
    	if(state.matches("booting|stranded|operational|shutting-down|decommissioning")) {
			//Can always SSH as long as server is running
			menu.findItem(R.id.menu_ssh_server).setVisible(true);
    	}
    	
    	if(state.matches("booting|operational")) {
    		//Can terminate or reboot server if it's running and isn't in terminating state
    		menu.findItem(R.id.menu_reboot_server).setVisible(true);
    	}

    	if(state.matches("bidding|pending|booting|stranded|operational|shutting-down|decommissioning")) {
    		menu.findItem(R.id.menu_terminate_server).setVisible(true);    		
    	}
    	
    	return true;
    }    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	if(_currentServer == null) {
    		return false;
    	}

    	StringBuilder toast = new StringBuilder();

    	Uri server = getServerUri();
        int colNickname = _currentServer.getColumnIndexOrThrow("nickname");
    	String nickname = _currentServer.getString(colNickname);
    	
    	ServerAction action = null;
    	
    	switch(item.getItemId()) {
    	case R.id.menu_ssh_server:
    		int colAddress = _currentServerSettings.getColumnIndexOrThrow("ip_address");
    		String address = _currentServerSettings.getString(colAddress);
    		StringBuilder sb = new StringBuilder();
    		sb.append("ssh://root@").append(address).append(":22/#").append("foo"); //TODO
    		Uri uri = Uri.parse(sb.toString());    		
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			try {
				startActivity(intent);
			}
			catch(Exception e) {
				DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}					
				};
				
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.ssh_failure_title).setMessage(R.string.ssh_failure);
				builder.setNeutralButton(R.string.ok, listener);
				builder.show();
			}
			break;
			
    	case R.id.menu_launch_server:
    		action = new ServerAction(server) {
    			public void run() {
    	    		Dashboard.performAction(getBaseContext(), _server, _helper.getAccountId(), Dashboard.ACTION_LAUNCH);
    	    		//TODO reload content
    			}
    		};
			toast.append(nickname).append(" has been launched."); //TODO i18n
            break;
            
    	case R.id.menu_reboot_server:
    		action = new ServerAction(server) {
    			public void run() {
    	    		Dashboard.performAction(getBaseContext(), _server, _helper.getAccountId(), Dashboard.ACTION_REBOOT);    				
    	    		//TODO reload content
    			}
    		};
			toast.append(nickname).append(" is being rebooted."); //TODO i18n
            break;
            
    	case R.id.menu_terminate_server:
    		action = new ServerAction(server) {
    			public void run() {
    	    		Dashboard.performAction(getBaseContext(), _server, _helper.getAccountId(), Dashboard.ACTION_TERMINATE);
    	    		//TODO reload content
    			}
    		};
			toast.append(nickname).append(" has been terminated."); //TODO i18n
            break;
    	}
    	
    	if(action != null) {
	    	new Thread(action).start();
	        if(toast.length() > 0) {
	        	//TODO use a resource template for the toast (i18n friendliness)
	        	Toast.makeText(this, toast.toString(), Toast.LENGTH_SHORT).show();
	    	}
	        return true;
    	}
    	else {
    		return false;
    	}
    }

    public void loadContent()
    {
    	if(!_loaded) {
    		_helper.onLoadContent();
    		ContentTransfer.load(this, this, new Handler(), SERVER);
    		ContentTransfer.load(this, this, new Handler(), SERVER_SETTINGS);
    	}
    }
    
	public Cursor produceContent(String tag)
		throws RestException
	{
		if(tag == SERVER) {
	    	ContentResolver cr = getContentResolver();
	
			String[] whereArgs = { _helper.getAccountId(), getServerId() };
	    	return cr.query(_helper.getContentRoute("servers"), Dashboard.SERVER_COLUMNS, "account_id = ? AND id = ?", whereArgs, null);
		}
		else if(SERVER_SETTINGS == tag) {
        	ContentResolver cr = getContentResolver();

    		String[] whereArgs = { _helper.getAccountId(), getServerId() };
        	return cr.query(_helper.getContentRoute("server_settings"), Dashboard.SERVER_SETTING_COLUMNS, "account_id = ? AND server_id = ?", whereArgs, null);    		
    	}
		else {
			return null;
		}
	}    
    
	public void consumeContent(Cursor cursor, String tag) {
		_loaded = true;
		_helper.onConsumeContent();

		if(tag == SERVER) {
			if(_currentServer != null) {
				stopManagingCursor(_currentServer);
			}
			cursor.moveToFirst();
			_currentServer = cursor;
			startManagingCursor(_currentServer);
		}
		else if(tag == SERVER_SETTINGS) {
    		if(_currentServerSettings != null) {
    			stopManagingCursor(_currentServerSettings);
    		}
    		cursor.moveToFirst();
    		startManagingCursor(cursor);
    		_currentServerSettings = cursor;			
		}
	}

	public void consumeContentError(Throwable t, String tag) {
		_helper.onConsumeContentError(t);
	}

    protected String getServerId() {
    	Uri contentUri      = getServerUri();
		List<String> path   = contentUri.getPathSegments();
		return path.get(path.size() - 1);             	
    }
    
    protected Uri getServerUri() {
        return getIntent().getData();
    }
}
