package com.rightscale;

import java.util.List;

import android.app.TabActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TextView;

public class ShowServer extends TabActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_server);
        TabHost tabHost = getTabHost();

        TabHost.TabSpec tabSpec;        
        TextView view;

        view = new TextView(this);
        view.setText("Hello there!");
        tabSpec = tabHost.newTabSpec("Info").setIndicator("Info").setContent(view.getId()); 
        tabHost.addTab(tabSpec);

        view = new TextView(this);
        view.setText("Hello there!");
        tabSpec = tabHost.newTabSpec("Stuff").setIndicator("Stuff").setContent(view.getId()); 
        tabHost.addTab(tabSpec);
    }

    private String getServerId() {
        Intent intent      = getIntent();
    	Uri contentUri      = intent.getData();
		List<String> path   = contentUri.getPathSegments();
		return path.get(path.size() - 1);             	
    }    
}
