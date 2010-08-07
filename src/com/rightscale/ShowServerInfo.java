package com.rightscale;

import java.util.List;

import com.rightscale.ShowDeployment.ServersArrayAdapter;
import com.rightscale.provider.Dashboard;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ShowServerInfo extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_server_info);
        
        consumeServer(loadServer());
        consumeServerSettings(loadServerSettings());
    }

	protected void consumeServer(Cursor cursor) {
		cursor.moveToFirst();
		int colState = cursor.getColumnIndex("state");

		TextView view = (TextView)findViewById(R.id.show_server_info_state);
		view.setText(cursor.getString(colState));
	}

	protected void consumeServerSettings(Cursor cursor) {
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

	protected Cursor loadServer() {
    	ContentResolver cr = getContentResolver();

		String[] whereArgs = { getServerId() };
    	return cr.query(Dashboard.SERVERS_URI, Dashboard.SERVER_COLUMNS, "server_id = ?", whereArgs, null);
	}

	protected Cursor loadServerSettings() {
    	ContentResolver cr = getContentResolver();

		String[] whereArgs = { getServerId() };
    	return cr.query(Dashboard.SERVER_SETTINGS_URI, Dashboard.SERVER_SETTING_COLUMNS, "server_id = ?", whereArgs, null);
	}

    private String getServerId() {
        Intent intent      = getIntent();
    	Uri contentUri      = intent.getData();
		List<String> path   = contentUri.getPathSegments();
		return path.get(path.size() - 1);             	
    }
}
