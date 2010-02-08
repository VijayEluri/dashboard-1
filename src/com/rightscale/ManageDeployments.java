package com.rightscale;

import com.rightscale.provider.Dashboard;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.SimpleCursorAdapter;

public class ManageDeployments extends ListActivity {
	private static String[] FROM = {Dashboard.DEPLOYMENT_COLUMN_NICKNAME};
	private static int[]    TO   = {R.id.deployment_name};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        getDeployments();
    }

    private void getDeployments() {
    	ContentResolver cr = getContentResolver();
    	Cursor cursor = cr.query(Dashboard.DEPLOYMENTS_URI, Dashboard.DEPLOYMENT_COLUMNS, null, null, null);
    	startManagingCursor(cursor);
    	showDeployments(cursor);
    }
    
    private void showDeployments(Cursor cursor) {
    	SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.deployment_item, cursor, FROM, TO);
    	setListAdapter(adapter);
    }
}