package com.rightscale;

import java.util.List;

import net.xeger.rest.RestException;
import net.xeger.rest.ui.ContentConsumer;
import net.xeger.rest.ui.ContentProducer;
import net.xeger.rest.ui.ContentTransfer;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.rightscale.provider.Dashboard;

public class AbstractServerActivity extends Activity implements ContentConsumer, ContentProducer {
	static public final String SERVER          = "server";
	static public final String SERVER_SETTINGS = "server_settings";

	Cursor _currentServer         = null;
	Cursor _currentServerSettings = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContentTransfer.load(this, this, new Handler(), SERVER);
        ContentTransfer.load(this, this, new Handler(), SERVER_SETTINGS);
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
    	else {
    		//Can always SSH as long as server isn't inactive
    		menu.findItem(R.id.menu_ssh_server).setVisible(true);
    	}
    	
    	if(state.matches("stopped|bidding|booting|operational")) {
    		menu.findItem(R.id.menu_terminate_server).setVisible(true);    		
    	}
    	
    	if(state.matches("booting|operational")) {
    		//Can terminate or reboot server if it's running and isn't in terminating state
    		menu.findItem(R.id.menu_reboot_server).setVisible(true);
    	}

    	return true;
    }    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	if(_currentServer == null) {
    		return false;
    	}
    	
    	Uri server = getServerUri();
    	
    	switch(item.getItemId()) {
    	case R.id.menu_ssh_server:
    		int colAddress = _currentServerSettings.getColumnIndexOrThrow("ip_address");
    		String address = _currentServerSettings.getString(colAddress);
    		StringBuilder sb = new StringBuilder();
    		sb.append("ssh://root@").append(address).append(":22/#").append("foo"); //TODO
    		Uri uri = Uri.parse(sb.toString());    		
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(intent);
			return true;
    	case R.id.menu_launch_server:
    		Dashboard.performAction(getBaseContext(), server, Dashboard.ACTION_LAUNCH);
            ContentTransfer.load(this, this, new Handler());
    		return true;

    	case R.id.menu_reboot_server:
    		Dashboard.performAction(getBaseContext(), server, Dashboard.ACTION_REBOOT);
            ContentTransfer.load(this, this, new Handler());
    		return true;

    	case R.id.menu_terminate_server:
    		Dashboard.performAction(getBaseContext(), server, Dashboard.ACTION_TERMINATE);
            ContentTransfer.load(this, this, new Handler());
    		return true;

    	default:
    		return false;
    	}    	
    }

	public Cursor produceContent(String tag)
		throws RestException
	{
		if(tag == SERVER) {
	    	ContentResolver cr = getContentResolver();
	
			String[] whereArgs = { getServerId() };
	    	return cr.query(Dashboard.SERVERS_URI, Dashboard.SERVER_COLUMNS, "server_id = ?", whereArgs, null);
		}
		else if(SERVER_SETTINGS == tag) {
        	ContentResolver cr = getContentResolver();

    		String[] whereArgs = { getServerId() };
        	return cr.query(Dashboard.SERVER_SETTINGS_URI, Dashboard.SERVER_SETTING_COLUMNS, "server_id = ?", whereArgs, null);    		
    	}
		else {
			return null;
		}
	}    
    
	public void consumeContent(Cursor cursor, String tag) {
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
		Settings.handleError(t, getBaseContext());
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
