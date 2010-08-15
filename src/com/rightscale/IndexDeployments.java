package com.rightscale;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.rightscale.provider.Dashboard;
	
public class IndexDeployments extends AbstractDashboardActivity {
	private static String[] FROM = {"Nickname"};
	private static int[]    TO   = {android.R.id.text1};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.index_deployments);
        setTitle(R.string.rightscale_deployments);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
    	Uri deploymentUri = Uri.withAppendedPath(Dashboard.DEPLOYMENTS_URI, new Long(id).toString());
    	Intent i = new Intent(Intent.ACTION_VIEW, deploymentUri);
    	startActivity(i);
    }
    
    public Cursor produceContent(String tag) {
		ContentResolver cr = getContentResolver();
		return cr.query(Dashboard.DEPLOYMENTS_URI, Dashboard.DEPLOYMENT_COLUMNS, null, null, null);
    }
    
    public void consumeContent(Cursor cursor, String tag) {
		startManagingCursor(cursor);
    	SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursor, FROM, TO);
    	setListAdapter(adapter);
    }    
}