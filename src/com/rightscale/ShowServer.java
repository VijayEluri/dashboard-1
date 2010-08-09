package com.rightscale;

import java.util.List;

import net.xeger.rest.ui.ContentConsumer;
import net.xeger.rest.ui.ContentTransfer;
import net.xeger.rest.ui.ContentProducer;

import com.rightscale.provider.Dashboard;
import com.rightscale.provider.DashboardError;

import android.app.TabActivity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.TabContentFactory;

public class ShowServer extends TabActivity implements ContentConsumer, ContentProducer {
	private String _lastKnownServerState = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_server);
        
        TabHost tabHost = getTabHost();
        Resources res   = getResources();
        
        TabHost.TabSpec tabSpec;        
        Intent          intent;

        intent = new Intent(this, ShowServerInfo.class);
        intent.setData(getIntent().getData());
        tabSpec = tabHost.newTabSpec("info").setIndicator(null, res.getDrawable(android.R.drawable.ic_menu_info_details)).setContent(intent); 
        tabHost.addTab(tabSpec);
        
        intent = new Intent(this, ShowServerMonitoring.class);
        intent.setData(getIntent().getData());
        tabSpec = tabHost.newTabSpec("monitoring").setIndicator(null, res.getDrawable(android.R.drawable.ic_menu_gallery)).setContent(intent); 
        tabHost.addTab(tabSpec);
        
        ContentTransfer.load(this, this, new Handler());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);

    	if(_lastKnownServerState == null) {
    		//If server state isn't known yet, don't bother to create menu!
    		return false;
    	}
    	
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.show_server, menu);
    	
    	if(_lastKnownServerState.matches("inactive|stopped")) {
    		//Can launch server if it isn't currently running
    		menu.findItem(R.id.menu_launch_server).setVisible(true);
    	}
    	else {
    		//Can always SSH as long as server isn't inactive
    		menu.findItem(R.id.menu_ssh_server).setVisible(true);
    	}
    	
    	if(_lastKnownServerState.matches("stopped|bidding|booting|operational")) {
    		menu.findItem(R.id.menu_terminate_server).setVisible(true);    		
    	}
    	
    	if(_lastKnownServerState.matches("booting|operational")) {
    		//Can terminate or reboot server if it's running and isn't in terminating state
    		menu.findItem(R.id.menu_reboot_server).setVisible(true);
    	}

    	return true;
    }    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	Uri server = getIntent().getData();
    	
    	switch(item.getItemId()) {
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

	public Cursor produceContent(Object tag) {
    	ContentResolver cr = getContentResolver();

		String[] whereArgs = { getServerId() };
    	return cr.query(Dashboard.SERVERS_URI, Dashboard.SERVER_COLUMNS, "server_id = ?", whereArgs, null);    		
	}    
    
	public void consumeContent(Cursor c, Object tag) {
		c.moveToFirst();
		int colState = c.getColumnIndex("state");
		_lastKnownServerState = c.getString(colState);    				
	}

	public void consumeContentError(Throwable t, Object tag) {
		if(t instanceof DashboardError) {
			Throwable cause = t.getCause();
			Log.e("DashboardError", cause.toString());
			Intent intent = new Intent(Settings.ACTION_NOTIFY_ERROR, null, this, Settings.class);
			intent.putExtra("error", t);
			intent.putExtra("cause", cause);
			
			finish();
			startActivity(intent);
		}
		else if(t instanceof RuntimeException) {
			throw (RuntimeException)t;
		}
		else {
			throw new Error(t);
		}					
	}

    private String getServerId() {
        Intent intent      = getIntent();
    	Uri contentUri      = intent.getData();
		List<String> path   = contentUri.getPathSegments();
		return path.get(path.size() - 1);             	
    }
}
