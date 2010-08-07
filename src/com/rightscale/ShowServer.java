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
import android.view.View;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.TabContentFactory;

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
        
        intent = new Intent(this, ShowServerMonitoring.class);
        intent.setData(getIntent().getData());
        tabSpec = tabHost.newTabSpec("monitoring").setIndicator(null, res.getDrawable(android.R.drawable.ic_menu_gallery)).setContent(intent); 
        tabHost.addTab(tabSpec);
    }
}
