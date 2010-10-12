package com.rightscale.app.dashboard;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;

public class ShowServerInfo extends AbstractServerActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_server_info);
    }

    public void consumeContent(Cursor cursor, String tag) {
    	super.consumeContent(cursor, tag);
    	TextView view;
    	
    	if(SERVER == tag) {
    		cursor.moveToFirst();
    		int colNickname   = cursor.getColumnIndexOrThrow("nickname");
    		int colState = cursor.getColumnIndexOrThrow("state");

    		view = (TextView)findViewById(R.id.show_server_info_name);
    		view.setText(cursor.getString(colNickname));

    		view = (TextView)findViewById(R.id.show_server_info_state);
    		view.setText(cursor.getString(colState));    		
    	}
    	else if(SERVER_SETTINGS == tag) {
    		int colAddress    = cursor.getColumnIndexOrThrow("ip_address");
    		int colCloudId    = cursor.getColumnIndexOrThrow("cloud_id");
    		int colDatacenter = cursor.getColumnIndexOrThrow("datacenter");
    		int colPricing    = cursor.getColumnIndexOrThrow("pricing");
    		
    		view = (TextView)findViewById(R.id.show_server_info_address);
    		view.setText(cursor.getString(colAddress));

    		view = (TextView)findViewById(R.id.show_server_info_cloud);
    		int cloudId = cursor.getInt(colCloudId);
    		if(cloudId > 0) {
    			view.setText(cursor.getString(colCloudId));
    		}
    		else {
    			view.setText("(unknown)");
    		}
    		
    		view = (TextView)findViewById(R.id.show_server_info_datacenter);
    		view.setText(cursor.getString(colDatacenter));
    		
    		view = (TextView)findViewById(R.id.show_server_info_pricing);
    		view.setText(cursor.getString(colPricing));    		
    	}
    }    
}
