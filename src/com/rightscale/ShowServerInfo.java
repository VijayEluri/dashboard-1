package com.rightscale;

import java.util.List;

import net.xeger.rest.ui.ContentConsumer;
import net.xeger.rest.ui.ContentTransfer;
import net.xeger.rest.ui.ContentProducer;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import com.rightscale.provider.Dashboard;
import com.rightscale.provider.DashboardError;

public class ShowServerInfo extends Activity implements ContentConsumer, ContentProducer {
	private static final Object SERVER          = "server";
	private static final Object SERVER_SETTINGS = "server settings";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_server_info);
        
        ContentTransfer.load(this, this, new Handler(), SERVER_SETTINGS);
        ContentTransfer.load(this, this, new Handler(), SERVER);
    }

    public void consumeContent(Cursor cursor, Object tag) {
    	if(SERVER == tag) {
    		cursor.moveToFirst();
    		int colState = cursor.getColumnIndex("state");

    		TextView view = (TextView)findViewById(R.id.show_server_info_state);
    		view.setText(cursor.getString(colState));    		
    	}
    	else if(SERVER_SETTINGS == tag) {
    		cursor.moveToFirst();
    		int colAddress    = cursor.getColumnIndex("ip_address");
    		int colCloudId    = cursor.getColumnIndex("cloud_id");
    		int colDatacenter = cursor.getColumnIndex("datacenter");
    		int colPricing    = cursor.getColumnIndex("pricing");
    		TextView view;
    		
    		view = (TextView)findViewById(R.id.show_server_info_address);
    		view.setText(cursor.getString(colAddress));
    		view = (TextView)findViewById(R.id.show_server_info_datacenter);
    		view.setText(cursor.getString(colDatacenter));
    		view = (TextView)findViewById(R.id.show_server_info_pricing);
    		view.setText(cursor.getString(colPricing));    		
    	}
    }
    
    public Cursor produceContent(Object tag) {
    	if(SERVER == tag) {
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
