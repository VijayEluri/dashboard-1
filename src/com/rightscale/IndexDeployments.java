package com.rightscale;

import com.rightscale.provider.Dashboard;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
	
public class IndexDeployments extends DashboardListActivity {
	private static String[] FROM = {Dashboard.DEPLOYMENT_COLUMN_NICKNAME};
	private static int[]    TO   = {R.id.deployment_name};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.index_deployments);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
    	Uri deploymentUri = Uri.withAppendedPath(Dashboard.DEPLOYMENTS_URI, new Long(id).toString());
    	Intent i = new Intent(Intent.ACTION_VIEW, deploymentUri);
    	startActivity(i);
    }
    
    protected Cursor loadContent() {
		ContentResolver cr = getContentResolver();
		return cr.query(Dashboard.DEPLOYMENTS_URI, Dashboard.DEPLOYMENT_COLUMNS, null, null, null);
    }
    
    protected void consumeContent(Cursor cursor) {
		startManagingCursor(cursor);
    	SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.deployment_item, cursor, FROM, TO);
    	setListAdapter(adapter);
    }    
}