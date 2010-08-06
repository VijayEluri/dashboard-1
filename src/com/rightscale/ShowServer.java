package com.rightscale;

import java.util.List;

import android.app.*;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import android.widget.TabHost.TabContentFactory;

import com.rightscale.provider.Dashboard;

public class ShowServer extends TabActivity implements TabContentFactory {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_server);
        TabHost tabHost = getTabHost();

        TabHost.TabSpec tabSpec;        
        tabSpec = tabHost.newTabSpec("info").setIndicator("Info").setContent(this); 
        tabHost.addTab(tabSpec);
        tabSpec = tabHost.newTabSpec("monitoring").setIndicator("Mon").setContent(this); 
        tabHost.addTab(tabSpec);
        
        consumeContent(loadContent());
    }
    
	public View createTabContent(String tag) {
		if(tag.equals("info")) {
			TextView view = new TextView(this);
			view.setText("Here's some info for server " + getServerId());
			return view;
		}
		else if(tag.equals("monitoring")) {
			TextView view = new TextView(this);
			view.setText("Here's some monitoring!");
			return view;
		}
		else {
			throw new Error("Invalid tag specified!");
		}
	}	

    protected Cursor loadContent() {
    	ContentResolver cr = getContentResolver();

		String[] whereArgs = { getServerId() };
    	return cr.query(Dashboard.SERVERS_URI, Dashboard.SERVER_COLUMNS, "server_id = ?", whereArgs, null);
    }
    
    protected void consumeContent(Cursor cursor) {
    	startManagingCursor(cursor);
    }

    private String getServerId() {
        Intent intent      = getIntent();
    	Uri contentUri      = intent.getData();
		List<String> path   = contentUri.getPathSegments();
		return path.get(path.size() - 1);             	
    }
}
