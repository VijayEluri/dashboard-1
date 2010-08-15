package com.rightscale;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class ShowServer extends TabActivity {
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
        
        intent = new Intent(this, ShowServerScripts.class);
        intent.setData(getIntent().getData());
        tabSpec = tabHost.newTabSpec("scripts").setIndicator(null, res.getDrawable(android.R.drawable.ic_menu_manage)).setContent(intent); 
        tabHost.addTab(tabSpec);
        
        intent = new Intent(this, ShowServerMonitoring.class);
        intent.setData(getIntent().getData());
        tabSpec = tabHost.newTabSpec("monitoring").setIndicator(null, res.getDrawable(android.R.drawable.ic_menu_gallery)).setContent(intent); 
        tabHost.addTab(tabSpec);
    }
}
