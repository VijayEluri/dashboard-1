package com.rightscale;

import java.util.List;

import com.rightscale.provider.Dashboard;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.SimpleCursorAdapter;

/**
 * Activity for viewing the servers in a Deployment. This activity expects to be started with an Intent
 * whose data points to the content-URI of the deployment the user is interested in. Example:
 *   <code>
 *   Intent i = new Intent(Intent.ACTION_VIEW, new Uri("content://com.rightscale.provider.dashboard/deployments/12345"));
 *   startActivity(
 *   </code>
 */
public class ManageDeploymentServers extends ListActivity {
	private static String[] FROM = {Dashboard.SERVER_COLUMN_NICKNAME};
	private static int[]    TO   = {R.id.server_name};

	private int _deploymentId = 0;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        getDeploymentIdToView();
        getDeployment();
        getServers();
    }

    private void getDeploymentIdToView() {
        Intent intent = getIntent();
        Uri contentUri = intent.getData();
		List<String> path = contentUri.getPathSegments();
		String id = path.get(path.size() - 1);         
        _deploymentId = new Integer(id).intValue();
    }

    private void getDeployment() {
    	ContentResolver cr = getContentResolver();

    	String[] whereArgs = { new Integer(_deploymentId).toString() };
    	Cursor cursor = cr.query(Dashboard.DEPLOYMENTS_URI, Dashboard.DEPLOYMENT_COLUMNS, "deployment_id = ?", whereArgs, null);
    	int col = cursor.getColumnIndex("nickname");
    	if(cursor.moveToNext()) {
	    	String nickname = cursor.getString(col);
	    	this.setTitle(nickname);
    	}
    }
    
    private void getServers() {
    	ContentResolver cr = getContentResolver();

    	String[] whereArgs = { new Integer(_deploymentId).toString() };
    	Cursor cursor = cr.query(Dashboard.SERVERS_URI, Dashboard.SERVER_COLUMNS, "deployment_id = ?", whereArgs, null);
    	startManagingCursor(cursor);
    	showServers(cursor);
    }
    
    private void showServers(Cursor cursor) {
    	SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.server_item, cursor, FROM, TO);
    	setListAdapter(adapter);
    }
}