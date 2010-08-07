package com.rightscale;

import java.util.List;

import com.rightscale.provider.Dashboard;

import android.app.TabActivity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.TabContentFactory;

public class ShowServer extends TabActivity implements TabContentFactory {
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
        
        tabSpec = tabHost.newTabSpec("monitoring").setIndicator(null, res.getDrawable(android.R.drawable.ic_menu_slideshow)).setContent(this); 
        tabHost.addTab(tabSpec);
        
        consumeServer(loadServer());
    }
    
	public View createTabContent(String tag) {
		if(tag.equals("info")) {
			ListAdapter adapter = null;
			GridView view = (GridView)getLayoutInflater().inflate(R.layout.show_server_info, null);
			view.setAdapter(adapter);
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

	protected void consumeServer(Cursor cursor) {
		cursor.moveToFirst();
		int colNickname = cursor.getColumnIndex("nickname");
		this.setTitle(cursor.getString(colNickname));
	}

	protected Cursor loadServer() {
    	ContentResolver cr = getContentResolver();

		String[] whereArgs = { getServerId() };
    	return cr.query(Dashboard.SERVERS_URI, Dashboard.SERVER_COLUMNS, "server_id = ?", whereArgs, null);
	}

    private String getServerId() {
        Intent intent      = getIntent();
    	Uri contentUri      = intent.getData();
		List<String> path   = contentUri.getPathSegments();
		return path.get(path.size() - 1);             	
    }
}
