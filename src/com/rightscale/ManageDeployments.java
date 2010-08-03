package com.rightscale;

import com.rightscale.provider.Dashboard;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
	
public class ManageDeployments extends ListActivity {
	private static String[] FROM = {Dashboard.DEPLOYMENT_COLUMN_NICKNAME};
	private static int[]    TO   = {R.id.deployment_name};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_deployments);
        getDeployments();
    }

    private void getDeployments() {
    	//requestWindowFeature(Window.FEATURE_PROGRESS);
    	//getWindow().setFeatureInt(Window.FEATURE_PROGRESS, 5000);
		ContentResolver cr = getContentResolver();
		Cursor cursor = cr.query(Dashboard.DEPLOYMENTS_URI, Dashboard.DEPLOYMENT_COLUMNS, null, null, null);
    	//getWindow().setFeatureInt(Window.FEATURE_PROGRESS, 10000);
		startManagingCursor(cursor);
		showDeployments(cursor);
    }
    
    private void showDeployments(Cursor cursor) {
    	SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.deployment_item, cursor, FROM, TO);
    	setListAdapter(adapter);
    }
    
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	Uri deploymentUri = Uri.withAppendedPath(Dashboard.DEPLOYMENTS_URI, new Long(id).toString());
    	Intent i = new Intent(Intent.ACTION_VIEW, deploymentUri);
    	startActivity(i);
    }
}